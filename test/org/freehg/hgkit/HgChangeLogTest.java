package org.freehg.hgkit;

import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.junit.Test;


public class HgChangeLogTest {

	@Test
	public void testGetLog() {
		
		long start = System.currentTimeMillis();
		Repository repo = new Repository("hg-stable");
		org.freehg.hgkit.core.ChangeLog subject = repo.getChangeLog();
		
		long end = System.currentTimeMillis();
		System.out.println("Index took " + (end - start) );
		List<ChangeSet> revisions = subject.getLog();
		end = System.currentTimeMillis();
		
//		for(ChangeSet changeLog : revisions) {
//			print(changeLog);
//		    ++count;
//		};
		System.out.println("Took " + (end - start) + " ms to parse " + revisions.size());
	}

	private void print(ChangeSet changeLog) {
		log(changeLog.getChangeId().asShort());
		log(changeLog.getWhen());
		log(changeLog.getAuthor());
		log(changeLog.getComment());

		List<String> files = changeLog.getFiles();
		for (String file : files) {
		    log("> " + file);
		}
		log("");
		log("");
//		    List<FileStatus> status = subject.getFileStatus(changeLog);
//		    for (FileStatus st : status) {
//		//    	System.out.println(st);
//				
//			}
	}

	private void log(Object o ) {
		if(false) {
			System.out.println(o.toString());
		}
	}
}
