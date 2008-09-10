/*
 * IgnoreTest.java 10.09.2008
 * 
 */
package org.freehg.hgkit.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.freehg.hgkit.Util;
import org.freehg.hgkit.util.FileHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IgnoreTest {
    
    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Util.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testIgnoreRepository() {
        new Ignore(new Repository("."));
    }

    @Test
    public final void testIsIgnoredEmptyIgnores() {        
        Repository repo = new Repository(repoDir.getAbsolutePath());
        Ignore ignore = new Ignore(repo);
        assertFalse(ignore.isIgnored(new File("doesnotmatter")));        
    }

    @Test
    public final void testIsIgnoredAbsolutePath() {        
        Repository repo = new Repository(repoDir.getAbsolutePath());
        // this is somewhat of a fake, as .hgignore will be taken from the checked out copy!
        Ignore ignore = new Ignore(repo, new File(".hgignore"));
        assertTrue(ignore.isIgnored(new File(repoDir.getAbsolutePath(), "target")));        
    }
    
    @Test
    @org.junit.Ignore
    public final void testParse() {
        fail("Not yet implemented"); // TODO
    }

}
