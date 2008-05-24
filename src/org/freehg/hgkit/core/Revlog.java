package org.freehg.hgkit.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Revlog {

	public static final int AUTO_CLOSE = 1;

	private static final String READ_ONLY = "r";

	public static final int REVLOGV0 = 0;
	public static final int REVLOGNG = 1;
	public static final int REVLOGNGINLINEDATA = (1 << 16);
	public static final int REVLOG_DEFAULT_FLAGS = REVLOGNGINLINEDATA;
	public static final int REVLOG_DEFAULT_FORMAT = REVLOGNG;

	public static final int REVLOG_DEFAULT_VERSION = REVLOG_DEFAULT_FORMAT
			| REVLOG_DEFAULT_FLAGS;
	/* FIXME: Create NULLID */
	private static final NodeId NULLID = null;

	boolean isDataInline;
	private Map<NodeId, RevlogEntry> nodemap;
	ArrayList<RevlogEntry> index;

	private final File indexFile;

	private LinkedHashMap<RevlogEntry, byte[]> cache = new RevlogCache();

	private RandomAccessFile reader = null;

	private int styles = 0;

	public Revlog(File index) {
		this(index, AUTO_CLOSE);
	}

	public Revlog(File index, int style) {
		indexFile = index;
		this.styles = style;
		try {
			parseIndex(index);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<NodeId> getRevisions() {
		return Collections.unmodifiableSet(this.nodemap.keySet());
	}

	/**
	 * return an uncompressed revision data of a given nodeid NOTE: hgkit
	 * doesn't use caching for now
	 * 
	 * @param node
	 *            to nodeid to get data for
	 * @return an uncompressed revision data
	 */
	public byte[] revision(NodeId node) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		revision(node, out);
		return out.toByteArray();
	}

	public void revision(NodeId node, OutputStream out) {
		if (node.equals(NULLID)) {
			return;
		}

		final RevlogEntry target = nodemap.get(node);
		if ((target.getFlags() & 0xFFFF) != 0) {
			throw new IllegalStateException("Incompatible revision flag: "
					+ target.getFlags());
		}
		if (cache.containsKey(target)) {
			writeFromCache(target, out);
			return;
		}

		try {
			RevlogEntry baseRev = index.get(target.getBaseRev());
			byte[] baseData = cache.get(baseRev);
			if (baseData == null) {
				baseData = Util.decompress(baseRev.loadBlock(getDataFile()));
			}
			List<byte[]> patches = new ArrayList<byte[]>(target.revision
					- target.getBaseRev() + 1);

			long worstCaseSize = baseData.length;
			for (int rev = target.getBaseRev() + 1; rev <= target.revision; ++rev) {

				RevlogEntry nextEntry = this.index.get(rev);
				if (cache.containsKey(nextEntry)) {
					baseData = cache.get(nextEntry);
					patches.clear();
				} else {
					byte[] diff = Util.decompress(nextEntry.loadBlock(getDataFile()));
					patches.add(diff);
					worstCaseSize += diff.length;
				}
			}
			if (worstCaseSize < RevlogCache.CACHE_SMALL_REVISIONS) {
				CacheOutputStream cacheOut = new CacheOutputStream(out,
						(int) worstCaseSize);
				MDiff.patches(baseData, patches, cacheOut);
				this.cache.put(target, cacheOut.cache.toByteArray());

			} else {
				MDiff.patches(baseData, patches, out);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if ((styles & AUTO_CLOSE) != 0) {
				try {
					getDataFile().close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void writeFromCache(final RevlogEntry target, OutputStream out) {
		try {
			out.write(cache.get(target));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private RandomAccessFile getDataFile() throws FileNotFoundException {
		if(this.reader != null && this.reader.getChannel().isOpen()) {
			return this.reader;
		}
		if (this.isDataInline) {
			this.reader = new RandomAccessFile(this.indexFile, READ_ONLY);
		} else {
			String path = this.indexFile.getAbsolutePath();
			String dataFilePath = path.substring(0, path.length() - ".i".length())
					+ ".d";
			this.reader = new RandomAccessFile(new File(dataFilePath), READ_ONLY);
		}
		return this.reader;
	}

	public RevlogEntry tip() {
		return index.get(count() - 1);
	}

	public int count() {
		return index.size();
	}

	public void close() {
		if (reader == null) {
			return;
		}
		try {
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		reader = null;
	}

	/**
	 * versionformat = ">I", big endian, uint 4 bytes which includes version
	 * format
	 */
	private void parseIndex(File index) throws IOException {
		DataInputStream reader = new DataInputStream(new FileInputStream(index));
		int version = reader.readInt();
		reader.close();
		reader = new DataInputStream(new BufferedInputStream(
				new FileInputStream(index)));

		isDataInline = (version & REVLOGNGINLINEDATA) != 0;
		// Its pretty odd, but its the revlogFormat which is the "version"
		long revlogFormat = version & 0xFFFF;
		if (revlogFormat != REVLOGNG) {
			throw new IllegalStateException("Revlog format MUST be NG");
		}
		/*
		 * long flags = version & ~0xFFFF; TODO check index for unknown flags
		 * (see revlog.py)
		 */

		nodemap = new LinkedHashMap<NodeId, RevlogEntry>();
		this.index = new ArrayList<RevlogEntry>(100);

		byte[] data = Util.readWholeFile(reader);
		int length = data.length - RevlogEntry.BINARY_LENGTH;

		int indexCount = 0;
		int indexOffset = 0;

		while (indexOffset <= length) {
			RevlogEntry entry = RevlogEntry.valueOf(this, data, indexOffset);
			if (indexCount == 0) {
				entry.setOffset(0);
			}
			entry.revision = indexCount;
			nodemap.put(entry.nodeId, entry);
			this.index.add(entry);

			if (entry.getCompressedLength() < 0) {
				// What does this mean?
				System.err.println("e.compressedlength < 0");
				break;
			}
			if (isDataInline) {
				indexOffset += entry.getCompressedLength();
			}
			indexOffset += +RevlogEntry.BINARY_LENGTH;
			indexCount++;
		}

		printIndex();
	}

	void printIndex() {
		log("-------------------------------------");
		log("rev off  len         base    linkRev    nodeid      p1      p2");
		for (int i = 0; i < this.index.size(); i++) {
			RevlogEntry entry = this.index.get(i);
			log(entry);
		}
		log("number of revlogs: " + this.index.size());
	}

	static void log(Object msg) {
		if (true) {
			// logging is off
			return;
		}

		if (msg != null) {
			System.out.println(msg.toString());
		} else {
			System.out.println("null");
		}
	}

	private static class CacheOutputStream extends OutputStream {

		private ByteArrayOutputStream cache;
		private final OutputStream redirect;

		private CacheOutputStream(OutputStream redirect, int size) {
			this.redirect = redirect;
			this.cache = new ByteArrayOutputStream((int) size);
		}

		@Override
		public void write(int b) throws IOException {
			this.cache.write(b);
			this.redirect.write(b);
		}
	}

	private class RevlogCache extends LinkedHashMap<RevlogEntry, byte[]> {
		private static final long serialVersionUID = 6934630760462643470L;

		static final int CACHE_SMALL_REVISIONS = 4024;
		static final long MAX_CACHE_SIZE = 100000;

		private long cachedDataSize = 0;

		@Override
		protected boolean removeEldestEntry(Entry<RevlogEntry, byte[]> eldest) {
			boolean removeEldest = this.cachedDataSize > MAX_CACHE_SIZE;
			if (removeEldest) {
				cachedDataSize -= eldest.getValue().length;
			}
			return removeEldest;
		}

		@Override
		public byte[] put(RevlogEntry key, byte[] value) {
			byte[] result = super.put(key, value);
			int prevSize = 0;
			if (result != null) {
				prevSize = result.length;
			}
			cachedDataSize += value.length - prevSize;
			return result;
		}
	}
}
