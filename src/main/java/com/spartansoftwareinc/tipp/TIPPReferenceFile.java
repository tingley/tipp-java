package com.spartansoftwareinc.tipp;

import java.util.Objects;

public class TIPPReferenceFile extends TIPPFile {

    public enum LanguageChoice {
        source,
        target;
    }

    private LanguageChoice languageChoice;

    TIPPReferenceFile(TIPPSectionType sectionType, String name, int sequence, LanguageChoice langChoice) {
        super(sectionType, TIPPResourceType.REFERENCE_FILE, name, sequence);
        this.languageChoice = langChoice;
    }

    public LanguageChoice getLanguageChoice() {
        return languageChoice;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + Objects.hashCode(languageChoice);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPReferenceFile)) {
            return false;
        }
        TIPPReferenceFile f = (TIPPReferenceFile)o;
        return super.equals(f) &&
               Objects.equals(languageChoice, f.languageChoice);
    }
}
