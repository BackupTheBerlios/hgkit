package org.freehg.hgkit;

import java.io.File;

public class HgStatus {

    public enum Status { ADDED, REMOVED, MERGED, NOT_TRACKED, MANAGED, MODIFIED, IGNORED }; 
    private final File file;
    private Status status;

    HgStatus(File file) {
        this.file = file;
    }

    public HgStatus(File testee, Status status) {
        file = testee;
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return status + " " + file.getPath();
    }

}
