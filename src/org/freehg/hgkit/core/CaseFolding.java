package org.freehg.hgkit.core;
// FIXME Implement this casefolding
public final class CaseFolding {
	public static String fold(String name) {
		String doubleUnder = name.replaceAll("_", "__");
		String folded = doubleUnder.replaceAll("([A-Z])", "_$1").toLowerCase();
		return folded;
	}
	public static String unfold(String foldedName) {
		return foldedName;
	}
}
