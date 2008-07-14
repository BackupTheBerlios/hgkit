package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Manifest extends Revlog {

	public Manifest(File index) {
		super(index);
	}
	
	public Map<String, NodeId> get(ChangeLog.Entry changelog) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		super.revision(changelog.getManifestId(), out);
		String text = out.toString();
		return parse(text);
	}
	
	private Map<String, NodeId> parse(String text) {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		Map<String, NodeId> nodefilemap = new HashMap<String, NodeId>();
		try {
			String line = null;
			while(null != (line = reader.readLine())) {
				int cutAt = line.indexOf(0);
				String name = line.substring(0, cutAt);
				String nodeStr = line.substring(cutAt + 1, cutAt + 1 + NodeId.SHA_SIZE * 2);
				NodeId node = NodeId.parse(nodeStr);
				nodefilemap.put(name, node);
				
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return nodefilemap;
	}
}
