/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/**
 * @author mirko
 */
public class FileStatusTest {

    /**
     * Test method for {@link org.freehg.hgkit.FileStatus#toString()}.
     */
    @Test
    public final void testToString() {
        assertEquals("A /", FileStatus.valueOf(new File("/"), FileStatus.Status.ADDED).toString());
    }

}
