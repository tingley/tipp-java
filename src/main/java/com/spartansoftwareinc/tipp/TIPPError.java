package com.spartansoftwareinc.tipp;

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
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((errorType == null) ? 0 : errorType.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPError)) return false;
        TIPPError e = (TIPPError)o;
        return (errorType == e.getErrorType() &&
                 (message == e.getMessage() || 
                     message != null && message.equals(e.getMessage())));
    } 
    
    @Override
    public String toString() {
        return errorType + "(" + message + ")";
    }
}
