package com.spartansoftwareinc.tipp;

import java.util.Objects;

/**
 * Represents a TIPP resource.  Currently, all TIPP resources are files present within
 * the package.
 */
public abstract class TIPPResource {
    private TIPPSectionType sectionType;
    private TIPPResourceType type;
    private String name;
    private int sequence = 1;
    
    TIPPResource(TIPPSectionType sectionType, TIPPResourceType type, String name, int sequence) {
        this.sectionType = sectionType;
        this.type = type;
        this.name = name;
        this.sequence = sequence;
    }
    
    public TIPPSectionType getSectionType() {
        return sectionType;
    }

    public TIPPResourceType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public int getSequence() {
        return sequence;
    }
    
    @Override
    public String toString() {
        return name + "(" + sequence + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionType, name, sequence);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPResource)) {
            return false;
        }
        TIPPResource f = (TIPPResource)o;
        return sectionType == f.sectionType &&
               Objects.equals(name, f.name) &&
               Objects.equals(sequence, f.sequence);
    }
}
