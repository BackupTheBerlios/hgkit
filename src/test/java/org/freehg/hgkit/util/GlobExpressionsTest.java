package org.freehg.hgkit.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GlobExpressionsTest {

    private final String expected;

    private final String glob;

    public GlobExpressionsTest(final String expected, final String glob) {
        this.expected = expected;
        this.glob = glob;

    }

    @Parameters
    public static Collection<String[]> data() {
        ArrayList<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { ".*\\..*", "*.*" });
        data.add(new String[] { ".*.*\\/.*.*", "**/**" });
        data.add(new String[] { "He.lig", "He?lig" });
        data.add(new String[] { "[*]", "[*]" });
        data.add(new String[] { "[?]", "[?]" });
        return data;
    }

    @Test
    public void testGlobCompiler() {
        assertEquals(expected, GlobExpressions.toRegex(glob).pattern());
    }
}
