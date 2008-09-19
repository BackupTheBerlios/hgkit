/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.Manifest;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;
import org.freehg.hgkit.core.DirState.DirStateEntry;

/**
 * @author mirko
 *
 */
public class HgUpdateClient {

    private final String revision;
    private final Repository repo;

    /**
     * 
     */
    public HgUpdateClient(Repository repo, String revision) {
        this.repo = repo;
        this.revision = revision;
    }
    
    public void update() {        
        DirState dirState = repo.getDirState();
        Collection<DirStateEntry> states = dirState.getDirState();
        for (DirStateEntry state : states) {
            String path = state.getPath();
            System.err.println("path:" + path);
            final File absoluteFile = repo.makeAbsolute(path);
            System.err.println("absoluteFile:" + absoluteFile);
            File index = repo.getIndex(absoluteFile);
            System.err.println("index:" + index);
            Revlog revlog = repo.getRevlog(index);
            System.err.println(revlog.tip());            
        }
    }
}
