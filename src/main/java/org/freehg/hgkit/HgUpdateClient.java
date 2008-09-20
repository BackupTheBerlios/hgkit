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
            final File absoluteFile = repo.makeAbsolute(path);
            final Revlog revlog = repo.getRevlog(absoluteFile);
            final RevlogEntry tip = revlog.tip();
            if (".hgignore".equals(path)) {
                final NodeId nodeId = tip.getId();
                revlog.revision(nodeId, System.out);
            }
        }
    }
}
