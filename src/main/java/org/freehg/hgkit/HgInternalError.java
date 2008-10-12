/*
 * HgInternalError.java 11.09.2008
 *
 */
package org.freehg.hgkit;

/**
 * Internal unchecked exception if something weird is happening.
 * 
 * @author mfriedenhagen
 */
public class HgInternalError extends RuntimeException {

    /**
     * Constructor.
     */
    public HgInternalError() {
        super();
    }

    /**
     * Constructor with a String message.
     * @param message message
     */
    public HgInternalError(String message) {
        super(message);
    }

    /**
     * Constructor with a root cause.
     * @param cause Throwable
     */
    public HgInternalError(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with a String message and a root cause.
     * @param message message
     * @param cause Throwable
     */
    public HgInternalError(String message, Throwable cause) {
        super(message, cause);
    }

}
