package org.freehg.hgkit.core;

import java.io.File;
import java.util.Collection;

import org.freehg.hgkit.core.DirState.DirStateEntry;
import org.junit.Test;


public class DirStateTest {
	
	@Test
	public void testDirState() throws Exception {
		
		DirState state = new DirState(new File(".hg/dirstate"));
        Collection<DirStateEntry> result = state.getDirState();
        for (DirStateEntry dirStateEntry : result) {
            System.out.println(dirStateEntry);
        }
	}
}
