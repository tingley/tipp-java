package com.spartansoftwareinc.tipp;

import java.util.Objects;

// XXX Move into CollectingErrorHandler?
public class TIPPError {
    
    private TIPPErrorType errorType;
    private String message;
    private Exception exception;
    
    TIPPError(TIPPErrorType errorType) {
        this(errorType, null, null);
    }
    
    TIPPError(TIPPErrorType errorType, String message) {
        this(errorType, message, null);
    }
    
    TIPPError(TIPPErrorType errorType, String message, Exception e) {
        this.errorType = errorType;
        this.message = message;
        this.exception = e;
    }

    public TIPPErrorType getErrorType() {
        return errorType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Exception getException() {
        return exception;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorType, message);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPError)) return false;
        TIPPError e = (TIPPError)o;
        return Objects.equals(errorType, e.errorType) &&
               Objects.equals(message, e.message);
    } 
    
    @Override
    public String toString() {
        return errorType + "(" + message + ")";
    }
}
