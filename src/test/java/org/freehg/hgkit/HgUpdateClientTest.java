/**
 * Copyright 2008 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.util.FileHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class HgUpdateClientTest {

    private static File repoDir;

    @BeforeClass
    public static void createCopy() {
        repoDir = Tutil.createRepoCopy();
    }

    @AfterClass
    public static void deleteCopy() {
        assertTrue("Could not delete copy in " + repoDir, FileHelper.deleteDirectory(repoDir));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.HgUpdateClient#HgUpdateClient(org.freehg.hgkit.core.Repository)}
     * .
     */
    @Test
    public final void testHgUpdateClient() {
        new HgUpdateClient(new Repository(repoDir));
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgUpdateClient#update()}.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public final void testUpdate() throws IOException, InterruptedException {
        HgUpdateClient hgUpdateClient = new HgUpdateClient(new Repository(repoDir));
        final int updatedFiles = hgUpdateClient.update();
        final String command = "hg status --all";
        final Process process = Runtime.getRuntime().exec(command, null, repoDir);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        int lineCount = 0;
        String line = reader.readLine();
        while (line != null) {
            lineCount++;
            assertEquals(line + " does not start with 'C'", 'C', line.charAt(0));
            line = reader.readLine();
        }
        assertEquals(0, process.waitFor());
        assertEquals(updatedFiles, lineCount);
    }

}
