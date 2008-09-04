/**
 * TestHelper.java 04.09.2008
 * 
 */

package org.freehg.hgkit;

import java.io.File;
import java.io.IOException;

import org.freehg.hgkit.util.FileHelper;

/**
 * Defines some static helper-methods for tests.
 * 
 * @author mirko
 */
public class TestHelper {
    /**
     * Functions only.
     */
    private TestHelper() {
        // Functions only.
    }

    /**
     * @return
     */
    public static File createRepoCopy() {
        File sourceDirectory = new File(".hg");
        final File targetDir = new File(System.getProperty("java.io.tmpdir"), "hgkit-test");        
        try {
            FileHelper.copyDirectory(sourceDirectory, new File(targetDir.getAbsolutePath(), ".hg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetDir;
    }
}
