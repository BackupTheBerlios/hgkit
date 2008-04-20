package org.freehg.hgkit.core;

import java.io.File;

public class Repository {
	private static final String HG = ".hg/";
	private static final String STORE = HG + "store/";
	private static final String DATA = STORE + "/data/";
	
	private static final String INDEX_SUFFIX = ".i";

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
		String filePath = file.getAbsolutePath();
		String relativeRoot = filePath.substring(root.getAbsolutePath().length() - 1);
		String indexName = DATA + CaseFolding.fold(relativeRoot) + INDEX_SUFFIX;
		return new File(indexName);
	}

	public Revlog getChangeLog() {
		String logIndex = root.getAbsolutePath() + "/" + STORE + "00changelog.i";
		File index = new File(logIndex);
		return new Revlog(index,index);
	}
}
