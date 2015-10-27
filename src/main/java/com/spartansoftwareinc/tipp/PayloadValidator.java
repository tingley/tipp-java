package com.spartansoftwareinc.tipp;

import java.util.HashSet;
import java.util.Set;

import com.spartansoftwareinc.tipp.TIPPErrorType;
import static com.spartansoftwareinc.tipp.TIPPErrorType.*;

/**
 * Validates a package payload against its manifest.
 */
class PayloadValidator {

    /**
     * Checks the manifest against the package source and looks for 
     * discrepancies between the expected and actual objects.
     * 
     * @param manifest
     * @param source
     * @param status
     * @return true if successful, false if an error was found
     */
    boolean validate(Manifest manifest, Payload payload, TIPPErrorHandler errorHandler) {
        CollectingErrorHandler validationErrors = new CollectingErrorHandler();
        Set<String> objectPaths = payload.getPaths();
        Set<String> pathsInManifest = new HashSet<String>();
        for (TIPPSection section : manifest.getSections()) {
            for (TIPPResource obj : section.getResources()) {
                // TODO: some form of validation needs to be factored into 
                // the resource class.. or into the section somehow.
                if (obj instanceof TIPPFile) {
                    String expectedPath = Payload.getFilePath(section.getType(),
                                            manifest.getLocationForFile((TIPPFile)obj));
                    if (pathsInManifest.contains(expectedPath)) {
                        validationErrors.reportError(DUPLICATE_RESOURCE_LOCATION_IN_MANIFEST,
                                "Duplicate resource in manifest: " + expectedPath, null);
                    }
                    if (!objectPaths.contains(expectedPath)) {
                        validationErrors.reportError(MISSING_PAYLOAD_RESOURCE, 
                                "Missing resource: " + expectedPath, null);
                    }
                    pathsInManifest.add(expectedPath);
                }
            }
        }
        // Now check in the other direction
        for (String objectPath : objectPaths) {
            if (!pathsInManifest.contains(objectPath)) {
                validationErrors.reportError(TIPPErrorType.UNEXPECTED_PAYLOAD_RESOURCE, 
                                "Unexpected package resource: " + objectPath, null);
            }
        }
        for (TIPPError e : validationErrors.getErrors()) {
            errorHandler.reportError(e.getErrorType(), e.getMessage(), e.getException());
        }
        // Add all errors to the regular error.
        return validationErrors.getErrors().size() == 0;
    }
}
