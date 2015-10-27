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
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.spartansoftwareinc.tipp.TIPPReferenceFile.LanguageChoice;

class ManifestLoader {
    static final String XMLDSIG_SCHEMA_URI = 
            "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
    static final String XMLDSIG_NS_PREFIX =
            "http://www.w3.org/2000/09/xmldsig#";
    
    private TIPPErrorHandler errorHandler;

    Manifest loadFromStream(InputStream manifestStream, TIPPErrorHandler errorHandler)
            throws IOException {
        return loadFromStream(manifestStream, errorHandler, null, null);
    }

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
            Manifest manifest = loadManifest(document);
            // Extra validation
            validateManifest(manifest);
            return manifest;
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }    

    private void validateManifest(Manifest manifest) {
        TIPPTaskType taskType = manifest.getTask().getTaskType();
        for (TIPPSection section : manifest.getSections()) {
            checkSectionForDuplicateSequence(section);
            // Not covered by schema: make sure we don't get an unexpected section for
            // this task.  For custom task types, this is a no-op.
            if (taskType != null && !(taskType instanceof CustomTaskType) &&
                !taskType.getSupportedSectionTypes().contains(section.getType())) {
                    errorHandler.reportError(TIPPErrorType.INVALID_SECTION_FOR_TASK, 
                            "Invalid section for task type: " + section.getType(), null);
            }
        }
    }

    private void checkSectionForDuplicateSequence(TIPPSection section) {
        Set<Integer> seen = new HashSet<Integer>();
        for (TIPPResource r : section.getResources()) {
            if (seen.contains(r.getSequence())) {
                errorHandler.reportError(DUPLICATE_RESOURCE_SEQUENCE_IN_MANIFEST,
                        "Duplicate sequence number in " + section.getType().getElementName() +
                        ": " + r.getSequence(), null);
            }
            seen.add(r.getSequence());
        }
    }

    private Manifest loadManifest(Document document) {
        ManifestBuilder builder = new ManifestBuilder();
        Element manifestEl = getFirstChildElement(document);
        loadDescriptor(builder, getFirstChildByName(manifestEl, GLOBAL_DESCRIPTOR));
        // Either load the request or the response, depending on which is
        // present
        loadTaskRequestOrResponse(builder, manifestEl);
        
        loadPackageObjects(builder, getFirstChildByName(manifestEl, PACKAGE_OBJECTS));
        return builder.build();
    }

    private void loadDescriptor(ManifestBuilder builder, Element descriptor) {
        builder.setPackageId(getChildTextByName(descriptor, UNIQUE_PACKAGE_ID));
        builder.setCreator(loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR)));
    }

    private TIPPCreator loadCreator(Element creatorEl) {
        TIPPCreator creator = new TIPPCreator(
                getChildTextByName(creatorEl, Creator.NAME),
                getChildTextByName(creatorEl, Creator.ID),
                loadDate(getFirstChildByName(creatorEl, Creator.UPDATE)),
                loadTool(getFirstChildByName(creatorEl, TOOL)));
        return creator;
    }

    private void loadTaskRequestOrResponse(ManifestBuilder builder, Element descriptor) {
        Element requestEl = getFirstChildByName(descriptor, TASK_REQUEST);
        if (requestEl != null) {
            loadTaskRequest(builder, requestEl);
        }
        else {
            loadTaskResponse(builder, getFirstChildByName(descriptor, TASK_RESPONSE));
        }
    }

    private void loadTask(ManifestBuilder builder, Element taskEl) {
        builder.setSourceLocale(getChildTextByName(taskEl, Task.SOURCE_LANGUAGE));
        builder.setTargetLocale(getChildTextByName(taskEl, Task.TARGET_LANGUAGE));
        builder.setTaskType(getChildTextByName(taskEl, Task.TYPE));
    }

    private void loadTaskRequest(ManifestBuilder builder, Element requestEl) {
        loadTask(builder, requestEl);
        builder.setIsRequest(true);
    }

    private void loadTaskResponse(ManifestBuilder builder, Element responseEl) {
        loadTask(builder, responseEl);
        builder.setIsRequest(false);
        Element inResponseTo = getFirstChildByName(responseEl, 
                                TaskResponse.IN_RESPONSE_TO);
        builder.setRequestPackageId(getChildTextByName(inResponseTo, UNIQUE_PACKAGE_ID));
        builder.setRequestCreator(loadCreator(getFirstChildByName(inResponseTo, PACKAGE_CREATOR)));
        builder.setComment(getChildTextByName(responseEl, TaskResponse.COMMENT));
        String rawMessage = getChildTextByName(responseEl, TaskResponse.MESSAGE);
        TIPPResponseCode msg = TIPPResponseCode.valueOf(rawMessage);
        builder.setResponseCode(msg);
    }

    private Date loadDate(Element dateNode) {
        return FormattingUtil.parseTIPPDate(getTextContent(dateNode));
    }

    private TIPPTool loadTool(Element toolEl) {
        TIPPTool tool = new TIPPTool(
                getChildTextByName(toolEl, ContributorTool.NAME),
                getChildTextByName(toolEl, ContributorTool.ID),
                getChildTextByName(toolEl, ContributorTool.VERSION));
        return tool;
    }

    private void loadPackageObjects(ManifestBuilder builder, Element parent) {
        NodeList children = parent.getChildNodes();
        EnumSet<TIPPSectionType> seenSections = EnumSet.noneOf(TIPPSectionType.class);
        // parse all the sections
        Map<TIPPFile, String> locationMap = new HashMap<>();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            TIPPSection section = loadPackageObjectSection((Element)children.item(i), errorHandler, locationMap);
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
            builder.addSection(section);
        }
        builder.setLocationMap(locationMap);
    }

    static class Sequences {
        private int nextSequence = 1;
        private Set<Integer> seenSequences = new HashSet<>();

        int see(int sequence) {
            seenSequences.add(sequence);
            return sequence;
        }
        int nextSequence() {
            while (true) {
                int i = nextSequence++;
                if (!seenSequences.contains(i)) {
                    return see(i);
                }
            }
        }
    }

    private TIPPSection loadPackageObjectSection(Element section,
            TIPPErrorHandler errorHandler, Map<TIPPFile, String> locationMap) {
        TIPPSectionType type = 
                TIPPSectionType.byElementName(section.getNodeName());
        if (type == null) {
            throw new IllegalStateException("Invalid section element"); // Should never happen
        }
        List<TIPPResource> resources = new ArrayList<>();
        Sequences sequences = new Sequences();
        if (type.equals(TIPPSectionType.REFERENCE)) {
            NodeList children = section.getElementsByTagName(REFERENCE_FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                resources.add(loadReferenceFileResource((Element)children.item(i), type, locationMap, sequences));
            }
        }
        else {
            NodeList children = section.getElementsByTagName(FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                resources.add(loadFileResource((Element)children.item(i), type, locationMap, sequences));
            }
        }
        return type == TIPPSectionType.REFERENCE ? new TIPPReferenceSection(resources) :
                new TIPPSection(type, resources);
    }

    private int getSequence(Element el, Sequences sequences) {
        // The schema should enforce that this is an integer > 0
        Integer sequence = FormattingUtil.parseInt(el.getAttribute(ObjectFile.ATTR_SEQUENCE));
        return (sequence == null) ? sequence = sequences.nextSequence() : sequences.see(sequence);
    }

    private TIPPReferenceFile loadReferenceFileResource(Element el, TIPPSectionType sectionType,
                    Map<TIPPFile, String> locationMap, Sequences sequences) {
        int sequence = getSequence(el, sequences);
        String location = getChildTextByName(el, ObjectFile.LOCATION);
        if (!FormattingUtil.validLocationString(sectionType, location)) {
            errorHandler.reportError(INVALID_RESOURCE_LOCATION_IN_MANIFEST,
                            "Invalid location: " + location, null);
        }
        String name = getChildTextByName(el, ObjectFile.NAME);
        if (name == null) name = location;
        LanguageChoice lc = null;
        if (el.hasAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE)) {
            lc = TIPPReferenceFile.LanguageChoice.valueOf(
                            el.getAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE));
        }
        TIPPReferenceFile file = new TIPPReferenceFile(sectionType, name, sequence, lc);
        locationMap.put(file, location);
        return file;
    }

    private TIPPFile loadFileResource(Element el, TIPPSectionType sectionType, Map<TIPPFile, String> locationMap,
                                      Sequences sequences) {  
        int sequence = getSequence(el, sequences);
        String location = getChildTextByName(el, ObjectFile.LOCATION);
        if (!FormattingUtil.validLocationString(sectionType, location)) {
            errorHandler.reportError(INVALID_RESOURCE_LOCATION_IN_MANIFEST,
                            "Invalid location: " + location, null);
        }
        String name = getChildTextByName(el, ObjectFile.NAME);
        if (name == null) name = location;
        TIPPFile file = new TIPPFile(sectionType, name, sequence);
        locationMap.put(file, location);
        return file;
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
