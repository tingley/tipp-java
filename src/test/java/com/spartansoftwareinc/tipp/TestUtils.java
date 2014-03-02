package com.spartansoftwareinc.tipp;

import java.util.List;

import com.spartansoftwareinc.tipp.TIPPError;
import com.spartansoftwareinc.tipp.TIPPErrorSeverity;
import com.spartansoftwareinc.tipp.TIPPLoadStatus;

import static org.junit.Assert.*;

public class TestUtils {

    public static void expectLoadStatus(TIPPLoadStatus status, 
            int expectedSize, TIPPErrorSeverity expectedSeverity) {
        List<TIPPError> errors = status.getAllErrors();
        if (errors.size() != expectedSize || 
                !status.getSeverity().equals(expectedSeverity)) {
            System.err.println("Expected " + expectedSize + 
                    " errors, max severity " + expectedSeverity);
            for (TIPPError e : errors) {
                System.err.println("+ " + e);
                if (e.getException() != null) {
                    System.err.println("++ " + e.getException().getMessage());
                }
            }
            assertEquals(expectedSize, errors.size());
            assertEquals(expectedSeverity, status.getSeverity());
        }
    }
}
