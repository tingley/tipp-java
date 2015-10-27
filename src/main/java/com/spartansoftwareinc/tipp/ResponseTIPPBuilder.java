package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

public class ResponseTIPPBuilder extends AbstractTIPPBuilder {

    public ResponseTIPPBuilder() {
        super(false);
    }

    /**
     * Start building a response to the specified request TIPP.
     * @param tipp
     */
    public ResponseTIPPBuilder(RequestTIPP tipp) {
        super(false);
        setRequestPackageId(tipp.getPackageId());
        setRequestCreator(tipp.getCreator());
        manifestBuilder.setTaskType(tipp.getTaskType());
        manifestBuilder.setSourceLocale(tipp.getSourceLocale());
        manifestBuilder.setTargetLocale(tipp.getTargetLocale());
    }

    public ResponseTIPPBuilder setRequestPackageId(String packageId) {
        manifestBuilder.setRequestPackageId(packageId);
        return this;
    }

    public ResponseTIPPBuilder setComment(String comment) {
        manifestBuilder.setComment(comment);
        return this;
    }

    public ResponseTIPPBuilder setResponseCode(TIPPResponseCode code) {
        manifestBuilder.setResponseCode(code);
        return this;
    }

    public ResponseTIPPBuilder setRequestCreator(TIPPCreator requestCreator) {
        manifestBuilder.setRequestCreator(requestCreator);
        return this;
    }

    @Override
    public ResponseTIPPBuilder setPackageId(String packageId) {
        return (ResponseTIPPBuilder)super.setPackageId(packageId);
    }

    @Override
    public ResponseTIPPBuilder setCreator(TIPPCreator creator) {
        return (ResponseTIPPBuilder)super.setCreator(creator);
    }

    @Override
    public ResponseTIPPBuilder setTaskType(TIPPTaskType taskType) {
        return (ResponseTIPPBuilder)super.setTaskType(taskType);
    }

    @Override
    public ResponseTIPPBuilder setSourceLocale(String srcLang) {
        return (ResponseTIPPBuilder)super.setSourceLocale(srcLang);
    }

    @Override
    public ResponseTIPPBuilder setTargetLocale(String tgtLang) {
        return (ResponseTIPPBuilder)super.setTargetLocale(tgtLang);
    }

    @Override
    public ResponseTIPPBuilder addFile(TIPPSectionType sectionType, String name, InputStream is) throws IOException {
        return (ResponseTIPPBuilder)super.addFile(sectionType, name, is);
    }

    @Override
    public ResponseTIPPBuilder addReferenceFile(TIPPReferenceFile.LanguageChoice langChoice, String name, InputStream is)
                                    throws IOException {
        return (ResponseTIPPBuilder)super.addReferenceFile(langChoice, name, is);
    }

    public ResponseTIPP build() throws IOException {
        manifestBuilder.setLocationMap(payloadBuilder.getLocationMap());
        ResponsePackageBase responsePackage = new ResponsePackageBase(payloadBuilder.build(), manifestBuilder.build());
        return responsePackage;
    }
}
