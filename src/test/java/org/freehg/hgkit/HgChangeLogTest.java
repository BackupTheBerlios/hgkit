package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.freehg.hgkit.core.ChangeLog;
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
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    @Test
    public void testGetLog() {
        long start = System.currentTimeMillis();
        Repository repo = new Repository(repoDir.getAbsolutePath());
        ChangeLog subject = repo.getChangeLog();

        long end = System.currentTimeMillis();
        System.out.println("Index took " + (end - start));
        List<ChangeSet> revisions = subject.getLog();
        end = System.currentTimeMillis();
        final ChangeSet initialChangeSet = revisions.get(0);
        assertEquals("HemPc@PC212826566277", initialChangeSet.getAuthor());
        assertEquals("c9629f6b37d8", initialChangeSet.getChangeId().toString());
        assertEquals(1206558630000L, initialChangeSet.getWhen().getTime());
        System.out.println("Took " + (end - start) + " ms to parse " + revisions.size());
    }
}
