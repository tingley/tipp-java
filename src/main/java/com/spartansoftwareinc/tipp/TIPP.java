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
     * Is this package a request? If true, the package may be safely cast to
     * TIPRequestPackage; if false, teh package may be safely cast to
     * TIPResponsePackage.
     *
     * @return boolean
     */
    boolean isRequest();

    /**
     * If this is a request TIPP, return a {@link RequestTIPP} instance; otherwise
     * throw an exception.
     * @return request instance
     * @throws IllegalStateException if this is not a request TIPP
     */
    RequestTIPP asRequestTIPP();

    /**
     * If this is a request TIPP, return a {@link ResponseTIPP} instance; otherwise
     * throw an exception.
     * @return response instance
     * @throws IllegalStateException if this is not a response TIPP
     */
    ResponseTIPP asResponseTIPP();

    /**
     * Get the package id.
     *
     * @return package id
     */
    String getPackageId();

    /**
     * Get information about this packages creator.
     *
     * @return creator information
     */
    TIPPCreator getCreator();

    /**
     * Get the task type for this package.
     * @return task type
     */
    TIPPTaskType getTaskType();

    /**
     * Get the source locale for this package.
     * @return source locale, as a BCP 47-compliant string
     */
    String getSourceLocale();

    /**
     * Get the target locale for this package.
     * @return target locale, as a BCP 47-compliant string
     */
    String getTargetLocale();

    /**
     * Return all the sections in this package.
     *
     * @return collection of sections
     */
    Collection<TIPPSection> getSections();

    /**
     * Return the bilingual section, if present
     * @return bilingual section, or null
     */
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
     * Return the specified section, if present
     * @param sectionType section type
     * @return the package section of the specified type, or null
     */
    TIPPSection getSection(TIPPSectionType sectionType);

    /**
     * Open the contents of a file resource in this package.
     * @param file file resource present in this package
     * @return inputstream containing the contents of the specified file resource
     * @throws IOException
     */
    InputStream getFile(TIPPFile file) throws IOException;
}
