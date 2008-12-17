/**
 * Copyright 2008 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.core.Repository;
import org.junit.After;
import org.junit.Test;

/**
 * @author mirko
 *
 */
public class HgInitClientTest {

    private final static File TEST_REPO = new File("target/testrepo");
    
    /**
     * @throws IOException 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(TEST_REPO);
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgInitClient#HgInitClient(java.io.File)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testHgInitClientRootDirAlreadyExists() {
        new File(TEST_REPO, Repository.HG).mkdirs();
        new HgInitClient(TEST_REPO);
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgInitClient#createInitialDirectories()}.
     */
    @Test(expected=HgInternalError.class)
    public final void testCreateInitialDirectoriesStoreAlreadyExists() {
        HgInitClient client = new HgInitClient(TEST_REPO);
        new File(TEST_REPO, Repository.HG + "/" + Repository.STORE).mkdirs();
        client.createInitialDirectories();
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgInitClient#writeInitialFiles()}.
     */
    @Test(expected=HgInternalError.class)
    public final void testWriteInitialFilesChangelogIOException() {
        HgInitClient client = new HgInitClient(TEST_REPO);
        new File(TEST_REPO, Repository.HG + "/" + Repository.CHANGELOG_INDEX).mkdirs();        
        client.writeInitialFiles();
    }

    /**
     * Test method for {@link org.freehg.hgkit.HgInitClient#writeInitialFiles()}.
     */
    @Test(expected=HgInternalError.class)
    public final void testWriteInitialFilesRequiresIOException() {
        HgInitClient client = new HgInitClient(TEST_REPO);
        new File(TEST_REPO, Repository.HG + "/" + Repository.REQUIRES).mkdirs();        
        client.writeInitialFiles();
    }
    
    /**
     * Test method for {@link org.freehg.hgkit.HgInitClient#create(java.io.File)}.
     */
    @Test
    public final void testCreate() {
        HgInitClient.create(TEST_REPO);
    }

}
