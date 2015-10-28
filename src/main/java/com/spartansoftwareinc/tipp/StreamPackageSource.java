package com.spartansoftwareinc.tipp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * PackageSource that reads contents from a zipped package archive.
 */
class StreamPackageSource {
    public static final String SEPARATOR = "/";

    private InputStream inputStream;
    private TIPPErrorHandler errorHandler;

    private Path manifest;
    private Path objects;
    private Map<String, Path> payloadPaths = new HashMap<>();

    StreamPackageSource(InputStream inputStream, TIPPErrorHandler errorHandler) {
        this.inputStream = inputStream;
        this.errorHandler = errorHandler;
    }
    
    InputStream getManifest() throws IOException {
        if (manifest == null) {
            throw new FileNotFoundException("Missing manifest.xml");
        }
        return Files.newInputStream(manifest);
    }

    Payload getPayload() {
        return new Payload(objects, payloadPaths);
    }

    void expand() throws IOException {
        try (ZipInputStream zis = FileUtil.getZipInputStream(inputStream)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; 
                    entry = zis.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.equals(PackageBase.MANIFEST)) {
                    manifest = FileUtil.copyToTemp(zis, "manifest", ".xml");
                }
                else if (name.equals(PackageBase.PAYLOAD_FILE)) {
                    expandPayload(zis);
                }
                else {
                    errorHandler.reportError(TIPPErrorType.UNEXPECTED_PACKAGE_CONTENTS, 
                            "Unexpected package contents: " + name, null);
                }
            }
        }
        catch (IOException e) {
            // XXX Is this still true?
            // This exception is not called when you expect due to the 
            // odd behavior of the Java zip library.  For example, if the
            // ZIP file is not actually a ZIP, no error is thrown!  The stream
            // will just produce zero entries instead.
            errorHandler.reportError(TIPPErrorType.INVALID_PACKAGE_ZIP,
                            "Could not read package zip", e);
            throw new ReportedException(e);
        }
    }
    
    private void expandPayload(InputStream is) throws IOException {
        // There's a bug in the Java zip implementation -- I can't actually open 
        // a zip stream within another stream without buffering it.  As a result, I need 
        // to dump the contents of the payload object into a temporary location and then
        // read it back as a zip archive.
        // 
        // I also need to do this so I can retrieve the raw payload bytes for 
        // signature validation.
        Path tempPayload = FileUtil.copyToTemp(is, "payload", ".zip");
        objects = Files.createTempDirectory("tipp");
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempPayload))) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; 
                    entry = zis.getNextEntry()) {
                if (!entry.isDirectory()) {
                    Path p = objects.resolve(entry.getName());
                    Files.createDirectories(p.getParent());
                    Files.copy(zis, p);
                    payloadPaths.put(entry.getName(), p);
                }
            }
        }
        Files.delete(tempPayload);
    }

    // Very hacky
    void close() throws IOException {
        if (manifest != null) {
            Files.deleteIfExists(manifest);
        }
    }
    void cleanupSource() throws IOException {
        close();
        FileUtil.recursiveDelete(objects);
    }
}
