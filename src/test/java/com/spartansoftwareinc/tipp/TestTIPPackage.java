package com.spartansoftwareinc.tipp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.crypto.KeySelector;

import org.junit.*;

import com.spartansoftwareinc.tipp.FileSystemBackingStore;
import com.spartansoftwareinc.tipp.FileUtil;
import com.spartansoftwareinc.tipp.InMemoryBackingStore;
import com.spartansoftwareinc.tipp.PackageStore;
import com.spartansoftwareinc.tipp.RequestTIPP;
import com.spartansoftwareinc.tipp.ResponseTIPP;
import com.spartansoftwareinc.tipp.StandardTaskType;
import com.spartansoftwareinc.tipp.TIPP;
import com.spartansoftwareinc.tipp.TIPPCreator;
import com.spartansoftwareinc.tipp.TIPPError;
import com.spartansoftwareinc.tipp.TIPPErrorSeverity;
import com.spartansoftwareinc.tipp.TIPPFactory;
import com.spartansoftwareinc.tipp.TIPPFile;
import com.spartansoftwareinc.tipp.TIPPLoadStatus;
import com.spartansoftwareinc.tipp.TIPPResource;
import com.spartansoftwareinc.tipp.TIPPSection;
import com.spartansoftwareinc.tipp.TIPPSectionType;
import com.spartansoftwareinc.tipp.TIPPTool;
import com.spartansoftwareinc.tipp.TempFileBackingStore;

import static org.junit.Assert.*;

public class TestTIPPackage {
    
    public static void checkErrors(TIPPLoadStatus status, int expectedErrorCount) {
        if (expectedErrorCount != status.getAllErrors().size()) {
            System.out.println("Expected " + expectedErrorCount + 
                    " errors but found " + status.getAllErrors().size());
            for (TIPPError error : status.getAllErrors()) {
                System.out.println("> " + error);
            }
        }
        assertEquals(expectedErrorCount, status.getAllErrors().size());
    }
    
