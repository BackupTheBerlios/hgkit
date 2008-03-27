package org.freehg.hgkit.core;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Ignore;
import org.junit.Test;


public class RevlogTest {
	
	@Test
	public void testGetLatestRevision() {
		
		File index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");
		
		Revlog subject = new Revlog(index, index);
		NodeId last = null;
		System.out.println(subject);
		for(NodeId nodeId : subject.getRevisions()) {
			last = nodeId;
		};
		System.out.println(last);
		
		String revision = subject.revision(last);
		System.out.println(" ################################ ");
		System.out.println(revision);
	}
	
	@Ignore
	@Test
	public void testGetAllRevision() throws Exception {
	    
	    File index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");
	    
	    Revlog subject = new Revlog(index, index);
	    NodeId last = null;
	    System.out.println(subject);
	    
	    int count = 0;
	    for(NodeId nodeId : subject.getRevisions()) {
	        
	        count++;
	        FileOutputStream fos = new FileOutputStream("rev"+count+".txt");
	        last = nodeId;
	        String revision = subject.revision(last);
	        System.out.println(" ################################ ");
	        System.out.println(revision);
	        
	        fos.write(revision.getBytes());
	        fos.close();
	    };
	    
	}

}
