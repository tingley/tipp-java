package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPErrorSeverity.ERROR;
import static com.spartansoftwareinc.tipp.TIPPErrorSeverity.FATAL;
import static com.spartansoftwareinc.tipp.TIPPErrorSeverity.WARN;

public enum TIPPErrorType {
    INVALID_PACKAGE_ZIP(FATAL),
    INVALID_PAYLOAD_ZIP(ERROR),
    // package (not payload) contains an unexpected file
    UNEXPECTED_PACKAGE_CONTENTS(ERROR),
    // manifest was not present
    MISSING_MANIFEST(FATAL),
    // manifest was present but unparseable
    CORRUPT_MANIFEST(FATAL),
    // manifest failed schema validation
    INVALID_MANIFEST(FATAL),
    // manifest contained a duplicate section
    DUPLICATE_SECTION_IN_MANIFEST(ERROR),
    // invalid section type for a known task type
    INVALID_SECTION_FOR_TASK(ERROR),
    MISSING_PAYLOAD_RESOURCE(ERROR),
    UNEXPECTED_PAYLOAD_RESOURCE(ERROR),
    // Same resource location appeared twice
    DUPLICATE_RESOURCE_LOCATION_IN_MANIFEST(ERROR),
    // Same sequence appeared twice in a section
    DUPLICATE_RESOURCE_SEQUENCE_IN_MANIFEST(ERROR),
    // Invalid location attribute for payload file
    INVALID_RESOURCE_LOCATION_IN_MANIFEST(ERROR),
    INVALID_SIGNATURE(ERROR),
    UNABLE_TO_VERIFY_SIGNATURE(WARN);

    private TIPPErrorSeverity severity;
    TIPPErrorType(TIPPErrorSeverity severity) {
        this.severity = severity;
    }
    public TIPPErrorSeverity getSeverity() {
        return severity;
    }
}
