package org.freehg.hgkit;

import java.io.File;
import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.junit.Test;



public class HgStatusClientTest {

	
	
	@Test
	public void testStatusClient() {
		
		long start = System.currentTimeMillis();

		Repository repo = new Repository("../com.vectrace.MercurialEclipse/");
		HgStatusClient subject = new HgStatusClient(repo);

		List<HgStatus> status = subject.doStatus(new File("../com.vectrace.MercurialEclipse/"));
		long end = System.currentTimeMillis();
		for (HgStatus hgStatus : status) {
		     System.out.println(hgStatus);
            
        }
		
		System.out.println("Status walk took " + (end - start) + " ms");
	}
}
