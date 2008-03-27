package org.freehg.hgkit.core;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DirState {

	private Map<String, DirStateEntry> dirstate  = new HashMap<String, DirStateEntry>();
	
	public DirState(File dirState) {
		try {
			FileInputStream fis = new FileInputStream(dirState);
			DataInputStream in = new DataInputStream(new BufferedInputStream(fis));
			parse(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parse(DataInputStream in) throws IOException {
		
		dirstate.clear();
		// ">c l l l l"
		// state, mode, size, fileModTime, nameLength, bytes[namelength] as name (String) 
		parseHeader(in);
		while (in.available() > 0) {
			byte state = in.readByte();
			int mode = in.readInt();
			int size = in.readInt();
			int fileModTime = in.readInt();
			int nameLength = in.readInt();

			byte[] str = new byte[nameLength];
			in.read(str);
			String path = new String(str);
			DirStateEntry entry = new DirStateEntry(state, mode, size, fileModTime, path);
			this.dirstate.put(path, entry);
		}
	}
	/**
	 * NOTE: Remember to unFold the path before you use this 
	 * @param path
	 * @return a {@link DirStateEntry} if one is avaialable for this repository. Null otherwise
	 */
	public DirStateEntry getState(String path) {
		return this.dirstate.get(path);
	}

	private void parseHeader(DataInputStream in) throws IOException {
		in.skip(40);
	}

	public class DirStateEntry {
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

		public DirStateEntry(byte state, int mode, int size, int fileModTime,
				String path) {
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
}
