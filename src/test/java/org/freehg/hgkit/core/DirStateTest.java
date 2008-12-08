package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.freehg.hgkit.HgInternalError;
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
    
    @Test
    public void testDirStateFileNotFound() {
        try {
            new DirState(new File(".hg/dirstateDOESNOTEXIST"));
        } catch (HgInternalError e) {
            assertEquals(FileNotFoundException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void testDirIOException() {
        try {
            new DirState(new File(".hg/dirstate")) {
                /** {@inheritDoc} */
                @Override
                void parse(@SuppressWarnings("unused") DataInputStream in) throws IOException {
                    throw new IOException("Oops");
                }
            };
        } catch (HgInternalError e) {
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void testDirStateEntry() {
        DirStateEntry entry = new DirState.DirStateEntry((byte)'n', 020000, -1, -1, "a");
        assertEquals("n lnk         -1              unset a", entry.toString());
    }
}
