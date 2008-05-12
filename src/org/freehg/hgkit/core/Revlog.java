package org.freehg.hgkit.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Revlog {

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
    public byte[] revision(NodeId node) {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	revision(node, out);
    	return out.toByteArray();
    }

    public void revision(NodeId node, OutputStream out) {
    	if (node.equals(NULLID)) {
            return;
        }

        RevlogEntry target = nodemap.get(node);
        if ((target.getFlags() & 0xFFFF) != 0) {
            throw new IllegalStateException("Incompatible revision flag: "
                    + target.getFlags());
        }


        try {
            RandomAccessFile reader = new RandomAccessFile(this.dataFile, READ_ONLY);
            RevlogEntry baseRev = index.get(target.getBaseRev());
            byte[] baseData = Util.decompress(baseRev.loadBlock(reader));

            List<byte[]> patches = new ArrayList<byte[]>(target.revision - target.getBaseRev() + 1);
            for (int rev = target.getBaseRev() + 1; rev <= target.revision; rev++) {

                RevlogEntry nextEntry = this.index.get(rev);
                byte[] diff = Util.decompress(nextEntry.loadBlock(reader));
                patches.add(diff);
            }
            MDiff.patches(baseData, patches, out);

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
            indexOffset += entry.getCompressedLength() + RevlogEntry.BINARY_LENGTH;
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

    static void log(Object msg) {
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
