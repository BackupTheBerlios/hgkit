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
     * {@inheritDoc}
     */
    public HgInternalError() {
        super();
    }

    /**
     * {@inheritDoc}
     * @param message
     */
    public HgInternalError(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     * @param cause
     */
    public HgInternalError(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     * @param message
     * @param cause
     */
    public HgInternalError(String message, Throwable cause) {
        super(message, cause);
    }

}
