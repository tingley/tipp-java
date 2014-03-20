package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.crypto.KeySelector;

public class TIPPFactory {

    private PackageStoreFactory storeFactory = new InMemoryPackageStoreFactory();
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

    public PackageStoreFactory getPackageStoreFactory() {
        return storeFactory;
    }

    public void setPackageStoreFactory(PackageStoreFactory strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("null strategy");
        }
        this.storeFactory = strategy;
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
        return openFromStream(inputStream, storeFactory.newPackageStore(),
                              null);
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
        PackageStore store = storeFactory.newPackageStore();
        return openFromStream(inputStream, store, keySelector);
    }

    private TIPP openFromStream(InputStream inputStream,
            PackageStore store, KeySelector keySelector) throws IOException {
        try {
            // XXX Not clear that open, close are still needed
            // TODO: move this elsewhere
            PackageSource source = new StreamPackageSource(inputStream);
            source.open(errorHandler);
            source.copyToStore(store);
            source.close();

            return new PackageReader(store).load(errorHandler, keySelector);
        }
        catch (ReportedException e) {
            // Reported exceptions will be logged as part of the load status;
            // we catch them (to terminate loading), but don't need to propagate them
            // further.
            return null;
        }
    }

    /**
     * Create a new TIPP request with the specified task type and storage.
     * 
     * @param type task type for the new TIPP
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public RequestTIPP newRequestPackage(TIPPTaskType type) 
            throws TIPPException, IOException {
        RequestPackageBase tipPackage = new RequestPackageBase(storeFactory.newPackageStore());
        tipPackage.setManifest(Manifest.newRequestManifest(tipPackage, type));
        return tipPackage;
    }

    /**
     * Create a new TIPP response with the specified task type and storage.
     * 
     * @param type task type for the new TIPP
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public ResponseTIPP newResponsePackage(TIPPTaskType type)
            throws TIPPException, IOException {
        ResponsePackageBase tipPackage = new ResponsePackageBase(storeFactory.newPackageStore());
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, type));
        return tipPackage;
    }

    /**
     * Create a new TIPP response based on an existing request TIPP, using the specified storage.
     * <br>
     * The task type from the request TIPP will also become the task type for the response TIPP.
     * Additionally, the GlobalDescriptor information from the request TIPP (package id, tool, 
     * creator, etc) will be used to populate the InResponseTo information in the response.
     * @param requestPackage an existing request TIPP that will be used to populate the
     *        response metadata for the new TIPP.
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public ResponseTIPP newResponsePackage(RequestTIPP requestPackage)
            throws TIPPException, IOException {
        ResponsePackageBase tipPackage = new ResponsePackageBase(storeFactory.newPackageStore());
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, requestPackage));
        return tipPackage;	
    }
}