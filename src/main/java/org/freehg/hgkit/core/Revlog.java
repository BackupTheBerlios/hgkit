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

import org.freehg.hgkit.util.RemoveMetaOutputStream;

public class Revlog {

    private static final String READ_ONLY = "r";

    public static final int REVLOGV0 = 0;

    public static final int REVLOGNG = 1;

    public static final int REVLOGNGINLINEDATA = (1 << 16);

    public static final int REVLOG_DEFAULT_FLAGS = REVLOGNGINLINEDATA;

    public static final int REVLOG_DEFAULT_FORMAT = REVLOGNG;

    public static final int REVLOG_DEFAULT_VERSION = REVLOG_DEFAULT_FORMAT | REVLOG_DEFAULT_FLAGS;

    /* FIXME: Create NULLID */
    private static final NodeId NULLID = null;

    boolean isDataInline;

    private Map<NodeId, RevlogEntry> nodemap;

    ArrayList<RevlogEntry> index;

    private final File indexFile;

    private LinkedHashMap<RevlogEntry, byte[]> cache = new RevlogCache();

    private RandomAccessFile reader = null;

    public Revlog(File index) {
        indexFile = index;
        try {
            parseIndex(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<NodeId> getRevisions() {
        return Collections.unmodifiableSet(this.nodemap.keySet());
    }

    public int index(NodeId nodeId) {
        return nodemap.get(nodeId).revision;
    }

    public NodeId node(int index) {
        List<Entry<NodeId, RevlogEntry>> entries = new ArrayList<Entry<NodeId, RevlogEntry>>(this.nodemap.entrySet());
        for (Entry<NodeId, RevlogEntry> entry : entries) {
            if (entry.getValue().revision == index) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException(this + " has no such revision");
    }

    public NodeId linkrev(int linkrev) {
        List<Entry<NodeId, RevlogEntry>> entries = new ArrayList<Entry<NodeId, RevlogEntry>>(this.nodemap.entrySet());
        for (Entry<NodeId, RevlogEntry> entry : entries) {
            if (entry.getValue().linkRev == linkrev) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException(this + " has no such revision");
    }

    public Revlog revision(NodeId node, OutputStream out) {
        return revision(node, out, true);
    }

    public Revlog revision(NodeId node, OutputStream out, boolean noMetaData) {
        if (node.equals(NULLID)) {
            return this;
        }
        final RevlogEntry target = nodemap.get(node);
        return revision(target, out, noMetaData);
    }

    protected Revlog revision(int index, OutputStream out, boolean noMetaData) {
        return revision(this.index.get(index), out, noMetaData);
    }

    protected Revlog revision(final RevlogEntry target, OutputStream out, boolean noMetaData) {
        if (noMetaData) {
            out = new RemoveMetaOutputStream(out);
        }
        if ((target.getFlags() & 0xFFFF) != 0) {
            throw new IllegalStateException("Incompatible revision flag: " + target.getFlags());
        }
        if (cache.containsKey(target)) {
            writeFromCache(target, out);
            return this;
        }

        try {
            List<byte[]> patches = new ArrayList<byte[]>(target.revision - target.getBaseRev() + 1);
            byte[] baseData = null;
            for (int i = target.revision; i >= target.getBaseRev(); i--) {
                RevlogEntry entry = index.get(i);
                byte[] fromCache = cache.get(entry);
                if (fromCache != null) {
                    baseData = fromCache;
                    break;
                }
                if (baseData != null) {
                    patches.add(baseData);
                }
                baseData = Util.decompress(entry.loadBlock(getDataFile()));
            }
            Collections.reverse(patches);
            // FIXME worst case size value is calculated wrong (but kinda works)
            long worstCaseSize = baseData.length;

            if (worstCaseSize < RevlogCache.CACHE_SMALL_REVISIONS) {
                CacheOutputStream cacheOut = new CacheOutputStream(out, (int) worstCaseSize);
                MDiff.patches(baseData, patches, cacheOut);
                this.cache.put(target, cacheOut.cache.toByteArray());

            } else {
                MDiff.patches(baseData, patches, out);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void writeFromCache(final RevlogEntry target, OutputStream out) {
        try {
            out.write(cache.get(target));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private RandomAccessFile getDataFile() throws FileNotFoundException {
        if (this.reader != null && this.reader.getChannel().isOpen()) {
            return this.reader;
        }
        if (this.isDataInline) {
            this.reader = new RandomAccessFile(this.indexFile, READ_ONLY);
        } else {
            String path = this.indexFile.getAbsolutePath();
            String dataFilePath = path.substring(0, path.length() - ".i".length()) + ".d";
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
        final int version;
        try {
            version = reader.readInt();
        } finally {
            reader.close();
        }

        isDataInline = (version & REVLOGNGINLINEDATA) != 0;
        // Its pretty odd, but its the revlogFormat which is the "version"
        final long revlogFormat = version & 0xFFFF;
        if (revlogFormat != REVLOGNG) {
            throw new IllegalStateException("Revlog format MUST be NG");
        }
        /*
         * long flags = version & ~0xFFFF; TODO check index for unknown flags
         * (see revlog.py)
         */

        nodemap = new LinkedHashMap<NodeId, RevlogEntry>();
        this.index = new ArrayList<RevlogEntry>(100);

        reader = new DataInputStream(new BufferedInputStream(new FileInputStream(index)));
        final byte[] data;
        try {
            data = Util.readWholeFile(reader);
        } finally {
            reader.close();
        }
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
            indexOffset += RevlogEntry.BINARY_LENGTH;
            ++indexCount;
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

    private static class RevlogCache extends LinkedHashMap<RevlogEntry, byte[]> {
        
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
