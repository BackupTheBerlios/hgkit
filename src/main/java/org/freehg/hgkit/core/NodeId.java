package org.freehg.hgkit.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class NodeId {

    public static final int SHA_SIZE = 20;

    public static final int SHORT_SIZE = 6;

    private static final int SIZE = 32;

    private byte[] nodeid;

    private int hash = -1;

    private NodeId(byte[] data) {
        // if using 20 bytes node id, make it a 32 byte with trailing zeros
        if (data.length < SIZE) {
            this.nodeid = new byte[32];
            System.arraycopy(data, 0, this.nodeid, 0, data.length);
        } else {
            nodeid = data;
        }
    }

    @Override
    public int hashCode() {
        if (this.hash != -1) {
            return this.hash;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(nodeid);
        this.hash = result;
        return result;
    }

    public static NodeId valueOf(byte[] data) {
        if (!(data.length == SIZE || data.length == SHA_SIZE)) {
            throw new IllegalArgumentException("NodeId byte size must be either " + SHA_SIZE + " or " + SIZE
                    + " but was " + data.length);
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
        return toString(SHORT_SIZE);
    }

    public String asFull() {
        return toString(SHA_SIZE);

    }

    @Override
    public String toString() {
        return asShort();
    }

    public String toString(int length) {
        StringBuilder tos = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int b = nodeid[i] & 0x0FF;
            String hex = Integer.toHexString(Integer.valueOf(b));
            if (hex.length() < 2) {
                tos.append("0");
            } else {
                hex = hex.substring(0, 2);
            }
            tos.append(hex);
        }
        return tos.toString();
    }

    public static byte[] toBinArray(String hexStr) {
        byte bArray[] = new byte[hexStr.length() / 2];
        for (int i = 0; i < (hexStr.length() / 2); i++) {
            byte firstNibble = Byte.parseByte(hexStr.substring(2 * i, 2 * i + 1), 16); // [
                                                                                       // x
                                                                                       // ,
                                                                                       // y
                                                                                       // )
            byte secondNibble = Byte.parseByte(hexStr.substring(2 * i + 1, 2 * i + 2), 16);
            int finalByte = (secondNibble) | (firstNibble << 4); //bit-operations
                                                                 // only with
                                                                 // numbers, not
                                                                 // bytes.
            bArray[i] = (byte) finalByte;
        }
        return bArray;
    }

    public static NodeId parse(String nodeId) {

        byte[] bytes = toBinArray(nodeId);
        NodeId result = valueOf(bytes);
        String asFull = result.asFull();
        if (!nodeId.equals(asFull)) {
            System.err.println(asFull + " != " + nodeId);
            // FIXME Figure out when and why this happens!
        }
        return result;

    }

    public static NodeId read(InputStream in) throws IOException {
        byte[] data = new byte[20];
        in.read(data);
        return NodeId.valueOf(data);
    }
}
