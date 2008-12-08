package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.freehg.hgkit.core.DirState.DirStateEntry;
import org.junit.Test;

public class DirStateTest {

    @Test
    public void testDirState() throws Exception {
        DirState state = new DirState(new File(".hg/dirstate"));
        Collection<DirStateEntry> result = state.getDirState();
        for (DirStateEntry dirStateEntry : result) {
            assertEquals('n', dirStateEntry.getState());
            assertTrue("Size of '" + dirStateEntry + "' must be > -1, is " + dirStateEntry.getSize(), dirStateEntry.getSize() > -1);
        }
    }
}
