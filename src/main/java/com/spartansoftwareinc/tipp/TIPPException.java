package com.spartansoftwareinc.tipp;

/**
 * Base class for all exceptions generated within TIP.
 */
public class TIPPException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    TIPPException(String message) {
        super(message);
    }
    TIPPException(Throwable cause) {
        super(cause);
    }
    TIPPException(String message, Throwable cause) {
        super(cause);
    }
}
