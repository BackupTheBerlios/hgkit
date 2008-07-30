package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public final class ChangeLog extends Revlog {
	
	ChangeLog(File index) {
		super(index);
	}
	@Deprecated
	ChangeLog(File index, int style) {
		super(index);
	}
	
	public ChangeSet get(int revision) {
		NodeId node = super.node(revision);
		return this.get(node);
	}
	
	public ChangeSet get(NodeId node) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		super.revision(node, out).close();
		return new ChangeSet(node, index(node), out.toByteArray());
	}
	
	public List<ChangeSet> getLog() {
		try {
			int length = count();
			List<ChangeSet> log = new ArrayList<ChangeSet>(length);
			for(int revision = 0; revision < length; revision++) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				RevlogEntry revlogEntry = index.get(revision);
				super.revision(revlogEntry, out, false);
				ChangeSet entry = new ChangeSet(revlogEntry.nodeId, revision, out.toByteArray());
				log.add(entry);
				
			}
			return log;
		} finally {
			close();
		}
	}
	
	public static class ChangeSet {
		
		private NodeId manifestId;
		private int revision;
		private NodeId changeId;
		private Date when;
		private String author;
		private List<String> files = new ArrayList<String>();
		private String comment;

		public NodeId getChangeId() {
			return changeId;
		}
		
		public NodeId getManifestId() {
			return manifestId;
		}
		
		public int getRevision() {
			return revision;
		}
		
		public Date getWhen() {
			return when;
		}
		public String getAuthor() {
			return author;
		}
		public List<String> getFiles() {
			return new ArrayList<String>(files);
		}
		public String getComment() {
			return comment;
		}
		
		@Override
		public String toString() {
		    return changeId.asShort() + " "
		    + when + " "
		    + author + "\n"
		    + comment + "\n"
		    + files;
		}
		
		private void parse(final InputStream in) throws ParseException {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			try {
				while(null != (line = reader.readLine())) {
					manifestId = NodeId.parse(line);
					author = reader.readLine();

					String dateLine = reader.readLine();
					when = dateParse(dateLine);

					String fileLine = reader.readLine();
					// read while line aint empty, its a file, the it is the
					// comment
					while(0 < fileLine.trim().length()) {
						files.add(fileLine);
						fileLine = reader.readLine();
					}
					StringBuilder therest = new StringBuilder();
					char[] buff = new char[512];
					int len = 0;
					while(-1 != (len = reader.read(buff))) {
						therest.append(buff,0, len);
					}
					
					comment = therest.toString();
					
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private Date dateParse(String dateLine) {
		    String parts[] = dateLine.split(" ");
		    long secondsSinceEpoc = Integer.parseInt(parts[0]);
		    long offset = Integer.parseInt(parts[1]);
		    long msSinceEpoc = 1000 * (secondsSinceEpoc + offset);
	        return new Date(msSinceEpoc);
	    }
		
		
		ChangeSet(NodeId changeId, int revision, byte[] data) {
			
			try {
				parse(new ByteArrayInputStream(data));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			this.changeId = changeId;
			this.revision = revision;
			
		}
	}
}
