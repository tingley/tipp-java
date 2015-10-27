package com.spartansoftwareinc.tipp;

import java.util.Collections;
import java.util.Objects;
import java.util.Collection;

/**
 * Representation of a custom task type.
 */
public class CustomTaskType implements TIPPTaskType {

    private String type;
    private Collection<TIPPSectionType> supportedSectionTypes;
   
    @SuppressWarnings("unchecked")
    public CustomTaskType(String type, Collection<TIPPSectionType> supportedSectionTypes) {
        this.type = type;
        this.supportedSectionTypes = supportedSectionTypes != null ?  
                Collections.unmodifiableCollection(supportedSectionTypes) :
                Collections.EMPTY_SET;
    }
    
    public String getType() {
        return type;
    }

    public Collection<TIPPSectionType> getSupportedSectionTypes() {
        return supportedSectionTypes;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof CustomTaskType)) return false;
        return Objects.equals(type, ((CustomTaskType)o).type);
    }

    @Override
    public String toString() {
        return "CustomTaskType(" + type + ")";
    }
}
