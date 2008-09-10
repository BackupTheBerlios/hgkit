package org.freehg.hgkit.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class RevlogTest {

    @Test
    public void testGetLatestRevision() {

        File index = null;

        index = new File(".hg/store/data/src/org/freehg/hgkit/core/_m_diff.java.i");
        index = new File("../com.vectrace.MercurialEclipse/.hg/store/data/plugin.xml.i");
        index = new File("../com.vectrace.MercurialEclipse/.hg/store/00changelog.i");
        index = new File(".hg/store/00changelog.i");

        index = new File(".hg/store/data/src/org/freehg/hgkit/_hg_status_client.java.i");

        Revlog subject = new Revlog(index);
        int numRev = subject.count();
        log("Test file has : " + numRev + " revisions");
        RevlogEntry tip = subject.tip();

        log(subject);

        for (NodeId rev : subject.getRevisions()) {
            System.out.print(rev.asShort());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            subject.revision(rev, out);
            String revision = out.toString();
            System.out.println(" -- [OK]");
            log(revision);
        }
        subject.close();
    }

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

        System.err.println("Took " + (end - start) + " ms to get " + count
                + " revisions 1000 times totaling to " + totalBytes + " bytes.");

    }

    private void log(Object revision) {
        if (revision != null) {
            System.out.println(revision.toString());
        } else {
            System.out.println("null");
        }
    }
}
