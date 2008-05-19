package org.freehg.hgkit;

import java.util.List;

import org.freehg.hgkit.HgChangeLog.ChangeLog;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.junit.Test;


public class HgChangeLogTest {

	@Test
	public void testGetLog() {
		
		Repository repo = new Repository("mozilla-central");
		HgChangeLog subject = new HgChangeLog(repo);
		long start = System.currentTimeMillis();
		Revlog revlog = repo.getChangeLog();
		long end = System.currentTimeMillis();
		System.out.println("Index took " + (end - start) );
		int count = 0;
		for(ChangeLog changeLog : subject.getLog(revlog)) {
			++count;
			if(true) continue;
		    log(changeLog.getRevision().asShort());
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
		// TODO Auto-generated method stub
		
	}

}
