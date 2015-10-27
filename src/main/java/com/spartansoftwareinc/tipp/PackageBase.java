package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Base package implementations.  This is actually mutable, and the 
 * readable/writeable stuff is enforced by the interfaces.  Shhh, 
 * don't tell anybody.
 */
abstract class PackageBase implements TIPP {

    private Payload payload;
    private Manifest manifest;
    
    PackageBase(Payload payload, Manifest manifest) {
        this.payload = payload;
        this.manifest = manifest;
    }
    
    static final String MANIFEST = "manifest.xml";
    static final String PAYLOAD_FILE = "resources.zip";

    Manifest getManifest() {
        return manifest;
    }

    @Override
    public String getPackageId() {
        return getManifest().getPackageId();
    }

    @Override
    public TIPPCreator getCreator() {
        return getManifest().getCreator();
    }

    @Override
    public TIPPTaskType getTaskType() {
        return getManifest().getTask().getTaskType();
    }

    @Override
    public String getSourceLocale() {
        return getManifest().getTask().getSourceLocale();
    }

    @Override
    public String getTargetLocale() {
        return getManifest().getTask().getTargetLocale();
    }

    @Override
    public TIPPSection getSection(TIPPSectionType sectionType) {
        return getManifest().getSection(sectionType);
    }

    @Override
    public TIPPSection getBilingualSection() {
        return getManifest().getSection(TIPPSectionType.BILINGUAL);
    }

    @Override
    public TIPPSection getInputSection() {
        return getManifest().getSection(TIPPSectionType.INPUT);
    }

    @Override
    public TIPPSection getOutputSection() {
        return getManifest().getSection(TIPPSectionType.OUTPUT);
    }

    @Override
    public TIPPSection getSpecificationsSection() {
        return getManifest().getSection(TIPPSectionType.STS);
    }

    @Override
    public TIPPSection getTmSection() {
        return getManifest().getSection(TIPPSectionType.TM);
    }

    @Override
    public TIPPSection getTerminologySection() {
        return getManifest().getSection(TIPPSectionType.TERMINOLOGY);
    }

    @Override
    public TIPPReferenceSection getReferenceSection() {
        return (TIPPReferenceSection) getManifest()
                .getSection(TIPPSectionType.REFERENCE);
    }

    @Override
    public TIPPSection getPreviewSection() {
        return getManifest().getSection(TIPPSectionType.PREVIEW);
    }

    @Override
    public TIPPSection getMetricsSection() {
        return getManifest().getSection(TIPPSectionType.METRICS);
    }

    @Override
    public TIPPSection getExtrasSection() {
        return getManifest().getSection(TIPPSectionType.EXTRAS);
    }

    /**
     * Return only the non-empty sections.
     */
    @Override
    public Collection<TIPPSection> getSections() {
        return getManifest().getSections();
    }

    @Override
    public InputStream getFile(TIPPFile file) throws IOException {
        // Does not include section prefix
        String location = getManifest().getLocationForFile(file);
        return payload.getFileObject(file.getSectionType(), location);
    }

    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws IOException
     */
    @Override
    public void saveToStream(OutputStream outputStream) throws TIPPException, IOException {
        saveToStream(new ManifestWriter(), outputStream);
    }

    /**
     * Write this package to an output stream as a ZIP archive, including 
     * digital signature information in the Manifest using the specified keypair.
     * @param outputStream
     * @param keyPair keypair with which to sign the manifest
     * @throws TIPPException
     * @throws IOException
     */
    @Override
    public void saveToStream(OutputStream outputStream, KeyPair keyPair) throws TIPPException, IOException {
        ManifestWriter mw = new ManifestWriter();
        mw.setKeyPair(keyPair);
        saveToStream(mw, outputStream);
    }

    private void saveToStream(ManifestWriter mw, OutputStream outputStream) throws TIPPException, IOException {
        Path tempPayload = Files.createTempFile("resources", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            // For some reason writing the zip stream out within another
            // zip stream gives me strange zip corruption errors.  Write
            // resources.zip out to a temp file and copy it over.
            try (OutputStream tempOutputStream = Files.newOutputStream(tempPayload)) {
                writePayload(tempOutputStream);
            }

            // Write out all the parts as an inner archive
            zos.putNextEntry(new ZipEntry(PAYLOAD_FILE));
            FileUtil.copyStreamToStream(Files.newInputStream(tempPayload), zos);
            zos.closeEntry();

            // Now write out the manifest.  We do this last so we can
            // pass the objects reference.
            // Add the payload as well, in case we are signing.
            mw.setPayload(Files.newInputStream(tempPayload));
            zos.putNextEntry(new ZipEntry(MANIFEST));
            mw.saveToStream(manifest, zos);
            zos.closeEntry();
    
            zos.flush();
        }
        finally {
            if (tempPayload != null) {
                Files.delete(tempPayload);
            }
        }
    }

    /**
     * Create a zip archive of the package objects as a file on disk.
     * @return 
     */
    void writePayload(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        writeZipPayload(zos);
        zos.close();
    }

    /**
     * Write out objects as a zip archive to the specified stream.  
     * Leaves the output stream open.
     * @param outputStream
     * @throws IOException
     */
    void writeZipPayload(ZipOutputStream zos) throws IOException { 
        for (TIPPSection section : manifest.getSections()) {
            for (TIPPResource resource : section.getResources()) {
                String location = manifest.getLocationForFile((TIPPFile)resource);
                String path = Payload.getFilePath(section.getType(), location);
                zos.putNextEntry(new ZipEntry(path));
                try (InputStream is = payload.getFileObject(section.getType(), location)) {
                    FileUtil.copyStreamToStream(is, zos);
                    zos.closeEntry();
                }
            }
        }
        zos.flush();
    }

    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        manifest = null;
        payload.close();
    }
}
