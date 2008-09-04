package org.freehg.hgkit;

import java.io.File;

public class FileStatus {

    public enum Status { 
    	ADDED('A'), 
    	REMOVED('R'), 
    	DELETED('D'), 
    	MERGED('M'), 
    	NOT_TRACKED('?'), 
    	MANAGED('C'), 
    	MODIFIED('M'), 
    	IGNORED('I');
    	
    	Status(char c) {
    		this.asHg = c;
    	}
    	private char asHg;
    	@Override
    	public String toString() {
    		return Character.toString(this.asHg);
    	}
    }; 
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
