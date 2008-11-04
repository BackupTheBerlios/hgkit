/**
 * FileHelper.java 04.09.2008
 * 
 */

package org.freehg.hgkit.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.freehg.hgkit.HgInternalError;

/**
 * Some static methods for file-handling, directory creation etc.
 * 
 * @author mirko
 * 
 */
public class FileHelper {

    /**
     * Class with static helpers.
     */
    FileHelper() {
        // class with static helpers.
    }

    /**
     * Delete directory recursively.
     * 
     * @param path
     * @return false if directory could not be deleted.
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        System.err.println("Could not delete " + file.getAbsolutePath());
                    }
                }
            }
        }
        return path.delete();
    }

    /**
     * Copy sourceLocation to targetLocation.
     * 
     * @param sourceLocation
     * @param targetLocation
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            File[] children = sourceLocation.listFiles();
            for (File child : children) {
                copyDirectory(child.getAbsoluteFile(), new File(targetLocation.getAbsolutePath(), child.getName()));
            }
        } else {

            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(sourceLocation);
                out = new FileOutputStream(targetLocation);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                close(in);
                close(out);
            }
        }
    }

    /**
     * Close even null safely.
     * 
     * @param closable
     * 
     * @throws HgInternalError
     *             if there is an {@link IOException}.
     */
    public static void close(Closeable closable) throws HgInternalError {
        if (closable == null) {
            return;
        }
        try {
            closable.close();
        } catch (IOException e) {
            throw new HgInternalError("Error closing" + closable, e);
        }
    }

}
