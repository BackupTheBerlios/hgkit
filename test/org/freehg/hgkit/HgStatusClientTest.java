package org.freehg.hgkit;

import java.io.File;

import org.freehg.hgkit.core.DirState;
import org.junit.Test;



public class HgStatusClientTest {

	
	
	@Test
	public void testStatusClient() {
		
		long start = System.currentTimeMillis();
		DirState state = new DirState(new File(".hg/dirstate"));
		
		HgStatusClient subject = new HgStatusClient(state);
		
		subject.doStatus(new File("src"));
		long end = System.currentTimeMillis();
		
		System.out.println("Status walk took " + (end - start) + " ms");
	}
}
