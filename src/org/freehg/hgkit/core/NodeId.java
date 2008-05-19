package org.freehg.hgkit.core;

import java.util.Arrays;

public final class NodeId {

//	private static final int SHA_SIZE = 20;
	private static final int SHORT_SIZE = 6;
	private static final int SIZE = 32;
	private byte[] nodeid;
	private int hash = -1;

	private NodeId(byte[] data) {
		nodeid = data;
	}

	@Override
	public int hashCode() {
		if( this.hash != -1) {
			return this.hash ;
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(nodeid);
		this.hash = result;
		return result;
	}

	public static NodeId valueOf(byte[] data) {
		if (data.length != SIZE) {
			throw new IllegalArgumentException("NodeId byte size must be "
					+ SIZE);
		}
		return new NodeId(data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NodeId other = (NodeId) obj;
		if (!Arrays.equals(nodeid, other.nodeid)) {
			return false;
		}
		return true;
	}

	public String asShort() {
		StringBuilder tos = new StringBuilder();
		for (int i = 0; i < SHORT_SIZE; i++) {
			int b = nodeid[i] & 0x0FF;
            String hex = Integer.toHexString(Integer.valueOf(b));
			if( hex.length() < 2) {
				tos.append("0");
			} else {
				hex = hex.substring(0,2);
			}
			tos.append(hex);
		}
		return tos.toString();
	}

	public String asFull() {
		StringBuilder tos = new StringBuilder();
		for (int i = 0; i < SIZE; i++) {
			int b = nodeid[i] & 0x0FF;
			String hex = Integer.toHexString(Integer.valueOf(b));

			hex = hex.substring(0,1);
			tos.append(hex);
		}
		return tos.toString();

	}

	@Override
	public String toString() {
		return asShort();
	}

	public static NodeId parse(String nodeId) {
		byte[] bytes = new byte[SIZE];
		for(int i = 0; i < SIZE; i++) {
			char charAt = nodeId.charAt(i);
			byte val = Integer.valueOf("" + charAt, 16).byteValue();
			bytes[i] = val;
		}
		NodeId result = valueOf(bytes);
		String asFull = result.asFull();
		if(! nodeId.equals(asFull)) {
			// FIXME Figure out when and why this happens!
		}
		return result;

	}
}
