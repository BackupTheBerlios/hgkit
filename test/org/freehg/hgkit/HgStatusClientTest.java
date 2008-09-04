package org.freehg.hgkit;

import java.util.List;

import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.junit.Ignore;
import org.junit.Test;



public class HgStatusClientTest {



    private static final String TEST_REPO = System.getProperty("hgkit.test.repo", "hg-stable");

    @Test
	public void testStatusClient() throws Exception {

		Repository repo = new Repository(TEST_REPO);
		String cmd = "/Users/mirko/bin/hg up -C";
		Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor();
		// Repository repo = new Repository("../com.vectrace.MercurialEclipse");
		long start = System.currentTimeMillis();
		HgStatusClient subject = new HgStatusClient(repo);

		List<FileStatus> status = subject.doStatus(repo.getRoot());
		long end = System.currentTimeMillis();
		for (FileStatus hgStatus : status) {
		     System.out.println(hgStatus);

        }

		System.out.println("Status walk took " + (end - start) + " ms");
	}
	@Ignore
	@Test
	public void testStatusClientNasty() throws Exception {
		
		
		Repository repo = new Repository(TEST_REPO);
		List<ChangeSet> log = repo.getChangeLog().getLog();
		int count = 0;
		for (ChangeSet changeSet : log) {
			if(count++ % 100 == 0) {
				repo = new Repository(TEST_REPO);
				String cmd = "hg up -C -r " + changeSet.getChangeId().asShort();
				System.out.println(cmd);
				Runtime.getRuntime().exec(cmd, null, repo.getRoot()).waitFor();
				doStatus(repo);
			}
		}
	}

	private void doStatus(Repository repo) {
		long start = System.currentTimeMillis();
		HgStatusClient subject = new HgStatusClient(repo);
		List<FileStatus> status = subject.doStatus(repo.getRoot());
		long end = System.currentTimeMillis();
		for (FileStatus hgStatus : status) {
			// Assert.assertEquals(FileStatus.Status.MANAGED, hgStatus.getStatus());
			if(FileStatus.Status.MANAGED != hgStatus.getStatus()) {
				System.err.println(hgStatus + " should have been managed");
			}
		}
		System.out.println("Status walk took " + (end - start) + " ms (" + status.size() + ") files");
	}
}
