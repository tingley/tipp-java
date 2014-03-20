package com.spartansoftwareinc.tipp;

/**
 * Default error-handling behavior: throws a {@link TIPPException}
 * on fatal and error conditions, ignores warnings.
 */
public class DefaultErrorHandler implements TIPPErrorHandler {

    /**
     * Calls one of {@link #fatal}, {@link #error}, or {@link #warn},
     * depending on the severity of the reported error.
     * @param type
     * @param message
     * @param e
     */
    public final void reportError(TIPPErrorType type, String message, Exception e) {
        switch (type.getSeverity()) {
        case FATAL:
            fatal(type, message, e);
            break;
        case ERROR:
            error(type, message, e);
            break;
        case WARN:
            warn(type, message, e);
            break;
        }
    }
    
    public void fatal(TIPPErrorType type, String message, Exception exception) {
        fail(type, message, exception);
    }

    public void error(TIPPErrorType type, String message, Exception exception) {
        fail(type, message, exception);
    }
    
    public void warn(TIPPErrorType type, String message, Exception exception) {
        // Ignore
    }

    private void fail(TIPPErrorType type, String message, Exception exception) {
        StringBuilder sb = new StringBuilder(type.toString());
        if (message != null) {
            sb.append(" - ").append(message);
        }
        if (exception != null) {
            throw new TIPPException(sb.toString(), exception);
        }
        else {
            throw new TIPPException(sb.toString());
        }        
    }
}
