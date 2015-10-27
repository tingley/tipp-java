package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.crypto.KeySelector;

public class TIPPFactory {

    private TIPPErrorHandler errorHandler = new DefaultErrorHandler();
    
    public TIPPErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(TIPPErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException("errorHandler can't be null");
        }
        this.errorHandler = errorHandler;
    }

    /**
     * Create a new TIPP object from a byte stream representation of 
     * a zipped TIPP.  The package data will be expanded into the provided
     * PackageStore as part of processing.  If the package is signed, the
     * signature will <b>not</b> be verified.
     * <p>
     * This method will return a non-null TIPP as long as the package could be 
     * loaded without encounterning a fatal error.  However, the TIPPLoadStatus object
     * passed as a parameter should be examined to see how successful the loading 
     * actually was.
     * 
     * @param inputStream zipped package data
     * @param status a record of any errors encountered during loading
     * 
     * @return a TIPP if parsing was completed, or null if a FATAL error occurred.
     * @throws IOException 
     */
    public TIPP openFromStream(InputStream inputStream) throws IOException {
        return openFromStream(inputStream, null);
    }
    
    /**
     * Create a new TIPP object from a byte stream representation of 
     * a zipped TIPP.  The package data will be expanded into the provided
     * PackageStore as part of processing.  If the package is signed, it will
     * be verified using the provided key.
     * <p>
     * This method will return a non-null TIPP as long as the package could be 
     * loaded without encounterning a fatal error.  However, the TIPPLoadStatus object
     * passed as a parameter should be examined to see how successful the loading 
     * actually was.
     * 
     * @param inputStream zipped package data
     * @param status a record of any errors encountered during loading
     * 
     * @return a TIPP if parsing was completed, or null if a FATAL error occurred.
     * @throws IOException 
     */
    public TIPP openFromStream(InputStream inputStream,
            KeySelector keySelector) throws IOException {
        try {
            StreamPackageSource source = new StreamPackageSource(inputStream, errorHandler);
            source.expand();

            return new PackageReader(source).load(errorHandler, keySelector);
        }
        catch (ReportedException e) {
            // Reported exceptions will be logged as part of the load status;
            // we catch them (to terminate loading), but don't need to propagate them
            // further.
            return null;
        }
    }
}