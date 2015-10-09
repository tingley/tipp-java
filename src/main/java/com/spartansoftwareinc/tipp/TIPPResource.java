package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.ObjectFile;

/**
 * Represents a TIPP resource represented as a file.
 */
public abstract class TIPPResource {
    private PackageBase tipPackage;
    private TIPPSection section;

    private String name;
    private int sequence = 1;
    private boolean sequenceSet = false; // yuck
    
    TIPPResource() { }
    
    TIPPResource(String name, int sequence) {
        this.name = name;
        this.sequence = sequence;
        sequenceSet = true;
    }
    
    PackageBase getPackage() {
        return tipPackage;
    }
    
    TIPPSection getSection() {
        return section;
    }
    
    void setSection(TIPPSection section) {
        this.section = section;
    }
    
    public abstract BufferedInputStream getInputStream() throws IOException;

    public abstract BufferedOutputStream getOutputStream() throws IOException;
    
    void setPackage(PackageBase tipPackage) {
        this.tipPackage = tipPackage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean sequenceIsSet() {
        return sequenceSet;
    }
    
    public int getSequence() {
        return sequence;
    }

    /**
     * Set the sequence number for this reset.  Sequence numbers
     * must be non-zero positive integers. 
     * @param sequence new sequence number
     * @throws IllegalArgumentException if an invalid sequence number is
     *         supplied
     */
    public void setSequence(int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Invalid sequence number: " + sequence);
        }
        this.sequence = sequence;
        this.sequenceSet = true;
    }
    
    abstract Element toElement(Document doc);
    
    protected Element addChildren(Document doc, Element resourceElement) {
        resourceElement.setAttribute(ObjectFile.ATTR_SEQUENCE, 
                                     String.valueOf(getSequence()));
        appendElementChildWithText(doc, resourceElement, ObjectFile.NAME,
                                   getName());
        return resourceElement;
    }
    
    @Override
    public String toString() {
        return name + "(" + sequence + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sequence);
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
        return Objects.equals(name, f.name) &&
               Objects.equals(sequence, f.sequence);
    }
}
