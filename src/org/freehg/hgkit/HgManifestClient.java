package org.freehg.hgkit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.freehg.hgkit.HgChangeLog.ChangeLog;
import org.freehg.hgkit.HgManifest.ManifestEntry;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;


public class HgManifestClient {

	private final Repository repo;

	public HgManifestClient(Repository repo) {
		this.repo = repo;
		
	}
	
	public HgManifest getManifest(ChangeLog logEntry) {
		Revlog revlog = repo.getManifest();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		revlog.revision(logEntry.nodeId, out);
		return new HgManifest(logEntry.getChangeId(), parse(out.toString()));
		
	}
	
	public HgManifest getManifest(NodeId revision) {
		Revlog manifest = repo.getManifest();
		String text = new String(manifest.revision(revision));
		List<ManifestEntry> entries = parse(text);
		HgManifest manifestInstance = new HgManifest(revision, entries);
		return manifestInstance;
	}
	
	public void stuff() {
		Revlog manifest = repo.getManifest();
		for(NodeId nodeId : manifest.getRevisions()) {
			System.out.println(nodeId.asFull());
			System.out.println("-----------------------");
			String text = new String(manifest.revision(nodeId));
			List<ManifestEntry> entries = parse(text);
			HgManifest manifestInstance = new HgManifest(nodeId, entries);
			System.out.println(manifestInstance);
			System.out.println(text);
			System.out.println("-----------------------");
		};
	}

	private List<ManifestEntry> parse(String text) {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		List<ManifestEntry> entries = new ArrayList<ManifestEntry>();
		try {
			String line = null;
			while(null != (line = reader.readLine())) {
				int cutAt = line.indexOf(0);
				String name = line.substring(0, cutAt);
				String node = line.substring(cutAt + 1, cutAt + 1 + NodeId.SHA_SIZE * 2);
				ManifestEntry entry = new HgManifest.ManifestEntry(NodeId.parse(node), name);
				entries.add(entry);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return entries;
	}
}
