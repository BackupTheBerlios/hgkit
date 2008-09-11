package org.freehg.hgkit.core;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RevlogTest {

    private File index;

    /**
     * 
     */
    public RevlogTest(String indexName) {
        index = new File("src/test/resources/", indexName);
    }

    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[] { "test1.txt.i" }, new String[] { "test2.txt.i" },
                new String[] { "test3.txt.i" }, new String[] { "test4.txt.i" }, new String[] { "test5.txt.i" },
                new String[] { "bigger.txt.i" });
    }

    @Test
    public void testGetLatestRevision() {
        Revlog subject = new Revlog(index);
        int numRev = subject.count();
        System.err.println("Test file " + index + " has : " + numRev + " revisions");
        String prevRevision = null;
        NodeId prevNodeId = null;
        for (NodeId nodeId : subject.getRevisions()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            subject.revision(nodeId, out);
            final String currentRevision = out.toString();
            System.err.println(nodeId + ":" + currentRevision);
            assertFalse("Revisions " + nodeId + " and " + prevNodeId + " of " + index + " must not be equal!",
                    currentRevision.equals(prevRevision));
            prevRevision = currentRevision;
            prevNodeId = nodeId;
        }
        subject.close();
    }

    @Ignore
    @Test
    public void testGetAllRevision() throws Exception {
        // if( true ) return;
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

    private void log(Object revision) {
        if (revision != null) {
            System.out.println(revision.toString());
        } else {
            System.out.println("null");
        }
    }
}
