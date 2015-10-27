package com.spartansoftwareinc.tipp;

import java.util.List;

public class TIPPReferenceSection extends TIPPSection {

    public TIPPReferenceSection(List<TIPPResource> resources) {
        super(TIPPSectionType.REFERENCE, resources);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TIPPReferenceFile> getResources() {
        return (List<TIPPReferenceFile>)super.getResources();
    }
}
