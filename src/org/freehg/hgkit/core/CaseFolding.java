package org.freehg.hgkit.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// FIXME Implement this casefolding
public final class CaseFolding {
    final static char[] WIN_RESERVED = "\\:*?\"<>|".toCharArray();
    final static HashMap<String,String> CMAP;
    final static HashMap<String,String> DMAP;
    static {
        CMAP = new HashMap<String, String>();
        DMAP = new HashMap<String, String>();
        createLookupMap();        
        createInverseMap();
    }
    /**
     * 
     */
    private static void createLookupMap() {
        for (int i = 0; i < 127; i++) {
            final Character c = (char)i;
            CMAP.put(Character.toString(c), Character.toString(c));
        }
        for (int i = 126; i < 256; i++) {
            replaceSpecialLetters((char)i);
        }
        for (char c:WIN_RESERVED) {
            replaceSpecialLetters(c);
        }
        replaceSpecialLetters((char)32);
        replaceUpperCaseLetters();
    }

    /**
     * 
     */
    private static void replaceUpperCaseLetters() {
        for (char c = 'A'; c<= 'Z'; c++) {
            CMAP.put(Character.toString(c), "_" + Character.toString(Character.toLowerCase(c)));
        }
        CMAP.put("_", "__");
    }
    
    /**
     * Create the reverse map for unfolding. 
     */
    private static void createInverseMap() {
        Set<String> keySet = CMAP.keySet();
        for (String key : keySet) {
            DMAP.put(CMAP.get(key), key);            
        }
    }
    
    /**
     * Replace special characters with their Hex-Code. 
     * @param c
     */
    private static void replaceSpecialLetters(final Character c) {
        final String replacement = String.format("~%02x", (int)c);
        CMAP.put(Character.toString(c), replacement);
    }
    
	public static String fold(String name) {
	    StringBuilder folded = new StringBuilder();
        char[] charArray = name.toCharArray();
        for (char c : charArray) {            
            folded.append(CMAP.get(Character.toString(c)));
        }
        return folded.toString();
	}

    /**
     * @param map
     * @param name
     * @return
     */
    private static String replace(final HashMap<String, String> map, String name) {
        StringBuilder folded = new StringBuilder();
	    char[] charArray = name.toCharArray();
	    for (char c : charArray) {            
            folded.append(map.get(Character.toString(c)));
        }
	    return folded.toString();
    }
	
	public static String unfold(String foldedName) {
		return replace(DMAP, foldedName);
	}
	
	public static void main(String[] args) {
        System.err.println(CMAP);
    }
}
