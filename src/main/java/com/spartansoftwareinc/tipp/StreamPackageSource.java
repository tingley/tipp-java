package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

/**
 * PackageSource that reads contents from a zipped package archive.
 */
class StreamPackageSource extends PackageSource {

    private InputStream inputStream;
    private TIPPLoadStatus status;

    StreamPackageSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    boolean close() throws IOException {
        return true;
    }

    @Override
    void copyToStore(PackageStore store) throws IOException {
        try {
            ZipArchiveInputStream zis = FileUtil.getZipInputStream(inputStream);
            for (ZipArchiveEntry entry = zis.getNextZipEntry(); entry != null; 
                    entry = zis.getNextZipEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.equals(PackageBase.MANIFEST)) {
                    FileUtil.copyStreamToStreamAndCloseDest(zis, store.storeManifestData());
                }
                else if (name.equals(PackageBase.PAYLOAD_FILE)) {
                    copyPayloadToStore(zis, store);
                }
                else {
                    status.addError(TIPPError.Type.UNEXPECTED_PACKAGE_CONTENTS, 
                            "Unexpected package contents: " + name);
                }
            }
            zis.close();
        }
        catch (IOException e) {
            // XXX Is this still true?
            // This exception is not called when you expect due to the 
            // odd behavior of the Java zip library.  For example, if the
            // ZIP file is not actually a ZIP, no error is thrown!  The stream
            // will just produce zero entries instead.
            status.addError(TIPPError.Type.INVALID_PACKAGE_ZIP,
                            "Could not read package zip", e);
            throw new ReportedException(e);
        }
    }
    
    private void copyPayloadToStore(InputStream is, PackageStore store) throws IOException {
        // There's a bug in the Java zip implementation -- I can't actually open 
        // a zip stream within another stream without buffering it.  As a result, I need 
        // to dump the contents of the payload object into a temporary location and then
        // read it back as a zip archive.
        // 
        // I also need to do this so I can retrieve the raw payload bytes for 
        // signature validation.
        FileUtil.copyStreamToStreamAndCloseDest(is, 
                            store.storeRawPayloadData());
        ZipArchiveInputStream zis = FileUtil.getZipInputStream(store.getRawPayloadData());
        for (ZipArchiveEntry entry = zis.getNextZipEntry(); entry != null; 
                entry = zis.getNextZipEntry()) {
            if (entry.isDirectory()) {
                continue;
            }
            FileUtil.copyStreamToStreamAndCloseDest(zis, 
                    store.storeObjectFileData(entry.getName()));
        }
        zis.close();
    }
    
    @Override
    void open(TIPPLoadStatus status) throws IOException {
        this.status = status;
    }


}
