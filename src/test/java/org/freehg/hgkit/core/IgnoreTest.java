/*
 * IgnoreTest.java 10.09.2008
 * 
 */
package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.Tutil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IgnoreTest {
    
    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() throws IOException {
        FileUtils.deleteDirectory(repoDir);       
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
    public final void testParse() throws IOException {        
        Ignore ignore = new Ignore(null);
        StringReader reader = new StringReader("syntax: glob\ntarget\nsyntax:regex\n.*.class\nsyntax:glob\n.settings");
        ignore.parse(new BufferedReader(reader));
        assertEquals(3, ignore.ignores.size());
        assertTrue("'foobar.class' should be ignored.", ignore.isIgnored(new File("foobar.class")));
        assertTrue("'.settings' should be ignored.", ignore.isIgnored(new File(".settings")));
    }
    
    @Test(expected=HgInternalError.class)
    public final void testParseInvalidSyntax() throws IOException {        
        Ignore ignore = new Ignore(null);
        StringReader reader = new StringReader("syntax: blub\ntarget");
        ignore.parse(new BufferedReader(reader));
    }   
    
    @Test(expected=HgInternalError.class)
    public final void testParseInvalidRegex() throws IOException {        
        Ignore ignore = new Ignore(null);
        StringReader reader = new StringReader("syntax: regex\n[a-z]*[}");
        ignore.parse(new BufferedReader(reader));
    }   

}
