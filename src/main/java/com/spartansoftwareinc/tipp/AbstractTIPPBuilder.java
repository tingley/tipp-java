package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

abstract class AbstractTIPPBuilder {
    protected ManifestBuilder manifestBuilder = new ManifestBuilder();
    protected PayloadBuilder payloadBuilder = new PayloadBuilder();

    AbstractTIPPBuilder(boolean isRequest) {
        manifestBuilder.setIsRequest(isRequest);
    }

    public AbstractTIPPBuilder setPackageId(String packageId) {
        manifestBuilder.setPackageId(packageId);
        return this;
    }

    public AbstractTIPPBuilder setCreator(TIPPCreator creator) {
        manifestBuilder.setCreator(creator);
        return this;
    }

    public AbstractTIPPBuilder setTaskType(TIPPTaskType taskType) {
        manifestBuilder.setTaskType(taskType);
        return this;
    }

    public AbstractTIPPBuilder setSourceLocale(String srcLang) {
        manifestBuilder.setSourceLocale(srcLang);
        return this;
    }

    public AbstractTIPPBuilder setTargetLocale(String tgtLang) {
        manifestBuilder.setTargetLocale(tgtLang);
        return this;
    }

    public AbstractTIPPBuilder addFile(TIPPSectionType sectionType, String name, InputStream is) throws IOException {
        payloadBuilder.addFile(manifestBuilder.addFile(sectionType, name), is);
        return this;
    }

    public AbstractTIPPBuilder addReferenceFile(TIPPReferenceFile.LanguageChoice langChoice, String name, InputStream is)
                                    throws IOException {
        payloadBuilder.addFile(manifestBuilder.addReferenceFile(name, langChoice), is);
        return this;
    }

    public abstract TIPP build() throws IOException;
}
