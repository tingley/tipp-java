package com.spartansoftwareinc.tipp;

import java.util.List;

public class TIPPReferenceSection extends TIPPSection {

    public TIPPReferenceSection(List<TIPPFile> resources) {
        super(TIPPSectionType.REFERENCE, resources);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TIPPReferenceFile> getFileResources() {
        return (List<TIPPReferenceFile>)super.getFileResources();
    }
}
