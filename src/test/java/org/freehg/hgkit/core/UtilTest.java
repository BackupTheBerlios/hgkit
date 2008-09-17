/**
 * UtilTest.java 03.09.2008
 *  
 * 
 */

package org.freehg.hgkit.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;

import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.util.FileHelper;
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
    private static final byte[] COMPRESSED_ABC = new byte[] { 'x', (byte) 0x9c, 'K', 'L', 'J', 0x06, 0x00, 0x02, 'M',
            0x01, '\'' };

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
        final byte[] uncompressed_passwd;
        final byte[] compressed_passwd;
        uncompressed_passwd = Util.readResource("/passwd");
        compressed_passwd = Util.readResource("/compressed_passwd");
        assertEquals(new String(uncompressed_passwd), new String(Util.decompress(compressed_passwd)));
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.Util#decompress(byte[])}.
     * Tests invalid data.
     */
    @Test(expected = HgInternalError.class)
    public final void testDecompressError() {
        Util.decompress(new byte[] { 1 });
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#forwardSlashes(java.lang.String)}.
     * 
     * Simple tests for simple function :-).
     */
    @Test
    public final void testForwardSlashes() {
        assertEquals("/", Util.forwardSlashes("\\"));
        assertEquals("/me/myself/and/i//", Util.forwardSlashes("\\me\\myself\\and\\i\\\\"));
    }

    /**
     * Test method for
     * {@link org.freehg.hgkit.core.Util#toByteArray(java.io.InputStream)}.
     * 
     */
    @Test
    public final void testReadWholeFile() {
        final String prefix = "##\n# User Database";
        final String suffix = "_unknown:*:99:99:Unknown User:/var/empty:/usr/bin/false\n";
        String uncompressed_passwd = new String(Util.readResource("/passwd"));
        assertTrue("Should start with '" + prefix + "'", uncompressed_passwd.startsWith(prefix));
        assertTrue("Should end with '" + suffix + "'", uncompressed_passwd.endsWith(suffix));
        assertEquals(2888, uncompressed_passwd.length());
    }
}
