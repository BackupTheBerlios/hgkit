package org.freehg.hgkit.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import junit.framework.Assert;

import org.freehg.hgkit.Util;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.freehg.hgkit.util.FileHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LongRunningTest {

    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Util.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    private int numRevisions;

    @Test
    public void testAll() throws Exception {
        Repository subject = getSubject();
        int count = walk(subject, repoDir);
        System.out.println(count + " num files tested and " + numRevisions + " revivions");
    }

    private Repository getSubject() {
        return new Repository(repoDir.getAbsolutePath());
    }

    private int walk(Repository repo, File dir) throws IOException {
        String abs = dir.getAbsolutePath();

        if (dir.getName().contains(".hg")) {
            return 0;
        }
        int count = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                testFile(repo, file);
                count++;
            }
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory() && !file.equals(dir.getParent()) && !file.equals(dir)
                    && !file.getAbsolutePath().endsWith(".hg")) {
                count += walk(repo, file);
            }
        }
        return count;
    }

    private void testFile(Repository repo, final File file) throws IOException {
        final File index = repo.getIndex(file);
        Revlog revlog = new Revlog(index);
        ChangeLog changelog = repo.getChangeLog();
        ChangeSet log = changelog.get(repo.getDirState().getId());
        Map<String, NodeId> manifest = repo.getManifest().get(log);
        final FileInputStream stream = new FileInputStream(file);
        // paths are always stored with / instead of \
        NodeId fileNode = manifest.get(repo.makeRelative(file).toString().replace("\\", "/"));
        if (fileNode == null) {
            Assert.fail("Could not lookup manifest entry for file " + file);
        }
        revlog.revision(fileNode, new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                int fromFile = stream.read() & 0xFF;
                b = b & 0xFF;
                if ((fromFile != b)) {
                    throw new IllegalStateException("Tip of file: " + file + " did not match HgKit revision : " + b
                            + " != " + fromFile);
                }
            }
        }).close();
        this.numRevisions++;
        stream.close();
    }
}
