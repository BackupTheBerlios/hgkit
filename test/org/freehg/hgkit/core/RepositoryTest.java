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
		File theFile = new File("src/org/freehg/hgkit/core/MDiff.java");
		File index = subject.getIndex(theFile);
		Revlog revlog = new Revlog(index,index);
		
		for(NodeId nodeId : revlog.getRevisions()) {
			String revision = revlog.revision(nodeId);
			System.out.println(revision);
		}
	}
	
	@Test
	public void testAll() throws Exception {
		Repository subject = new Repository(".");
		walk(subject,new File("src"));
	}
	private void walk(Repository repo, File dir) {
		for(File file : dir.listFiles()) {
			if( file.isFile()) {
				System.out.println(file);
				testFile(repo, file);
			}
		}
		for(File file : dir.listFiles()) {
			if(file.isDirectory() 
					&& !file.equals(dir.getParent())
					&& !file.equals(dir)) {
				walk(repo,file);
			}
		}
	}
	
	private void testFile(Repository repo, File file) {
		File index = repo.getIndex(file);
		Revlog revlog = new Revlog(index,index);
		for(NodeId nodeId : revlog.getRevisions()) {
			revlog.revision(nodeId);
		}
	}
}