    @Test
    public void testPackageLoad() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_package.zip", status);
        checkErrors(status, 0);
        verifyRequestPackage(tip);
        for (TIPPResource file : 
        	 tip.getSection(TIPPSectionType.BILINGUAL).getResources()) {
            // Just instantiating the input stream is the real test..
            InputStream is = file.getInputStream();
            assertNotNull(is);
            is.close();
        }
        assertTrue("Could not clean up package", tip.close());
    }
    
    @Test
    public void testOpenFromInMemoryStore() throws Exception {
        testOpenToStore(new InMemoryPackageStoreFactory());
    }
    
    @Test
    public void testOpenFromTempFileBackingStore() throws Exception {
        testOpenToStore(new TempFilePackageStoreFactory());
    }
    
    private void testOpenToStore(PackageStoreFactory strategy) throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        InputStream is = getClass().getResourceAsStream("data/test_package.zip");
        TIPPFactory factory = new TIPPFactory();
        factory.setPackageStoreFactory(strategy);;
        TIPP tipp = factory.openFromStream(is, status);
        checkErrors(status, 0);
        verifyRequestPackage(tipp);
    }
    
    @Test
    public void testVerifyMissingManifest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/missing_manifest.zip", status);
        assertNull(tipp);
        checkErrors(status, 1);
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        TIPPError error = status.getAllErrors().get(0);
        assertEquals(TIPPError.Type.MISSING_MANIFEST, error.getErrorType());
    }
    
    @Test
    public void testVerifyCorruptManifest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_manifest.zip", status);
        assertNull(tipp);
        checkErrors(status, 1);
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        assertEquals(TIPPError.Type.CORRUPT_MANIFEST, status.getAllErrors().get(0).getErrorType());
    }
    
    @Test
    public void testVerifyMissingPayload() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/missing_payload.zip", status);
        // manifest is intact, so we should get a TIPP object
        assertNotNull(tipp);
        checkErrors(status, 7);
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(1).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(2).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(3).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(4).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(5).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(6).getErrorType());
    }
    
    @Test
    public void testVerifyCorruptPayloadZip() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_payload_zip.zip", status);
        assertNotNull(tipp);
        checkErrors(status, 7);
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
        for (TIPPError error : status.getAllErrors()) {
            assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, error.getErrorType());
        }
    }

    @Test
    public void testManifestPayloadMismatch() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/manifest_payload_mismatch.zip", status);
        assertNotNull(tipp);
        checkErrors(status, 2);
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPError.Type.UNEXPECTED_PAYLOAD_RESOURCE, status.getAllErrors().get(1).getErrorType());
    }
    
    @Test
    public void testVerifyCorruptPackageZip() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_package_zip.zip", status);
        assertNull(tipp);
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        TIPPError error = status.getAllErrors().get(0);
        assertEquals(TIPPError.Type.MISSING_MANIFEST, error.getErrorType());
    }
    
    @Test
    public void testPackageSave() throws Exception {
        // Load the package, save it out to a zip file, read it back.
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_package.zip", status);
        assertEquals(0, status.getAllErrors().size());
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        status = new TIPPLoadStatus();
        TIPP roundtrip  = new TIPPFactory().openFromStream(
                new BufferedInputStream(new FileInputStream(temp)), status);
        assertEquals(0, status.getAllErrors().size());
        verifyRequestPackage(roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testResponsePackage() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_response_package.zip", status);
        checkErrors(status, 0);
        assertFalse(tip.isRequest());
        verifyResponsePackage((ResponseTIPP)tip);
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        status = new TIPPLoadStatus();
        TIPP roundtrip = new TIPPFactory().openFromStream(
                new BufferedInputStream(new FileInputStream(temp)), status);
        assertEquals(0, status.getAllErrors().size());
        assertFalse(roundtrip.isRequest());
        verifyResponsePackage((ResponseTIPP)roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testNewPackage() throws Exception {
        PackageStore store = new InMemoryBackingStore();
        RequestTIPP tipp = TIPPFactory.newRequestPackage(StandardTaskType.TRANSLATE_STRICT_BITEXT, store);
        tipp.setCreator(
            new TIPPCreator("testname", "testid", 
                           TestTIPManifest.getDate(2011, 7, 12, 20, 35, 12), 
                           new TIPPTool("jtip", 
                                   "http://code.google.com/p/interoperability-now", "0.15"))
        );
        String requestPackageId = tipp.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));
        tipp.setSourceLocale("en-US");
        tipp.setTargetLocale("fr-FR");

        // Failing because the section doesn't have a pointer to the tipp
        TIPPFile f = tipp.getBilingualSection().addFile("test1.xlf");
        OutputStream fos = f.getOutputStream();
        FileUtil.copyStreamToStream(new ByteArrayInputStream("test".getBytes("UTF-8")), 
                                    fos);
        fos.close();
        
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);
        tipp.saveToStream(os);
        os.close();
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP roundTrip = 
            new TIPPFactory().openFromStream(new FileInputStream(temp), status);
        assertEquals(0, status.getAllErrors().size());
        assertNotNull(roundTrip);
        assertEquals(tipp.getPackageId(), roundTrip.getPackageId());
        assertEquals(tipp.getCreator(), roundTrip.getCreator());
        assertEquals(tipp.getTaskType(), roundTrip.getTaskType());
        assertEquals(tipp.getSourceLocale(), roundTrip.getSourceLocale());
        assertEquals(tipp.getTargetLocale(), roundTrip.getTargetLocale());
        comparePackageParts(tipp, roundTrip);        
        temp.delete();
        tipp.close();
    }
    
    //@Test
    public void testNewSignedPackage() throws Exception {
        PackageStore store = new InMemoryBackingStore();
        RequestTIPP tipp = TIPPFactory.newRequestPackage(StandardTaskType.TRANSLATE_STRICT_BITEXT, store);
        tipp.setCreator(
            new TIPPCreator("testname", "testid", 
                           TestTIPManifest.getDate(2011, 7, 12, 20, 35, 12), 
                           new TIPPTool("jtip", 
                                   "http://code.google.com/p/interoperability-now", "0.15"))
        );
        String requestPackageId = tipp.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));
        tipp.setSourceLocale("en-US");
        tipp.setTargetLocale("fr-FR");
               
        TIPPFile f = tipp.getBilingualSection().addFile("test1.xlf");
        OutputStream fos = f.getOutputStream();
        FileUtil.copyStreamToStream(new ByteArrayInputStream("test".getBytes("UTF-8")), 
                                    fos);
        fos.close();
        
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);
        
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        
        tipp.saveToStream(os, kp);
        os.close();
        System.out.println("Wrote package to " + temp);
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP roundTrip = 
            new TIPPFactory().openFromStream(new FileInputStream(temp), 
                    status, KeySelector.singletonKeySelector(kp.getPublic()));
        TestUtils.expectLoadStatus(status, 0, TIPPErrorSeverity.NONE);
        assertNotNull(roundTrip);
        assertEquals(tipp.getPackageId(), roundTrip.getPackageId());
        assertEquals(tipp.getCreator(), roundTrip.getCreator());
        assertEquals(tipp.getTaskType(), roundTrip.getTaskType());
        assertEquals(tipp.getSourceLocale(), roundTrip.getSourceLocale());
        assertEquals(tipp.getTargetLocale(), roundTrip.getTargetLocale());
        comparePackageParts(tipp, roundTrip);
        temp.delete();
        tipp.close();
    }
    
    private TIPP getSamplePackage(String path, TIPPLoadStatus status) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return new TIPPFactory().openFromStream(is, status);
    }
    
    private void comparePackageParts(TIPP p1, TIPP p2) throws Exception {
        Collection<TIPPSection> s1 = p1.getSections();
        Collection<TIPPSection> s2 = p2.getSections();
        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(s1, s2);
        for (TIPPSection s : s1) {
            TIPPSectionType type = s.getType();
        	Collection<TIPPResource> o1 = s.getResources();
        	Collection<TIPPResource> o2 = p2.getSection(type).getResources();
        	assertNotNull(o1);
        	assertNotNull(o2);
        	assertEquals(o1, o2);
	        // XXX Again, this cheats slightly by assuming a particular order
            Iterator<TIPPResource> fit1 = o1.iterator();
            Iterator<TIPPResource> fit2 = o2.iterator();
            while (fit1.hasNext()) {
                TIPPResource f1 = fit1.next();
                assertTrue(fit2.hasNext());
                TIPPResource f2 = fit2.next();
                assertEquals(f1, f2);
                InputStream is1 = f1.getInputStream();
                InputStream is2 = f2.getInputStream();
                verifyBytes(is1, is2);
                is1.close();
                is2.close();
            }
        }
    }
    
    static void verifyRequestPackage(TIPP tip) {
        assertTrue(tip.isRequest());
    	verifySamplePackage(tip, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    @SuppressWarnings("serial")
    static void verifySamplePackage(TIPP tip, String packageId) {
        assertEquals(packageId, tip.getPackageId());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                        tip.getCreator());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, TIPPSectionType.BILINGUAL,
                new ArrayList<TIPPResource>() { {
                        add(new TIPPFile("Peanut_Butter.xlf", 1)); }});
        expectObjectSection(tip, TIPPSectionType.PREVIEW,
                new ArrayList<TIPPResource>() {
                    {
                        add(new TIPPFile(
                                "Peanut_Butter.html.skl", 1));
                        add(new TIPPFile(
                                "resources/20px-Padlock-silver.svg.png", 2));
                        add(new TIPPFile("resources/load.php", 3));
                        add(new TIPPFile(
                                "resources/290px-PeanutButter.jpg", 4));
                        add(new TIPPFile(
                                "resources/load(1).php", 5));
                        add(new TIPPFile(
                                "resources/magnify-clip.png", 6));
                    }
                });
    }
    
    static void verifyResponsePackage(ResponseTIPP tip) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", tip.getPackageId());
        assertEquals(new TIPPCreator("Test Testerson", "http://interoperability-now.org",
                TestTIPManifest.getDate(2011, 4, 18, 19, 3, 15), new TIPPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                        tip.getCreator());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                        tip.getRequestCreator());
        assertEquals("urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
        			 tip.getRequestPackageId());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, TIPPSectionType.BILINGUAL,
                new ArrayList<TIPPResource>() { {
                        add(new TIPPFile("Peanut_Butter.xlf", 1)); }});
    }
    
    private static void expectObjectSection(TIPP tipp,
            TIPPSectionType type, Collection<TIPPResource> files) {
        Collection<TIPPResource> found = tipp.getSection(type).getResources();
        assertNotNull(found);
        assertEquals(files, found);
    }
    
    private void verifyBytes(InputStream is1, InputStream is2) throws IOException {
        byte[] b1 = new byte[4096];
        byte[] b2 = new byte[4096];
        while (true) {
            Arrays.fill(b1, (byte)0);
            Arrays.fill(b2, (byte)0);
            int read1 = is1.read(b1);
            int read2 = is2.read(b2);
            assertEquals(read1, read2);
            if (read1 == -1) {
                break;
            }
            assertTrue(Arrays.equals(b1, b2));
        }
    }
}
