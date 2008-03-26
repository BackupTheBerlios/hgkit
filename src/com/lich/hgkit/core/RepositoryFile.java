package com.lich.hgkit.core;

import java.io.File;

public final class RepositoryFile {

	public static RepositoryFile lookUp(Repository repo,File file, long revision) {
		if(revision == -1) {
			// LookUp Head
		}
		
		throw new IllegalStateException("This is not implemented yet");
	}
}
