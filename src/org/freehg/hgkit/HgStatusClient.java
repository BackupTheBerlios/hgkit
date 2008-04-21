package org.freehg.hgkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.freehg.hgkit.HgStatus.Status;
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
	
	public List<HgStatus> doStatus(final File file) {
	    return doStatus(file, true);
	}
	public List<HgStatus> doStatus(final File file, final boolean recurse) {
	    List<HgStatus> result = new ArrayList<HgStatus>();
		if(recurse && file.isDirectory()) {
			for(File sub : file.listFiles()) {
				result.addAll(doStatus(sub, recurse));
			}
			return result;
		}
		if( file.isFile()) {
    		result.add(getFileState(file));
		}
		return result;		
	}

    private HgStatus getFileState(final File file) {
        if(! file.isFile()) {
            throw new IllegalArgumentException(file + " must be a file");
        }
        DirStateEntry state = this.dirState.getState(file.getPath().replace("\\", "/"));
        
        HgStatus status = new HgStatus(file);
        if(state == null) {
            status.setStatus(HgStatus.Status.NOT_TRACKED);
        }else if( state.getState() == 'a') {
            status.setStatus(HgStatus.Status.ADDED);
        }else if( state.getState() == 'r') {
            status.setStatus(HgStatus.Status.REMOVED);
        } else if( state.getState() == 'm') {
            status.setStatus(HgStatus.Status.MERGED);
        } else if(state.getState() == 'n') {
        	status.setStatus(checkStateNormal(file, state));
        }
        return status;
    }

	private Status checkStateNormal(File file, DirStateEntry state) {
		// On (n)ormal files
		// 		if size and mod time is same as in dirstate nothing has happened
		// 		if the size HAS changed, the file must have changed
		
		// Hg uses seconds, java milliseconds
		long lastModified = file.lastModified() / 1000;
		if( state.getSize() == file.length() && state.getFileModTime() == lastModified) {
		    return HgStatus.Status.MANAGED;
		}
		if( state.getSize() != file.length()) {
		    return HgStatus.Status.MODIFIED;
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
		            local.close();
		            return HgStatus.Status.MODIFIED;
		        }
		    }
		    local.close();
		} catch(IOException ex) {
		    throw new RuntimeException(ex);
		}
		return HgStatus.Status.MANAGED;
	}
}