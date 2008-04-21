package org.freehg.hgkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.DirState.DirStateEntry;
import org.freehg.hgkit.core.Revlog.RevlogEntry;


public class HgStatusClient {

	private DirState dirState;
    private final Repository repo;
	
	public HgStatusClient(Repository repo) {
		this.repo = repo;
        if( repo== null) {
			throw new IllegalArgumentException("Repository may not be null");
		}
		this.dirState = repo.getDirState();
	}
	
	public void doStatus(final File file) {
		if(file.isDirectory()) {
			for(File sub : file.listFiles()) {
				doStatus(sub);
			}
			return;
		}
		// System.out.println();
		
		DirStateEntry state = this.dirState.getState(file.getPath().replace("\\", "/"));
		
		
		if(state == null) {
			System.out.println("?");
		}else if( state.getState() == 'a') {
			System.out.println("A");
		}else if( state.getState() == 'r') {
			System.out.println("R");
		} else if( state.getState() == 'm') {
			System.out.println("3-way merged");
		} if( state.getState() == 'n') {
			checkStateNormal(file, state);
		}
		System.out.println(" - " + file.getName());		
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
			System.out.print("C");
			return;
		}
		if( state.getSize() != file.length()) {
			System.out.print("M");
			return;
		}
		// 		if the filemod time has changed but the size haven't
		// 		then we must compare against the repository version
		Revlog revlog = repo.getRevlog(file);
		RevlogEntry tip = revlog.tip();
		byte[] repoContent = revlog.revision(tip.getId());
		try {
		    FileInputStream local = new FileInputStream(file);
		    for(int i = 0; i < file.length(); i++) {
		        byte a = (byte) local.read();
		        byte b = repoContent[i];
		        if( a != b) {
		            System.out.print("Mn");
		            local.close();
		            return;
		        }
		    }
		    local.close();
		} catch(IOException ex) {
		    throw new RuntimeException(ex);
		}
		System.out.print("Cn");
		// TODO Binary compare the repository version against the supplied version

	}
	
	public void addedMethod() {
	    // this method were added
	}
}
