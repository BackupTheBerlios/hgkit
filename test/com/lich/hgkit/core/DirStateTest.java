package com.lich.hgkit.core;

import static org.junit.Assert.*;

import java.io.File;

import org.freehg.hgkit.core.DirState;
import org.junit.Test;


public class DirStateTest {
	
	@Test
	public void testDirState() throws Exception {
		
		DirState state = new DirState(new File(".hg/dirstate"));
		
		System.out.println(state);
	}

}
