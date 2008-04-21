package org.freehg.hgkit.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {

	
	@Test
	public void getChangeLog() {
		Repository subject = getSubject();
		subject.getChangeLog();
	}
	@Test
	public void testGetIndex() {
		Repository subject = getSubject();
		File theFile = new File("src/org/freehg/hgkit/HgStatusClient.java");
		File index = subject.getIndex(theFile);
		// System.out.println(index);
		assertTrue(index.exists());
	}
	private Repository getSubject() {
		Repository subject = new Repository(".");
		return subject;
	}

	@Test
	public void testReadIndex() {
		Repository subject = getSubject();
		File theFile = new File("src/org/freehg/hgkit/core/MDiff.java");
		File index = subject.getIndex(theFile);
		Revlog revlog = new Revlog(index,index);
		
		for(NodeId nodeId : revlog.getRevisions()) {
			revlog.revision(nodeId);
			// System.out.println(revision);
		}
	}
	
	
	private int numRevisions = 0;
	
	
	@Before
	public void setUp() {
	    numRevisions = 0;
	}
	
	@Test
	public void testAll() throws Exception {
		Repository subject = getSubject();
		int count = walk(subject,new File("src"));
		System.out.println(count + " num files tested and " + numRevisions + " revivions");
	}
	private int walk(Repository repo, File dir) {
	    int count = 0;
		for(File file : dir.listFiles()) {
			if( file.isFile()) {
				testFile(repo, file);
				count++;
			}
		}
		for(File file : dir.listFiles()) {
			if(file.isDirectory() 
					&& !file.equals(dir.getParent())
					&& !file.equals(dir)) {
				count += walk(repo,file);
			}
		}
		return count;
	}
	
	private void testFile(Repository repo, File file) {
		File index = repo.getIndex(file);
		Revlog revlog = new Revlog(index,index);
		for(NodeId nodeId : revlog.getRevisions()) {
			revlog.revision(nodeId);
			numRevisions++;
		}
	}
}