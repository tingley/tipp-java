package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.ATTR_SECTION_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a TIPP section.  Sections are identified by
 * type.  A section contains one or more resources of the specified type.
 */
public class TIPPSection {
    private static int INITIAL_SEQUENCE = 1;
    private PackageBase tipp;
    private TIPPSectionType type;
    private List<TIPPFile> resources = new ArrayList<TIPPFile>();
    private boolean sorted = false;
    private int nextSequence = INITIAL_SEQUENCE;

    TIPPSection() { }

    TIPPSection(TIPPSectionType type) {
        this.type = type;
    }

    public TIPPSectionType getType() {
        return type;
    }
    
    void setPackage(PackageBase tip) {
        this.tipp = tip;
    }
    
    TIPP getPackage() {
        return tipp;
    }
    
    void setType(TIPPSectionType type) {
        this.type = type;
    }
    
    /**
     * Remove all resources in this section.
     */
    public void clear() {
        resources.clear();
        sorted = true;
        nextSequence = INITIAL_SEQUENCE;
    }
    
    /**
     * Returns the section contents as a list, ordered by 
     * sequence number.  The contents of this list can't be modified.
     * @return list of contents.
     */
    public List<? extends TIPPFile> getResources() {
        if (!sorted) {
            Collections.sort(resources, SEQUENCE_COMPARATOR);
            sorted = true;
        }
        return Collections.unmodifiableList(resources);
    }

    /**
     * Remove from this section the resource with the specified 
     * name.
     * @param name name of the resource to remove
     * @return removed resource, or null if no resource was found with that name
     */
    public TIPPResource removeResource(String name) {
        for (int i = 0; i < resources.size(); i++) {
            TIPPResource r = resources.get(i);
            if (r.getName().equals(name)) {
                sorted = false;
                TIPPResource removed = resources.remove(i);
                // If the section is empty, restart sequence allocation
                if (resources.size() == 0) {
                    nextSequence = INITIAL_SEQUENCE;
                }
                return removed;
            }
        }
        return null;
    }
    
    protected int getNextSequence() {
        return nextSequence++;
    }
    
    protected String getLocationForName(String name) {
        return name; // no-op for now
    }
    
    protected TIPPFile createFile(String name, int sequence) {
        return new TIPPFile(name, getLocationForName(name), sequence);
    }
    
    public TIPPFile addFile(String name) {
        return _addFile(createFile(name, getNextSequence()));
    }
    
    public TIPPFile addFile(String name, int sequence) {
        return _addFile(createFile(name,  sequence));
    }

    /**
     * This method assumes that the sequence has already been set to 
     * a valid value.
     * @param file
     */
    void addFile(TIPPFile file) {
        _addFile(file);
    }
    
    private TIPPFile _addFile(TIPPFile file) {
        sorted = false;
        resources.add(file);
        file.setPackage(tipp);
        file.setSection(this);
        if (file.getSequence() >= nextSequence) {
            nextSequence = file.getSequence() + 1;
        }
        return file;
    }

    Element toElement(Document doc) {
        Element sectionEl = doc.createElement(getType().getElementName());
        sectionEl.setAttribute(ATTR_SECTION_NAME, getType().getElementName());
        for (TIPPFile file : getResources()) {
            sectionEl.appendChild(file.toElement(doc));
        }
        return sectionEl;
    }

    @Override
    public String toString() {
        return type.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPSection)) {
            return false;
        }
        TIPPSection s = (TIPPSection)o;
        return type.equals(s.getType()) &&
                resources.equals(s.getResources());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + resources.hashCode();
        return result;
    }

    static final SequenceComparator SEQUENCE_COMPARATOR = new SequenceComparator();
    static class SequenceComparator implements Comparator<TIPPResource> {
        public int compare(TIPPResource r1, TIPPResource r2) {
            return Integer.valueOf(r1.getSequence()).compareTo(r2.getSequence());
        }
    }
}
