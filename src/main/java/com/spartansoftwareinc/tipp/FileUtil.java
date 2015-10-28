package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipInputStream;

class FileUtil {

    static Path copyToTemp(InputStream is, String prefix, String suffix) throws IOException {
        Path temp = Files.createTempFile(prefix, suffix);
        Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }

    static Path copyToTemp(InputStream is, Path parent, String prefix, String suffix) throws IOException {
        Path temp = Files.createTempFile(parent, prefix, suffix);
        Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }

    static ZipInputStream getZipInputStream(InputStream inputStream) 
            throws IOException {
        if (inputStream instanceof ZipInputStream) {
            return (ZipInputStream)inputStream;
        }
        else {
            return new ZipInputStream(inputStream);
        }
    }
    
    static void recursiveDelete(Path root) throws IOException {
        if (root == null) return;
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Copy contents of an input stream to an output stream.  Leave both 
     * streams open afterwards.
     * @param is input stream
     * @param os output stream
     * @throws IOException
     */
    static void copyStreamToStream(InputStream is, OutputStream os) 
            throws IOException {
        byte[] buffer = new byte[4096];
        for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
            os.write(buffer, 0, read);
        }
        os.flush();
    }
}
