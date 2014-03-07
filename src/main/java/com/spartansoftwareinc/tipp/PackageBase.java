package com.spartansoftwareinc.tipp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Collection;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * Base package implementations.  This is actually mutable, and the 
 * readable/writeable stuff is enforced by the interfaces.  Shhh, 
 * don't tell anybody.
 */
abstract class PackageBase implements TIPP {

    private PackageStore store;
    private Manifest manifest;
    
    PackageBase(PackageStore store) {
        this.store = store;
    }
    
    static final String MANIFEST = "manifest.xml";
    static final String PAYLOAD_FILE = "resources.zip";

    Manifest getManifest() {
        return manifest;
    }
    
    void setManifest(Manifest manifest) {
    	this.manifest = manifest;
    }
    
    public String getPackageId() {
		return getManifest().getPackageId();
	}
	
	public TIPPCreator getCreator() {
		return getManifest().getCreator();
	}
	
	public String getTaskType() {
		return getManifest().getTask().getTaskType();
	}
	
	public String getSourceLocale() {
		return getManifest().getTask().getSourceLocale();
	}
	
	public String getTargetLocale() {
		return getManifest().getTask().getTargetLocale();
	}

	public void setPackageId(String id) {
		getManifest().setPackageId(id);
	}
	
	public void setCreator(TIPPCreator creator) {
		getManifest().setCreator(creator);
	}
	
	public void setTaskType(String taskTypeUri) {
		getManifest().getTask().setTaskType(taskTypeUri);
	}
	
	public void setSourceLocale(String sourceLocale) {
		getManifest().getTask().setSourceLocale(sourceLocale);
	}
	
	public void setTargetLocale(String targetLocale) {
		getManifest().getTask().setTargetLocale(targetLocale);
	}

	public TIPPSection getSection(TIPPSectionType sectionType) {
	    return getManifest().getSection(sectionType);
	}
	
	public TIPPSection getBilingualSection() {
	    return getManifest().getSection(TIPPSectionType.BILINGUAL);
	}
	public TIPPSection getInputSection() {
	    return getManifest().getSection(TIPPSectionType.INPUT);
	}
	public TIPPSection getOutputSection() {
	    return getManifest().getSection(TIPPSectionType.OUTPUT);
	}
	public TIPPSection getSpecificationsSection() {
	    return getManifest().getSection(TIPPSectionType.STS);    
	}
	public TIPPSection getTmSection() {
	    return getManifest().getSection(TIPPSectionType.TM);
	}
	public TIPPSection getTerminologySection() {
	    return getManifest().getSection(TIPPSectionType.TERMINOLOGY);
	}
	public TIPPReferenceSection getReferenceSection() {
	    return (TIPPReferenceSection)getManifest().getSection(TIPPSectionType.REFERENCE);
	}
	public TIPPSection getPreviewSection() {
	    return getManifest().getSection(TIPPSectionType.PREVIEW);
	}
	public TIPPSection getMetricsSection() {
	    return getManifest().getSection(TIPPSectionType.METRICS);
	}
	public TIPPSection getExtrasSection() {
	    return getManifest().getSection(TIPPSectionType.EXTRAS);
	}
	
	/**
	 * Return only the non-empty sections.
	 */
	public Collection<TIPPSection> getSections() {
	    return getManifest().getSections();
	}
	
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPPException
     * @throws IOException
     */
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
    public void saveToStream(OutputStream outputStream, KeyPair keyPair) throws TIPPException, IOException {
        ManifestWriter mw = new ManifestWriter();
        mw.setKeyPair(keyPair);
        saveToStream(mw, outputStream);
    }
    
    private void saveToStream(ManifestWriter mw, OutputStream outputStream) throws TIPPException, IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream);

        // For some reason writing the zip stream out within another
        // zip stream gives me strange zip corruption errors.  Write
        // resources.zip out to a temp file and copy it over.
        OutputStream tempOutputStream = store.storeTransientData("output-stream");
        writePayload(tempOutputStream);
        tempOutputStream.close();
        
        // Write out all the parts as an inner archive
        zos.putArchiveEntry(new ZipArchiveEntry(PAYLOAD_FILE));
        FileUtil.copyStreamToStream(store.getTransientData("output-stream"), zos);
        zos.closeArchiveEntry();

        // Now write out the manifest.  We do this last so we can
        // pass the objects reference.
        // Add the payload as well, in case we are signing.
        mw.setPayload(store.removeTransientData("output-stream"));
        zos.putArchiveEntry(new ZipArchiveEntry(MANIFEST));
        mw.saveToStream(manifest, zos);
        zos.closeArchiveEntry();

        zos.flush();
        zos.close();
    }
    
    /**
     * Create a zip archive of the package objects as a file on disk.
     * @return 
     */
    void writePayload(OutputStream os) throws IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
        writeZipPayload(zos);
        zos.close();
    }
    
    /**
     * Write out objects as a zip archive to the specified stream.  
     * Leaves the output stream open.
     * @param outputStream
     * @throws IOException
     */
    void writeZipPayload(ZipArchiveOutputStream zos) throws IOException {
        for (TIPPSection section : manifest.getSections()) {
            for (TIPPFile file : section.getResources()) {
                String path = ((TIPPFile)file).getCanonicalObjectPath();
                zos.putArchiveEntry(new ZipArchiveEntry(path));
                InputStream is = file.getInputStream();
                FileUtil.copyStreamToStream(is, zos);
                zos.closeArchiveEntry();
                is.close();
            }
        }
        zos.flush();
    }
    
    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     * @return true if this succeeds, false is some resources could
     *         not be released
     */
    public boolean close() throws IOException {
        manifest = null;
        return store.close();
    }
    
    BufferedInputStream getPackageObjectInputStream(String path) throws IOException {
    	return new BufferedInputStream(store.getObjectFileData(path));
    }
    
    BufferedOutputStream getPackageObjectOutputStream(String path) throws IOException, TIPPException {
    	return new BufferedOutputStream(store.storeObjectFileData(path));
    }
}
