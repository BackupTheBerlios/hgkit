package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.FileStatus;
import org.freehg.hgkit.HgInternalError;

/**
 * ChangeLog is just another {@link Revlog} with a specific format.
 * 
 * @see <a href="http://mercurial.selenic.com/wiki/Changelog">ChangeLog page at selenic</a>
 * 
 * <p><tt>hg debugindex .hg/store/00changelog.i</tt>
 * shows the revisions contained in the changelog.</p>
 * 
 * <p>The data in a specific revision might then be obtained with e.g.
 * <tt>hg debugdata .hg/store/00changelog.i 256</tt>.</p>
 * 
 * <pre>
 * 4b6add21b702e18a679686779efaba97a9beff2e
 * Mirko Friedenhagen <mfriedenhagen@users.berlios.de>
 * 1251923138 -7200
 * src/main/java/org/freehg/hgkit/core/ChangeLog.java
 * 
 * Use IOUtils.
 * </pre>
 * 
 * <p>The entry consists of:</p>
 * <ol>
 *  <li>the SHA1-Key of the corresponding manifest entry for this revision</li>
 *  <li>the committer of this revision</li>
 *  <li>a timestamp given in seconds and the offset from UTC of this revision</li>
 *  <li>a list of files in this revision</li>
 *  <li>a blank, separating line</li>
 *  <li>the comment of this revision</li>
 * </ol>
 */
public final class ChangeLog extends Revlog {

    private final Repository repo;

    ChangeLog(Repository repo, File index) {
        super(index);
        this.repo = repo;
    }

    public ChangeSet get(int revision) {
        NodeId node = super.node(revision);
        return this.get(node);
    }

    public ChangeSet get(NodeId node) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        super.revision(node, out).close();
        return new ChangeSet(node, index(node), out.toByteArray());
    }

    public List<FileStatus> getFileStatus(ChangeSet changeset) {
        Manifest manifest = repo.getManifest();
        int revision = changeset.getRevision();
        // TODO Beware of rev 0
        Map<String, NodeId> currMan = manifest.get(changeset);
        Map<String, NodeId> prevMan = new HashMap<String, NodeId>();
        if (revision > 0) {
            ChangeSet prev = get(revision - 1);
            prevMan = manifest.get(prev);
        }
        Set<String> allKeys = new HashSet<String>();
        allKeys.addAll(currMan.keySet());
        allKeys.addAll(prevMan.keySet());
        List<FileStatus> result = new ArrayList<FileStatus>();

        // in both keyset == modified
        // only in prev == removed
        // only in curr == added
        for (String string : allKeys) {
            FileStatus status = null;
            if (currMan.containsKey(string) && prevMan.containsKey(string)) {
                status = FileStatus.valueOf(new File(string), FileStatus.Status.MODIFIED);
            } else if (currMan.containsKey(string)) {
                status = FileStatus.valueOf(new File(string), FileStatus.Status.ADDED);
            } else { // prevMan contains
                status = FileStatus.valueOf(new File(string), FileStatus.Status.REMOVED);
            }
            result.add(status);
        }
        return result;
    }

    public List<ChangeSet> getLog() {
        try {
            int length = count();
            List<ChangeSet> log = new ArrayList<ChangeSet>(length);
            for (int revision = 0; revision < length; revision++) {
                RevlogEntry revlogEntry = index.get(revision);
                ByteArrayOutputStream out = new ByteArrayOutputStream((int) revlogEntry.getUncompressedLength());
                super.revision(revlogEntry, out, false);
                ChangeSet entry = new ChangeSet(revlogEntry.nodeId, revision, out.toByteArray());
                log.add(entry);
            }
            return log;
        } finally {
            close();
        }
    }

    public static class ChangeSet {

        private NodeId manifestId;

        private int revision;

        private NodeId changeId;

        private Date when;

        private String author;

        private List<String> files = new ArrayList<String>();

        private String comment;

        public NodeId getChangeId() {
            return changeId;
        }

        public NodeId getManifestId() {
            return manifestId;
        }

        public int getRevision() {
            return revision;
        }

        public Date getWhen() {
            return (Date)when.clone();
        }

        public String getAuthor() {
            return author;
        }

        public List<String> getFiles() {
            return new ArrayList<String>(files);
        }

        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return changeId.asShort() + " " + when + " " + author + "\n" + comment + "\n" + files;
        }

        /**
         * See {@link ChangeLog} class documentation for specifics.
         * 
         * @param in the stream to parse
         */
        private void parse(final InputStream in) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line = null;
            try {
                // First line may be null, so do not use private readline-method.
                while (null != (line = reader.readLine())) {
                    manifestId = NodeId.valueOf(line);
                    author = readLine(reader);
                    when = dateParse(readLine(reader));

                    String fileLine = readLine(reader);
                    // read while line is not empty, its a file, the rest is the
                    // comment
                    while (0 < fileLine.trim().length()) {
                        files.add(fileLine);
                        fileLine = readLine(reader);
                    }
                    comment = IOUtils.toString(reader);
                }
            } catch (IOException e) {                
                throw new HgInternalError("Error parsing " + in, e);
            }
        }

        /**
         * Returns a line of reader. 
         * @param reader
         * @return
         * @throws IOException
         * @throws HgInternalError if we could not read a line.
         */
        private String readLine(BufferedReader reader) throws IOException {
            final String line = reader.readLine();
            if (line == null) {
                throw new HgInternalError(reader.toString());
            }
            return line;
        }

        private Date dateParse(String dateLine) {
            String parts[] = dateLine.split(" ");
            long secondsSinceEpoc = Integer.parseInt(parts[0]);
            long offset = Integer.parseInt(parts[1]);
            long msSinceEpoc = TimeUnit.MILLISECONDS.convert(secondsSinceEpoc + offset, TimeUnit.SECONDS);
            return new Date(msSinceEpoc);
        }

        ChangeSet(NodeId changeId, int revision, byte[] data) {
            parse(new ByteArrayInputStream(data));
            this.changeId = changeId;
            this.revision = revision;

        }
    }
}
