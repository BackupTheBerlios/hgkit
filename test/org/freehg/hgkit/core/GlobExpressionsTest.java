package org.freehg.hgkit.core;

import junit.framework.TestCase;

public class GlobExpressionsTest extends TestCase {

    public void testGlobCompiler() {
        System.out.println(GlobExpressions.toRegex("*.*").pattern());
        System.out.println(GlobExpressions.toRegex("**/**").pattern());
        System.out.println(GlobExpressions.toRegex("He?lig").pattern());
    }
}
