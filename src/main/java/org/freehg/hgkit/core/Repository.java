package org.freehg.hgkit.core;

import java.io.File;
import java.io.IOException;

import org.freehg.hgkit.HgInternalError;

/**
 * The Repository.
 */
public class Repository {

    public static final String HG = ".hg/";

    private static final String STORE = HG + "store/";

    private static final String DATA = STORE + "data/";

    private static final String INDEX_SUFFIX = ".i";

    private static final String DIRSTATE = HG + "dirstate";

    private final File root;

    private final String absoluteRootPath;

    /**
     * Creates a Repository-instance for the given file.
     * 
     * @param root
     *            of the repository.
     */
    public Repository(File root) {
        if (!root.exists()) {
            throw new IllegalArgumentException(root + " must exist");
        }
        try {
            this.root = root.getCanonicalFile();
        } catch (IOException e) {
            throw new HgInternalError("root=" + root, e);
        }
        final File dataDir = new File(this.root, DATA);
        if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException(dataDir + " must exist");
        }
        absoluteRootPath = this.root.getAbsolutePath();
    }

    /**
     * Creates a Repository-instance for the given path.
     * 
     * @param rootPath
     *            of the repository.
     */
    public Repository(String rootPath) {
        this(new File(rootPath));
    }

    /**
     * Returns the Mercurial index file containing the revisions.
     * 
     * @param file
     * @return
     */
    public File getIndex(File file) {
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
     * @return ignore
     */
    public Ignore getIgnore() {
        File ignoreFile = new File(root.getAbsoluteFile(), ".hgignore");
        return new Ignore(this, ignoreFile);
    }

    /**
     * Returns the changelog instance of the repository.
     * 
     * @return changelog
     */
    public ChangeLog getChangeLog() {
        String logIndex = absoluteRootPath + "/" + STORE + "00changelog.i";
        return new ChangeLog(this, new File(logIndex));
    }

    /**
     * Returns the manifest instance of the repository.
     * 
     * @return manifest.
     */
    public Manifest getManifest() {
        String manifestIndex = absoluteRootPath + "/" + STORE + "00manifest.i";
        return new Manifest(new File(manifestIndex));

    }

    /**
     * Returns the dirstate instance of the repository.
     * 
     * @return dirstate
     */
    public DirState getDirState() {
        String dirStatePath = absoluteRootPath + "/" + DIRSTATE;
        return new DirState(new File(dirStatePath));
    }

    /**
     * Returns the {@link Revlog} for the given file.
     * 
     * @param file
     *            file for which we want the Revlog.
     * @return revlog.
     */
    public Revlog getRevlog(File file) {
        File revIndex = getIndex(file);
        return new Revlog(revIndex);
    }

    /**
     * Returns a file relative to the repository-root.
     * 
     * @param file
     *            file for which we want the relative file.
     * @return relative file
     */
    public File makeRelative(final File file) {
        final File absoluteFile;
        try {
            absoluteFile = file.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(file.toString(), e);
        }
        final String abs = absoluteFile.getAbsolutePath();
        if (!abs.startsWith(absoluteRootPath)) {
            throw new IllegalArgumentException(file + " is not a child of " + absoluteRootPath);
        }
        if (abs.length() == absoluteRootPath.length()) {
            return root;
        }
        final String relativePath = abs.substring(absoluteRootPath.length() + 1);
        return new File(relativePath);
    }

    /**
     * Returns the canonical file related to the repo-root.
     * 
     * @param path
     *            of the file
     * @return a canonical file.
     */
    public File makeAbsolute(final String path) {
        final File absoluteRoot = root.getAbsoluteFile();
        try {
            return new File(absoluteRoot, path).getCanonicalFile();
        } catch (IOException e) {
            // Don't know when this will happen
            throw new HgInternalError(absoluteRoot.toString(), e);
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
