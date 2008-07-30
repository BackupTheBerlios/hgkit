package org.freehg.hgkit;

import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.junit.Test;


public class HgChangeLogTest {

	@Test
	public void testGetLog() {
		
		Repository repo = new Repository("hg-stable");
		org.freehg.hgkit.core.ChangeLog subject = repo.getChangeLog();
		
		long start = System.currentTimeMillis();
		Revlog revlog = repo.getChangeLog();
		long end = System.currentTimeMillis();
		System.out.println("Index took " + (end - start) );
		int count = 0;
		List<ChangeSet> revisions = subject.getLog();
		
		for(ChangeSet changeLog : revisions) {
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
		    ++count;
		    
		};
		end = System.currentTimeMillis();
		System.out.println("Took " + (end - start) + " ms to parse " + count);
	}

	private void log(Object o ) {
		if(false) {
			System.out.println(o.toString());
		}
	}
}
