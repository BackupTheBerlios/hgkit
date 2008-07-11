package org.freehg.hgkit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.freehg.hgkit.HgChangeLog.ChangeLog;
import org.freehg.hgkit.HgStatus.Status;
import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.Ignore;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;
import org.freehg.hgkit.core.DirState.DirStateEntry;

public class HgStatusClient {

	private static final char STATE_MERGED = 'm';
	private static final char STATE_ADDED = 'a';
	private static final char STATE_REMOVED = 'r';
    private static final char STATE_NORMAL = 'n';
    private DirState dirState;
    private final Repository repo;
    private Ignore ignore;
	private ChangeLog logEntry;
	private HgManifest manifest;

    public HgStatusClient(Repository repo) {
        this.repo = repo;
        if (repo == null) {
            throw new IllegalArgumentException("Repository may not be null");
        }
        this.dirState = repo.getDirState();
        this.ignore = repo.getIgnore();
        
        HgManifestClient manifestClient = new HgManifestClient(repo);
        HgChangeLog logClient = new HgChangeLog(repo);
        logEntry = logClient.getLog(dirState.getId());
        manifest = manifestClient.getManifest(logEntry);
        
    }

    public List<HgStatus> doStatus(final File file) {
        return doStatus(file, true);
    }

    public List<HgStatus> doStatus(final File file, final boolean recurse) {
        List<HgStatus> result = getStatus(file, recurse, isIgnored(file));
        result.addAll(getMissing());
        return result;
    }

    private List<HgStatus> getStatus(final File file, final boolean recurse, boolean parentIgnored) {
        if(Repository.isRepoPrivate(file)) {
        	return Collections.EMPTY_LIST;
        }
        List<HgStatus> result = new ArrayList<HgStatus>();

        if (recurse && file.isDirectory()) {
            for (File sub : file.listFiles()) {
                result.addAll(getStatus(sub, recurse, parentIgnored | isIgnored(file)));
            }
            return result;
        }
        if (file.isFile()) {
            result.add(getFileState(file, parentIgnored));
        }
        return result;
    }

    private List<HgStatus> getMissing() {
        Collection<DirStateEntry> state = dirState.getDirState();
        List<HgStatus> missing = new ArrayList<HgStatus>();
        for (DirStateEntry entry : state) {
            File testee = repo.makeAbsolute(entry.getPath());
            if (!testee.exists()) {
                missing.add(new HgStatus(testee, Status.DELETED));
            }
        }
        return missing;
    }

    private boolean isIgnored(final File file) {
        return file.getName().equalsIgnoreCase(".hg") || ignore.isIgnored(file);
    }

    private HgStatus getFileState(final File file, boolean parentIgnored) {
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " must be a file");
        }
        File relativeFile = repo.makeRelative(file);
        HgStatus status = new HgStatus(relativeFile);
        DirStateEntry state = this.dirState.getState(relativeFile.getPath().replace(
                "\\", "/"));

        if(state != null) {
	        switch(state.getState()) {
	            case STATE_ADDED:
	                status.setStatus(HgStatus.Status.ADDED);
	                break;
	            case STATE_REMOVED:
	                status.setStatus(HgStatus.Status.REMOVED);
	                break;
	            case STATE_MERGED:
	                status.setStatus(HgStatus.Status.MERGED);
	                break;
	            case STATE_NORMAL:
	                status.setStatus(checkStateNormal(file, state));
	                break;
	            default:
	        }
        } else {
	        status.setStatus(HgStatus.Status.NOT_TRACKED);
	        if (parentIgnored || isIgnored(relativeFile)) {
	        	status.setStatus(HgStatus.Status.IGNORED);
	        }
        }
        return status;
    }

    private Status checkStateNormal(File file, DirStateEntry state) {
        // On (n)ormal files
        // if size and mod time is same as in dirstate nothing has happened
        // if the size HAS changed, the file must have changed
    	// After an update, dirstate is not written back and contains -1
        if (0 <= state.getSize() && state.getSize() != file.length()) {
            return HgStatus.Status.MODIFIED;
        }

        // Hg uses seconds, java milliseconds
        long lastModified = file.lastModified() / 1000;
        if (state.getFileModTime() == lastModified) {
            return HgStatus.Status.MANAGED;
        }
        // if the filemod time has changed but the size haven't
        // then we must compare against the repository version
        Revlog revlog = repo.getRevlog(file);
        
        try {
        	
            InputStream local = new BufferedInputStream(new FileInputStream(
                    file));
            ComparingStream comparator = new ComparingStream(local);
            revlog.revision(manifest.getState(state.getPath()), comparator);
            local.close();
            if(comparator.equals) {
                return HgStatus.Status.MANAGED;
            }
            return HgStatus.Status.MODIFIED;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class ComparingStream extends OutputStream {
        private boolean equals = true;
        private final InputStream in;
        ComparingStream(InputStream in) {
            this.in = in;

        }
        @Override
        public void write(int b) throws IOException {
            if( b != in.read()) {
                this.equals = false;
            }
        }
    }
}
