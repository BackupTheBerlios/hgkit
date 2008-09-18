/*
 Copyright 2008 Stefan Chyssler 

 This software may be used and distributed according to the terms of
 the GNU General Public License or under the Eclipse Public Licence (EPL)
 */
package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.util.GlobExpressions;

public final class Ignore {

    List<IgnoreEntry> ignores = new ArrayList<IgnoreEntry>();

    private enum Syntax {
        GLOB, REGEX
    };

    private Syntax currentSyntax = Syntax.REGEX;

    private final Repository repo;

    Ignore(Repository repo) {
        this.repo = repo;
    }

    public Ignore(Repository repo, File file) {
        this.repo = repo;
        if (file.exists()) {
            try {
                parse(file);
            } catch (IOException e) {
                throw new HgInternalError("Could not parse " + file, e);
            }
        }
    }

    public boolean isIgnored(File file) {
        File relativeFile = file;
        if (this.ignores.isEmpty()) {
            return false;
        }
        if (file.isAbsolute()) {
            relativeFile = repo.makeRelative(file);
        }
        for (IgnoreEntry ignore : this.ignores) {
            if (ignore.ignores(relativeFile.getPath())) {
                return true;
            }
        }
        return false;
    }

    void parse(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            parse(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * Take a reader, this for easier tests.
     * 
     * @param reader
     * @throws IOException
     */
    void parse(BufferedReader reader) throws IOException {
        String readLine = null;
        while (null != (readLine = reader.readLine())) {
            String line = readLine.trim();
            if (0 < line.length()) {
                try {
                    parseLine(line);
                } catch (PatternSyntaxException e) {
                    throw new HgInternalError("Could not parse line " + line, e);
                }
            }
        }
    }

    void parseLine(String line) {
        if (line.startsWith("syntax:")) {
            changeSyntax(line.replace("syntax:", "").trim());
        } else {
            switch (currentSyntax) {
            case GLOB:
                this.ignores.add(new RegexIgnoreEntry(GlobExpressions.toRegex(line)));
                break;
            case REGEX:
                this.ignores.add(new RegexIgnoreEntry(Pattern.compile(line)));
                break;
            }
        }
    }

    void changeSyntax(String text) {
        if (text.equalsIgnoreCase("glob")) {
            this.currentSyntax = Syntax.GLOB;
        } else if (text.equalsIgnoreCase("regex")) {
            this.currentSyntax = Syntax.REGEX;
        } else {
            throw new HgInternalError("Unknown Ignore-Syntax:" + text);
        }
    }

    private interface IgnoreEntry {
        /**
         * 
         * @param path
         *            - a path name relative repository root
         * @return true if this entry say it should be ignored
         */
        boolean ignores(String path);
    }

    static class RegexIgnoreEntry implements IgnoreEntry {
        private final Pattern pattern;

        public RegexIgnoreEntry(Pattern compile) {
            this.pattern = compile;
        }

        public boolean ignores(String path) {
            return this.pattern.matcher(path).matches();
        }
    }
}
