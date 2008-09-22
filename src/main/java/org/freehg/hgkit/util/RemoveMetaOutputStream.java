package org.freehg.hgkit.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Removes the metadata from a patch-set. Metadata-borders are marked by special
 * bytes.
 * 
 */
public final class RemoveMetaOutputStream extends OutputStream {

    private static final int FIRST_STOP_BYTE = 3;

    private static final int WRITE_OUT = '\n';

    private static final int SECOND_BYTE = 1;

    private static final int FIRST_BYTE = 0;

    private OutputStream current;

    private final OutputStream state1;

    private final OutputStream state2;

    private final OutputStream state3;

    private final OutputStream state4;

    /**
     * @param out
     *            an OutputStream containing Metadata.
     */
    public RemoveMetaOutputStream(final OutputStream out) {
        /**
         * This may look weird but uses the state pattern
         * 
         * <pre>
         *   S1 -&gt;  S2 -&gt; S3 &lt;-&gt; S4
         *    \     |          /
         *     \    |         /
         *      \  \/        /
         *       \- &gt;S5 &lt; -/
         * </pre>
         */
        state1 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == SECOND_BYTE) {
                    current = state2;
                } else {
                    current = out;
                    current.write(b);
                }

            }
        };

        state2 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == WRITE_OUT) {
                    current = state3;
                } else {
                    current = out;
                    current.write(1);
                    current.write(b);
                }
            }
        };
        
        state3 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == '1') {
                    current = state4;
                }
            }
        };

        state4 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == WRITE_OUT) {
                    current = out;
                } else {
                    current = state3;
                }
            }
        };
        this.current = state1;

    }

    @Override
    public void write(int b) throws IOException {
        this.current.write(b);
    }

}
