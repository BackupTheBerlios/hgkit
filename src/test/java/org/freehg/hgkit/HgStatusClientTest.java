package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class HgStatusClientTest {

    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() throws IOException {
        FileUtils.deleteDirectory(repoDir);        
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyRepos() {
        new HgStatusClient(null);
    }
    
    @Test
    public void testStatusClient() throws InterruptedException, IOException {
        Repository repo = new Repository(repoDir.getAbsolutePath());        
        // hg must be found in your PATH!
        final String cmd = "hg update --clean";
        assertEquals("'" + cmd + "' did not exit properly.", 0, Runtime.getRuntime().exec(cmd, null, repo.getRoot())
                .waitFor());
        long start = System.currentTimeMillis();
        HgStatusClient subject = new HgStatusClient(repo);

        List<FileStatus> status = subject.doStatus(repo.getRoot());
        long end = System.currentTimeMillis();
        for (FileStatus hgStatus : status) {
            assertEquals(hgStatus.getFile().getAbsolutePath(), FileStatus.Status.MANAGED, hgStatus.getStatus());
        }

        System.err.println("Status walk took " + (end - start) + " ms for " + status.size() + " files.");
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
