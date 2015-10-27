package com.spartansoftwareinc.tipp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

class Payload {
    private Map<String, Path> files;

    Payload(Map<String, Path> files) {
        this.files = files;
    }

    protected Map<String, Path> getFiles() {
        return files;
    }

    Set<String> getPaths() {
        return getFiles().keySet();
    }

    InputStream getFileObject(TIPPSectionType type, String path) throws IOException {
        String fullPath = getFilePath(type, path);
        Path p = getFiles().get(fullPath);
        if (p != null) {
            return Files.newInputStream(p);
        }
        return null;
    }

    static String getFilePath(TIPPSectionType type, String name) {
        // TODO: handle '..', etc
        return type.getDefaultName() + StreamPackageSource.SEPARATOR + name;
    }

    void close() throws IOException {
        for (Path file : files.values()) {
            Files.delete(file);
        }
    }
}
