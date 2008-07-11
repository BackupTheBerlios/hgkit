/**
 *
 */
package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class RevlogEntry {

    /** The corresponding length of indexformatng (python) >Qiiiiii20s12x */
    static final int BINARY_LENGTH = 64;
    private static RevlogEntry nullInstance;

    private final Revlog parent;
    private long compressedLength;
    private long uncompressedLength;

    private long offset;
    private int baseRev;
    int linkRev;
    private int flags;
    private int firstParentRev;
    private int secondParentRev;

    int revision;

    NodeId nodeId;

    RevlogEntry(Revlog parent) {
        this.parent = parent;
    }

    /**
    *
    * @param parent
    * @param data
    * @param off
    *            where in data to begin extracting data
    * @return
    */
   public static RevlogEntry valueOf(Revlog parent, byte[] data, int off) {

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

    public int getBaseRev() {
        return baseRev;
    }

    public int getLinkRev() {
        return linkRev;
    }

    public byte[] loadBlock(RandomAccessFile file) throws IOException {
        long off = this.offset;
        if (parent.isDataInline) {
            off += (revision + 1) * RevlogEntry.BINARY_LENGTH;
        }
        file.seek(off);
        return read(file);
    }

    private byte[] read(RandomAccessFile file) throws IOException {
        byte[] data = new byte[(int) this.compressedLength];
        file.read(data);
        return data;
    }

    public long getOffset() {
		return offset;
	}

    public void setOffset(int offset) {
		this.offset = offset;
	}

    long getUncompressedLength() {
        return uncompressedLength;
    }
    long getCompressedLength() {
    	return compressedLength;
    }

    public String toString() {

        RevlogEntry p1 = getNullEntry();
        RevlogEntry p2 = getNullEntry();

        if (0 <= firstParentRev) {
            p1 = parent.index.get(firstParentRev);
        }
        if (0 <= secondParentRev) {
            p2 = parent.index.get(secondParentRev);
        }
        return revision + "  " + offset + "	" + compressedLength + " 		"
                // + uncompressedLength + " "
                + baseRev + " 	" + linkRev + " 	" + nodeId.asShort() + " 	"
                + p1.nodeId.asShort() + " 	" + p2.nodeId.asShort();

    }

    static RevlogEntry getNullEntry() {
        if (nullInstance == null) {
            nullInstance = valueOf(null, new byte[64], 0);
        }
        return nullInstance;
    }

    private void read(DataInputStream reader) throws IOException {

        offset = ((long) reader.readShort() << 32) + reader.readInt();
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

	public int getFlags() {
		return flags;
	}
}