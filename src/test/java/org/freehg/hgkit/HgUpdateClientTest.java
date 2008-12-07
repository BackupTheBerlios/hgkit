/**
 * Copyright 2008 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Util;
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
    public static void deleteCopy() throws IOException {
        FileUtils.deleteDirectory(repoDir);
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
        final String hgrc = Util.readFile(System.getProperty("user.home") + File.separator + ".hgrc");        
        final String command = hgrc.contains("\nhgext.color") ?  "hg status --all --no-color" : "hg status --all";
        final Process process = Runtime.getRuntime().exec(command, null, repoDir);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        int lineCount = 0;
        String line = reader.readLine();
        while (line != null) {
            lineCount++;
            final char firstChar = line.charAt(0);
            assertEquals("'" + line + "' does not start with 'C' but " + firstChar, 'C', firstChar);
            line = reader.readLine();
        }
        assertEquals(0, process.waitFor());
        assertEquals(updatedFiles, lineCount);
    }

}
