package com.spartansoftwareinc.tipp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.spartansoftwareinc.tipp.TIPPConstants.*;
import static com.spartansoftwareinc.tipp.XMLUtil.*;

/**
 * Convert a TIPManifest into a DOM tree.
 */
class ManifestDOMBuilder {

    private Manifest manifest;
    private Document document;
    
    ManifestDOMBuilder(Manifest manifest) {
        this.manifest = manifest;
    }
    
    public static final String TIPP_NAMESPACE = 
            "http://schema.interoperability-now.org/tipp/1_5/";
    
    Document makeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Namespaces are required for xml-dsig
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        document = docBuilder.newDocument();
        Element root = document.createElement(MANIFEST);
        // QUESTIONABLE: I'm disabling writing out the schema location, because
        // a) it is causes havoc with the xml-dsig signing, for some reason, and
        // b) it's only meant to be a hint anyways.
        // root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
        //      "schemaLocation", SCHEMA_LOCATION);
        root.appendChild(makeDescriptor());
        root.appendChild(manifest.getTask().toElement(document));
        root.appendChild(makePackageObjects());
        root.setAttribute("xmlns", TIPP_NAMESPACE);
        root.setAttribute(ATTR_VERSION, SCHEMA_VERSION);
        document.appendChild(root);
        return document;
    }
    
    private Element makeDescriptor() {
        Element descriptor = document.createElement(GLOBAL_DESCRIPTOR);
        appendElementChildWithText(document, 
                descriptor, UNIQUE_PACKAGE_ID, manifest.getPackageId());
        descriptor.appendChild(manifest.getCreator().toElement(document));
        return descriptor;
    }
 
    private Element makePackageObjects() {
        Element objects = document.createElement(PACKAGE_OBJECTS);
        for (TIPPSection section : manifest.getSections()) {
            objects.appendChild(section.toElement(document));
        }
        return objects;
    }
}
