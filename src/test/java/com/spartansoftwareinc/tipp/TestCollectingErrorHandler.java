package com.spartansoftwareinc.tipp;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.spartansoftwareinc.tipp.TIPPError;
import com.spartansoftwareinc.tipp.TIPPErrorSeverity;

import static org.junit.Assert.*;
import static com.spartansoftwareinc.tipp.TIPPErrorType.*;
import static com.spartansoftwareinc.tipp.TIPPErrorSeverity.*;

public class TestCollectingErrorHandler {

    @Test
    public void testNoErrors() { 
        CollectingErrorHandler status = new CollectingErrorHandler();
        assertNotNull(status.getErrors());
        assertEquals(0, status.getErrors().size());
        assertEquals(TIPPErrorSeverity.NONE, status.getMaxSeverity());
    }
    
    @Test
    public void testSeverity() {
        CollectingErrorHandler status = new CollectingErrorHandler();
        status.reportError(INVALID_PAYLOAD_ZIP, "", null);
        status.reportError(MISSING_MANIFEST, "", null);
        status.reportError(MISSING_PAYLOAD_RESOURCE, "", null);
        assertEquals(3, status.getErrors().size());
        assertEquals(FATAL, status.getMaxSeverity());
        Set<TIPPError> errors = new HashSet<TIPPError>(status.getErrors());
        assertTrue(errors.contains(new TIPPError(INVALID_PAYLOAD_ZIP, "", null)));
        assertTrue(errors.contains(new TIPPError(MISSING_MANIFEST, "", null)));
        assertTrue(errors.contains(new TIPPError(MISSING_PAYLOAD_RESOURCE, "", null)));
    }
}
