/**
 * Copyright 2008 mirko
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * @author mirko
 * 
 */
public class RemoveMetaOutputStreamTest {

    private static final String DATA = "##\n# User Database\n# ";

    private static final String META_DATA = "\ncopyrev: f19cc7cb916200d98e4ef68d7715604da8a4a09f\ncopy: src/test/java/passwd\n\n";

    @Test
    public void testWithMetaData() throws IOException {
        byte[] input = (META_DATA + DATA).getBytes();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        for (byte b : input) {
            removeMetaOutputStream.write(b);
        }        
        assertEquals("", new String(out.toByteArray()));
    }
    
    @Test
    public void testWithoutMetaData() throws IOException {
        byte[] input = DATA.getBytes();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        RemoveMetaOutputStream removeMetaOutputStream = new RemoveMetaOutputStream(out);
        for (byte b : input) {
            removeMetaOutputStream.write(b);
        }
        assertEquals(DATA, new String(out.toByteArray()));
    }

}
