package org.freehg.hgkit;

import org.freehg.hgkit.core.Repository;
import org.junit.Test;


public class HgManifestClientTest {

	@Test
	public void testStuff() {
		Repository repo = new Repository(".");
		HgManifestClient subject = new HgManifestClient(repo);
		subject.stuff();
	}

}
