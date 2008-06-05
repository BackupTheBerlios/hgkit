package org.freehg.hgkit.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

public class LongRunningTest {

	private int numRevisions;

	@Test
	public void testAll() throws Exception {
		Repository subject = getSubject();
		int count = walk(subject,new File("hg-stable"));
		System.out.println(count + " num files tested and " + numRevisions + " revivions");
	}
	private Repository getSubject() {
		return new Repository("hg-stable");
	}
	private int walk(Repository repo, File dir) throws IOException {
		String abs = dir.getAbsolutePath();
		
		if(dir.getName().contains(".hg")) 
		{
			return 0;
		}
	    int count = 0;
		for(File file : dir.listFiles()) {
			if( file.isFile()) {
				testFile(repo, file);
				count++;
			}
		}
		for(File file : dir.listFiles()) {
			if(file.isDirectory() 
					&& !file.equals(dir.getParent())
					&& !file.equals(dir)
					&& !file.getAbsolutePath().endsWith(".hg")) {
				count += walk(repo,file);
			}
		}
		return count;
	}
	
	private void testFile(Repository repo, final File file) throws IOException {
		final File index = repo.getIndex(file);
		Revlog revlog = new Revlog(index);
		final FileInputStream stream = new FileInputStream(file);
		revlog.revision(revlog.tip().nodeId, new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					int fromFile = stream.read() & 0xFF;
					b = b & 0xFF;
					if((fromFile != b)) {
						throw new IllegalStateException("Tip of file: " + file + " did not match HgKit revision : " + b + " != " + fromFile);
					}
				}
			});
		this.numRevisions++;
		stream.close();
	}
}
