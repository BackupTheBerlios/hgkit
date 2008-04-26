package org.freehg.hgkit;

import java.util.List;

import org.freehg.hgkit.HgChangeLog.ChangeLog;
import org.freehg.hgkit.core.Repository;
import org.junit.Test;


public class HgChangeLogTest {

	@Test
	public void testGetLog() {
		
		Repository repo = new Repository(".");
		HgChangeLog subject = new HgChangeLog(repo);
		
		for(ChangeLog changeLog : subject.getLog()) {
		    System.out.println(changeLog.getRevision().asShort());
		    System.out.println(changeLog.getWhen());
		    System.out.println(changeLog.getAuthor());
		    System.out.println(changeLog.getComment());

		    List<String> files = changeLog.getFiles();
		    for (String file : files) {
                System.out.println("> " + file);
            }
		    System.out.println("");
		    System.out.println("");
		    
		};
		
	}

}
