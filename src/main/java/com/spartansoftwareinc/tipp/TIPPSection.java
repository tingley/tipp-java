package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.ATTR_SECTION_NAME;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
    private BitSet sequenceNumbers = new BitSet(8);
    private int nextAutomaticLocationIndex = 1;

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
        sequenceNumbers.clear();
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
                sequenceNumbers.clear(removed.getSequence());
                // If the section is empty, reset everything
                if (resources.size() == 0) {
                    clear();
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
        if (TIPPFormattingUtil.validLocationString(this, name)) {
            return name;
        }
        // Name needs normalization.  We do this by a pretty dumb mechanism - 
        // keep the suffix/filetype, but just use incrementing integers to 
        // normalize the paths.
        String suffix = "";
        int suffixStart = name.lastIndexOf('.');
        if (suffixStart != -1) {
            suffix = name.substring(suffixStart);
        }
        for (int i = nextAutomaticLocationIndex; i < Integer.MAX_VALUE; i++) {
            String proposal = "" + i + suffix;
            if (locationIsAvailable(proposal)) {
                nextAutomaticLocationIndex = i + 1;
                return proposal;
            }
        }
        // This should only happen if the package already somehow contains
        // numbered files up to MAX_VALUE
        throw new IllegalStateException();
    }

    protected boolean locationIsAvailable(String location) {
        // XXX linear for now until I see if something better is needed
        for (TIPPFile f : getResources()) {
            if (f.getLocation().equalsIgnoreCase(location)) {
                return false;
            }
        }
        return true;
    }

    protected TIPPFile createFile(String name, String location, int sequence) {
        return new TIPPFile(location, name, sequence);
    }
    
    /**
     * Add a TIPPFile with the specified name to this section. A 
     * sequence number will be allocated automatically.
     * @param name
     * @return newly created TIPPFile
     */
    public TIPPFile addFile(String name) {
        return _addFile(createFile(name, getLocationForName(name), getNextSequence()));
    }

    /**
     * Add a TIPPFile with the specified name and sequence number to
     * this section.
     * @param name
     * @param sequence
     * @return newly created TIPPFile
     * @throws IllegalArgumentException if the specified sequence is already
     *         in use 
     */
    public TIPPFile addFile(String name, int sequence) {
        return _addFile(createFile(name,  getLocationForName(name), sequence));
    }

    /**
     * This method assumes that the sequence has already been set to 
     * a valid value.
     * @param file
     */
    void addFile(TIPPFile file) {
        _addFile(file);
    }

    /**
     * Return true if the specified sequence number is in use. 
     */
    boolean checkSequence(int sequence) {
        return sequenceNumbers.get(sequence);
    }
    
    private TIPPFile _addFile(TIPPFile file) {
        if (file.getSequence() < 1) {
            throw new IllegalArgumentException("Invalid sequence number: " + file.getSequence());
        }
        if (sequenceNumbers.get(file.getSequence())) {
            throw new IllegalArgumentException("Sequence number already in use for section " +
                type + ": " + file.getSequence());
        }
        sequenceNumbers.set(file.getSequence());
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
        return Objects.equals(type,  s.type) &&
               Objects.equals(resources, s.resources);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, resources);
    }

    static final SequenceComparator SEQUENCE_COMPARATOR = new SequenceComparator();
    static class SequenceComparator implements Comparator<TIPPResource> {
        public int compare(TIPPResource r1, TIPPResource r2) {
            return Integer.valueOf(r1.getSequence()).compareTo(r2.getSequence());
        }
    }
}
