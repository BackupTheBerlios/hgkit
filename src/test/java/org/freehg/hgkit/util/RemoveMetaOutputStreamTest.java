/**
 * Copyright 2008 mirko
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
import java.io.IOException;

import org.freehg.hgkit.Tutil;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class RemoveMetaOutputStreamTest {

    private static final String DATA = "##\n# User Database\n# ";

    private static final String META_DATA = "\ncopyrev: f19cc7cb916200d98e4ef68d7715604da8a4a09f\ncopy: src/test/java/passwd\n\n";
    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Test
    public void testWithMetaData() {
        Repository repository = new Repository(repoDir);
        File absolute = repository.makeAbsolute("src/test/resources/moved-file");
        Revlog revlog = repository.getRevlog(absolute);
        NodeId nodeId = revlog.tip().getId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        revlog.revision(nodeId, out);
        assertEquals("this file is moved and has Metadata", out.toString());
    }
    
    @Test
    public void testWithoutMetaData() throws IOException {
        byte[] input = DATA.getBytes();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        for (byte b : input) {
            removeMetaOutputStream.write(b);
        }
        assertEquals(DATA, new String(out.toByteArray()));
    }

}
