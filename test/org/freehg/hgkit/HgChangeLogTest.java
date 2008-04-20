package org.freehg.hgkit;

import org.freehg.hgkit.core.Repository;
import org.junit.Test;


public class HgChangeLogTest {

	@Test
	public void testGetLog() {
		
		Repository repo = new Repository(".");
		HgChangeLog subject = new HgChangeLog(repo);
		
		subject.getLog();
		
	}

}
