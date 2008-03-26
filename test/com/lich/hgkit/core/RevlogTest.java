package com.lich.hgkit.core;

import java.io.File;

import org.junit.Test;


public class RevlogTest {
	
	@Test
	public void testReadManifest() {
		
		File index = new File(".hg/store/data/src/com/lich/hgkit/_hg_status_client.java.i");
		
		Revlog subject = new Revlog(index, index);
		NodeId last = null;
		System.out.println(subject);
		for(NodeId nodeId : subject.getRevisions()) {
			last = nodeId;
		};
		System.out.println(last);
		
		String revision = subject.revision(last);
		
		System.out.println(revision);
	}

}
