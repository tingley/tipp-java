package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.*;
import static com.spartansoftwareinc.tipp.XMLUtil.*;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.spartansoftwareinc.tipp.TIPPConstants.ContributorTool;
import com.spartansoftwareinc.tipp.TIPPConstants.Creator;
import com.spartansoftwareinc.tipp.TIPPConstants.ObjectFile;
import static com.spartansoftwareinc.tipp.TIPPErrorType.*;

import javax.xml.crypto.KeySelector;

/**
 * This class is unfortunately contains both the representation
 * and the logic to assemble it from XML.
 * @author chase
 *
 */
class Manifest {

    static final String XMLDSIG_SCHEMA_URI = 
            "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
    static final String XMLDSIG_NS_PREFIX =
            "http://www.w3.org/2000/09/xmldsig#";
    
    // Only for construction
	private TIPPTaskType taskType;
	
    private PackageBase tipPackage;
    private String packageId;
    private TIPPTask task; // Either request or response
    private TIPPCreator creator = new TIPPCreator();
    private TIPPErrorHandler errorHandler;
    
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
    
    void setTaskType(TIPPTaskType type) {
    	this.taskType = type;
    }
    
    TIPPTaskType getTaskType() {
    	return taskType;
    }

    boolean loadFromStream(InputStream manifestStream, TIPPErrorHandler errorHandler)
            throws IOException {
        return loadFromStream(manifestStream, errorHandler, null, null);
    }

    // XXX This should blow away any existing settings 
    boolean loadFromStream(InputStream manifestStream, TIPPErrorHandler errorHandler,
                           KeySelector keySelector, InputStream payloadStream) 
                throws IOException {
        if (manifestStream == null) {
            errorHandler.reportError(TIPPErrorType.MISSING_MANIFEST, 
                    "Package contained no manifest", null);
            return false;
        }
        this.errorHandler = errorHandler;
    	try {
	        Document document = parse(manifestStream);
	        if (document == null) {
	            return false;
	        }
	        // Validate the schema
	        if (!validate(document)) {
	            return false;
	        }
	        // Validate the XML Signature if we are given a key
            if (!validateSignature(document, keySelector, payloadStream)) {
                return false;
            }
	        loadManifest(document);
	        return true;
    	}
    	catch (ParserConfigurationException e) {
    		throw new RuntimeException(e);
    	}
    }    
    
    private void loadManifest(Document document) {
        Element manifest = getFirstChildElement(document);
        loadDescriptor(getFirstChildByName(manifest, GLOBAL_DESCRIPTOR));
        // Either load the request or the response, depending on which is
        // present
        task = loadTaskRequestOrResponse(manifest);
        loadPackageObjects(getFirstChildByName(manifest, PACKAGE_OBJECTS));
        
        // Perform additional validation that isn't covered by the schema
        TIPPTaskType taskType = getTaskType();
        if (taskType != null) {
            for (TIPPSection section : getSections()) {
                if (!taskType.getSupportedSectionTypes().contains(section.getType())) {
                    errorHandler.reportError(TIPPErrorType.INVALID_SECTION_FOR_TASK, 
                            "Invalid section for task type: " + section.getType(), null);
                }
            }
        }
    }
    
