/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * Tests for {@link RemoveMetaOutputStream}.
 */
public class RemoveMetaOutputStreamTest {

    private final static byte[] CONTENT_WITH_METADATA_BYTES = new byte[] { 1, 10, 99, 111, 112, 121, 114, 101, 118, 58,
            32, 53, 99, 98, 52, 53, 98, 53, 100, 48, 97, 99, 54, 99, 100, 49, 55, 101, 98, 49, 49, 101, 50, 53, 99,
            101, 101, 100, 48, 100, 50, 56, 98, 54, 98, 48, 57, 53, 97, 55, 50, 10, 99, 111, 112, 121, 58, 32, 115,
            114, 99, 47, 116, 101, 115, 116, 47, 114, 101, 115, 111, 117, 114, 99, 101, 115, 47, 117, 110, 109, 111,
            118, 101, 100, 45, 102, 105, 108, 101, 10, 1, 10, 116, 104, 105, 115, 32, 102, 105, 108, 101, 32, 105, 115,
            32, 109, 111, 118, 101, 100, 32, 97, 110, 100, 32, 104, 97, 115, 32, 77, 101, 116, 97, 100, 97, 116, 97, 10 };

    private final static String CONTENT = "this file is moved and has Metadata\n";

    private final static byte[] CONTENT_WITH_METADATA_BYTES_BUT_WITHOUT_METADATA = new byte[] { 1, 116, 104, 105, 115,
            32, 102, 105, 108, 101, 32, 105, 115, 32, 109, 111, 118, 101, 100, 32, 97, 110, 100, 32, 104, 97, 115, 32,
            77, 101, 116, 97, 100, 97, 116, 97, 10 };

    /**
     * Tests wether
     * {@link RemoveMetaOutputStreamTest#CONTENT_WITH_METADATA_BYTES} really
     * ends with {@link RemoveMetaOutputStreamTest#CONTENT}.
     */
    @Test
    public void testTestdata() {
        final int contentLength = CONTENT.getBytes().length;
        final byte[] contentBytes = new byte[contentLength];
        System.arraycopy(CONTENT_WITH_METADATA_BYTES, CONTENT_WITH_METADATA_BYTES.length - contentLength, contentBytes,
                0, contentLength);
        assertEquals(CONTENT, new String(contentBytes));
    }

    /**
     * Tests removal of metadata from
     * {@link RemoveMetaOutputStreamTest#CONTENT_WITH_METADATA_BYTES}.
     * 
     * @throws IOException
     *             should not happen.
     */
    @Test
    public void testWithMeta() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        removeMetaOutputStream.write(CONTENT_WITH_METADATA_BYTES);
        assertEquals(CONTENT, out.toString());
    }

    /**
     * Tests wether {@link RemoveMetaOutputStreamTest#CONTENT} is unaltered by
     * metadata-removal.
     * 
     * @throws IOException
     *             should not happen.
     */
    @Test
    public void testWithoutMeta() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        removeMetaOutputStream.write(CONTENT.getBytes());
        assertEquals(CONTENT, out.toString());
    }

    /**
     * Tests wether
     * {@link RemoveMetaOutputStreamTest#CONTENT_WITH_METADATA_BYTES_BUT_WITHOUT_METADATA}
     * is unaltered by metadata-removal.
     * 
     * @throws IOException
     *             should not happen.
     */
    @Test
    public void testContentWithMetaBytes() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        removeMetaOutputStream.write(CONTENT_WITH_METADATA_BYTES_BUT_WITHOUT_METADATA);
        assertEquals(new String(CONTENT_WITH_METADATA_BYTES_BUT_WITHOUT_METADATA), out.toString());
    }
}
