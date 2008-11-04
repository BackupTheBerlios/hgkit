/**
 * FileHelperTest.java 17.09.2008
 * 
 * Copyright (c) 2008 1 & 1 AG, Karlsruhe. All rights reserved.
 * 
 */

package org.freehg.hgkit.util;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;

/**
 * @author mirko
 *
 */
public class FileHelperTest {

    /**
     * Test method for
     * {@link org.freehg.hgkit.util.FileHelper#close(java.io.Closeable)}.
     */
    @Test
    public final void testClose() {
        FileHelper.close(null);
        FileHelper.close(FileHelper.class.getResourceAsStream("/passwd"));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.util.FileHelper#close(java.io.Closeable)}. Tests wether
     * our close really converts {@link IOException} to {@link RuntimeException}
     * .
     */
    @Test(expected = RuntimeException.class)
    public final void testCloseException() {
        FileHelper.close(new Closeable() {

            public void close() throws IOException {
                throw new IOException("Oops");
            }

        });
    }
}
