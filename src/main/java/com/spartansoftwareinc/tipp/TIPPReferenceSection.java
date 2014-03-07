package com.spartansoftwareinc.tipp;

import java.util.List;

public class TIPPReferenceSection extends TIPPSection {

    public TIPPReferenceSection() {
        super(TIPPSectionType.REFERENCE);
    }

    @Override
    protected TIPPReferenceFile createFile(String name) {
        return new TIPPReferenceFile(name, name, getNextSequence());
    }
    
    @Override
    public TIPPReferenceFile addFile(String name) {
        return (TIPPReferenceFile)super.addFile(name);
    }
    
    @Override
    void addFile(TIPPFile file) {
        if (!(file instanceof TIPPReferenceFile)) {
            throw new IllegalStateException("Reference section can only contain ReferenceFiles");
        }
        super.addFile(file);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TIPPReferenceFile> getResources() {
        return (List<TIPPReferenceFile>)super.getResources();
    }
}
