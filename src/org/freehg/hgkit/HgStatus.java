package org.freehg.hgkit;

import java.io.File;

public class HgStatus {

    public enum Status { ADDED, REMOVED, MERGED, NOT_TRACKED, MANAGED, MODIFIED }; 
    private final File file;
    private Status status;

    HgStatus(File file) {
        this.file = file;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return status + " " + file.getPath();
    }

}
