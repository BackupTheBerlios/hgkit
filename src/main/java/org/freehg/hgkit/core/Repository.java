package org.freehg.hgkit.core;

import java.io.File;
import java.io.IOException;

public class Repository {
    
    public static final String HG = ".hg/";

    private static final String STORE = HG + "store/";

    private static final String DATA = STORE + "data/";

    private static final String INDEX_SUFFIX = ".i";

    private static final String DIRSTATE = "dirstate";

    private final File root;

    public Repository(File root) {
        if (!root.exists()) {
            throw new IllegalArgumentException(root + " must exist");
        }
        try {
            this.root = root.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Repository(String string) {
        this(new File(string));
    }

    /**
     * Returns the Mercurial index file containing the revisions.
     * 
     * @param file
     * @return
     */
    public File getIndex(File file) {
        if (!file.isFile()) {
//            throw new IllegalArgumentException(file + " must be a file");
        }
        String filePath;
        try {
            filePath = file.getCanonicalPath();
            String rootPath = Util.forwardSlashes(root.getCanonicalPath());
            String relativeRoot = Util.forwardSlashes(filePath.substring(rootPath.length()));
            String indexName = DATA + CaseFolding.fold(relativeRoot) + INDEX_SUFFIX;
            return new File(rootPath + "/" + indexName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the parsed content of the '.hgignore'-file.
     * 
     * @return
     */
    public Ignore getIgnore() {
        File ignoreFile = new File(root.getAbsoluteFile(), ".hgignore");
        return new Ignore(this, ignoreFile);
    }

    public ChangeLog getChangeLog() {
        String logIndex = root.getAbsolutePath() + "/" + STORE + "00changelog.i";
        File index = new File(logIndex);
        return new ChangeLog(this, index);
    }

    public Manifest getManifest() {
        String logIndex = root.getAbsolutePath() + "/" + STORE + "00manifest.i";
        File index = new File(logIndex);
        return new Manifest(index);

    }

    public DirState getDirState() {

        String path = new StringBuilder(root.getAbsolutePath()).append('/').append(HG).append(DIRSTATE).toString();
        File dirStateFile = new File(path);
        if (!dirStateFile.exists()) {
            throw new IllegalStateException("Unable to find dirstate file at location: " + path);
        }
        return new DirState(dirStateFile);
    }

    public Revlog getRevlog(File file) {
        File revIndex = getIndex(file);
        return new Revlog(revIndex);
    }

    /**
     * Returns a file relative to the repository-root.
     * 
     * @param file
     * @return
     */
    public File makeRelative(final File file) {
        final File absoluteFile;
        try {
            absoluteFile = file.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String abs = absoluteFile.getAbsolutePath();
        final String rootPath = root.getAbsolutePath();
        if (!abs.startsWith(rootPath)) {
            throw new IllegalArgumentException(file + " is not a child of " + root);
        }
        if (abs.length() == rootPath.length()) {
            return root;
        }
        String relativePath = abs.substring(rootPath.length() + 1);
        return new File(relativePath);
    }

    /**
     * Returns the canonical file related to the repo-root.
     * 
     * @param path
     * @return a canonical file.
     */
    public File makeAbsolute(final String path) {
        final File absoluteRoot = root.getAbsoluteFile();
        try {
            return new File(absoluteRoot, path).getCanonicalFile();
        } catch (IOException e) {
            // Don't know when this will happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the repository root directory.
     * 
     * @return root-directory.
     */
    public File getRoot() {
        return root;
    }

    /**
     * Checks wether this file is the .hg-directory holding Mercurial
     * information.
     * 
     * @param file
     *            the file to check.
     * @return true if this is the .hg-directory.
     */
    public static boolean isRepoPrivate(File file) {
        return file.isDirectory() && file.getName().equalsIgnoreCase(".hg");
    }
}
