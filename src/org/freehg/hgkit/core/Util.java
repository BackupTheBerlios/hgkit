package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.jcraft.jzlib.ZInputStream;

final class Util {

	private static final int BUFF_SIZE = 1024;
    private static final char ZLIB_COMPRESSION = 'x';
    private static final char UNCOMPRESSED = 'u';
    static final int EOF = -1;

    static byte[] doDecompress(byte[] data) throws IOException {
	    // decompress the bytearray using what should be python zlib
	    ByteArrayInputStream datain = new ByteArrayInputStream(data);
	    ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream();
	    final byte[] buff = new byte[BUFF_SIZE];

	    InputStream _dec = new ZInputStream(datain);
	    int read = 0;
	    while (EOF != (read = _dec.read(buff))) {
	        uncompressedOut.write(buff, 0, read);
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
			    case 0:
			        return data;
			    case UNCOMPRESSED:
			    	byte[] copy = new byte[data.length - 1];
			    	System.arraycopy(data, 1, copy, 0, data.length - 1);
			    	return copy;
			    case ZLIB_COMPRESSION:
			        return doDecompress(data);
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
	static byte[] readWholeFile(DataInputStream reader) throws IOException {
		byte[] buf = new byte[512];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(reader
				.available());
		int read = 0;
		while ((read = reader.read(buf)) != Util.EOF) {
			buffer.write(buf, 0, read);
		}
		return buffer.toByteArray();
	}
}
