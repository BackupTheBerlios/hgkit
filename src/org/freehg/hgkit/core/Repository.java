package org.freehg.hgkit.core;

import java.io.File;
import java.io.IOException;

public class Repository {
    public static final String HG = ".hg/";
    private static final String STORE = HG + "store/";
    private static final String DATA = STORE + "/data/";

    private static final String INDEX_SUFFIX = ".i";
    private static final String DIRSTATE = "dirstate";

    private final File root;


    public Repository(File root) {
        if(!root.exists()) {
            throw new IllegalArgumentException(root + " must exist");
        }
        this.root = root;
    }

    public Repository(String string) {
        this(new File(string));
    }

    public File getIndex(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " must be a file");
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

    public Revlog getChangeLog() {
        String logIndex = root.getAbsolutePath() + "/" + STORE + "00changelog.i";
        File index = new File(logIndex);
        return new Revlog(index,index);
    }

    public Revlog getManifest() {
        String logIndex = root.getAbsolutePath() + "/" + STORE + "00manifest.i";
        File index = new File(logIndex);
        return new Revlog(index,index);

    }

    public DirState getDirState() {
        String path = root.getAbsoluteFile() + "/" + HG + DIRSTATE;
        File dirStateFile = new File(path);
        if(! dirStateFile.exists()) {
            throw new IllegalStateException("Unable to find dirstate file at location: " + path);
        }
        return new DirState(dirStateFile);
    }

    public Revlog getRevlog(File file) {
        File revIndex = getIndex(file);
        return new Revlog(revIndex, revIndex);
    }

    public File makeRelative(File file) {
        String abs = file.getAbsolutePath();
        if(!abs.startsWith(root.getAbsolutePath())) {
            throw new IllegalArgumentException(file + " is not a child of " + root);
        }
        String relativePath = abs.substring(root.getAbsolutePath().length() + 1);
        return new File(relativePath);
    }

    public File makeAbsolute(String path) {
        StringBuilder abs = new StringBuilder(root.getAbsolutePath());
        if(! (path.startsWith("/") || path.startsWith("\\"))) {
            abs.append("/");
        }
        abs.append(path);
        try {
            return new File(abs.toString()).getCanonicalFile();
        } catch (IOException e) {
            // Don't know when this will happen
            throw new RuntimeException(e);
        }
    }
}
