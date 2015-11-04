package com.spartansoftwareinc.tipp;

import java.util.Collection;

/**
 * Representation of a TIPP task type.  This may be a built-in 
 * type (see {@link StandardTaskType}) or a {@link CustomTaskType}.
 *
 */
public interface TIPPTaskType {

    /**
     * Get the URI of this task.
     * @return task URI, as a String
     */
    public String getTaskURI();

    /**
     * Return the sections that may be present for this task.
     * @return collection of allowed section types
     */
    public Collection<TIPPSectionType> getSupportedSectionTypes();
}
