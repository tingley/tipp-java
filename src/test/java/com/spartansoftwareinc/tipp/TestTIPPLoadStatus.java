package com.spartansoftwareinc.tipp;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.spartansoftwareinc.tipp.TIPPError;
import com.spartansoftwareinc.tipp.TIPPErrorSeverity;
import com.spartansoftwareinc.tipp.TIPPLoadStatus;

import static org.junit.Assert.*;
import static com.spartansoftwareinc.tipp.TIPPError.Type.*;
import static com.spartansoftwareinc.tipp.TIPPErrorSeverity.*;

public class TestTIPPLoadStatus {

    @Test
    public void testNoErrors() { 
        TIPPLoadStatus status = new TIPPLoadStatus();
        assertNotNull(status.getAllErrors());
        assertEquals(0, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.NONE, status.getSeverity());
    }
    
    @Test
    public void testSeverity() {
        TIPPLoadStatus status = new TIPPLoadStatus();
        status.addError(INVALID_PAYLOAD_ZIP);
        status.addError(MISSING_MANIFEST);
        status.addError(MISSING_PAYLOAD_RESOURCE);
        assertEquals(3, status.getAllErrors().size());
        assertEquals(FATAL, status.getSeverity());
        Set<TIPPError> errors = new HashSet<TIPPError>(status.getAllErrors());
        assertTrue(errors.contains(new TIPPError(INVALID_PAYLOAD_ZIP)));
        assertTrue(errors.contains(new TIPPError(MISSING_MANIFEST)));
        assertTrue(errors.contains(new TIPPError(MISSING_PAYLOAD_RESOURCE)));
    }
}
