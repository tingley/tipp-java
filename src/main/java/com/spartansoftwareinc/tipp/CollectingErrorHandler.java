package com.spartansoftwareinc.tipp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link TIPPErrorHandler} implementation that collects all
 * the errors it encounters, rather than acting on them immediately.
 */
public class CollectingErrorHandler implements TIPPErrorHandler {
    private List<TIPPError> errors = new ArrayList<TIPPError>();

    public List<TIPPError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void reportError(TIPPErrorType type, String message, Exception e) {
        errors.add(new TIPPError(type, message, e));
    }

    public TIPPErrorSeverity getMaxSeverity() {
        TIPPErrorSeverity sev = TIPPErrorSeverity.NONE;
        for (TIPPError e : errors) {
            if (e.getErrorType().getSeverity().ordinal() > sev.ordinal()) {
                sev = e.getErrorType().getSeverity();
            }
        }
        return sev;
    }
}
