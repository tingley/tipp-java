package com.spartansoftwareinc.tipp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

class FileUtil {

    /**
     * Create a temporary directory.  The directory will be 
     * deleted when the JVM exits.
     * @param prefix prefix for the temp directory
     * @return temporary directory
     * @throws IOException on IOError
     * @throws IllegalStateException if the directory (somehow) already exists
     */
    public static File createTempDir(String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, "");
        String name = tempFile.getCanonicalPath();
        tempFile.delete();
        File tempDir = new File(name);
        if (!tempDir.mkdir()) {
            throw new IllegalStateException("Unable to create directory " + name);
        }
        tempDir.deleteOnExit();
        return tempDir;
    }
    
    public static ZipArchiveInputStream getZipInputStream(InputStream inputStream) 
            throws IOException {
        if (inputStream instanceof ZipArchiveInputStream) {
            return (ZipArchiveInputStream)inputStream;
        }
        else {
            return new ZipArchiveInputStream(inputStream);
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
     * Copy contents of a stream to a target file.  Leave the input
     * stream open afterwards.
     * @param is input stream
     * @param outputFile target file
     * @throws IOException
     */
    public static void copyStreamToFile(InputStream is, File outputFile) 
            throws IOException {
        OutputStream os = 
            new BufferedOutputStream(new FileOutputStream(outputFile));
        copyStreamToStream(is, os);
        os.close();
    }
    
    public static void copyFileToStream(File input, OutputStream os) 
            throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(input));
        copyStreamToStream(is, os);
        is.close();
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
