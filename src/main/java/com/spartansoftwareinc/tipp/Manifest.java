package com.spartansoftwareinc.tipp;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Manifest {
    private TIPPTask task;
    private String packageId;
    private TIPPCreator creator;
    private boolean isRequest;
    private EnumMap<TIPPSectionType, TIPPSection> sections = new EnumMap<>(TIPPSectionType.class);
    private Map<TIPPFile, String> locationMap = new HashMap<>();

    Manifest(String packageId, TIPPCreator creator, TIPPTask task, boolean isRequest,
             EnumMap<TIPPSectionType, TIPPSection> sections,
             Map<TIPPFile, String> locationMap) {
        this.task = task;
        this.isRequest = isRequest;
        this.packageId = packageId;
        this.creator = creator;
        this.sections = sections;
        this.locationMap = locationMap;
    }

    boolean isRequest() {
        return isRequest;
    }

    TIPPTask getTask() {
        return task;
    }

    String getPackageId() {
        return packageId;
    }

    TIPPCreator getCreator() {
        return creator;
    }

    boolean hasSection(TIPPSectionType type) {
        return sections.containsKey(type);
    }

    String getLocationForFile(TIPPFile file) {
        return locationMap.get(file);
    }

    /**
     * Return the object section for a given type.  
     * @param type section type
     * @return object section for the specified section type, or
     *         null if no section with that type exists in the TIPP
     */
    TIPPSection getSection(TIPPSectionType type) {
        return sections.get(type);
    }

    /**
     * Return a collection of all non-empty sections.
     * @return (possibly empty) collection of sections that each contain at least one resource
     */
    Collection<TIPPSection> getSections() {
        return sections.values();
    }

    TIPPReferenceSection getReferenceSection() {
        return (TIPPReferenceSection)sections.get(TIPPSectionType.REFERENCE);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Manifest)) return false;
        Manifest m = (Manifest)o;
        return Objects.equals(getPackageId(), m.getPackageId()) &&
               Objects.equals(getCreator(), m.getCreator()) &&
               Objects.equals(getTask(), m.getTask()) &&
               Objects.equals(getSections(), m.getSections());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPackageId(), getCreator(),
                            getTask(), getSections());
    }

    @Override
    public String toString() {
        return "PManifest(id=" + getPackageId() + ", creator=" + getCreator()
                + ", task=" + getTask() + ", sections=" + getSections() 
                + ")";
    }
}