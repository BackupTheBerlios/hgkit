package org.freehg.hgkit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.freehg.hgkit.HgChangeLog.ChangeLog;
import org.freehg.hgkit.core.Repository;
import org.junit.Test;



public class HgStatusClientTest {



	@Test
	public void testStatusClient() throws Exception {
		long start = System.currentTimeMillis();

		Repository repo = new Repository("hg-stable");
		String cmd = "hg up -C";
		Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor();
		// Repository repo = new Repository("../com.vectrace.MercurialEclipse");
		HgStatusClient subject = new HgStatusClient(repo);

		List<FileStatus> status = subject.doStatus(repo.getRoot());
		long end = System.currentTimeMillis();
		for (FileStatus hgStatus : status) {
		     System.out.println(hgStatus);

        }

		System.out.println("Status walk took " + (end - start) + " ms");
	}
	@Test
	public void testStatusClientNasty() throws Exception {
		
		
		Repository repo = new Repository("hg-stable");
		HgChangeLog logClient = new HgChangeLog(repo);
		List<ChangeLog> log = logClient.getLog();
		// log = log.subList(3895, log.size());
		log = log.subList(5880, log.size());
		ChangeLog last = log.get(log.size() - 1);
		int count = 0;
		for (ChangeLog changeLog : log) {
			if(count++ % 100 == 0) {
				repo = new Repository("hg-stable");
				String cmd = "hg up -C -r " + changeLog.getChangeId().asShort();
				System.out.println(cmd);
				Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor();
				doStatus(repo);
			}
		}
		// Repository repo = new Repository("../com.vectrace.MercurialEclipse");
		
	}
	private void doStatus(Repository repo) {
		long start = System.currentTimeMillis();
		HgStatusClient subject = new HgStatusClient(repo);
		List<FileStatus> status = subject.doStatus(repo.getRoot());
		long end = System.currentTimeMillis();
		for (FileStatus hgStatus : status) {
			Assert.assertEquals(FileStatus.Status.MANAGED, hgStatus.getStatus());
		}
		System.out.println("Status walk took " + (end - start) + " ms (" + status.size() + ") files");
	}
}
