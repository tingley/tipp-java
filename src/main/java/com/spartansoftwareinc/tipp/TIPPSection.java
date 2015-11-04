package com.spartansoftwareinc.tipp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TIPPSection {
    private TIPPSectionType type;
    private List<TIPPFile> resources;

    public TIPPSection(TIPPSectionType type, List<TIPPFile> resources) {
        this.type = type;
        Collections.sort(resources, SEQUENCE_COMPARATOR);
        this.resources = Collections.unmodifiableList(resources);
    }

    public TIPPSectionType getType() {
        return type;
    }

    /**
     * Returns the section contents as a list, ordered by 
     * sequence number.  The contents of this list can't be modified.
     * @return list of contents.
     */
    public List<? extends TIPPFile> getFileResources() {
        return resources;
    }

    static final SequenceComparator SEQUENCE_COMPARATOR = new SequenceComparator();
    static class SequenceComparator implements Comparator<TIPPFile> {
        public int compare(TIPPFile r1, TIPPFile r2) {
            return Integer.compare(r1.getSequence(), r2.getSequence());
        }
    }

    @Override
    public String toString() {
        return "TIPPSection(" + type + ", " + resources.size() + " resources)";
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resources);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPSection)) return false;
        TIPPSection s = (TIPPSection)o;
        return type == s.type && Objects.equals(resources, s.resources);
    }
}