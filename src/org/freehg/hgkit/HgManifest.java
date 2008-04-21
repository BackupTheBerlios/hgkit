package org.freehg.hgkit;

import java.util.ArrayList;
import java.util.List;

import org.freehg.hgkit.core.NodeId;

public class HgManifest {

	private NodeId stateId;
	private List<ManifestEntry> state = new ArrayList<ManifestEntry>();

	HgManifest(NodeId stateId, List<ManifestEntry> state) {
		this.stateId = stateId;
		this.state = state;
	}

	public static final class ManifestEntry {
		NodeId nodeId;
		String filename;
	
		public ManifestEntry(NodeId nodeId, String filename) {
			super();
			this.nodeId = nodeId;
			this.filename = filename;
		}
	}
}
