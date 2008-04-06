package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.jcraft.jzlib.ZInputStream;

class Util {

	static byte[] doDecompress(byte[] data) throws IOException {
	    // decompress the bytearray using what should be python zlib
	    ByteArrayInputStream datain = new ByteArrayInputStream(data);
	    ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream();
	    byte[] buff = new byte[512];
	    InputStream _dec = new ZInputStream(datain);
	
	    int read = 0;
	    while (-1 != (read = _dec.read(buff))) {
	        uncompressedOut.write(buff, 0, read);
	    }
	    return uncompressedOut.toByteArray();
	}

	static byte[] decompress(byte[] data) {
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
			    
			    case 'u':
			        byte[] uncompressed = new byte[data.length - 1];
			        ByteBuffer.wrap(data).get(uncompressed,1, uncompressed.length);
			        return uncompressed;
			    
			    case 'x':
			        return doDecompress(data);
			    
			    default:
			        throw new RuntimeException("Unknown compression type : "
			                + (char) (dataHeader));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	static final int EOF = -1;

}
