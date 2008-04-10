package org.freehg.hgkit.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class RepositoryTest {

	@Test
	public void testGetIndex() {
		Repository subject = new Repository(".");
		File theFile = new File("src/org/freehg/hgkit/HgStatusClient.java");
		File index = subject.getIndex(theFile);
		System.out.println(index);
		assertTrue(index.exists());
	}

	@Test
	public void testReadIndex() {
		Repository subject = new Repository(".");
		File theFile = new File("src/org/freehg/hgkit/HgStatus.java");
		File index = subject.getIndex(theFile);
		Revlog revlog = new Revlog(index,index);
		
		for(NodeId nodeId : revlog.getRevisions()) {
			String revision = revlog.revision(nodeId);
			System.out.println(revision);
		}
	}
}
