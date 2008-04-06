package org.freehg.hgkit.core;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MDiffTest {

	byte[] newPatch(int start, int len, String patch) {
		try {
			
			byte[] data = patch.getBytes();
			int patchLength = 12 + data.length;

			ByteArrayOutputStream bytes = new ByteArrayOutputStream(patchLength);
			DataOutputStream out = new DataOutputStream(bytes);
			// a patch is start, end, length, data
			out.writeInt(start);
			out.writeInt(start + len);
			out.writeInt(data.length);
			out.write(data);
			return bytes.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void testSimple() {
		String text = "12345xxxx";
		byte[] patch = newPatch(5, 4, "6789");
		byte[] result = MDiff.patches(text.getBytes(), patch);
		String resultString = new String(result);
		
		assertEquals("123456789", resultString);
	}
	@Test
	public void testTwo() {
		String text = "12345xxxx";
		List<byte[]> list = new ArrayList<byte[]>();
		
		list.add(newPatch(5, 2, "67"));
		list.add(newPatch(7, 2, "89"));
		
		
		byte[] result = MDiff.patches(text.getBytes(), list);
		String resultString = new String(result);
		
		assertEquals("123456789", resultString);
	}
}
