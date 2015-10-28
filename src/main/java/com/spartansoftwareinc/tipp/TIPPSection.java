package com.spartansoftwareinc.tipp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TIPPSection {
    private TIPPSectionType type;
    private List<TIPPResource> resources;

    public TIPPSection(TIPPSectionType type, List<TIPPResource> resources) {
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
    public List<? extends TIPPResource> getResources() {
        return resources;
    }

    static final SequenceComparator SEQUENCE_COMPARATOR = new SequenceComparator();
    static class SequenceComparator implements Comparator<TIPPResource> {
        public int compare(TIPPResource r1, TIPPResource r2) {
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