package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.COMMON_SCHEMA_LOCATION;
import static com.spartansoftwareinc.tipp.TIPPConstants.FILE_RESOURCE;
import static com.spartansoftwareinc.tipp.TIPPConstants.GLOBAL_DESCRIPTOR;
import static com.spartansoftwareinc.tipp.TIPPConstants.PACKAGE_CREATOR;
import static com.spartansoftwareinc.tipp.TIPPConstants.PACKAGE_OBJECTS;
import static com.spartansoftwareinc.tipp.TIPPConstants.REFERENCE_FILE_RESOURCE;
import static com.spartansoftwareinc.tipp.TIPPConstants.TASK_REQUEST;
import static com.spartansoftwareinc.tipp.TIPPConstants.TASK_RESPONSE;
import static com.spartansoftwareinc.tipp.TIPPConstants.TOOL;
import static com.spartansoftwareinc.tipp.TIPPConstants.UNIQUE_PACKAGE_ID;
import static com.spartansoftwareinc.tipp.TIPPErrorType.DUPLICATE_RESOURCE_SEQUENCE_IN_MANIFEST;
import static com.spartansoftwareinc.tipp.TIPPErrorType.INVALID_RESOURCE_LOCATION_IN_MANIFEST;
import static com.spartansoftwareinc.tipp.TIPPErrorType.INVALID_SIGNATURE;
import static com.spartansoftwareinc.tipp.TIPPErrorType.UNABLE_TO_VERIFY_SIGNATURE;
import static com.spartansoftwareinc.tipp.XMLUtil.getChildTextByName;
import static com.spartansoftwareinc.tipp.XMLUtil.getFirstChildByName;
import static com.spartansoftwareinc.tipp.XMLUtil.getFirstChildElement;
import static com.spartansoftwareinc.tipp.XMLUtil.getTextContent;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.EnumSet;

import javax.xml.crypto.KeySelector;
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
import com.spartansoftwareinc.tipp.TIPPConstants.Task;
import com.spartansoftwareinc.tipp.TIPPConstants.TaskResponse;

class ManifestLoader {
    static final String XMLDSIG_SCHEMA_URI = 
            "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
    static final String XMLDSIG_NS_PREFIX =
            "http://www.w3.org/2000/09/xmldsig#";
    
    private TIPPErrorHandler errorHandler;
    private Manifest manifest = new Manifest(null);

    Manifest loadFromStream(InputStream manifestStream, TIPPErrorHandler errorHandler)
            throws IOException {
        return loadFromStream(manifestStream, errorHandler, null, null);
    }

    // XXX This should blow away any existing settings 
    Manifest loadFromStream(InputStream manifestStream, TIPPErrorHandler errorHandler,
                           KeySelector keySelector, InputStream payloadStream) 
                throws IOException {
        if (manifestStream == null) {
            errorHandler.reportError(TIPPErrorType.MISSING_MANIFEST, 
                    "Package contained no manifest", null);
            return null;
        }
        this.errorHandler = errorHandler;
        try {
            Document document = parse(manifestStream);
            if (document == null) {
                return null;
            }
            // Validate the schema
            if (!validate(document)) {
                return null;
            }
            // Validate the XML Signature if we are given a key
            if (!validateSignature(document, keySelector, payloadStream)) {
                return null;
            }
            loadManifest(document);
            return manifest;
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }    
    
    // TODO how does this stuff get into the Manifest

    private void loadManifest(Document document) {
        Element manifestEl = getFirstChildElement(document);
        loadDescriptor(getFirstChildByName(manifestEl, GLOBAL_DESCRIPTOR));
        // Either load the request or the response, depending on which is
        // present
        manifest.setTask(loadTaskRequestOrResponse(manifestEl));
        
        loadPackageObjects(getFirstChildByName(manifestEl, PACKAGE_OBJECTS));
        
        // Perform additional validation that isn't covered by the schema
        TIPPTaskType taskType = manifest.getTaskType();
        if (taskType != null) {
            for (TIPPSection section : manifest.getSections()) {
                if (!taskType.getSupportedSectionTypes().contains(section.getType())) {
                    errorHandler.reportError(TIPPErrorType.INVALID_SECTION_FOR_TASK, 
                            "Invalid section for task type: " + section.getType(), null);
                }
            }
        }
    }
    
    private void loadDescriptor(Element descriptor) {
        manifest.setPackageId(getChildTextByName(descriptor, UNIQUE_PACKAGE_ID));
        manifest.setCreator(loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR)));
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
        manifest.setTaskType(StandardTaskType.forTypeUri(task.getTaskType()));
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
        return FormattingUtil.parseTIPPDate(getTextContent(dateNode));
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
        EnumSet<TIPPSectionType> seenSections = EnumSet.noneOf(TIPPSectionType.class);
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
            if (seenSections.contains(section.getType())) {
                errorHandler.reportError(TIPPErrorType.DUPLICATE_SECTION_IN_MANIFEST, 
                        "Duplicate section: " + section.getType(), null);
                continue;
            }
            seenSections.add(section.getType());
            manifest.addSection(section);
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
        if (!FormattingUtil.validLocationString(section, location)) {
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
}
