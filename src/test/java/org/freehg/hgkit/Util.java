/**
 * Util.java 04.09.2008
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
public class Util {
    /**
     * Functions only.
     */
    private Util() {
        // Functions only.
    }

    /**
     * This will copy the repo to target trying to delete an existing copy
     * beforehand. (Will not work safely on Windows to it's stupid locking
     * semantics :-().
     * 
     * @return a file pointing to a copy of hgkit.
     */
    public static File createRepoCopy() {
        File sourceDirectory = new File(".hg");        
        final File targetDir = new File("target", "hgkit-test");
        FileHelper.deleteDirectory(targetDir);
        try {
            FileHelper.copyDirectory(sourceDirectory, new File(targetDir.getAbsolutePath(), ".hg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetDir;
    }
}
