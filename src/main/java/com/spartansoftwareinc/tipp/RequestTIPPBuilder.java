package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

/**
 * An object to construct a {@link RequestTIPP} instance.
 */
public class RequestTIPPBuilder extends AbstractTIPPBuilder {
    public RequestTIPPBuilder() {
        super(true);
    }

    @Override
    public RequestTIPPBuilder setPackageId(String packageId) {
        return (RequestTIPPBuilder)super.setPackageId(packageId);
    }

    @Override
    public RequestTIPPBuilder setCreator(TIPPCreator creator) {
        return (RequestTIPPBuilder)super.setCreator(creator);
    }

    @Override
    public RequestTIPPBuilder setTaskType(TIPPTaskType taskType) {
        return (RequestTIPPBuilder)super.setTaskType(taskType);
    }

    @Override
    public RequestTIPPBuilder setSourceLocale(String srcLang) {
        return (RequestTIPPBuilder)super.setSourceLocale(srcLang);
    }

    @Override
    public RequestTIPPBuilder setTargetLocale(String tgtLang) {
        return (RequestTIPPBuilder)super.setTargetLocale(tgtLang);
    }

    @Override
    public RequestTIPPBuilder addFile(TIPPSectionType sectionType, String name, InputStream is) throws IOException {
        return (RequestTIPPBuilder)super.addFile(sectionType, name, is);
    }

    @Override
    public RequestTIPPBuilder addReferenceFile(TIPPReferenceFile.LanguageChoice langChoice, String name, InputStream is)
                                    throws IOException {
        return (RequestTIPPBuilder)super.addReferenceFile(langChoice, name, is);
    }

    @Override
    protected TIPP buildTIPP(Payload payload, Manifest manifest) {
        return new RequestPackageBase(payload, manifest);
    }

    @Override
    public RequestTIPP build() throws IOException {
        return (RequestTIPP)super.build();
    }
}
