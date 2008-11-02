package org.freehg.hgkit.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.freehg.hgkit.HgInternalError;

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
     * ASCII upper max.
     */
    private static final int ASCII_MAX = 256;

    /**
     * ASCII lower max.
     */
    private static final int ASCII_LOWER_MAX = 126;

    /**
     * Space character.
     */
    private static final int ASCII_SPACE = 32;

    /**
     * Reserved letters in the Windows filesystem.
     */
    final static char[] WIN_RESERVED = "\\:*?\"<>|".toCharArray();

    /**
     * Reserved filenames in Windows.
     */
    final static Set<String> WIN_RESERVED_FILESNAMES = new HashSet<String>();

    /**
     * Folding map.
     */
    final static HashMap<String, String> FOLD_MAP;

    /**
     * Unfolding map.
     */
    final static HashMap<String, String> UNFOLD_MAP;

    /**
     * Initializes the lookup-HashMaps.
     */
    static {
        FOLD_MAP = new HashMap<String, String>();
        UNFOLD_MAP = new HashMap<String, String>();
        Collections.addAll(WIN_RESERVED_FILESNAMES,
                "con prn aux nul com1 com2 com3 com4 com5 com6 com7 com8 com9 lpt1 lpt2 lpt3 lpt4 lpt5 lpt6 lpt7 lpt8 lpt9"
                        .split(" "));
        createFoldMap();
        createUnfoldMap();
    }

    /**
     * Static helper class.
     */
    private CaseFolding() {
        // static helpers
    }

    /**
     * Creates the map for folding.
     */
    private static void createFoldMap() {
        for (int i = 0; i < ASCII_LOWER_MAX + 1; i++) {
            final Character c = (char) i;
            FOLD_MAP.put(Character.toString(c), Character.toString(c));
        }
        for (int i = ASCII_LOWER_MAX; i < ASCII_MAX; i++) {
            escapeCharacter((char) i);
        }
        for (char c : WIN_RESERVED) {
            FOLD_MAP.put(Character.toString(c), escapeCharacter(c));
        }
        FOLD_MAP.put(Character.toString((char) ASCII_SPACE), escapeCharacter((char) ASCII_SPACE));
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
    private static void createUnfoldMap() {
        Set<Entry<String, String>> entrySet = FOLD_MAP.entrySet();
        for (Entry<String, String> entry : entrySet) {
            UNFOLD_MAP.put(entry.getValue(), entry.getKey());
        }
        for (String reservedFilename : WIN_RESERVED_FILESNAMES) {
            String lastCharacter = reservedFilename.substring(reservedFilename.length() - 1);
            UNFOLD_MAP.put(escapeCharacter(lastCharacter.charAt(0)), lastCharacter);
        }
    }

    /**
     * Escapes character with it's tilde prefixed hex-code in
     * {@link CaseFolding#FOLD_MAP}.
     * 
     * @param c
     *            character to replace.
     */
    private static String escapeCharacter(final Character c) {
        return String.format("~%02x", (int) c);
    }

    /**
     * Folds a pathname as Mercurial does in util.encodefilename.
     * 
     * @param name
     *            the unfolded name.
     * @return the folded name.
     */
    public static String fold(String name) {
        StringBuilder folded = new StringBuilder();
        for (char c : name.toCharArray()) {
            folded.append(FOLD_MAP.get(Character.toString(c)));
        }
        return folded.toString();
    }

    /**
     * Unfolds a pathname as Mercurial does in util.decodefilename.
     * 
     * @param foldedName
     *            a folded name.
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
                throw new HgInternalError("UNFOLD_MAP does not contain '" + key + "' foldedname = " + foldedName);
            }
            unfolded.append(UNFOLD_MAP.get(key));
        }
        return unfolded.toString();
    }

    /**
     * Returns normalized form for reserved Windows filenames.
     * 
     * @see <a
     *      href="http://marc.info/?l=mercurial-devel&m=122079447309319&w=2">Posting
     *      of Marc on the devel-list</a>
     * 
     * @param path
     *            to inspect
     * @return escaped path
     */
    public static String auxencode(final String path) {
        StringBuilder result = new StringBuilder();
        for (String pathElement : path.split("/")) {
            if (pathElement.length() > 0) {
                final String[] split = pathElement.split("\\.");
                String base = split[0];
                if (WIN_RESERVED_FILESNAMES.contains(base)) {
                    final String replacement = String.format("~%02x", (int) base.charAt(2));
                    pathElement = pathElement.substring(0, 2) + replacement + pathElement.substring(3);
                }
            }
            result.append(pathElement).append("/");
        }
        return result.toString().substring(0, result.length() - 1);
    }
}
