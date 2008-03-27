package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcraft.jzlib.ZInputStream;

public class Revlog {

	public static final int REVLOGV0 = 0;
	public static final int REVLOGNG = 1;
	public static final int REVLOGNGINLINEDATA = (1 << 16);
	public static final int REVLOG_DEFAULT_FLAGS = REVLOGNGINLINEDATA;
	public static final int REVLOG_DEFAULT_FORMAT = REVLOGNG;
	public static final int REVLOG_DEFAULT_VERSION = REVLOG_DEFAULT_FORMAT
			| REVLOG_DEFAULT_FLAGS;

	private static final int EOF = -1;
	/* FIXME: Create NULLID */
	private static final NodeId NULLID = null;
	private boolean isDataInline;
	private Map<NodeId, RevlogEntry> nodemap;
	private ArrayList<RevlogEntry> index;
	private final File dataFile;

	public Revlog(File index, File dataFile) {
		this.dataFile = dataFile;
		try {
			parseIndex(index);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void parseIndex(File index) throws IOException {
		DataInputStream reader = new DataInputStream(new FileInputStream(index));
		/*
		 * versionformat = ">I", big endian, uint 4 bytes which includes version
		 * format
		 */
		int version = reader.readInt();
		reader.close();
		reader = new DataInputStream(new FileInputStream(index));

		isDataInline = (version & REVLOGNGINLINEDATA) != 0;
		long flags = version & ~0xFFFF;
		// Its pretty odd, but its the revlogFormat which is the "version"
		long revlogFormat = version & 0xFFFF;

		if (revlogFormat != REVLOGNG) {
			throw new IllegalStateException("Revlog format MUST be NG");
		}
		/*
		 * TODO: This should also be checked if fmt == REVLOGNG and flags &
		 * ~REVLOGNGINLINEDATA: raise RevlogError(_("index %s unknown flags
		 * %#04x for revlogng") % (self.indexfile, flags >> 16))
		 */

		nodemap = new LinkedHashMap<NodeId, RevlogEntry>();
		this.index = new ArrayList<RevlogEntry>();

		int indexCount = 0;
		int indexOffset = 0;
		// if we're not using lazymap, always read the whole index
		// data = fp.read()
		byte[] data = readWholeFile(reader);
		int length = data.length - RevlogEntry.BINARY_LENGTH;
		if (isDataInline) {
			System.out.println("Data is inline => do inline parsing");
			// cache = (0, data)

			System.out.println("while " + indexOffset + " <= " + length);
			while (indexOffset <= length) {
				System.out.println("Read a new RevLogEntry at " + indexOffset);
				RevlogEntry entry = RevlogEntry
						.valueOf(this, data, indexOffset);
				if (indexCount == 0) {
					entry.offset = 0;
				}
				nodemap.put(entry.nodeId, entry);
				this.index.add(entry);
				// e = _unpack(indexformatng, data[off:off + s])
				// nodemap[e[7]] = n
				// append(e)
				indexCount += 1;
				// What does this mean?
				if (entry.compressedLength < 0) {
					System.out.println("e.compressedlength < 0");
					break;
				}
				indexOffset += entry.compressedLength
						+ RevlogEntry.BINARY_LENGTH;
			}
		} else {
			throw new IllegalStateException(
					"Non inline data not implemented yet");
		}

		System.out.println("-------------------------------------");
		for (int i = 0; i < this.index.size(); i++) {
			// FIXME REAL BAD HERE. setting revision number here ????
			RevlogEntry entry = this.index.get(i);
			entry.revision = i;
			System.out.print(i + "	");
			System.out.print(entry.nodeId.asShort() + "	");
			System.out.println(entry);
		}
		System.out.println("number of revlogs: " + indexCount);
	}

	public Set<NodeId> getRevisions() {
		return Collections.unmodifiableSet(this.nodemap.keySet());

	}

	public String revision(NodeId node) {
		// /**
		// * <pre>
		// *
		// def revision(self, node):
		// """return an uncompressed revision of a given"""
		// if node == nullid:
		// return &quot;&quot;
		// if self._cache and self._cache[0] == node:
		// return str(self._cache[2])
		if (node.equals(NULLID)) {
			return "";
		}
		// no caching for now

		// # look up what we need to read
		// rev = self.rev(node)
		// base = self.base(rev)
		RevlogEntry entry = nodemap.get(node);
		int rev = index.indexOf(entry);
		int base = entry.baseRev; // = self.base(rev)

		System.out.println("## Must lookup " + base + " to " + rev);
		/*
		 * # check rev flags if self.index[rev][0] & 0xFFFF: raise
		 * RevlogError(_('incompatible revision flag %x') % (self.index[rev][0]
		 * &amp; 0xFFFF))
		 * 
		 */
		if ((entry.flags & 0xFFFF) != 0) {
			throw new IllegalStateException("Incompatible revision flag: "
					+ entry.flags);
		}

		// if self._inline:
		// # we probably have the whole chunk cached
		// df = None
		// else:
		// df = self.opener(self.datafile)
		//		  
		if (!isDataInline) {
			throw new IllegalStateException(
					"Only inline reading implemented yet");
		}

		String text = null;
		byte[] allData = new byte[0];
		try {
			FileInputStream fis = new FileInputStream(this.dataFile);
			// entry.loadBlock(fis);

			// text = chunk(base);
			// All data needed is contained in this byte array
			// the data need to be split up into "chunks"
			// byte[] data = chunk(allData, base);

			byte[] data = index.get(base).loadBlock(fis);
			text = decompress(data);

			System.out.println("Got base text: " + text);
			List<String> bins = new ArrayList<String>();
			for (int r = base + 1; r < rev + 1; r++) {
				// byte[] chunk = chunk(allData, r);
				byte[] chunk = this.index.get(r).loadBlock(fis);
				String diff = decompress(chunk);
				System.out.println("Got mdiff: " + diff);
				bins.add(diff);
			}
			text = MDiff.patches(text, bins);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return text;
		// text = mdiff.patches(text, bins);

		// * text = None
		// * # do we have useful data cached?
		// * if self._cache and self._cache[1] >= base and self._cache[1] < rev:
		// * base = self._cache[1]
		// * text = str(self._cache[2])
		// * self._loadindex(base, rev + 1)
		// * else:
		// * self._loadindex(base, rev + 1)
		// * text = self.chunk(base, df=df)
		// *
		// * bins = [ self.chunk(r, df) for r in xrange(base + 1, rev + 1) ]
		// * text = mdiff.patches(text, bins)
		// * p1, p2 = self.parents(node)
		// * if node != hash(text, p1, p2):
		// * raise RevlogError(_(&quot;integrity check failed on %s:%d&quot;)
		// * % (self.datafile, rev))
		// *
		// * self._cache = (node, rev, text)
		// * return text
		// * </pre>
		// */
	}

//	private byte[] chunk(byte[] bigchunk, int revision) {
//		// get compressed data from a revision index
//
//		RevlogEntry entry = this.index.get(revision);
//		int start = (int) entry.offset;
//		int length = (int) entry.compressedLength;
//		ByteArrayOutputStream chunker = new ByteArrayOutputStream(length);
//		chunker.write(bigchunk, start, length);
//		return chunker.toByteArray();
//	}

	private String decompress(byte[] data) {
		try {
			if (data == null) {
				return null;
			}
			if (data.length < 1) {
				return "";
			}
			String uncompressed = new String(data);

			System.out.println("Decompress: " + uncompressed);
			// <pre>
			// t = bin[0]
			// if t == '\0':
			// return bin
			// if t == 'x':
			// return _decompress(bin)
			// if t == 'u':
			// return bin[1:]
			if (data[0] == 0) {
				return new String(data);
			}
			if (data[0] == 'u') {
				// FIXME: Bad performance/memory waste
				System.out.println("[DECOMPRESS] Uncompressed data found");
				return new String(data).substring(1);
			}
			if (data[0] == 'x') {
				
				return doDecompress(data);
				
				
//				throw new RuntimeException("Decompress not implemented yet");
			}
			throw new RuntimeException("Unknown compression type : "
					+ (char) (data[0]));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String doDecompress(byte[] data) throws FileNotFoundException,
			IOException {
		FileOutputStream fos = new FileOutputStream("c:\\comp.zip");
		fos.write(data);
		fos.flush();
		fos.close();
		// decompress the bytearray using what should be python zlib
		System.out
				.println("[DECOMPRESS] Compressed data found _decompress");
		ByteArrayInputStream datain = new ByteArrayInputStream(data);
		
		ByteArrayOutputStream unc = new ByteArrayOutputStream();
		byte[] buff = new byte[512];
		InputStream _dec = new ZInputStream(datain);
		int read = 0;
		while( 0 < (read = _dec.read(buff))) {
			unc.write(buff,0,read);
		}
		return new String(unc.toString());
	}


	private byte[] readWholeFile(DataInputStream reader) throws IOException {
		byte[] buf = new byte[512];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(reader
				.available());
		int read = 0;
		while ((read = reader.read(buf)) != EOF) {
			buffer.write(buf, 0, read);
		}
		return buffer.toByteArray();
	}

	public static class RevlogEntry {

		/** The corresponding length of indexformatng >Qiiiiii20s12x */
		private static final int BINARY_LENGTH = 64;

		private long compressedLength;
		private long uncompressedLength;
		private int baseRev;
		private int linkRev;
		private int firstParentRev;
		private int secondParentRev;

		private NodeId nodeId;

		private long offset;

		private int flags;

		private final Revlog parent;

		private int revision;

		RevlogEntry(Revlog parent) {
			this.parent = parent;

		}
		
		public int getLinkRev() {
            return linkRev;
        }
		
		public int getBaseRev() {
            return baseRev;
        }

		private void read(DataInputStream reader) throws IOException {

			System.out.println("Reading RevlogIndexEntry");
			offset = ((long) reader.readShort() << 32)
					+ reader.readInt();
			flags = reader.readShort();
			compressedLength = reader.readInt();
			uncompressedLength = reader.readInt();

			baseRev = reader.readInt();
			linkRev = reader.readInt();

			firstParentRev = reader.readInt();
			secondParentRev = reader.readInt();

			int nodeidSize = 32;
			byte[] nodeid = new byte[nodeidSize];
			reader.read(nodeid);
			nodeId = NodeId.valueOf(nodeid);
		}

		public byte[] loadBlock(FileInputStream fis) throws IOException {
			System.out.println("Loading block for: " + this.nodeId);
			long pos = fis.getChannel().position();

			long off = this.offset;
			if (parent.isDataInline) {
				off += (revision + 1) * RevlogEntry.BINARY_LENGTH;
			}
			long skip = off - pos;

			if (skip < 0) {
				throw new IllegalStateException("Cannot skip negative");
			}
			System.out.println(pos + " = pos, offset = " + off + " Skipping: "
					+ skip);
			fis.skip(skip);
			byte[] data = new byte[(int) this.compressedLength];
			fis.read(data);
			return data;

		}

		public String toString() {
			return offset + "	" + flags + " 	" + compressedLength + " 		"
					+ uncompressedLength + " 		" + firstParentRev + " 	"
					+ secondParentRev;
		}

		public static RevlogEntry valueOf(Revlog parent, byte[] data, int off) {
			byte[] mydata = new byte[BINARY_LENGTH];
			for (int i = 0; i < BINARY_LENGTH; i++) {
				mydata[i] = data[off + i];
			}
			ByteArrayInputStream copy = new ByteArrayInputStream(data, off,
					BINARY_LENGTH);
			DataInputStream reader = new DataInputStream(copy);
			RevlogEntry entry = new RevlogEntry(parent);
			try {
				entry.read(reader);
			} catch (IOException e) {
				// This should just never happen
				throw new RuntimeException(e);
			}
			return entry;
		}
	}

}
