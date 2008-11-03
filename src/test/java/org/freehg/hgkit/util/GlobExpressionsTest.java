package org.freehg.hgkit.util;

import org.junit.Test;

public class GlobExpressionsTest {

    @Test
    public void testGlobCompiler() {
        System.out.println(GlobExpressions.toRegex("*.*").pattern());
        System.out.println(GlobExpressions.toRegex("**/**").pattern());
        System.out.println(GlobExpressions.toRegex("He?lig").pattern());
    }
    
}
