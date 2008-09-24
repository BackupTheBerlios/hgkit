/**
 * Copyright 2008 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import static org.junit.Assert.assertTrue;

import java.io.File;

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
     * Test method for {@link org.freehg.hgkit.HgUpdateClient#HgUpdateClient(org.freehg.hgkit.core.Repository)}.
     */
    @Test
    public final void testHgUpdateClient() {
        new HgUpdateClient(new Repository(repoDir));
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgUpdateClient#update()}.
     */
    @Test
    public final void testUpdate() {
        HgUpdateClient hgUpdateClient = new HgUpdateClient(new Repository(repoDir));
        hgUpdateClient.update();
    }

}
