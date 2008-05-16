package org.freehg.hgkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;

public class HgChangeLog {

	private final Repository repo;

	public HgChangeLog(Repository repo) {
		this.repo = repo;

	}

	public List<ChangeLog> getLog() {
		Revlog revlog = repo.getChangeLog();
		List<ChangeLog> logEntries = new ArrayList<ChangeLog>(revlog.getRevisions().size());

		try {
		for (NodeId nodeId : revlog.getRevisions()) {
				String logString = new String(revlog.revision(nodeId));
				ChangeLog logEntry = parse(logString);
				logEntries.add(logEntry);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return logEntries;
	}
	private ChangeLog parse(String text) throws ParseException {
		ChangeLog entry = new ChangeLog();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line = null;
		try {
			while(null != (line = reader.readLine())) {
				entry.revision = NodeId.parse(line);
				entry.author = reader.readLine();

				String dateLine = reader.readLine();
				// TODO: Parse the dateformat, it is num seconds since epoch +- offset
				entry.when = dateParse(dateLine);

				String fileLine = reader.readLine();
				// read while line aint empty, its a file, the it is the comment
				while(0 < fileLine.trim().length()) {
					entry.files.add(fileLine);
					fileLine = reader.readLine();
				}
				CharBuffer theRest = CharBuffer.allocate(text.length());
				reader.read(theRest);
				theRest.flip();
				String comment = theRest.toString();
				entry.comment = comment;

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
		private NodeId revision;
		private Date when;
		private String author;
		private List<String> files = new ArrayList<String>();
		private String comment;

		public NodeId getRevision() {
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
		    return revision.asShort() + " "
		    + when + " "
		    + author + "\n"
		    + comment + "\n"
		    + files;
		}
	}

}
