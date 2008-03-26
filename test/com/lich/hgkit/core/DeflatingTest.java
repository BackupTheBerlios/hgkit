package com.lich.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.jcraft.jzlib.ZInputStream;

public class DeflatingTest {

	@Test
	public void testDeflateContents() throws Exception {
		byte[] comp = getCompressedData();
		ByteArrayInputStream in = new ByteArrayInputStream(comp);
		print("RAW-COMPRESSED", in);

		System.out.println("--------------------------------------------");
		DeflaterInputStream stream = new DeflaterInputStream(in, new Deflater(
				Deflater.HUFFMAN_ONLY));
		print("DEFLATER", stream);

		print("JZlib", new ZInputStream(in));
		
		ZipInputStream zipin = new ZipInputStream(in);
		zipin.getNextEntry();
		print("ZipIn",  zipin);
		

	}

	private void print(InputStream stream) {
		print("", stream);
	}
	private void print(String name, InputStream stream) {
		try {
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("---------------------------------------");
			System.out.println("[" + name + "]");
			ByteArrayOutputStream def = new ByteArrayOutputStream();
			byte[] buf = new byte[512];
			int read = 0;
			while (0 < (read = stream.read(buf))) {
				def.write(buf, 0, read);
			}
			System.out.println(def.toString());
			System.out.println("---------------------------------------");
			System.out.println(def.size() + " uncomressed");
			System.out.println("---------------------------------------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] getCompressedData() throws FileNotFoundException,
			IOException {
		String name = "c:\\comp.gz";
		File f = new File(name);
		FileInputStream fis = new FileInputStream(name);

		byte[] comp = new byte[(int) f.length()];

		fis.read(comp);
		fis.close();
		return comp;
	}
}
