package com.spartansoftwareinc.tipp;

import java.util.Objects;

/**
 * Represents a TIPP resource represented as a file.
 */
public abstract class TIPPResource {
    private TIPPSectionType sectionType;
    private String name;
    private int sequence = 1;
    
    TIPPResource(TIPPSectionType sectionType, String name, int sequence) {
        this.sectionType = sectionType;
        this.name = name;
        this.sequence = sequence;
    }
    
    TIPPSectionType getSectionType() {
        return sectionType;
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
