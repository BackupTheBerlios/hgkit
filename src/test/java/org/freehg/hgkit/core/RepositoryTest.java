package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
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
     * Tests for {@link Repository#makeRelative(File)}
     * 
     * @throws IOException
     */
    @Test
    public void testMakeRelative() throws IOException {
        final File root = new File(".", "abc").getCanonicalFile();
        assertEquals("abc", subject.makeRelative(root).toString());
    }

    private final Repository subject;

    private int numRevisions = 0;

    @Before
    public void setUp() {
        numRevisions = 0;
    }
}
