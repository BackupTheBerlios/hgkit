/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Set;

import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.Manifest;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;
import org.freehg.hgkit.core.DirState.DirStateEntry;
import org.freehg.hgkit.util.FileHelper;

class UpdateFile {

    private final File absoluteFile;

    private final Revlog revlog;

    /**
     * 
     */
    public UpdateFile(Repository repo, String path) {
        absoluteFile = repo.makeAbsolute(path);
        revlog = repo.getRevlog(absoluteFile);
    }

    /**
     * Checks out the tip-revision of {@link UpdateFile#absoluteFile}. 
     */
    public void tip() {
        final RevlogEntry tip = revlog.tip();
        final NodeId nodeId = tip.getId();
        System.err.println(absoluteFile);
        File parentFile = absoluteFile.getParentFile();
        parentFile.mkdirs();
        final BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(absoluteFile));
        } catch (FileNotFoundException e) {
            throw new HgInternalError(e);
        }
        try {
            revlog.revision(nodeId, out);
        } finally {
            FileHelper.close(out);
        }
    }
}

/**
 * @author mirko
 * 
 */
public class HgUpdateClient {

    private final Repository repo;

    /**
     * 
     */
    public HgUpdateClient(Repository repo) {
        this.repo = repo;
    }

    public void update() {
        Collection<DirStateEntry> states = repo.getDirState().getDirState();
        for (DirStateEntry state : states) {
            final String path = state.getPath();
            if (true) {
                new UpdateFile(repo, path).tip();
            }
        }
    }
}
