package com.lich.hgkit.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.lich.hgkit.core.DirState;

public class DirStateTest {
	
	@Test
	public void testDirState() throws Exception {
		
		DirState state = new DirState(new File(".hg/dirstate"));
		
		System.out.println(state);
	}

}
