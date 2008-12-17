package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.HgInitClient;
import org.junit.Test;

public class RepositoryTest {

    /**
     * 
     */
    private static final String HG_STATUS_CLIENT_SOURCE = "src/main/java/org/freehg/hgkit/HgStatusClient.java";

    /**
     * 
     */
    private static final String MDIFF_SOURCE = "src/main/java/org/freehg/hgkit/core/MDiff.java";

    /**
     * 
     */
    public RepositoryTest() {
        subject = getSubject();
    }

    @Test
    public void getChangeLog() {
        subject.getChangeLog().close();
    }

    /**
     * Tests for {@link Repository#getIndex(File)}
     */
    @Test
    public void testGetIndex() {
        testGetIndex(HG_STATUS_CLIENT_SOURCE, "_hg_status_client.java.i");
        testGetIndex(MDIFF_SOURCE, "_m_diff.java.i");
    }

    /**
     * @param source
     * @param suffix
     */
    private void testGetIndex(final String source, final String suffix) {
        final File index = subject.getIndex(new File(source));
        final String name = index.getName();
        assertTrue(index.exists());
        assertTrue(name + " does not end with " + suffix, name.endsWith(suffix));
    }

    private Repository getSubject() {
        return new Repository(".");
    }

    @Test
    public void testReadIndex() {
        File theFile = new File(MDIFF_SOURCE);
        File index = subject.getIndex(theFile);

        Revlog revlog = new Revlog(index);

        for (NodeId nodeId : revlog.getRevisions()) {
            revlog.revision(nodeId, new ByteArrayOutputStream());
        }
        revlog.close();
    }

    /**
     * Tests for {@link Repository#makeAbsolute(String)}
     * 
     * @throws IOException
     */
    @Test
    public void testMakeAbsolute() throws IOException {
        final File root = new File(".", "abc").getCanonicalFile();
        assertEquals(root.toString(), subject.makeAbsolute("abc").toString());
        assertEquals(root.toString(), subject.makeAbsolute("./abc").toString());
        assertEquals(root.toString(), subject.makeAbsolute("./abc/../abc").toString());
    }

    /**
     * Test for {@link Repository}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingDataDir() {
        new Repository("src");
    }

    /**
     * Test for {@link Repository}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingRootDir() {
        new Repository("abc");
    }
    
    /**
     * Tests for {@link Repository#makeRelative(File)}
     * 
     * @throws IOException
     */
    @Test
    public void testMakeRelative() throws IOException {
        final File root = new File(".", "abc").getCanonicalFile();
        assertEquals("abc", subject.makeRelative(root).toString());
    }

    @Test
    public void testNewFreshRepository() throws IOException {
        File testrepoDir = new File("target/testrepo");
        FileUtils.deleteDirectory(testrepoDir);
        testrepoDir.mkdirs();
        try {
            HgInitClient.create(testrepoDir);            
            Repository repository = new Repository(testrepoDir);
            repository.getRoot();
            repository.getIgnore();
//            repository.getDirState();
//            repository.getChangeLog();
//            repository.getManifest();
        } finally {
//            FileUtils.deleteDirectory(testrepoDir);
        }
    }
    private final Repository subject;
}
