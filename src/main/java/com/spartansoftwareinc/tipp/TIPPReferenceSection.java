package com.spartansoftwareinc.tipp;

import java.util.List;

public class TIPPReferenceSection extends TIPPSection {

    public TIPPReferenceSection() {
        super(TIPPSectionType.REFERENCE);
    }

    @Override
    protected TIPPReferenceFile createFile(String name, String location, int sequence) {
        return new TIPPReferenceFile(location, name, sequence);
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
