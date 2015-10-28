package com.spartansoftwareinc.tipp;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.crypto.KeySelector;

class PackageReader {
    private StreamPackageSource source;
    PackageReader(StreamPackageSource source) {
        this.source = source;
    }

    PackageBase load(TIPPErrorHandler errorHandler, KeySelector keySelector) throws IOException {
        try {
            Manifest manifest = new ManifestLoader().loadFromStream(source.getManifest(), errorHandler);
            if (manifest == null) {
                return null;
            }
            // What kind of manifest was it?
            PackageBase tipp = null;
            if (manifest.isRequest()) {
                tipp = new RequestPackageBase(source.getPayload(), manifest);
            }
            else {
                tipp = new ResponsePackageBase(source.getPayload(), manifest);
            }

            // Verify the manifest against the package contents
            new PayloadValidator().validate(manifest, source.getPayload(), errorHandler);
            return tipp;
        }
        catch (FileNotFoundException e) {
            errorHandler.reportError(TIPPErrorType.MISSING_MANIFEST, 
                               "Package contained no manifest", null);
            throw new ReportedException(e);
        }
        finally {
            source.close();
        }
    }
}
