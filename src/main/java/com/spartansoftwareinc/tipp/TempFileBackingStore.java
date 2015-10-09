package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Backing store that persists data to local temporary files.
 */
public class TempFileBackingStore extends FileSystemBackingStore {

    public TempFileBackingStore() throws IOException {
        super(Files.createTempDirectory("tipp").toFile());
    }
}
