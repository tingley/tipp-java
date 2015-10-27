package com.spartansoftwareinc.tipp;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.spartansoftwareinc.tipp.TIPPReferenceFile.LanguageChoice;

class ManifestBuilder {
    private TIPPTaskType taskType;
    private String packageId;
    private TIPPCreator creator = new TIPPCreator("", "", new Date(), new TIPPTool("", "", ""));
    private boolean isRequest = true;
    private String srcLang, tgtLang;
    private EnumMap<TIPPSectionType, SectionBuilder> sectionBuilders = new EnumMap<>(TIPPSectionType.class);
    private EnumMap<TIPPSectionType, TIPPSection> sections = new EnumMap<>(TIPPSectionType.class);
    private Map<TIPPFile, String> locationMap = new HashMap<>();
    // Response-specific parameters
    private String requestPackageId, responseComment;
    private TIPPCreator requestCreator;
    private TIPPResponseCode responseCode;

    ManifestBuilder() {
        // Generate an initial package id
        this.packageId = "urn:uuid:" + UUID.randomUUID().toString();
    }

    void setTaskType(TIPPTaskType taskType) {
        this.taskType = taskType;
    }

    void setTaskType(String taskTypeUri) {
        taskType = StandardTaskType.forTypeUri(taskTypeUri);
        if (taskType == null) {
            taskType = new CustomTaskType(taskTypeUri, Arrays.asList(TIPPSectionType.values()));
        }
    }

    void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    void setCreator(TIPPCreator creator) {
        this.creator = creator;
    }

    void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    void setSourceLocale(String srcLang) {
        this.srcLang = srcLang;
    }

    void setTargetLocale(String tgtLang) {
        this.tgtLang = tgtLang;
    }

    void setLocationMap(Map<TIPPFile, String> locationMap) {
        this.locationMap = locationMap;
    }

    TIPPFile addFile(TIPPSectionType sectionType, String name) {
        return getBuilder(sectionType).addFile(name);
    }

    TIPPFile addReferenceFile(String name, LanguageChoice languageChoice) {
        return getBuilder(TIPPSectionType.REFERENCE).addReferenceFile(name, languageChoice);
    }

    void addSection(TIPPSection section) {
        sections.put(section.getType(), section);
    }

    void setRequestPackageId(String packageId) {
        this.requestPackageId = packageId;
    }

    void setComment(String comment) {
        this.responseComment = comment;
    }

    void setResponseCode(TIPPResponseCode code) {
        this.responseCode = code;
    }

    void setRequestCreator(TIPPCreator requestCreator) {
        this.requestCreator = requestCreator;
    }

    private SectionBuilder getBuilder(TIPPSectionType sectionType) {
        SectionBuilder builder = sectionBuilders.get(sectionType);
        if (builder == null) {
            builder = new SectionBuilder(sectionType);
            sectionBuilders.put(sectionType, builder);
        }
        return builder;
    }

    Manifest build() {
        // TODO validate both request and response params
        for (Map.Entry<TIPPSectionType, SectionBuilder> e : sectionBuilders.entrySet()) {
            sections.put(e.getKey(), e.getValue().build());
        }
        TIPPTask task = isRequest ?
                new TIPPTaskRequest(taskType, srcLang, tgtLang) :
                new TIPPTaskResponse(taskType, srcLang, tgtLang, requestPackageId, requestCreator,
                                     responseCode, responseComment);
        return new Manifest(packageId, creator, task, isRequest, sections, locationMap);
    }
}
