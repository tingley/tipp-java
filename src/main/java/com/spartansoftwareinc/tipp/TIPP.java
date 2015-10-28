package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Collection;

public interface TIPP extends AutoCloseable {

    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     */
	void close() throws IOException;
	
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPPException
     * @throws IOException
     */
    void saveToStream(OutputStream outputStream) throws TIPPException, IOException;

    /**
     * Write this package to an output stream as a ZIP archive, signed using the 
     * provided KeyPair.
     * @param outputStream
     * @param keyPair a public/private keypair for generating a digital signature.
     * @throws TIPPException
     * @throws IOException
     */
    void saveToStream(OutputStream outputStream, KeyPair keyPair) throws TIPPException, IOException;

	/**
	 * Is this package a request?  If true, the package may be safely
	 * cast to TIPRequestPackage; if false, teh package may be safely
	 * cast to TIPResponsePackage.
	 *   
	 * @return boolean
	 */
	boolean isRequest();
	
	String getPackageId();
	
	TIPPCreator getCreator();
	
	TIPPTaskType getTaskType();
	
	String getSourceLocale();
	
	String getTargetLocale();
	
	TIPPSection getBilingualSection();
	TIPPSection getInputSection();
	TIPPSection getOutputSection();
	TIPPSection getSpecificationsSection();
	TIPPSection getTmSection();
	TIPPSection getTerminologySection();
	TIPPReferenceSection getReferenceSection();
	TIPPSection getPreviewSection();
	TIPPSection getMetricsSection();
	TIPPSection getExtrasSection();
	
	/**
	 * Return all the sections in this package.
	 * @return collection of sections
	 */
	Collection<TIPPSection> getSections();

	TIPPSection getSection(TIPPSectionType sectionType);

	InputStream getFile(TIPPFile file) throws IOException;
}
