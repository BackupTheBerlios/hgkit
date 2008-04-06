package org.freehg.hgkit.core;

import java.io.File;
import java.io.FileOutputStream;

import org.freehg.hgkit.core.Revlog.RevlogEntry;
import org.junit.Test;


public class RevlogTest {
	
	@Test
	public void testGetLatestRevision() {
		
		File index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");
		
		Revlog subject = new Revlog(index, index);
		int numRev = subject.count();
		System.out.println("Test file has : " + numRev + " revisions");
		RevlogEntry tip = subject.tip();
		
		NodeId last = null;
		System.out.println(subject);
		
		System.out.println(last);
		
		String revision = subject.revision(tip.getId());
		System.out.println(" ################################ ");
		System.out.println(revision);
	}
	
	// @Ignore
	@Test
	public void testGetAllRevision() throws Exception {
//	    if( true ) return; 
	    File index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");
	    
	    Revlog subject = new Revlog(index, index);
	    int count = 0;
	    for(NodeId nodeId : subject.getRevisions()) {
	        count++;
	        FileOutputStream fos = new FileOutputStream("rev"+count+".txt");
	        String revision = subject.revision(nodeId);
	        fos.write(revision.getBytes());
	        fos.close();
	    };
	    
	}

}
