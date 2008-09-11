package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.freehg.hgkit.HgInternalError;

final class Util {

    private static final int ASSUMED_COMPRESSION_RATIO = 3;

    private static final char ZLIB_COMPRESSION = 'x';

    private static final char UNCOMPRESSED = 'u';

    static final int EOF = -1;

    /**
     * Decompresses zlib-compressed data.
     * 
     * @param data
     * @return decompressed data
     * @throws IOException
     */
    static byte[] doDecompress(byte[] data) throws IOException {
        ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream(data.length * ASSUMED_COMPRESSION_RATIO);
        // decompress the bytearray using what should be python zlib
        final byte[] buffer = new byte[1024];
        final InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data));
        int len = 0;
        while ((len = inflaterInputStream.read(buffer)) != EOF) {
            uncompressedOut.write(buffer, 0, len);
        }
        return uncompressedOut.toByteArray();
    }

    /**
     * Eventually decompresses the given data.
     * 
     * @param data
     * @return byte-array with decompressed data.
     */
    final static byte[] decompress(byte[] data) {
        try {
            if (data.length < 1) {
                return new byte[0];
            }
            byte dataHeader = data[0];
            switch (dataHeader) {
            case UNCOMPRESSED:
                final byte[] copy = new byte[data.length - 1];
                System.arraycopy(data, 1, copy, 0, data.length - 1);
                return copy;
            case ZLIB_COMPRESSION:
                return doDecompress(data);
            case 0:
                return data;
            default:
                throw new HgInternalError("Unknown compression type : " + (char) (dataHeader));
            }
        } catch (IOException e) {
            throw new HgInternalError(e);
        }
    }

    /**
     * Replace every backslash with a forward slash
     * 
     * @param path
     * @return corrected path.
     */
    static String forwardSlashes(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Reads an {@link InputStream} until no more data is available and returns
     * it as byte-array. read-Operations on <code>in</code> are not buffered.
     * 
     * @param in
     * @return the content of <code>in</code> as byte-array.
     * @throws IOException
     */
    static byte[] toByteArray(InputStream in) throws IOException {
        byte[] buf = new byte[512];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(in.available());
        int read = 0;
        while ((read = in.read(buf)) != Util.EOF) {
            buffer.write(buf, 0, read);
        }
        return buffer.toByteArray();
    }

    /**
     * Reads resource into byte array and closes it immediately.
     * 
     * @param name
     *            resource name.
     * @return
     * @throws IOException
     */
    static byte[] readResource(final String name) {
        final byte[] resourceBytes;
        InputStream in = UtilTest.class.getResourceAsStream(name);
        try {
            try {
                resourceBytes = toByteArray(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resourceBytes;
    }
}
