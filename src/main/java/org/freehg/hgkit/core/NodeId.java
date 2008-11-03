package org.freehg.hgkit.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Describe a nodeId.
 */
public final class NodeId {

    private static final int HASHCODE_PRIME = 31;

    private static final int NODE_ID_LENGTH = 20;

    public static final int SHA_SIZE = 20;

    public static final int SHORT_SIZE = 6;

    private static final int SIZE = 32;

    private final byte[] nodeid;

    private int hash = -1;

    /**
     * Constructs a NodeId from data.
     * 
     * @param data
     *            data
     */
    private NodeId(byte[] data) {
        // if using 20 bytes node id, make it a 32 byte with trailing zeros
        if (data.length < SIZE) {
            nodeid = new byte[SIZE];
            System.arraycopy(data, 0, nodeid, 0, data.length);
        } else {
            nodeid = data;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if (hash != -1) {
            return hash;
        }
        // will replace the constant in the calculation,
        // but keep this declaration.
        hash = HASHCODE_PRIME + Arrays.hashCode(nodeid);
        return hash;
    }

    /**
     * Factory method for NodeIds. data.length must be either
     * {@link NodeId#SHA_SIZE} or {@link NodeId#SIZE}.
     * 
     * @param data
     *            nodeId data
     * @return a new NodeId
     */
    public static NodeId valueOf(byte[] data) {
        switch (data.length) {
        case SIZE:
        case SHA_SIZE:
            return new NodeId(data);
        default:
            throw new IllegalArgumentException("NodeId byte size must be either " + SHA_SIZE + " or " + SIZE
                    + " but was " + data.length);
        }
    }

    /** {@inheritDoc} */
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

    /**
     * Returns the short string representation of the nodeId.
     * 
     * @return nodeId
     */
    public String asShort() {
        return toString(SHORT_SIZE);
    }

    /**
     * Returns the complete SHA string representation of the nodeId.
     * 
     * @return nodeId
     */
    public String asFull() {
        return toString(SHA_SIZE);

    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return asShort();
    }

    /**
     * Returns a part of the nodeId as Hex-String.
     * 
     * @param length
     *            how long should the nodeId be
     * @return nodeId
     */
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

    /**
     * Returns the byte representation of a Hex-String.
     * 
     * @param hexStr
     *            to convert.
     * @return byte array
     */
    public static byte[] toBinArray(String hexStr) {
        byte bArray[] = new byte[hexStr.length() / 2];
        for (int i = 0; i < (hexStr.length() / 2); i++) {
            byte firstNibble = Byte.parseByte(hexStr.substring(2 * i, 2 * i + 1), 16); // [
            // x
            // ,
            // y
            // )
            byte secondNibble = Byte.parseByte(hexStr.substring(2 * i + 1, 2 * i + 2), 16);
            int finalByte = (secondNibble) | (firstNibble << 4); // bit-operations
            // only with
            // numbers, not
            // bytes.
            bArray[i] = (byte) finalByte;
        }
        return bArray;
    }

    /**
     * Creates a new NodeId from the given Hex-String.
     * 
     * @param nodeId
     *            as Hex-String
     * @return nodeId
     */
    public static NodeId parse(String nodeId) {
        final NodeId result = valueOf(toBinArray(nodeId));
        final String asFull = result.asFull();
        if (!nodeId.equals(asFull)) {
            System.err.println(asFull + " != " + nodeId);
            // FIXME Figure out when and why this happens!
        }
        return result;

    }

    /**
     * Creates a new NodeId by reading the first {@link NodeId#NODE_ID_LENGTH}
     * bytes from the given {@link InputStream}.
     * 
     * @param in
     *            inputStream
     * @return nodeId
     * @throws IOException
     *             when reading from <code>in</code> does not succeed.
     */
    public static NodeId read(InputStream in) throws IOException {
        byte[] data = new byte[NODE_ID_LENGTH];
        int read = in.read(data);
        assert read == NODE_ID_LENGTH;
        return NodeId.valueOf(data);
    }
}
