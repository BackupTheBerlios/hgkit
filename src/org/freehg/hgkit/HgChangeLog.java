package org.freehg.hgkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.text.DateFormat;
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
	
	public void getLog() {
		File index = repo.getChangeLog();
		Revlog revlog = new Revlog(index,index);
		List<ChangeLog> logEntries = new ArrayList<ChangeLog>(revlog.getRevisions().size());
		
		try {
		for (NodeId nodeId : revlog.getRevisions()) {
				System.out.println(nodeId);
				System.out.println("---------------------------------");
				String logString = revlog.revision(nodeId);
				System.out.println(logString);
				System.out.println("---------------------------------");
				ChangeLog logEntry = parse(logString);

				logEntries.add(logEntry);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
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
				// entry.when = DateFormat.getDateInstance().parse(dateLine);
				
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
	}
	
}
