package org.freehg.hgkit;

import java.io.File;

public class FileStatus {

    public enum Status { ADDED, REMOVED, DELETED, MERGED, NOT_TRACKED, MANAGED, MODIFIED, IGNORED }; 
    private final File file;
    private Status status;

    FileStatus(File file) {
        this.file = file;
    }

    public FileStatus(File testee, Status status) {
        file = testee;
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {
		return status;
	}
    
    @Override
    public String toString() {
        return status + " " + file.getPath();
    }

	public File getFile() {
		return this.file;
	}

}
