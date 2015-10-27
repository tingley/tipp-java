package com.spartansoftwareinc.tipp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;

class FileUtil {

    public static Path copyToTemp(InputStream is, String prefix, String suffix) throws IOException {
        Path temp = Files.createTempFile(prefix, suffix);
        Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }

    public static ZipInputStream getZipInputStream(InputStream inputStream) 
            throws IOException {
        if (inputStream instanceof ZipInputStream) {
            return (ZipInputStream)inputStream;
        }
        else {
            return new ZipInputStream(inputStream);
        }
    }
    
    /**
     * Delete a file and all its descendants, indicating success or
     * failure. 
     * @param file
     * @throws IOException
     * @return true if this succeeds, or false if one or more files
     *      or directories can't be deleted. 
     */
    public static boolean recursiveDelete(File file) throws IOException {
        boolean success = true;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!recursiveDelete(child)) {
                    success = false;
                }
            }
        }
        if (!file.delete()) {
            success = false;
        }
        return success;
    }
    
    /**
     * Create a file, including all parent directories.
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean recursiveCreate(File file) throws IOException {
        boolean success = true;

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            success = parent.mkdirs();
        }
        if (success) {
            success = file.createNewFile();
        }
        return success;
    }

    /**
     * Copy contents of an input stream to an output stream.  Leave both 
     * streams open afterwards.
     * @param is input stream
     * @param os output stream
     * @throws IOException
     */
    public static void copyStreamToStream(InputStream is, OutputStream os) 
            throws IOException {
        byte[] buffer = new byte[4096];
        for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
            os.write(buffer, 0, read);
        }
        os.flush();
    }
    
    /**
     * Copy contents of an input stream to an output stream.  Close the 
     * output stream.
     * @param is input stream
     * @param os output stream
     * @throws IOException
     */
    public static void copyStreamToStreamAndCloseDest(InputStream is, 
            OutputStream os) throws IOException {
        copyStreamToStream(is, os);
        os.close();
    }
}
