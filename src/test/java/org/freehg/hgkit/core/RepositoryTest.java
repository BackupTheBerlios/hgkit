package org.freehg.hgkit.core;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {


	/**
     * 
     */
    private static final String HG_STATUS_CLIENT_SOURCE = "src/main/java/org/freehg/hgkit/HgStatusClient.java";
    /**
     * 
     */
    private static final String MDIFF_SOURCE = "src/main/java/org/freehg/hgkit/core/MDiff.java";


    @Test
	public void getChangeLog() {
		Repository subject = getSubject();
		subject.getChangeLog().close();
	}
	@Test
	public void testGetIndex() {
		Repository subject = getSubject();
		File theFile = new File(HG_STATUS_CLIENT_SOURCE);
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
		File theFile = new File(MDIFF_SOURCE);
		File index = subject.getIndex(theFile);

		Revlog revlog = new Revlog(index);
		
		for(NodeId nodeId : revlog.getRevisions()) {
			revlog.revision(nodeId, new ByteArrayOutputStream());
		}
		revlog.close();
	}


	private int numRevisions = 0;


	@Before
	public void setUp() {
	    numRevisions = 0;
	}
}
