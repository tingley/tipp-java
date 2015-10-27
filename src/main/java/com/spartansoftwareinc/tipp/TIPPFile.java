package com.spartansoftwareinc.tipp;

/**
 * TIPP File resources are identified by an abstract name as well
 * as a sequence. The location within the TIPP section (which may or may not
 * be identical to the name) is not considered part of the file information,
 * as it's an implementation detail, but may be obtained by querying the
 * package itself for a given TIPPFile location.
 */
public class TIPPFile extends TIPPResource {
    TIPPFile(TIPPSectionType sectionType, String name, int sequence) {
        super(sectionType, name, sequence);
    }
}
