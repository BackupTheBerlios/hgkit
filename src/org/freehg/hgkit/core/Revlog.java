package org.freehg.hgkit.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Revlog {

    private static final String READ_ONLY = "r";

    public static class RevlogEntry {

        /** The corresponding length of indexformatng >Qiiiiii20s12x */
        private static final int BINARY_LENGTH = 64;
        private static RevlogEntry nullInstance;

        /**
         * 
         * @param parent
         * @param data
         * @param off
         *            where in data to begin extracting data
         * @return
         */
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

        public byte[] loadBlock(RandomAccessFile file) throws IOException {
            log("Loading block for: " + this.nodeId);

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

        long getUncompressedLength() {
            return uncompressedLength;
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
    }

    public static final int REVLOGV0 = 0;
    public static final int REVLOGNG = 1;
    public static final int REVLOGNGINLINEDATA = (1 << 16);
    public static final int REVLOG_DEFAULT_FLAGS = REVLOGNGINLINEDATA;
    public static final int REVLOG_DEFAULT_FORMAT = REVLOGNG;

    public static final int REVLOG_DEFAULT_VERSION = REVLOG_DEFAULT_FORMAT
            | REVLOG_DEFAULT_FLAGS;
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
     * return an uncompressed revision data of a given nodeid NOTE: hgkit
     * doesn't use caching for now
     * 
     * @param node
     *            to nodeid to get data for
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
            RandomAccessFile reader = new RandomAccessFile(this.dataFile, READ_ONLY);
            RevlogEntry baseRev = index.get(target.baseRev);
            byte[] baseData = Util.decompress(baseRev.loadBlock(reader));

            List<byte[]> patches = new ArrayList<byte[]>(target.revision - target.baseRev + 1);
            for (int rev = target.baseRev + 1; rev <= target.revision; rev++) {

                RevlogEntry nextEntry = this.index.get(rev);
                byte[] diff = Util.decompress(nextEntry.loadBlock(reader));
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
        byte[] data = Util.readWholeFile(reader);
        int length = data.length - RevlogEntry.BINARY_LENGTH;

        int indexCount = 0;
        int indexOffset = 0;

        while (indexOffset <= length) {
            RevlogEntry entry = RevlogEntry.valueOf(this, data, indexOffset);
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
            indexOffset += entry.compressedLength + RevlogEntry.BINARY_LENGTH;
            indexCount++;
        }
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

    private static void log(Object msg) {
        if(true) {
            // logging is off
            return;
        }
        
        if (msg != null) {
            System.out.println(msg.toString());
        } else {
            System.out.println("null");
        }
    }
}
