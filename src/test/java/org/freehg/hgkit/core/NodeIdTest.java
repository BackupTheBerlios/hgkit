/**
 * Copyright 2009 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Mirko Friedenhagen
 *
 */
public class NodeIdTest {

    /**
     * Test method for {@link org.freehg.hgkit.core.NodeId#valueOf(byte[])}.
     * This tests a corner case with an invalid hashcode.
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testValueOfByteArrayWithInvalidLength() {
        NodeId.valueOf("abc".getBytes());
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.NodeId#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        final NodeId nodeId1 = NodeId.valueOf("7c642352d00f15512c2db3d6d3e3154693bda5ec");
        final NodeId nodeId2 = nodeId1;
        assertThat(nodeId1, is(nodeId2));
        assertThat(nodeId1.equals("abc"), is(false));
        assertThat(nodeId1, is(NodeId.valueOf("7c642352d00f15512c2db3d6d3e3154693bda5ec")));
        assertThat(nodeId1, not(NodeId.valueOf("7c642352d00f15512c2db3d6d3e3154693bda5ed")));
    }

    /**
     * Test method for {@link org.freehg.hgkit.core.NodeId#valueOf(java.lang.String)}.
     * This tests a corner case with an invalid hashcode.
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testValueOfStringWithInvalidLength() {
        NodeId.valueOf("abc");
    }

}
