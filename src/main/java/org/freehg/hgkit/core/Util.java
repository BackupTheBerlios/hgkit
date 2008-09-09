package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

final class Util {

	private static final char ZLIB_COMPRESSION = 'x';
	private static final char UNCOMPRESSED = 'u';

    static final int EOF = -1;

    static byte[] doDecompress(byte[] data) throws IOException {
	    ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream(1024);
	    // decompress the bytearray using what should be python zlib
	    final byte[] buffer = new byte[1024];
        final InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data));
        int len = inflaterInputStream.read(buffer);
        while (len != -1) {
            uncompressedOut.write(buffer, 0, len);
            len = inflaterInputStream.read(buffer);
        }
	    return uncompressedOut.toByteArray();
	}

	final static byte[] decompress(byte[] data) {
		try {
			if (data == null) {
				return null;
			}
			if (data.length < 1) {
				return new byte[0];
			}
	
			byte dataHeader = data[0];
			switch(dataHeader) {
			    case UNCOMPRESSED:
			    	final byte[] copy = new byte[data.length - 1];
			    	System.arraycopy(data, 1, copy, 0, data.length - 1);
			    	return copy;
			    case ZLIB_COMPRESSION:
			        return doDecompress(data);
			    case 0:
			    	return data;
			    default:
			        throw new RuntimeException("Unknown compression type : "
			                + (char) (dataHeader));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	static String forwardSlashes(String path) {
		return path.replace('\\', '/');
	}
	static byte[] readWholeFile(InputStream reader) throws IOException {
		byte[] buf = new byte[512];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(reader
				.available());
		int read = 0;
		while ((read = reader.read(buf)) != Util.EOF) {
			buffer.write(buf, 0, read);
		}
		return buffer.toByteArray();
	}
	
	public static void close(Closeable closable) {
		if(closable == null) {
			return;
		}
		try {
			closable.close();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
	}
}