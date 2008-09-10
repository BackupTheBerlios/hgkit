package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.freehg.hgkit.util.FileHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class HgStatusClientTest {

    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Util.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Test
    public void testStatusClient() throws InterruptedException, IOException {
        Repository repo = new Repository(repoDir.getAbsolutePath());
        System.err.println("Repo to inspect: " +repo.getRoot().getAbsolutePath());
        // hg must be found in your PATH!
        final String cmd = "hg update --clean";
        assertEquals("'" + cmd + "' did not exit properly.", 0, Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor());
        long start = System.currentTimeMillis();
        HgStatusClient subject = new HgStatusClient(repo);

        List<FileStatus> status = subject.doStatus(repo.getRoot());
        long end = System.currentTimeMillis();
        for (FileStatus hgStatus : status) {
            System.out.println(hgStatus);
        }

        System.out.println("Status walk took " + (end - start) + " ms");
    }

    @Ignore
    @Test
    public void testStatusClientNasty() throws InterruptedException, IOException {

        Repository repo = new Repository(repoDir.getAbsolutePath());
        List<ChangeSet> log = repo.getChangeLog().getLog();
        int count = 0;
        for (ChangeSet changeSet : log) {
            if (count++ % 100 == 0) {
                repo = new Repository(repoDir.getAbsoluteFile());
                final String cmd = "hg update --clean --rev " + changeSet.getChangeId().asShort();
                System.out.println(cmd);
                Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor();
                doStatus(repo);
            }
        }
    }

    private void doStatus(Repository repo) {
        long start = System.currentTimeMillis();
        HgStatusClient subject = new HgStatusClient(repo);
        List<FileStatus> status = subject.doStatus(repo.getRoot());
        long end = System.currentTimeMillis();
        for (FileStatus hgStatus : status) {
            // Assert.assertEquals(FileStatus.Status.MANAGED,
            // hgStatus.getStatus());
            if (FileStatus.Status.MANAGED != hgStatus.getStatus()) {
                System.err.println(hgStatus + " should have been managed");
            }
        }
        System.out.println("Status walk took " + (end - start) + " ms (" + status.size() + ") files");
    }
}
