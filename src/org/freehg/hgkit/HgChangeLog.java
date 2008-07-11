package org.freehg.hgkit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;

public class HgChangeLog {

	private final Repository repo;

	public HgChangeLog(Repository repo) {
		this.repo = repo;

	}

	public List<ChangeLog> getLog() {
		Revlog revlog = repo.getChangeLog(0);
		return getLog(revlog);
	}
	
	public ChangeLog getLog(NodeId nodeId) {
		Revlog revlog = repo.getChangeLog(Revlog.AUTO_CLOSE);
		try {
			return getEntry(revlog, nodeId);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	List<ChangeLog> getLog(Revlog revlog) {
		Set<NodeId> revisions = revlog.getRevisions();
		List<ChangeLog> logEntries = new ArrayList<ChangeLog>(revisions.size());
		
		try {
			for (NodeId nodeId : revisions) {
				ChangeLog logEntry = getEntry(revlog, nodeId);
				
				logEntries.add(logEntry);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} finally {
			revlog.close();
		}
		return logEntries;
	}

	private ChangeLog getEntry(Revlog revlog, NodeId nodeId)
			throws ParseException {
		ByteArrayOutputStream content = new ByteArrayOutputStream(4096);
		revlog.revision(nodeId, content);
		
		ChangeLog logEntry = parse(new ByteArrayInputStream(content.toByteArray()));
		logEntry.changeId = nodeId;
		logEntry.revision = revlog.index(nodeId);
		return logEntry;
	}
	private ChangeLog parse(final InputStream in) throws ParseException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		ChangeLog entry = new ChangeLog();
		String line = null;
		try {
			while(null != (line = reader.readLine())) {
				entry.nodeId = NodeId.parse(line);
				entry.author = reader.readLine();

				String dateLine = reader.readLine();
				entry.when = dateParse(dateLine);

				String fileLine = reader.readLine();
				// read while line aint empty, its a file, the it is the comment
				while(0 < fileLine.trim().length()) {
					entry.files.add(fileLine);
					fileLine = reader.readLine();
				}
				StringBuilder therest = new StringBuilder();
				char[] buff = new char[512];
				int len = 0;
				while(-1 != (len = reader.read(buff))) {
					therest.append(buff,0, len);
				}
				
				entry.comment = therest.toString();
				
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return entry;
	}

	private Date dateParse(String dateLine) {
	    String parts[] = dateLine.split(" ");
	    long secondsSinceEpoc = Integer.parseInt(parts[0]);
	    long offset = Integer.parseInt(parts[1]);
	    long msSinceEpoc = 1000 * (secondsSinceEpoc + offset);
        return new Date(msSinceEpoc);
    }

    public static class ChangeLog {
		public NodeId nodeId;
		public int revision;
		private NodeId changeId;
		private Date when;
		private String author;
		private List<String> files = new ArrayList<String>();
		private String comment;

		public NodeId getChangeId() {
			return changeId;
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
	}
}
