package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.FILE_RESOURCE;
import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.ObjectFile;

public class TIPPFile extends TIPPResource {
    private String location;

    TIPPFile() { }

    /**
     * Constructor where name and location are the same.
     * @param location
     */

    TIPPFile(String location, int sequence) {
        super(location, sequence);
        this.location = location;
    }
    
    TIPPFile(String location, String name, int sequence) {
        this(name, sequence);
        this.location = location;
    }
    
    @Override
    public String getName() {
        return super.getName() != null ? super.getName() : getLocation(); 
    }
        
    @Override
    public BufferedInputStream getInputStream() throws IOException {
        return getPackage().getPackageObjectInputStream(getCanonicalObjectPath());
    }

    @Override
    public BufferedOutputStream getOutputStream() throws IOException {
        return getPackage().getPackageObjectOutputStream(getCanonicalObjectPath());
    }

    public String getCanonicalObjectPath() {
        return getSection().getType().getDefaultName() + PackageSource.SEPARATOR + location;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    Element toElement(Document doc) {
        return addChildren(doc, doc.createElement(FILE_RESOURCE));
    }
    
    @Override
    protected Element addChildren(Document doc, Element resourceElement) {
        super.addChildren(doc, resourceElement);
        appendElementChildWithText(doc, resourceElement, ObjectFile.LOCATION,
                                   getLocation());
        return resourceElement;
    }

    @Override
    public String toString() {
        return getName() + "(" + location + ", " + getSequence() + ")";
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + location.hashCode(); 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TIPPFile)) return false;
        TIPPFile f = (TIPPFile)o;
        return super.equals(o) && f.getLocation().equals(getLocation()); 
    }
}
