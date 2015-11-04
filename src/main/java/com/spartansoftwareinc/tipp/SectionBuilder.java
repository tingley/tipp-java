package com.spartansoftwareinc.tipp;

import java.util.ArrayList;
import java.util.List;

import com.spartansoftwareinc.tipp.TIPPReferenceFile.LanguageChoice;

class SectionBuilder {
    private static int INITIAL_SEQUENCE = 1;
    private int nextSequence = INITIAL_SEQUENCE;
    private TIPPSectionType sectionType;
    private List<TIPPFile> resources = new ArrayList<>();

    SectionBuilder(TIPPSectionType sectionType) {
        this.sectionType = sectionType;
    }

    TIPPFile addFile(String name) {
        TIPPFile file = new TIPPFile(sectionType, name, nextSequence++);
        resources.add(file);
        return file;
    }

    TIPPReferenceFile addReferenceFile(String name, LanguageChoice languageChoice) {
        if (sectionType != TIPPSectionType.REFERENCE) {
            throw new IllegalArgumentException("Only Reference sections support reference files");
        }
        TIPPReferenceFile file = new TIPPReferenceFile(sectionType, name, nextSequence++, languageChoice);
        resources.add(file);
        return file;
    }

    TIPPSection build() {
        return new TIPPSection(sectionType, resources);
    }
}
