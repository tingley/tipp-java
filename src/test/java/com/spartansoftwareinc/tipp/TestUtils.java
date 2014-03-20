package com.spartansoftwareinc.tipp;

import java.util.List;

import com.spartansoftwareinc.tipp.TIPPError;
import com.spartansoftwareinc.tipp.TIPPErrorSeverity;

import static org.junit.Assert.*;

public class TestUtils {

    public static void expectLoadStatus(CollectingErrorHandler handler, 
            int expectedSize, TIPPErrorSeverity expectedSeverity) {
        List<TIPPError> errors = handler.getErrors();
        if (errors.size() != expectedSize || 
                !handler.getMaxSeverity().equals(expectedSeverity)) {
            System.err.println("Expected " + expectedSize + 
                    " errors, max severity " + expectedSeverity);
            for (TIPPError e : errors) {
                System.err.println("+ " + e);
                if (e.getException() != null) {
                    System.err.println("++ " + e.getException().getMessage());
                }
            }
            assertEquals(expectedSize, errors.size());
            assertEquals(expectedSeverity, handler.getMaxSeverity());
        }
    }


    public static TIPPFactory createFactory(TIPPErrorHandler handler) {
        TIPPFactory factory = new TIPPFactory();
        factory.setErrorHandler(handler);
        return factory;
    }
}
