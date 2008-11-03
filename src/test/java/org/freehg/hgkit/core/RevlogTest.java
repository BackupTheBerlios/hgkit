package org.freehg.hgkit.core;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RevlogTest {

    private File indexFile;

    public RevlogTest(String indexName) {
        indexFile = new File(indexName);
    }

    @Parameters
    public static Collection<String[]> data() {
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] testFiles = new String[] { "test1.txt.i", "test2.txt.i", "test3.txt.i", "test4.txt.i", "test5.txt.i",
                "bigger.txt.i" };
        for (String testFile : testFiles) {
            result.add(new String[] { "src/test/resources/" + testFile });
        }
        result.add(new String[] { ".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i" });
        return result;
    }

    @Test
    public void testGetLatestRevision() {
        Revlog subject = new Revlog(indexFile);
        // int numRev = subject.count();
        // System.err.println("Test file " + indexFile + " has : " + numRev +
        // " revisions");
        String prevRevision = null;
        NodeId prevNodeId = null;
        for (NodeId nodeId : subject.getRevisions()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            subject.revision(nodeId, out);
            final String currentRevision = out.toString();
            // System.err.println(nodeId + ":" + currentRevision.length());
            assertFalse("Revisions " + nodeId + " and " + prevNodeId + " of " + indexFile + " must not be equal!",
                    currentRevision.equals(prevRevision));
            prevRevision = currentRevision;
            prevNodeId = nodeId;
        }
        subject.close();
    }

    @Ignore(value="performancetest")
    @Test
    public void testGetAllRevision() {

        File index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");

        Revlog subject = new Revlog(index);
        long totalBytes = 0;
        int count = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            final Set<NodeId> revisions = subject.getRevisions();
            count = revisions.size();
            for (NodeId nodeId : revisions) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                subject.revision(nodeId, out);
                totalBytes += out.size();
            }
        }
        subject.close();
        long end = System.currentTimeMillis();

        System.err.println("Took " + (end - start) + " ms to get " + count + " revisions 1000 times totaling to "
                + totalBytes + " bytes.");

    }

    public static void main(String[] args) {
        RevlogTest revlogTest = new RevlogTest(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");
        testUncached(revlogTest);
        testCached(revlogTest);
    }

    /**
     * This test is not really cached, as caching is disabled in {@link Revlog} right now.
     * However after reenabling it should run much faster than {@link RevlogTest#testUncached(RevlogTest)}.
     * @param revlogTest
     */
    private static void testCached(RevlogTest revlogTest) {
        long start = System.nanoTime();
        revlogTest.testGetAllRevision();
        long end = System.nanoTime();
        System.err.println("testGetAllRevision took " + (end - start) / 1000000 + " ms. (Cached)");
    }

    /**
     * @param revlogTest
     */
    private static void testUncached(RevlogTest revlogTest) {
        {
            long start = System.nanoTime();
            final int iterations = 1000;
            for (int i = 0; i < iterations; i++) {
                if (i % 100 == 0) {
                    System.err.println(i);
                }
                revlogTest.testGetLatestRevision();
            }
            long end = System.nanoTime();
            System.err.println(iterations + " iterations of testGetLatestRevision took " + (end - start) / 1000000
                    + " ms.");
        }
    }
}
