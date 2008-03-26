package com.lich.hgkit.core;

import java.util.Arrays;

public final class NodeId {

	private static final int SHA_SIZE = 20;
	private static final int SHORT_SIZE = 6;
	private static final int SIZE = 32;
	private byte[] nodeid;

	private NodeId(byte[] data) {
		nodeid = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(nodeid);
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
			String hex = Integer.toHexString(Integer.valueOf(nodeid[i]));
			if( hex.length() < 2) {
				tos.append("0");
			} else {
				hex = hex.substring(0,2);
			}
			tos.append(hex);
		}
		return tos.toString();
	}

	@Override
	public String toString() {
		return asShort();
	}
}
