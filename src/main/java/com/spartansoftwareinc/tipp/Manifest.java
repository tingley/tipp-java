package com.spartansoftwareinc.tipp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * This class is unfortunately contains both the representation
 * and the logic to assemble it from XML.
 * @author chase
 *
 */
class Manifest {
    private TIPPTaskType taskType;

    private PackageBase tipPackage;
    private String packageId;
    private TIPPTask task; // Either request or response
    private TIPPCreator creator = new TIPPCreator();
    
    private EnumMap<TIPPSectionType, TIPPSection> sections = 
            new EnumMap<TIPPSectionType, TIPPSection>(TIPPSectionType.class);
    
    
    Manifest(PackageBase tipPackage) {
        this.tipPackage = tipPackage;
    }

    static Manifest newManifest(PackageBase tipPackage) {
        Manifest manifest = new Manifest(tipPackage);
        manifest.setPackageId("urn:uuid:" + UUID.randomUUID().toString());
        return manifest;
    }
    
    static Manifest newRequestManifest(PackageBase tipPackage, TIPPTaskType type) {
    	Manifest manifest = newManifest(tipPackage);
    	TIPPTaskRequest request = new TIPPTaskRequest();
    	request.setTaskType(type.getType());
    	manifest.setTaskType(type);
    	manifest.setTask(request);
    	return manifest;
    }
    
    static Manifest newResponseManifest(PackageBase tipPackage, TIPPTaskType type) {
    	Manifest manifest = newManifest(tipPackage);
    	TIPPTaskResponse response = new TIPPTaskResponse();
    	response.setTaskType(type.getType());
    	manifest.setTaskType(type);
    	manifest.setTask(response);
    	return manifest;
    }
    
    static Manifest newResponseManifest(ResponsePackageBase tipPackage, 
    									   TIPP requestPackage) {
    	if (!requestPackage.isRequest()) {
    		throw new IllegalArgumentException(
    				"Can't construct a response to a response package");
    	}
    	Manifest manifest = newManifest(tipPackage);
    	// Copy all the fields over.  Tedious.
    	TIPPTaskResponse response = new TIPPTaskResponse();
    	response.setRequestCreator(requestPackage.getCreator());
    	response.setRequestPackageId(requestPackage.getPackageId());
    	response.setTaskType(requestPackage.getTaskType());
    	response.setSourceLocale(requestPackage.getSourceLocale());
    	response.setTargetLocale(requestPackage.getTargetLocale());
    	manifest.setTask(response);
    	// If it's a standard type, assign that as well.
    	manifest.setTaskType(
    			StandardTaskType.forTypeUri(requestPackage.getTaskType()));
    	return manifest;
    }
    
    public void setTaskType(TIPPTaskType type) {
    	this.taskType = type;
    }
    
    public TIPPTaskType getTaskType() {
    	return taskType;
    }
    
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
    
    public TIPPCreator getCreator() {
        return creator;
    }

    public void setCreator(TIPPCreator creator) {
        this.creator = creator;
    }

    TIPPTask getTask() {
        return task;
    }
    
    void setTask(TIPPTask task) {
        this.task = task;
    }
    
    public boolean isRequest() {
        return (task instanceof TIPPTaskRequest);
    }

    /**
     * Return the object section for a given type.  
     * @param type section type
     * @return object section for the specified section type, or
     *         null if no section with that type exists in the TIPP
     */
    public TIPPSection getSection(TIPPSectionType type) {
        TIPPSection s = sections.get(type);
        if (s == null) {
            s = createSection(type);
            sections.put(type, s);
            s.setPackage(tipPackage);
        }
        return s;
    }

    void addSection(TIPPSection section) {
        TIPPSectionType type = section.getType();
        if (sections.containsKey(type)) {
            // This should be reported as an error before control gets to this point
            throw new IllegalStateException("Manifest contains multiple sections for type " + type);
        }
        section.setPackage(tipPackage);
        sections.put(type, section);
    }

    /**
     * Return a collection of all non-empty sections.
     * @return (possibly empty) collection of sections that each contain at least one resource
     */
    public Collection<TIPPSection> getSections() {
        List<TIPPSection> s = new ArrayList<TIPPSection>();
        for (TIPPSection section : sections.values()) {
            if (!section.getResources().isEmpty()) {
                s.add(section);
            }
        }
        return s;
    }
    
    public TIPPReferenceSection getReferenceSection() {
        return (TIPPReferenceSection)sections.get(TIPPSectionType.REFERENCE);
    }

    // TODO: Clean this up?
    private TIPPSection createSection(TIPPSectionType type) {
        if (type == TIPPSectionType.REFERENCE) {
            return new TIPPReferenceSection();
        }
        return new TIPPSection(type);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Manifest)) return false;
        Manifest m = (Manifest)o;
        return m.getPackageId().equals(getPackageId()) &&
                m.getCreator().equals(getCreator()) &&
                m.getTask().equals(getTask()) &&
                m.getSections().equals(getSections());
    }
    
    @Override
    public String toString() {
        return "TIPManifest(id=" + getPackageId() + ", creator=" + getCreator()
                + ", task=" + getTask() + ", sections=" + getSections() 
                + ")";
    }

}
