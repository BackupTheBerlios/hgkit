package org.freehg.hgkit.core;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Implements folding and unfolding of filenames by replacing:
 * <ul>
 * <li>uppercase letters with underscore and uppercase letters</li>
 * <li>reserved windows filesystem letters and non-ascii letters with their
 * tilde-prefixed hex-code</li>
 * <li>leaving the rest as is.
 * <li>
 * </ul>
 * 
 * @author mirko
 */
public final class CaseFolding {

    /**
     * Reserved letters in the Windows filesystem.
     */
    final static char[] WIN_RESERVED = "\\:*?\"<>|".toCharArray();

    /**
     * Folding map.
     */
    final static HashMap<String, String> FOLD_MAP;

    /**
     * Unfolding map.
     */
    final static HashMap<String, String> UNFOLD_MAP;

    static {
        FOLD_MAP = new HashMap<String, String>();
        UNFOLD_MAP = new HashMap<String, String>();
        createLookupMap();
        createInverseMap();
    }

    /**
     * Creates the map for folding.
     */
    private static void createLookupMap() {
        for (int i = 0; i < 127; i++) {
            final Character c = (char) i;
            FOLD_MAP.put(Character.toString(c), Character.toString(c));
        }
        for (int i = 126; i < 256; i++) {
            replaceSpecialLetters((char) i);
        }
        for (char c : WIN_RESERVED) {
            replaceSpecialLetters(c);
        }
        replaceSpecialLetters((char) 32);
        replaceUpperCaseLetters();
    }

    /**
     * Replaces uppercase letters and underscore by underscore + lowercase
     * letter in {@link CaseFolding#FOLD_MAP}.
     */
    private static void replaceUpperCaseLetters() {
        for (char c = 'A'; c <= 'Z'; c++) {
            FOLD_MAP.put(Character.toString(c), "_" + Character.toLowerCase(c));
        }
        FOLD_MAP.put("_", "__");
    }

    /**
     * Creates the reverse map {@link CaseFolding#UNFOLD_MAP} for unfolding.
     */
    private static void createInverseMap() {
        Set<Entry<String, String>> entrySet = FOLD_MAP.entrySet();
        for (Entry<String, String> entry : entrySet) {
            UNFOLD_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Replaces or sets special character with it's tilde prefixed hex-code in
     * {@link CaseFolding#FOLD_MAP}.
     * 
     * @param c
     *            character to replace.
     */
    private static void replaceSpecialLetters(final Character c) {
        final String replacement = String.format("~%02x", (int) c);
        FOLD_MAP.put(Character.toString(c), replacement);
    }

    /**
     * Folds a pathname as Mercurial does in util.encodefilename.
     * 
     * @param name
     * @return the folded name.
     */
    public static String fold(String name) {
        StringBuilder folded = new StringBuilder();
        char[] charArray = name.toCharArray();
        for (char c : charArray) {
            folded.append(FOLD_MAP.get(Character.toString(c)));
        }
        return folded.toString();
    }

    /**
     * Unfolds a pathname as Mercurial does in util.decodefilename.
     * 
     * @param foldedName
     * @return the unfolded name.
     */
    public static String unfold(String foldedName) {
        final StringBuilder unfolded = new StringBuilder();
        for (int i = 0; i < foldedName.length();) {
            final char[] keyCharacters;
            if (foldedName.charAt(i) == '~') {
                keyCharacters = new char[] { foldedName.charAt(i++), foldedName.charAt(i++), foldedName.charAt(i++) };
            } else if (foldedName.charAt(i) == '_') {
                keyCharacters = new char[] { foldedName.charAt(i++), foldedName.charAt(i++) };
            } else {
                keyCharacters = new char[] { foldedName.charAt(i++) };
            }
            final String key = new String(keyCharacters);
            if (!UNFOLD_MAP.containsKey(key)) {
                throw new AssertionError("UNFOLD_MAP does not contain " + key);
            }
            unfolded.append(UNFOLD_MAP.get(key));
        }
        return unfolded.toString();
    }
}
