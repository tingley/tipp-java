package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

abstract class AbstractTIPPBuilder {
    private PayloadBuilder payloadBuilder = new PayloadBuilder();
    private ManifestBuilder manifestBuilder = new ManifestBuilder();

    AbstractTIPPBuilder(boolean isRequest) {
        manifestBuilder.setIsRequest(isRequest);
    }

    protected ManifestBuilder getManifestBuilder() {
        return manifestBuilder;
    }

    public AbstractTIPPBuilder setPackageId(String packageId) {
        getManifestBuilder().setPackageId(packageId);
        return this;
    }

    public AbstractTIPPBuilder setCreator(TIPPCreator creator) {
        getManifestBuilder().setCreator(creator);
        return this;
    }

    public AbstractTIPPBuilder setTaskType(TIPPTaskType taskType) {
        getManifestBuilder().setTaskType(taskType);
        return this;
    }

    public AbstractTIPPBuilder setSourceLocale(String srcLang) {
        getManifestBuilder().setSourceLocale(srcLang);
        return this;
    }

    public AbstractTIPPBuilder setTargetLocale(String tgtLang) {
        getManifestBuilder().setTargetLocale(tgtLang);
        return this;
    }

    public AbstractTIPPBuilder addFile(TIPPSectionType sectionType, String name, InputStream is) throws IOException {
        payloadBuilder.addFile(getManifestBuilder().addFile(sectionType, name), is);
        return this;
    }

    public AbstractTIPPBuilder addReferenceFile(TIPPReferenceFile.LanguageChoice langChoice, String name, InputStream is)
                                    throws IOException {
        payloadBuilder.addFile(getManifestBuilder().addReferenceFile(name, langChoice), is);
        return this;
    }

    protected abstract TIPP buildTIPP(Payload payload, Manifest manifest);

    public TIPP build() throws IOException {
        manifestBuilder.setLocationMap(payloadBuilder.getLocationMap());
        return buildTIPP(payloadBuilder.build(), manifestBuilder.build());
    }
}
