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

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.util.RemoveMetaOutputStream;

/**
 * The Java-Implementation of RevlogNG. This baseclass is extended by
 * {@link Manifest} and {@link ChangeLog}.
 * 
 * @see <a href="http://www.selenic.com/mercurial/wiki/index.cgi/Revlog">Revlog
 *      page at selenic</a>
 * @see <a
 *      href="http://www.selenic.com/mercurial/wiki/index.cgi/RevlogNG">RevlogNG
 *      page at selenic</a>
 * @see <a
 *      href="http://hgbook.red-bean.com/hgbookch4.html#x8-640004">Documention
 *      in HgBook</a>
 * 
 */
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

    /**
     * Creates a Revlog from the given Mercurial-index-file.
     * 
     * @param index
     */
    public Revlog(File index) {
        indexFile = index;
        try {
            parseIndex(index);
        } catch (IOException e) {
            throw new HgInternalError("Error creating Revlog for " + index.toString(), e);
        }
    }

    /**
     * Returns the revisions in the Revlog-file.
     * 
     * @return revisions.
     */
    public Set<NodeId> getRevisions() {
        return Collections.unmodifiableSet(this.nodemap.keySet());
    }

    /**
     * Returns the numeric index for a given nodeId.
     * 
     * @param nodeId
     *            the nodeId
     * @return index
     */
    public int index(NodeId nodeId) {
        return nodemap.get(nodeId).revision;
    }

    /**
     * Returns the nodeId for a given numeric index.
     * 
     * @param revisionIndex
     *            the numeric index
     * @return nodeId
     */
    public NodeId node(int revisionIndex) {
        List<Entry<NodeId, RevlogEntry>> entries = new ArrayList<Entry<NodeId, RevlogEntry>>(this.nodemap.entrySet());
        for (Entry<NodeId, RevlogEntry> entry : entries) {
            if (entry.getValue().revision == revisionIndex) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException(this + " has no such revision");
    }

    /**
     * Returns the NodeId of the linkrev.
     * 
     * @param linkrev
     *            numeric revision
     * @return nodeId
     */
    public NodeId linkrev(int linkrev) {
        List<Entry<NodeId, RevlogEntry>> entries = new ArrayList<Entry<NodeId, RevlogEntry>>(this.nodemap.entrySet());
        for (Entry<NodeId, RevlogEntry> entry : entries) {
            final RevlogEntry value = entry.getValue();
            if (value.getLinkRev() == linkrev) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException(this + " has no such revision");
    }

    /**
     * Writes the revision specified by nodeId to the given outputstream.
     * Metadata is not considered.
     * 
     * @param nodeId
     *            the nodeId
     * @param out
     *            outputstream
     * @return revlog
     */
    public Revlog revision(NodeId nodeId, OutputStream out) {
        return revision(nodeId, out, true);
    }

    /**
     * Writes the revision specified by nodeId to the given outputstream.
     * Metadata may be considered.
     * 
     * @param nodeId
     *            the nodeId
     * @param out
     *            outputstream
     * @param noMetaData
     *            should we ignore metadata.
     * @return revlog
     */
    public Revlog revision(NodeId nodeId, OutputStream out, boolean noMetaData) {
        if (nodeId.equals(NULLID)) {
            return this;
        }
        final RevlogEntry target = nodemap.get(nodeId);
        return revision(target, out, noMetaData);
    }

    /**
     * Writes the revision specified by the numeric index to the given
     * outputstream. Metadata may be considered.
     * 
     * @param revisionIndex
     *            numeric index.
     * @param out
     *            outputstream
     * @param noMetaData
     *            should we ignore metadata.
     * @return revlog
     */
    protected Revlog revision(int revisionIndex, OutputStream out, boolean noMetaData) {
        return revision(this.index.get(revisionIndex), out, noMetaData);
    }

    /**
     * Writes the specific revlogentry to the given outputstream. Metadata may
     * be considered.
     * 
     * @param target
     *            revlogentry
     * @param out
     *            outputstream
     * @param noMetaData
     *            should we ignore metadata.
     * @return revlog
     */
    protected Revlog revision(final RevlogEntry target, OutputStream out, boolean noMetaData) {
        final OutputStream finalOut;
        if (noMetaData) {
            finalOut = new RemoveMetaOutputStream(out);
        } else {
            finalOut = out;
        }
        if ((target.getFlags() & 0xFFFF) != 0) {
            throw new IllegalStateException("Incompatible revision flag: " + target.getFlags());
        }
        if (cache.containsKey(target)) {
            writeFromCache(target, finalOut);
            return this;
        }

        try {
            List<byte[]> patches = new ArrayList<byte[]>(target.revision - target.getBaseRev() + 1);
            byte[] baseData = null;
            for (int i = target.revision; i >= target.getBaseRev(); i--) {
                RevlogEntry entry = index.get(i);
                // TODO reenable caching
                // byte[] fromCache = cache.get(entry);
                // if (fromCache != null) {
                // baseData = fromCache;
                // break;
                // }
                if (baseData != null) {
                    patches.add(baseData);
                }
                baseData = Util.decompress(entry.loadBlock(getDataFile()));
            }
            Collections.reverse(patches);
            // FIXME worst case size value is calculated wrong (but kinda works)
            long worstCaseSize = baseData.length;

            // cache data if it is small enough.
            if (worstCaseSize < RevlogCache.CACHE_SMALL_REVISIONS) {
                CacheOutputStream cacheOut = new CacheOutputStream(finalOut, (int) worstCaseSize);
                MDiff.patches(baseData, patches, cacheOut);
                this.cache.put(target, cacheOut.cache.toByteArray());
            } else {
                MDiff.patches(baseData, patches, finalOut);
            }

        } catch (IOException e) {
            throw new HgInternalError(target.toString(), e);
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

    /**
     * Returns a RandomAccessFile to the data stored in revlog. Switch to
     * accessing the datafile instead of the indexfile, if data is not inlined.
     * 
     * @return randomAccessFile
     * @throws FileNotFoundException
     *             if the index or data file is not available.
     */
    private RandomAccessFile getDataFile() throws FileNotFoundException {
        if (this.reader != null && this.reader.getChannel().isOpen()) {
            return this.reader;
        }
        if (this.isDataInline) {
            this.reader = new RandomAccessFile(this.indexFile, READ_ONLY);
        } else {
            final String path = this.indexFile.getAbsolutePath();
            final String dataFilePath = path.substring(0, path.length() - ".i".length()) + ".d";
            this.reader = new RandomAccessFile(new File(dataFilePath), READ_ONLY);
        }
        return this.reader;
    }

    /**
     * Returns the revlogentry for the tip revision.
     * 
     * @return revlogentry
     */
    public RevlogEntry tip() {
        return index.get(count() - 1);
    }

    /**
     * Returns the number of revisions.
     * 
     * @return revisions-count
     */
    public int count() {
        return index.size();
    }

    /**
     * Closes the underlying reader of the revlog-file.
     */
    public void close() {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new HgInternalError("reader" + reader, e);
        }
        reader = null;
    }

    /**
     * versionformat = ">I", big endian, uint 4 bytes which includes version
     * format
     */
    private void parseIndex(File fileOfIndex) throws IOException {
        final int version = readVersion(fileOfIndex);

        isDataInline = (version & REVLOGNGINLINEDATA) != 0;
        checkRevlogFormat(version);

        nodemap = new LinkedHashMap<NodeId, RevlogEntry>();
        this.index = new ArrayList<RevlogEntry>(100);

        final byte[] data = toByteArray(fileOfIndex);

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

    /**
     * Reads all data from the file. Reading is buffered and data is read as
     * {@link DataInputStream}.
     * 
     * @param fileOfIndex
     *            file
     * @return byte-array.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private byte[] toByteArray(File fileOfIndex) throws FileNotFoundException, IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileOfIndex)));
        try {
            return IOUtils.toByteArray(in);
        } finally {
            in.close();
        }
    }

    /**
     * Checks the Revlog-format.
     * 
     * @param version
     *            of the revlog-file.
     */
    private void checkRevlogFormat(final int version) {
        // Its pretty odd, but its the revlogFormat which is the "version"
        final long revlogFormat = version & 0xFFFF;
        if (revlogFormat != REVLOGNG) {
            throw new IllegalStateException("Revlog format MUST be NG");
        }
        /*
         * long flags = version & ~0xFFFF; TODO check index for unknown flags
         * (see revlog.py)
         */
    }

    /**
     * Reads the version from the first bytes of fileOfIndex.
     * 
     * @param fileOfIndex
     *            file
     * @return version
     * @throws FileNotFoundException
     * @throws IOException
     */
    private int readVersion(File fileOfIndex) throws FileNotFoundException, IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(fileOfIndex));
        try {
            return in.readInt();
        } finally {
            in.close();
        }
    }

    /**
     * Caching decorator. This class caches all writes in a
     * {@link ByteArrayOutputStream}.
     */
    private static class CacheOutputStream extends OutputStream {

        private ByteArrayOutputStream cache;

        private final OutputStream cached;

        /**
         * @param cached
         *            the cached outputstream.
         * @param size
         *            initial size of the caching {@link ByteArrayOutputStream}
         */
        private CacheOutputStream(OutputStream cached, int size) {
            this.cached = cached;
            this.cache = new ByteArrayOutputStream(size);
        }

        /** {@inheritDoc} */
        @Override
        public void write(int b) throws IOException {
            this.cache.write(b);
            this.cached.write(b);
        }
    }

    /**
     * Caches all {@link RevlogEntry} in a {@link LinkedHashMap}.
     */
    private static class RevlogCache extends LinkedHashMap<RevlogEntry, byte[]> {

        private static final long serialVersionUID = 6934630760462643470L;

        static final int CACHE_SMALL_REVISIONS = 4024;

        static final long MAX_CACHE_SIZE = 100000;

        private long cachedDataSize = 0;

        /** {@inheritDoc} */
        @Override
        protected boolean removeEldestEntry(Entry<RevlogEntry, byte[]> eldest) {
            boolean removeEldest = this.cachedDataSize > MAX_CACHE_SIZE;
            if (removeEldest) {
                cachedDataSize -= eldest.getValue().length;
            }
            return removeEldest;
        }

        /** {@inheritDoc} */
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
