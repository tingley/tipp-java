package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

/**
 * An object to construct a {@link ResponseTIPP} instance.  Response
 * TIPPs may be constructed ad-hoc, or based on an existing request.
 */
public class ResponseTIPPBuilder extends AbstractTIPPBuilder {

    public ResponseTIPPBuilder() {
        super(false);
    }

    /**
     * Start building a response to the specified request TIPP.  This is
     * equivalent to passing the appropriate values to these methods:
     * <ul>
     * <li> {@link #setRequestCreator}
     * <li> {@link #setRequestPackageId}
     * <li> {@link #setTaskType}
     * <li> {@link #setSourceLocale}
     * <li> {@link #setTargetLocale}
     * </ul>
     * @param tipp request TIPP
     */
    public ResponseTIPPBuilder(RequestTIPP tipp) {
        super(false);
        setRequestPackageId(tipp.getPackageId());
        setRequestCreator(tipp.getCreator());
        getManifestBuilder().setTaskType(tipp.getTaskType());
        getManifestBuilder().setSourceLocale(tipp.getSourceLocale());
        getManifestBuilder().setTargetLocale(tipp.getTargetLocale());
    }

    public ResponseTIPPBuilder setRequestPackageId(String packageId) {
        getManifestBuilder().setRequestPackageId(packageId);
        return this;
    }

    public ResponseTIPPBuilder setComment(String comment) {
        getManifestBuilder().setComment(comment);
        return this;
    }

    public ResponseTIPPBuilder setResponseCode(TIPPResponseCode code) {
        getManifestBuilder().setResponseCode(code);
        return this;
    }

    public ResponseTIPPBuilder setRequestCreator(TIPPCreator requestCreator) {
        getManifestBuilder().setRequestCreator(requestCreator);
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

    @Override
    protected TIPP buildTIPP(Payload payload, Manifest manifest) {
        return new ResponsePackageBase(payload, manifest);
    }

    @Override
    public ResponseTIPP build() throws IOException {
        return (ResponseTIPP)super.build();
    }
}
