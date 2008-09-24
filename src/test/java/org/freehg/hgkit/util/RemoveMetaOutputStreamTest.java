/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.freehg.hgkit.Tutil;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author mirko
 * 
 */
@RunWith(Parameterized.class)
public class RemoveMetaOutputStreamTest {

    private static File repoDir;
    private final String content;
    private final String filename;

    @BeforeClass
    public static void createCopy() {
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Parameters
    public static Collection<String[]> data() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(new String[] { "src/test/resources/moved-file", "this file is moved and has Metadata" });
        list.add(new String[] { "src/test/resources/anunmoved-file", "this file is not moved and has no Metadata\n" });
        return list;
    }

    public RemoveMetaOutputStreamTest(final String filename, final String content) {
        this.filename = filename;
        this.content = content;        
    }
    
    @Test
    public void testMetaData() {
        Repository repository = new Repository(repoDir);
        File absolute = repository.makeAbsolute(filename);
        Revlog revlog = repository.getRevlog(absolute);
        NodeId nodeId = revlog.tip().getId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        revlog.revision(nodeId, out);
        assertEquals(content, out.toString());
    }
}
