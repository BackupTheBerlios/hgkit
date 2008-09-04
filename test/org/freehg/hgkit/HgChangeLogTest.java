package org.freehg.hgkit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.freehg.hgkit.util.FileHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HgChangeLogTest {

    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = TestHelper.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Test
    public void testGetLog() {

        long start = System.currentTimeMillis();
        Repository repo = new Repository(repoDir.getAbsolutePath());
        org.freehg.hgkit.core.ChangeLog subject = repo.getChangeLog();

        long end = System.currentTimeMillis();
        System.out.println("Index took " + (end - start));
        List<ChangeSet> revisions = subject.getLog();
        end = System.currentTimeMillis();

        // for (ChangeSet changeLog : revisions) {
        // print(changeLog);
        // }
        System.out.println("Took " + (end - start) + " ms to parse " + revisions.size());
    }

    @SuppressWarnings("unused")
    private void print(ChangeSet changeLog) {
        log(changeLog.getChangeId().asShort());
        log(changeLog.getWhen());
        log(changeLog.getAuthor());
        log(changeLog.getComment());

        List<String> files = changeLog.getFiles();
        for (String file : files) {
            log("> " + file);
        }
        log("");
        log("");
        // List<FileStatus> status = subject.getFileStatus(changeLog);
        // for (FileStatus st : status) {
        // // System.out.println(st);
        //				
        // }
    }

    private void log(Object o) {
        if (true) {
            System.out.println(o.toString());
        }
    }
}
