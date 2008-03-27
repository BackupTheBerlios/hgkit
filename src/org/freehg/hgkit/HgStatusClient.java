package org.freehg.hgkit;

import java.io.File;

import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.DirState.DirStateEntry;


public class HgStatusClient {

	private DirState dirState;
	
	public HgStatusClient(DirState state) {
		if( state == null) {
			throw new IllegalArgumentException("DirState may not be null");
		}
		this.dirState = state;
	}
	
	public void doStatus(final File file) {
		if(file.isDirectory()) {
			for(File sub : file.listFiles()) {
				doStatus(sub);
			}
			return;
		}
		System.out.println();
		
		DirStateEntry state = this.dirState.getState(file.getPath().replace("\\", "/"));
		
		System.out.print(file.getName() + " - ");
		if(state == null) {
			System.out.println("Not managed");
			return;
		}
		if( state.getState() == 'a') {
			System.out.println("Added");
			return;
		}
		
		if( state.getState() == 'r') {
			System.out.println("REMOVED");
			return;
		}
		if( state.getState() == 'm') {
			System.out.println("3-way merged");
			return;
		}
		
		if( state.getState() == 'n') {
			checkStateNormal(file, state);
			return;
		}
		
		// 1 Check dirstate
		
		// On (a)dded files the status is added
		// on (r)emoved files, the status is removed
		// on m filees, the file is merged
		
		// files NOT found in dirstate are ?
		// files found in dirstate but not in directory is "missing/deleted"
	}

	private void checkStateNormal(File file, DirStateEntry state) {
		// On (n)ormal files
		// 		if size and mod time is same as in dirstate nothing has happened
		// 		if the size HAS changed, the file must have changed
		
		// Hg uses seconds, java milliseconds
		long lastModified = file.lastModified() / 1000;
//		System.out.println( state.getSize() + " == " + file.length() + " && " + state.getFileModTime() + " == " + lastModified);
		if( state.getSize() == file.length() && state.getFileModTime() == lastModified) {
			System.out.println("Not Changed");
			return;
		}
		if( state.getSize() != file.length()) {
			System.out.println("Must have changed");
			return;
		}
		// 		if the filemod time has changed but the size haven't
		// 		then we must compare against the repository version
		System.out.println("Must use repository to determine if it has changed");
		// TODO Binary compare the repository version against the supplied version

		
		// There should be more in the uncompressed shit than this
	}
}
