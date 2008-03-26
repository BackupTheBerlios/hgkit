package com.lich.hgkit;

import java.io.File;

import org.junit.Test;

import com.lich.hgkit.core.DirState;


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
