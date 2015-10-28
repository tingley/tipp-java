package com.spartansoftwareinc.tipp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.ContributorTool;
import com.spartansoftwareinc.tipp.TIPPConstants.Creator;
import com.spartansoftwareinc.tipp.TIPPConstants.ObjectFile;
import com.spartansoftwareinc.tipp.TIPPConstants.Task;
import com.spartansoftwareinc.tipp.TIPPConstants.TaskResponse;

import static com.spartansoftwareinc.tipp.TIPPConstants.*;
import static com.spartansoftwareinc.tipp.XMLUtil.*;

/**
 * Convert a TIPManifest into a DOM tree.
 */
class ManifestDOMBuilder {

    private Manifest manifest;
    
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
        Document document = docBuilder.newDocument();
        Element root = document.createElement(MANIFEST);
        // QUESTIONABLE: I'm disabling writing out the schema location, because
        // a) it is causes havoc with the xml-dsig signing, for some reason, and
        // b) it's only meant to be a hint anyways.
        // root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
        //      "schemaLocation", SCHEMA_LOCATION);
        root.appendChild(makeDescriptor(document));
        root.appendChild(taskToElement(manifest.getTask(), manifest.isRequest(), document));
        root.appendChild(makePackageObjects(document));
        root.setAttribute("xmlns", TIPP_NAMESPACE);
        root.setAttribute(ATTR_VERSION, SCHEMA_VERSION);
        document.appendChild(root);
        return document;
    }

    Element taskToElement(TIPPTask task, boolean isRequest, Document doc) {
        if (isRequest) {
            return requestTaskToElement(task, doc);
        }
        else {
            return responseTaskToElement((TIPPTaskResponse)task, doc);
        }
    }

    private Element requestTaskToElement(TIPPTask task, Document doc) {
        Element requestEl = doc.createElement(TASK_REQUEST);
        return addTaskData(task, doc, requestEl);
    }

    Element responseTaskToElement(TIPPTaskResponse task, Document doc) {
        Element responseEl = doc.createElement(TASK_RESPONSE);
        responseEl.appendChild(makeInResponseTo(task, doc));
        appendElementChildWithText(doc, responseEl,
                TaskResponse.MESSAGE, task.getMessage().toString());
        String comment = task.getComment() != null ? task.getComment() : "";
        appendElementChildWithText(doc, responseEl,
                TaskResponse.COMMENT, comment);        
        return responseEl;
    }

    private Element makeInResponseTo(TIPPTaskResponse task, Document doc) {
        Element inReEl = doc.createElement(TaskResponse.IN_RESPONSE_TO);
        addTaskData(task, doc, inReEl);
        appendElementChildWithText(doc, inReEl, UNIQUE_PACKAGE_ID, task.getRequestPackageId());
        inReEl.appendChild(creatorToElement(task.getRequestCreator(), doc));
        return inReEl;
    }

    private Element addTaskData(TIPPTask task, Document doc, Element el) {
        appendElementChildWithText(doc, el, Task.TYPE, task.getTaskType().getType());
        appendElementChildWithText(doc, el, Task.SOURCE_LANGUAGE, task.getSourceLocale());
        appendElementChildWithText(doc, el, Task.TARGET_LANGUAGE, task.getTargetLocale());
        return el;
    }

    private Element makeDescriptor(Document doc) {
        Element descriptor = doc.createElement(GLOBAL_DESCRIPTOR);
        appendElementChildWithText(doc,
                descriptor, UNIQUE_PACKAGE_ID, manifest.getPackageId());
        descriptor.appendChild(creatorToElement(manifest.getCreator(), doc));
        return descriptor;
    }

    private Element creatorToElement(TIPPCreator creator, Document doc) {
        Element creatorEl = doc.createElement(PACKAGE_CREATOR);
        appendElementChildWithText(doc, creatorEl, Creator.NAME, creator.getName());
        appendElementChildWithText(doc, creatorEl, Creator.ID, creator.getId());
        appendElementChildWithText(doc, creatorEl, Creator.UPDATE, FormattingUtil.writeTIPPDate(creator.getDate()));
        creatorEl.appendChild(toolToElement(creator.getTool(), doc));
        return creatorEl;
    }

    private Element toolToElement(TIPPTool tool, Document doc) {
        Element toolEl = doc.createElement(TOOL);
        appendElementChildWithText(doc, toolEl, ContributorTool.NAME, tool.getName());
        appendElementChildWithText(doc, toolEl, ContributorTool.ID, tool.getId());
        appendElementChildWithText(doc, toolEl, ContributorTool.VERSION, tool.getVersion());
        return toolEl;
    }

    private Element makePackageObjects(Document doc) {
        Element objects = doc.createElement(PACKAGE_OBJECTS);
        for (TIPPSection section : manifest.getSections()) {
            objects.appendChild(sectionToElement(section, doc));
        }
        return objects;
    }

    private Element sectionToElement(TIPPSection section, Document doc) {
        Element sectionEl = doc.createElement(section.getType().getElementName());
        sectionEl.setAttribute(ATTR_SECTION_NAME, section.getType().getElementName());
        for (TIPPResource resource : section.getResources()) {
            if (resource instanceof TIPPReferenceFile) {
                sectionEl.appendChild(referenceFileToElement((TIPPReferenceFile)resource, doc));
            }
            else {
                sectionEl.appendChild(fileToElement((TIPPFile)resource, doc));
            }
        }
        return sectionEl;
    }

    private Element resourceAddChildren(TIPPResource resource, Document doc, Element resourceElement) {
        resourceElement.setAttribute(ObjectFile.ATTR_SEQUENCE, String.valueOf(resource.getSequence()));
        appendElementChildWithText(doc, resourceElement, ObjectFile.NAME, resource.getName());
        return resourceElement;
    }

    private Element fileToElement(TIPPFile file, Document doc) {
        return addFileChildren(file, doc, doc.createElement(FILE_RESOURCE));
    }

    private Element addFileChildren(TIPPFile file, Document doc, Element resourceElement) {
        resourceAddChildren(file, doc, resourceElement);
        appendElementChildWithText(doc, resourceElement, ObjectFile.LOCATION, manifest.getLocationForFile(file));
        return resourceElement;
    }

    private Element referenceFileToElement(TIPPReferenceFile file, Document doc) {
        Element el = doc.createElement(REFERENCE_FILE_RESOURCE);
        if (file.getLanguageChoice() != null) {
            el.setAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE, file.getLanguageChoice().name());
        }
        return addFileChildren(file, doc, el);
    }

}
