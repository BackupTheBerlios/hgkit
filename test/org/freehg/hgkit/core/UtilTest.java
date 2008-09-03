/**
 * UtilTest.java 03.09.2008
 * 
 * Copyright (c) 2008 1 & 1 AG, Karlsruhe. All rights reserved.
 * 
 */

package org.freehg.hgkit.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class UtilTest {

    /**
     * 
     */
    private static final byte[] ABC = new byte[] { 'a', 'b', 'c' };

    /**
     * 
     */
    private static final byte[] COMPRESSED_ABC = new byte[] { 'x', (byte) 0x9c,
            'K', 'L', 'J', 0x06, 0x00, 0x02, 'M', 0x01, '\'' };

    /**
     * Test method for {@link org.freehg.hgkit.core.Util#doDecompress(byte[])}.
     * 
     * @throws IOException
     */
    @Test
    public final void testDoDecompress() throws IOException {
        assertArrayEquals(ABC, Util.doDecompress(COMPRESSED_ABC));

    }

    /**
     * Test method for {@link org.freehg.hgkit.core.Util#decompress(byte[])}.
     * Tests the different paths of this method except the invalid case.
     */
    @Test
    public final void testDecompress() {
        final byte[] uncompressed = new byte[ABC.length + 1];
        assertArrayEquals(null, Util.decompress(null));
        assertArrayEquals(new byte[0], Util.decompress(new byte[0]));
        assertArrayEquals(ABC, Util.decompress(COMPRESSED_ABC));
        uncompressed[0] = 'u';
        System.arraycopy(ABC, 0, uncompressed, 1, ABC.length);
        assertArrayEquals(ABC, Util.decompress(uncompressed));
        uncompressed[0] = 0;
        assertEquals(uncompressed, Util.decompress(uncompressed));
    }
    
    /**
     * Test method for {@link org.freehg.hgkit.core.Util#decompress(byte[])}.
     * Tests decompressing a whole file.
     */
    @Test
    public final void testDecompressWholeFile() {
        final byte[] passwd;
        final byte[] compressed_passwd;
        try {
            passwd = Util.readWholeFile(UtilTest.class.getResourceAsStream("/passwd"));
            compressed_passwd = Util.readWholeFile(UtilTest.class.getResourceAsStream("/compressed_passwd"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertArrayEquals(passwd, Util.decompress(compressed_passwd));
    }
    /**
     * Test method for {@link org.freehg.hgkit.core.Util#decompress(byte[])}.
     */
    @Test(expected = RuntimeException.class)
    public final void testDecompressError() {
        Util.decompress(new byte[] { 1 });
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#forwardSlashes(java.lang.String)}.
     */
    @Test
    public final void testForwardSlashes() {
        assertEquals("/", Util.forwardSlashes("\\"));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#readWholeFile(java.io.InputStream)}.
     * 
     * @throws IOException
     */
    @Test
    public final void testReadWholeFile() throws IOException {
        final String prefix = "##\n# User Database";
        final String suffix = "_unknown:*:99:99:Unknown User:/var/empty:/usr/bin/false\n";
        InputStream in = UtilTest.class.getResourceAsStream("/passwd");
        String passwd = new String(Util.readWholeFile(in));
        assertTrue("Should start with '" + prefix + "'", passwd.startsWith(prefix));        
        assertTrue("Should end with '" + suffix + "'", passwd.endsWith(suffix));
        assertEquals(2888, passwd.length());
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#close(java.io.Closeable)}.
     */
    @Test
    public final void testClose() {
        Util.close(null);
        Util.close(UtilTest.class.getResourceAsStream("/passwd"));
    }
    
    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#close(java.io.Closeable)}.
     * @throws IOException 
     */
    @Test(expected=RuntimeException.class)
    public final void testCloseException() throws IOException {
        Util.close(
        new Closeable() {

            public void close() throws IOException {
                throw new IOException("Oops");                
            }
            
        });
    }

}
