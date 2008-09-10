package org.freehg.hgkit.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class RemoveMetaOutputStream extends OutputStream {

    private static final int FIRST_STOP_BYTE = 3;

    private static final int WRITE_OUT = 10;

    private static final int SECOND_BYTE = 1;

    private static final int FIRST_BYTE = 0;

    private OutputStream out;

    private int mode = 0;

    private final ByteArrayOutputStream buff = new ByteArrayOutputStream();

    private OutputStream current;

    private OutputStream state1;

    private OutputStream state2;

    private OutputStream state3;

    private OutputStream state4;

    public RemoveMetaOutputStream(OutputStream out) {
        this.out = out;

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
                if (b == 1) {
                    current = state2;
                } else {
                    current = RemoveMetaOutputStream.this.out;
                    current.write(b);
                }

            }
        };

        state2 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == '\n') {
                    current = state3;
                } else {
                    current = RemoveMetaOutputStream.this.out;
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
                if (b == '\n') {
                    current = RemoveMetaOutputStream.this.out;
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
