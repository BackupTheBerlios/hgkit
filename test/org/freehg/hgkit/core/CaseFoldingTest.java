package org.freehg.hgkit.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class CaseFoldingTest {

    private final String unfolded;
    private final String folded;

    public CaseFoldingTest(final String unfolded, final String folded) {
        this.unfolded = unfolded;
        this.folded = folded;
    }
    
    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(        
                new String[] {":\\", "~3a~5c"},
                new String[] {"the_path is Here/HgStatus.java", "the__path~20is~20_here/_hg_status.java"},
                new String[] {"C:\\WindowS\\foo.*", "_c~3a~5c_window_s~5cfoo.~2a"}
                );
    }
    
    /**
     * Testmethod for {@link CaseFolding#fold(String)}.
     */
    @Test
    public void testFold() {
        String result = CaseFolding.fold(unfolded);        
        assertEquals(folded, result);        
    }

    /**
     * Tests wether {@link CaseFolding#fold(String)} and {@link CaseFolding#unfold(String)} are symmetric.
     */
    @Test
    public void testSymmetry() {
        String result = CaseFolding.unfold(CaseFolding.fold(unfolded));
        assertEquals(unfolded, result);        
    }

    @Test(expected=AssertionError.class)
    public void testErrorInUnfold() {
        CaseFolding.unfold(folded + "A");
    }
}
