package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.freehg.hgkit.util.FileHelper;

/**
 * State of the working directory.
 * 
 * @see <a
 *      href="http://www.selenic.com/mercurial/wiki/index.cgi/DirState>DirState<
 *      / a >
 * @see <a
 *      href="http://www.selenic.com/mercurial/wiki/index.cgi/WorkingDirectory">WorkingDirectory</a>
 * 
 */
public class DirState {

    private static final int HEADER_PAD_SIZE = 20;

    private static final int INITIAL_LIST_SIZE = 1024;

    private NodeId currentId;

    private final Map<String, DirStateEntry> dirstate = new HashMap<String, DirStateEntry>();

    private final List<DirStateEntry> values = new ArrayList<DirStateEntry>(INITIAL_LIST_SIZE);

    /**
     * Creates a {@link DirState}.
     * 
     * @param dirStateFile
     *            dirstate file
     */
    DirState(File dirStateFile) {
        try {
            DataInputStream in = toDataInputStream(dirStateFile);
            parse(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts all bytes of dirStateFile to a {@link DataInputStream}.
     * 
     * @param dirStateFile
     *            dirstate file
     * @return DataInputStream
     * @throws FileNotFoundException
     *             if dirStateFile could not be found.
     * @throws IOException
     *             if reading the file does not succeed.
     */
    private DataInputStream toDataInputStream(File dirStateFile) throws FileNotFoundException, IOException {
        final FileInputStream fis = new FileInputStream(dirStateFile);
        final byte[] data;
        try {
            data = Util.toByteArray(fis);
        } finally {
            FileHelper.close(fis);
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        return in;
    }

    private void parse(DataInputStream in) throws IOException {
        // ">c l l l l"
        // state, mode, size, fileModTime, nameLength, bytes[namelength] as name
        // (String)
        parseHeader(in);
        while (in.available() > 0) {
            DirStateEntry entry = DirStateEntry.readNext(in);
            final String path = entry.getPath();
            // put with both / and \ as path separator
            dirstate.put(path.replace('/', '\\'), entry);
            dirstate.put(path.replace('\\', '/'), entry);
            values.add(entry);

        }
    }

    /**
     * NOTE: Remember to unFold the path before you use this
     * 
     * @param path
     * @return a {@link DirStateEntry} if one is avaialable for this repository.
     *         Null otherwise
     */
    public DirStateEntry getState(String path) {
        return this.dirstate.get(path);
    }

    /**
     * Returns all entries for the directory state.
     * 
     * @return Collection of {@link DirStateEntry}
     */
    public Collection<DirStateEntry> getDirState() {
        return Collections.unmodifiableList(this.values);
    }

    /**
     * Reads the currentId and skips the padding.
     * 
     * @param in
     *            stream to read from
     * @throws IOException
     */
    private void parseHeader(DataInputStream in) throws IOException {
        currentId = NodeId.read(in);
        long skip = in.skip(HEADER_PAD_SIZE);
        assert skip == HEADER_PAD_SIZE;
    }

    /**
     * Returns the current nodeId.
     * 
     * @return nodeId
     */
    public NodeId getId() {
        return this.currentId;
    }

    /**
     * Describes the status of a single file in the working copy.
     */
    public static class DirStateEntry {

        private final long size;

        private final long mode;

        private final long fileModTime;

        private final int state;

        private final String path;

        /**
         * Returns the size of the file in bytes. If Mercurial is not able to
         * determine the state by size and modification time, returns -1.
         * 
         * @return size
         */
        public long getSize() {
            return size;
        }

        /**
         * Returns the POSIX-mode of the file.
         * 
         * @return mode
         */
        public long getMode() {
            return mode;
        }

        /**
         * Returns the modification time of the file as seconds since the
         * {@link Date#Date(long) standard base time}.
         * 
         * @return modification time
         */
        public long getFileModTime() {
            return fileModTime;
        }

        /**
         * Returns the state of the file. The states that are tracked are:
         * <dl>
         * <dt>n</dt>
         * <dd>normal</dd>
         * <dt>a</dt>
         * <dd>added</dd>
         * <dt>r</dt>
         * <dd>removed</dd>
         * <dt>m</dt>
         * <dd>3-way merged</dd>
         * </dl>
         * 
         * @return state
         */
        public char getState() {
            return (char) state;
        }

        /**
         * Returns the path of the file.
         * 
         * @return path
         */
        public String getPath() {
            return path;
        }

        /**
         * Creates a new {@link DirStateEntry}.
         * 
         * @param state
         *            {@link DirStateEntry#getState()}
         * @param mode
         *            {@link DirStateEntry#getMode()}
         * @param size
         *            {@link DirStateEntry#getSize()}
         * @param fileModTime
         *            {@link DirStateEntry#getFileModTime()}
         * @param path
         *            {@link DirStateEntry#getPath()}
         */
        DirStateEntry(final byte state, final int mode, final int size, final int fileModTime, final String path) {
            this.state = state;
            this.mode = mode;
            this.size = size;
            this.fileModTime = fileModTime;
            this.path = path;
        }

        /**
         * Reads a {@link DirStateEntry} from <code>in</code>.
         * 
         * @param in
         *            DataInputStream
         * @return DirStateEntry
         * @throws IOException
         *             when reading from <code>in</code> does not succeed.
         */
        public static DirStateEntry readNext(final DataInputStream in) throws IOException {
            final byte state = in.readByte();
            final int mode = in.readInt();
            final int size = in.readInt();
            final int fileModTime = in.readInt();
            final String path = readFileName(in);
            return new DirStateEntry(state, mode, size, fileModTime, path);
        }

        /**
         * Reads the filename from <code>in</code>.
         * 
         * @param in
         *            DataInputStream
         * @return filename
         * @throws IOException
         *             when reading from <code>in</code> does not succeed.
         */
        private static String readFileName(final DataInputStream in) throws IOException {
            final int nameLength = in.readInt();
            final byte[] str = new byte[nameLength];
            in.readFully(str);
            final String path = new String(str);
            return path;
        }

        /**
         * Returns a String representation of a DirState as returned by
         * <code>hg debugstate</code>.
         */
        @Override
        public String toString() {
            final String dateString;
            if (getFileModTime() == -1) {
                dateString = String.format(Locale.ENGLISH, "%18s", "unset");
            } else {
                dateString = String.format(Locale.ENGLISH, "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(
                        getFileModTime() * 1000));
            }
            final String modeString;
            if ((getMode() & 020000) != 0) {
                modeString = "lnk";
            } else {
                modeString = String.format(Locale.ENGLISH, "%3o", getMode() & 0777);
            }
            return String.format(Locale.ENGLISH, "%s %s %10d %s %s", getState(), modeString, getSize(), dateString,
                    getPath());
        }
    }

}