    private void loadDescriptor(Element descriptor) {
        packageId = getChildTextByName(descriptor, UNIQUE_PACKAGE_ID);
        
        creator = loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR));
    }
    
    private TIPPCreator loadCreator(Element creatorEl) {
        TIPPCreator creator = new TIPPCreator();
        creator.setName(getChildTextByName(creatorEl, Creator.NAME));
        creator.setId(getChildTextByName(creatorEl, Creator.ID));
        creator.setDate(
            loadDate(getFirstChildByName(creatorEl, Creator.UPDATE)));
        creator.setTool(loadTool(
                getFirstChildByName(creatorEl, TOOL)));
        return creator;
    }
    
    private TIPPTask loadTaskRequestOrResponse(Element descriptor) {
        Element requestEl = getFirstChildByName(descriptor, TASK_REQUEST);
        if (requestEl != null) {
            return loadTaskRequest(requestEl);
        }
        return loadTaskResponse(getFirstChildByName(descriptor, 
                                 TASK_RESPONSE));
    }
    
    private void loadTask(Element taskEl, TIPPTask task) {
        task.setTaskType(getChildTextByName(taskEl, Task.TYPE));
        task.setSourceLocale(getChildTextByName(taskEl, Task.SOURCE_LANGUAGE));
        task.setTargetLocale(getChildTextByName(taskEl, Task.TARGET_LANGUAGE));
        setTaskType(StandardTaskType.forTypeUri(task.getTaskType()));
    }
    
    private TIPPTaskRequest loadTaskRequest(Element requestEl) {
        TIPPTaskRequest request = new TIPPTaskRequest();
        loadTask(requestEl, request);
        return request;
    }
    
    private TIPPTaskResponse loadTaskResponse(Element responseEl) {
        TIPPTaskResponse response = new TIPPTaskResponse();
        loadTask(responseEl, response);
        Element inResponseTo = getFirstChildByName(responseEl, 
                                TaskResponse.IN_RESPONSE_TO);
        response.setRequestPackageId(getChildTextByName(inResponseTo,
                                                 UNIQUE_PACKAGE_ID));
        response.setRequestCreator(loadCreator(
                getFirstChildByName(inResponseTo, PACKAGE_CREATOR)));
        response.setComment(getChildTextByName(responseEl, 
                            TaskResponse.COMMENT));
        String rawMessage = getChildTextByName(responseEl, 
                            TaskResponse.MESSAGE);
        TIPPResponseCode msg = TIPPResponseCode.valueOf(rawMessage);
        response.setMessage(msg);
        return response;
    }
    
    private Date loadDate(Element dateNode) {
        return TIPPFormattingUtil.parseTIPPDate(getTextContent(dateNode));
    }
    
    private TIPPTool loadTool(Element toolEl) {
        TIPPTool tool = new TIPPTool();
        tool.setName(getChildTextByName(toolEl, ContributorTool.NAME));
        tool.setId(getChildTextByName(toolEl, ContributorTool.ID));
        tool.setVersion(getChildTextByName(toolEl, ContributorTool.VERSION));
        return tool;
    }
    
    private void loadPackageObjects(Element parent) {
        NodeList children = parent.getChildNodes();
        // parse all the sections
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            TIPPSection section = 
                loadPackageObjectSection((Element)children.item(i), errorHandler);
            if (section == null) {
                continue;
            }
            // Don't allow duplicate sections
            if (sections.containsKey(section.getType())) {
                errorHandler.reportError(TIPPErrorType.DUPLICATE_SECTION_IN_MANIFEST, 
                        "Duplicate section: " + section.getType(), null);
                continue;
            }
            sections.put(section.getType(), section);
        }
    }

    private TIPPSection loadPackageObjectSection(Element section,
            TIPPErrorHandler errorHandler) {
        TIPPSectionType type = 
                TIPPSectionType.byElementName(section.getNodeName());
        if (type == null) {
            throw new IllegalStateException("Invalid section element"); // Should never happen
        }
        // XXX Too much duplicated code in these two clauses, needs a refactor
        if (type.equals(TIPPSectionType.REFERENCE)) {
            TIPPReferenceSection refSection = new TIPPReferenceSection();
            NodeList children = section.getElementsByTagName(REFERENCE_FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                TIPPReferenceFile file = loadReferenceFile((Element)children.item(i),
                                            refSection);
                addFileToSection(refSection, file);
            }
            return refSection;
        }
        else {
            TIPPSection objSection = new TIPPSection(type);
            objSection.setPackage(tipPackage);
            NodeList children = section.getElementsByTagName(FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                TIPPFile file = loadFile((Element)children.item(i), objSection);
                addFileToSection(objSection, file);
            }
            return objSection;
        }
    }

    private void addFileToSection(TIPPSection section, TIPPFile file) {
        // For now, we will require sequence numbers to be present.  The spec
        // implies this, but the schema doesn't require it!  However, that issue
        // is caught elsewhere, so this code just needs to not crash.
        if (file.sequenceIsSet() && section.checkSequence(file.getSequence())) {
            errorHandler.reportError(DUPLICATE_RESOURCE_SEQUENCE_IN_MANIFEST,
                    "Duplicate sequence number in " + section.getType().getElementName() +
                    ": " + file.getSequence(), null);
        }
        else {
            section.addFile(file);
        }
    }

    private TIPPReferenceFile loadReferenceFile(Element file,
                            TIPPSection section) {
        TIPPReferenceFile object = new TIPPReferenceFile();
        loadFileResource(object, file, section);
        if (file.hasAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE)) {
            object.setLanguageChoice( 
                    TIPPReferenceFile.LanguageChoice.valueOf(
                            file.getAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE)));
        }
        return object;
    }
    
    private TIPPFile loadFile(Element file, TIPPSection section) {
        TIPPFile object = new TIPPFile();
        loadFileResource(object, file, section);
        return object;
    }
    
    private void loadFileResource(TIPPFile object, Element file,
                                  TIPPSection section) {  
        object.setPackage(tipPackage);
        String rawSequence = file.getAttribute(ObjectFile.ATTR_SEQUENCE);
        try {
            // The schema will enforce that this is an integer > 0
            object.setSequence(Integer.parseInt(rawSequence));
        }
        catch (NumberFormatException e) {
            // This should be caught by validation
        }
        String location = getChildTextByName(file, ObjectFile.LOCATION);
        object.setLocation(location);
        if (!TIPPFormattingUtil.validLocationString(section, location)) {
            errorHandler.reportError(INVALID_RESOURCE_LOCATION_IN_MANIFEST,
                            "Invalid location: " + location, null);
        }
        String name = getChildTextByName(file, ObjectFile.NAME);
        object.setName((name == null) ? object.getLocation() : name);
    }
    
    Document parse(InputStream is) throws ParserConfigurationException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(is);
        }
        catch (Exception e) {
            errorHandler.reportError(TIPPErrorType.CORRUPT_MANIFEST, "Could not parse manifest", e);
            return null;
        }
        finally {
            is.close();
        }
    }
    
    boolean validate(final Document dom) {
        try {
            InputStream is = 
                getClass().getResourceAsStream("/TIPPManifest-1_5.xsd");
            SchemaFactory factory = 
                SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(new LSResourceResolver() {
                public LSInput resolveResource(String type, String namespaceURI, 
                        String publicId, String systemId, String baseURI)  {
                    LSInput input = ((DOMImplementationLS)dom
                            .getImplementation()).createLSInput();
                    if (("TIPPCommon.xsd".equals(systemId) && W3C_XML_SCHEMA_NS_URI.equals(type)) ||
                         COMMON_SCHEMA_LOCATION.equalsIgnoreCase(baseURI)) {
                        input.setByteStream(getClass().getResourceAsStream("/TIPPCommon-1_5.xsd"));
                    }
                    else if (XMLDSIG_SCHEMA_URI.equalsIgnoreCase(baseURI) ||
                        XMLDSIG_NS_PREFIX.equalsIgnoreCase(namespaceURI)) {
                        input.setByteStream(getClass().getResourceAsStream("/xmldsig-core-schema.xsd"));
                    }
                    else if ("http://www.w3.org/2001/XMLSchema.dtd".equals(baseURI) ||
                             "http://www.w3.org/2001/XMLSchema.dtd".equals(systemId)) {
                        input.setByteStream(getClass().getResourceAsStream("/XMLSchema.dtd"));
                    }
                    else if ("datatypes.dtd".equals(systemId)) {
                        input.setByteStream(getClass().getResourceAsStream("/datatypes.dtd"));
                    }
                    else {
                        return null;
                    }
                    return input;
                }
            });
            Schema schema = factory.newSchema(new StreamSource(is));
            // need an errorHandler.reportError handler
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(dom));
            is.close();
            return true;
        }
        catch (Exception e) {
            errorHandler.reportError(TIPPErrorType.INVALID_MANIFEST, "Invalid manifest", e);
            return false;
        }
    }

    boolean validateSignature(final Document doc,
                           KeySelector keySelector,
                           InputStream payloadStream) {
        ManifestSigner signer = new ManifestSigner();
        if (signer.hasSignature(doc)) {
            if (keySelector != null) {
                if (!signer.validateSignature(doc, keySelector,
                            payloadStream)) {
                    errorHandler.reportError(INVALID_SIGNATURE, 
                            "Invalid digital signature", null);
                    return false;
                }
            }
            else {
                // The manifest has a signature, but we're not able to 
                // validate it because no key was provided by the user.
                errorHandler.reportError(UNABLE_TO_VERIFY_SIGNATURE,
                        "No key provided to verify digital signature", null);
                return false;
            }
        }
        return true;
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

    public TIPPTask getTask() {
        return task;
    }
    
    public void setTask(TIPPTask task) {
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
