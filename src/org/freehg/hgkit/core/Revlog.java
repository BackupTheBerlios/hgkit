package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcraft.jzlib.ZInputStream;

public class Revlog {

	public static class RevlogEntry {

		/** The corresponding length of indexformatng >Qiiiiii20s12x */
		private static final int BINARY_LENGTH = 64;
        private static RevlogEntry nullInstance;
		/**
		 * 
		 * @param parent
		 * @param data
		 * @param off where in data to begin extracting data
		 * @return
		 */
		public static RevlogEntry valueOf(Revlog parent, 
		        byte[] data, 
		        int off) {
		    
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

		private final Revlog parent;
		private long compressedLength;
		private long uncompressedLength;

		private long offset;
		private int baseRev;
		private int linkRev;
		private int flags;
		private int firstParentRev;
		private int secondParentRev;

		private int revision;

		private NodeId nodeId;
		
		RevlogEntry(Revlog parent) {
			this.parent = parent;
		}
		
		public int getBaseRev() {
            return baseRev;
        }

		public int getLinkRev() {
            return linkRev;
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
		
		long getUncompressedLength() {
            return uncompressedLength;
        }

		public String toString() {
		    
		    RevlogEntry p1 = getNullEntry();
		    RevlogEntry p2 = getNullEntry();

		    if(0 <= firstParentRev){ 
		        p1 = parent.index.get(firstParentRev);
		    }
		    if( 0 <= secondParentRev) {
		        p2 = parent.index.get(secondParentRev);
		    }
			return revision + "  " 
			        + offset + "	" 
			        + compressedLength + " 		"
					// + uncompressedLength + " 		" 
					+ baseRev + " 	"
					+ linkRev + " 	"
					+ nodeId.asShort() + " 	"
					+ p1.nodeId.asShort() + " 	"
					+ p2.nodeId.asShort();
					
		}

		static RevlogEntry getNullEntry() {
            if( nullInstance == null) {
                nullInstance = valueOf(null, new byte[64], 0);
            }
            return nullInstance;
        }

        private void read(DataInputStream reader) throws IOException {

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

        public NodeId getId() {
            return nodeId;
        }
	}
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
	public Set<NodeId> getRevisions() {
	    return Collections.unmodifiableSet(this.nodemap.keySet());
	}

    /**
	 * return an uncompressed revision data of a given nodeid
	 * NOTE: hgkit doesn't use caching for now
	 * @param node to nodeid to get data for
	 * @return an uncompressed revision data
	 */
	public String revision(NodeId node) {
		if (node.equals(NULLID)) {
			return "";
		}

		RevlogEntry target = nodemap.get(node);
		if ((target.flags & 0xFFFF) != 0) {
			throw new IllegalStateException("Incompatible revision flag: "
					+ target.flags);
		}
		
		try {
			FileInputStream reader = new FileInputStream(this.dataFile);
			
			RevlogEntry baseRev = index.get(target.baseRev);
			byte[] baseData = decompress(baseRev.loadBlock(reader));

			List<byte[]> patches = new ArrayList<byte[]>();
			for (int rev = target.baseRev + 1; 
			         rev <= target.revision; 
			         rev++) {
			    
				RevlogEntry nextEntry = this.index.get(rev);
                byte[] diff = decompress(nextEntry.loadBlock(reader));
				patches.add(diff);
			}
			byte[] revisionData = MDiff.patches(baseData, patches);
			return new String(revisionData);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RevlogEntry tip() {
        return index.get(count() - 1);
	}
    
    public int count() {
        return index.size();
    }

    private byte[] decompress(byte[] data) {
		try {
			if (data == null) {
				return null;
			}
			if (data.length < 1) {
				return new byte[0];
			}

			byte dataHeader = data[0];
			switch(dataHeader) {
			    case 0:
			        return data;
			    
			    case 'u':
			        byte[] uncompressed = new byte[data.length - 1];
			        ByteBuffer.wrap(data).get(uncompressed,1, uncompressed.length);
			        return uncompressed;
			    
			    case 'x':
			        return doDecompress(data);
			    
			    default:
			        throw new RuntimeException("Unknown compression type : "
			                + (char) (dataHeader));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private byte[] doDecompress(byte[] data) throws IOException {
        // decompress the bytearray using what should be python zlib
        ByteArrayInputStream datain = new ByteArrayInputStream(data);
        ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream();
        byte[] buff = new byte[512];
        InputStream _dec = new ZInputStream(datain);

        int read = 0;
        while (-1 != (read = _dec.read(buff))) {
            uncompressedOut.write(buff, 0, read);
        }
        return uncompressedOut.toByteArray();
    }

	/**
	 * versionformat = ">I", big endian, uint 4 bytes which includes version
	 * format
	 */
	private void parseIndex(File index) throws IOException {
		DataInputStream reader = new DataInputStream(new FileInputStream(index));
		int version = reader.readInt();
		reader.close();
		reader = new DataInputStream(new FileInputStream(index));

		isDataInline = (version & REVLOGNGINLINEDATA) != 0;
		// Its pretty odd, but its the revlogFormat which is the "version"
		long revlogFormat = version & 0xFFFF;
		if (revlogFormat != REVLOGNG) {
			throw new IllegalStateException("Revlog format MUST be NG");
		}
		/*
		 * long flags = version & ~0xFFFF;
		 * TODO check index for unknown flags (see revlog.py) 
		 */

		nodemap = new LinkedHashMap<NodeId, RevlogEntry>();
		this.index = new ArrayList<RevlogEntry>();
		
		if (isDataInline) {
		    parseInlineIndex(reader);
		} else {
			throw new IllegalStateException(
					"Non inline data not implemented yet");
		}
		printIndex();
	}

	private void parseInlineIndex(DataInputStream reader) throws IOException {
        byte[] data = readWholeFile(reader);
        int length = data.length - RevlogEntry.BINARY_LENGTH;
        
        int indexCount = 0;
        int indexOffset = 0;
        
        while (indexOffset <= length) {
        	RevlogEntry entry = RevlogEntry
        			.valueOf(this, data, indexOffset);
        	if (indexCount == 0) {
        		entry.offset = 0;
        	}
        	entry.revision = indexCount;
        	nodemap.put(entry.nodeId, entry);
        	this.index.add(entry);
        	
        	if (entry.compressedLength < 0) {
        	    // What does this mean?
        		System.err.println("e.compressedlength < 0");
        		break;
        	}
        	indexOffset += entry.compressedLength
        			+ RevlogEntry.BINARY_LENGTH;
        	indexCount++;
        }
    }


	void printIndex() {
        System.out.println("-------------------------------------");
        System.out.println("rev off  len         base    linkRev    nodeid      p1      p2");
		for (int i = 0; i < this.index.size(); i++) {
			RevlogEntry entry = this.index.get(i);
			System.out.println(entry);
		}
		System.out.println("number of revlogs: " + this.index.size());
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
}
