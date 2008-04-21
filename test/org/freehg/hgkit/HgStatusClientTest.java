package org.freehg.hgkit;

import java.io.File;

import org.freehg.hgkit.core.Repository;
import org.junit.Test;



public class HgStatusClientTest {

	
	
	@Test
	public void testStatusClient() {
		
		long start = System.currentTimeMillis();
		Repository repo = new Repository(".");
		
		HgStatusClient subject = new HgStatusClient(repo);
		
		subject.doStatus(new File("src"));
		long end = System.currentTimeMillis();
		
		System.out.println("Status walk took " + (end - start) + " ms");
	}
}
