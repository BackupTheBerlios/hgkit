package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class CaseFoldingTest {

    private static final String TEST_STRING = "the_path is Here/HgStatus.java";

    @Test
    public void testFold() {
        String result = CaseFolding.fold(TEST_STRING);
        assertFalse(result.equals(TEST_STRING));
    }

    @Test
    public void testUnfold() {
        String result = CaseFolding.unfold(CaseFolding.fold(TEST_STRING));
        assertEquals(TEST_STRING, result);
        
    }

}
