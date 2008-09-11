package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freehg.hgkit.util.FileHelper;

public class DirState {

    private NodeId currentId;

    private Map<String, DirStateEntry> dirstate = new HashMap<String, DirStateEntry>();

    private List<DirStateEntry> values = new ArrayList<DirStateEntry>(1024);

    DirState(File dirState) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(dirState);
            byte[] data = Util.toByteArray(fis);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            parse(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileHelper.close(fis);
        }
    }

    private void parse(DataInputStream in) throws IOException {

        dirstate.clear();
        values.clear();
        // ">c l l l l"
        // state, mode, size, fileModTime, nameLength, bytes[namelength] as name
        // (String)
        parseHeader(in);
        while (in.available() > 0) {
            byte state = in.readByte();
            int mode = in.readInt();
            int size = in.readInt();
            int fileModTime = in.readInt();
            int nameLength = in.readInt();

            byte[] str = new byte[nameLength];
            in.readFully(str);
            String path = new String(str);
            DirStateEntry entry = new DirStateEntry(state, mode, size, fileModTime, path);
            // put with both / and \ as path separator
            this.dirstate.put(path.replace('/', '\\'), entry);
            this.dirstate.put(path.replace('\\', '/'), entry);
            this.values.add(entry);

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

    public Collection<DirStateEntry> getDirState() {
        return Collections.unmodifiableList(this.values);
    }

    private void parseHeader(DataInputStream in) throws IOException {
        this.currentId = NodeId.read(in);
        in.skip(20);
    }

    public static class DirStateEntry {
        
        private long size;

        private long mode;

        private long fileModTime;

        private int state;

        private String path;

        public long getSize() {
            return size;
        }

        public long getMode() {
            return mode;
        }

        public long getFileModTime() {
            return fileModTime;
        }

        public int getState() {
            return state;
        }

        public String getPath() {
            return path;
        }

        public DirStateEntry(byte state, int mode, int size, int fileModTime, String path) {
            this.state = state;
            this.mode = mode;
            this.size = size;
            this.fileModTime = fileModTime;
            this.path = path;
        }

        @Override
        public String toString() {
            return mode + "	" + (char) state + "	" + size + "	" + fileModTime + "	" + path;
        }
    }

    public NodeId getId() {
        return this.currentId;
    }
}
