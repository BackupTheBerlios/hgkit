/**
 * Copyright 2009 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.freehg.hgkit.FileStatus;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class ChangeLogTest {

    private ChangeLog objUnderTest;

    @Before
    public void createRepo() {
        final Repository repository = new Repository(".");
        objUnderTest = new ChangeLog(repository, new File(".hg/store/00changelog.i"));
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.ChangeLog#get(int)}.
     */
    @Test
    public void testGetInt() {
        final ChangeSet changeSet = objUnderTest.get(0);
        assertThat(changeSet.getAuthor(), is("HemPc@PC212826566277"));
        assertThat(changeSet.getComment(), is("initial code"));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.ChangeLog#get(org.freehg.hgkit.core.NodeId)}
     * .
     * 
     * @TODO fix this.
     */
    @Test
    public void testGetNodeId() {
        final ChangeSet changeSet = objUnderTest.get(NodeId.valueOf("7c642352d00f15512c2db3d6d3e3154693bda5ec"));
        assertThat(changeSet.getAuthor(), is("HemPc@PC212826566277"));
        assertThat(changeSet.getComment(), is("Forgot to add licence"));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.ChangeLog#getFileStatus(org.freehg.hgkit.core.ChangeLog.ChangeSet)}
     * .
     */
    @Test
    public void testGetFileStatus() {
        List<FileStatus> fileStates = objUnderTest.getFileStatus(objUnderTest.get(0));
        assertThat(fileStates.size(), is(30));
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.ChangeLog#getLog()}.
     */
    @Test
    public void testGetLog() {
        final List<ChangeSet> log = objUnderTest.getLog();
        final ChangeSet changeSet = log.get(0);
        assertThat(changeSet.getAuthor(), is("HemPc@PC212826566277"));
        assertThat(changeSet.getComment(), is("initial code"));
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.ChangeLog.ChangeSet}.
     */
    @Test
    public void testChangeSet() {
        ChangeSet changeSet = objUnderTest.get(0);
        assertThat(changeSet.getAuthor(), is("HemPc@PC212826566277"));
        assertThat(changeSet.getChangeId().asShort(), is("c9629f6b37d8"));
        assertThat(changeSet.getComment(), is("initial code"));
        assertThat(changeSet.getFiles().size(), is(30));
        assertThat(changeSet.getManifestId(), is(NodeId.valueOf("7c0567b712859cb41aff6b57d6a0de357bc4d4a8")));
        assertThat(changeSet.getRevision(), is(0));
        assertThat(changeSet.getWhen(), is(new Date(1206558630000L)));
    }

}
