package org.freehg.hgkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freehg.hgkit.core.NodeId;

public class HgManifest {

	private NodeId stateId;
	private Map<String, NodeId> state = new HashMap<String,NodeId>();

	HgManifest(NodeId stateId, List<ManifestEntry> state) {
		
		this.stateId = stateId;
		for (ManifestEntry manifestEntry : state) {
			this.state.put(manifestEntry.filename, manifestEntry.nodeId);
		}
	}
	
	public NodeId getState(String filename) {
		return state.get(filename);
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
