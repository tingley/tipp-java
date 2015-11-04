package com.spartansoftwareinc.tipp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.KeySelector;

import org.junit.*;

import static com.spartansoftwareinc.tipp.TestUtils.*;

import static org.junit.Assert.*;

@SuppressWarnings("serial")
public class TestTIPPPackage {

    public static void checkErrors(CollectingErrorHandler status,
            int expectedErrorCount) {
        if (expectedErrorCount != status.getErrors().size()) {
            System.out.println("Expected " + expectedErrorCount
                    + " errors but found " + status.getErrors().size());
            for (TIPPError error : status.getErrors()) {
                System.out.println("> " + error);
            }
        }
        assertEquals(expectedErrorCount, status.getErrors().size());
    }

    @Test
    public void testPackageLoad() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/test_package.zip", status)) {
            checkErrors(status, 0);
            verifyRequestPackage(tipp);
            for (TIPPFile file : tipp.getSection(TIPPSectionType.BILINGUAL)
                    .getFileResources()) {
                try (InputStream is = tipp.getFile(file)) {
                    // Just instantiating the input stream is the real test..
                    assertNotNull(is);
                }
            }
        }
    }

    @Test
    public void testVerifyMissingManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/missing_manifest.zip",
                status)) {
            assertNull(tipp);
            checkErrors(status, 1);
            assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
            TIPPError error = status.getErrors().get(0);
            assertEquals(TIPPErrorType.MISSING_MANIFEST, error.getErrorType());
        }
    }

    @Test
    public void testVerifyCorruptManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/corrupt_manifest.zip",
                status)) {
            assertNull(tipp);
            checkErrors(status, 1);
            assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
            assertEquals(TIPPErrorType.CORRUPT_MANIFEST,
                    status.getErrors().get(0).getErrorType());
        }
    }

    @Test
    public void testVerifyMissingPayload() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/missing_payload.zip", status)) {
            // manifest is intact, so we should get a TIPP object
            assertNotNull(tipp);
            checkErrors(status, 7);
            assertEquals(TIPPErrorSeverity.ERROR, status.getMaxSeverity());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(0).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(1).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(2).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(3).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(4).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(5).getErrorType());
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(6).getErrorType());
        }
    }

    @Test
    public void testVerifyCorruptPayloadZip() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/corrupt_payload_zip.zip",
                status)) {
            assertNotNull(tipp);
            checkErrors(status, 7);
            assertEquals(TIPPErrorSeverity.ERROR, status.getMaxSeverity());
            for (TIPPError error : status.getErrors()) {
                assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                        error.getErrorType());
            }
        }
    }

    @Test
    public void testManifestPayloadMismatch() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/manifest_payload_mismatch.zip",
                status)) {
            assertNotNull(tipp);
            checkErrors(status, 2);
            assertEquals(TIPPErrorType.MISSING_PAYLOAD_RESOURCE,
                    status.getErrors().get(0).getErrorType());
            assertEquals(TIPPErrorType.UNEXPECTED_PAYLOAD_RESOURCE,
                    status.getErrors().get(1).getErrorType());
        }
    }

    @Test
    public void testVerifyCorruptPackageZip() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/corrupt_package_zip.zip",
                status)) {
            assertNull(tipp);
            assertEquals(1, status.getErrors().size());
            assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
            TIPPError error = status.getErrors().get(0);
            assertEquals(TIPPErrorType.MISSING_MANIFEST, error.getErrorType());
        }
    }

    @Test
    public void testPackageSave() throws Exception {
        // Load the package, save it out to a zip file, read it back.
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tip = getSamplePackage("data/test_package.zip", status)) {
            assertEquals(0, status.getErrors().size());
            Path temp = Files.createTempFile("tiptest", ".zip");
            try (OutputStream os = Files.newOutputStream(temp)) {
                tip.saveToStream(os);
            }
            status = new CollectingErrorHandler();
            try (TIPP roundtrip = createFactory(status)
                    .openFromStream(Files.newInputStream(temp))) {
                assertEquals(0, status.getErrors().size());
                verifyRequestPackage(roundtrip);
                comparePackageParts(tip, roundtrip);
                Files.delete(temp);
            }
        }
    }

    @Test
    public void testResponsePackage() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tip = getSamplePackage("data/test_response_package.zip",
                status)) {
            checkErrors(status, 0);
            assertFalse(tip.isRequest());
            verifyResponsePackage((ResponseTIPP) tip);
            Path temp = Files.createTempFile("tiptest", ".zip");
            try (OutputStream os = Files.newOutputStream(temp)) {
                tip.saveToStream(os);
            }
            status = new CollectingErrorHandler();
            try (TIPP roundtrip = createFactory(status)
                    .openFromStream(Files.newInputStream(temp))) {
                assertEquals(0, status.getErrors().size());
                assertFalse(roundtrip.isRequest());
                verifyResponsePackage((ResponseTIPP) roundtrip);
                comparePackageParts(tip, roundtrip);
                Files.delete(temp);
            }
        }
    }

    @Test
    public void testNewPackage() throws Exception {
        RequestTIPP tipp = new RequestTIPPBuilder()
                .setTaskType(StandardTaskType.TRANSLATE_STRICT_BITEXT)
                .setCreator(new TIPPCreator("testname", "testid",
                        TestManifest.getDate(2011, 7, 12, 20, 35, 12),
                        new TIPPTool("jtip",
                                "http://code.google.com/p/interoperability-now",
                                "0.15")))
                .setSourceLocale("en-US").setTargetLocale("fr-FR")
                .addFile(TIPPSectionType.BILINGUAL, "test1.xlf",
                        new ByteArrayInputStream("test".getBytes("UTF-8")))
                .build();

        String requestPackageId = tipp.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));

        File temp = File.createTempFile("tiptest", ".tipp");
        OutputStream os = new FileOutputStream(temp);
        tipp.saveToStream(os);
        os.close();
        CollectingErrorHandler status = new CollectingErrorHandler();
        TIPP roundTrip = createFactory(status)
                .openFromStream(new FileInputStream(temp));
        assertEquals(0, status.getErrors().size());
        assertNotNull(roundTrip);
        assertEquals(tipp.getPackageId(), roundTrip.getPackageId());
        assertEquals(tipp.getCreator(), roundTrip.getCreator());
        assertEquals(tipp.getTaskType(), roundTrip.getTaskType());
        assertEquals(tipp.getSourceLocale(), roundTrip.getSourceLocale());
        assertEquals(tipp.getTargetLocale(), roundTrip.getTargetLocale());
        comparePackageParts(tipp, roundTrip);
        temp.delete();
        roundTrip.close();
        tipp.close();
    }

    // @Test
    public void testNewSignedPackage() throws Exception {
        RequestTIPP tipp = new RequestTIPPBuilder()
                .setTaskType(StandardTaskType.TRANSLATE_STRICT_BITEXT)
                .setCreator(new TIPPCreator("testname", "testid",
                        TestManifest.getDate(2011, 7, 12, 20, 35, 12),
                        new TIPPTool("jtipp",
                                "http://code.google.com/p/interoperability-now",
                                "0.15")))
                .setSourceLocale("en-US").setTargetLocale("fr-FR")
                .addFile(TIPPSectionType.BILINGUAL, "test1.xlf",
                        new ByteArrayInputStream("test".getBytes("UTF-8")))
                .build();

        String requestPackageId = tipp.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));

        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();

        tipp.saveToStream(os, kp);
        os.close();
        System.out.println("Wrote package to " + temp);
        CollectingErrorHandler status = new CollectingErrorHandler();
        TIPP roundTrip = createFactory(status).openFromStream(
                new FileInputStream(temp),
                KeySelector.singletonKeySelector(kp.getPublic()));
        expectLoadStatus(status, 0, TIPPErrorSeverity.NONE);
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

/*
     @Test
     public void testLocationNormalization() throws Exception {
         addLocationNormalizationFiles(new TIPPSectionImpl(TIPPSectionType.BILINGUAL));
     }

     @Test
     public void testLocalizatioNormalizationInReferenceSection() throws Exception {
         addLocationNormalizationFiles(new TIPPReferenceSection());
     }

     private void addLocationNormalizationFiles(TIPPSectionImpl section) throws Exception {
         TIPPFile file1 = section.addFile(
             "012345678901234567890123456789012345678901234567890123456789" +
             "012345678901234567890123456789012345678901234567890123456789" +
             "012345678901234567890123456789012345678901234567890123456789" +
             "012345678901234567890123456789012345678901234567890123456789" +
             "012345678901234567890123456789012345678901234567890123456789" +
             "012345678901234567890123456789012345678901234567890123456789.xlf");
         assertTrue(validLocationString(section, file1.getLocation()));
         assertTrue(validLocationString(section,
         section.addFile("a\\b").getLocation()));
         assertTrue(validLocationString(section,
         section.addFile("a&(b)c!!d").getLocation()));
         assertTrue(validLocationString(section,
         section.addFile("a.xlf").getLocation())); // ok
         assertTrue(validLocationString(section,
         section.addFile("?????_!)(**(&%@#$").getLocation()));
         assertTrue(validLocationString(section,
         section.addFile("foo/./bar/../baz").getLocation()));
     }
*/

    // XTM's initial implementation uses zip64 by default in order to allow for
    // very large packages. The TIPP spec does not specify whether this behavior
    // is correct or incorrect. This implementation allows it.
    @Test
    public void testZip64() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try (TIPP tipp = getSamplePackage("data/xtm-zip64.tipp", status)) {
            assertEquals(0, status.getErrors().size());
            TIPPSection bilingual = tipp.getBilingualSection();
            assertEquals(1, bilingual.getFileResources().size());
        }
    }

    private TIPP getSamplePackage(String path, CollectingErrorHandler status)
            throws Exception {
        InputStream is = getClass().getResourceAsStream(path);
        return createFactory(status).openFromStream(is);
    }

    private void comparePackageParts(TIPP p1, TIPP p2) throws Exception {
        Collection<TIPPSection> s1 = p1.getSections();
        Collection<TIPPSection> s2 = p2.getSections();
        assertNotNull(s1);
        assertNotNull(s2);
        for (TIPPSection s : s1) {
            TIPPSectionType type = s.getType();
            List<? extends TIPPFile> o1 = s.getFileResources();
            TIPPSection _s = p2.getSection(type);
            assertEquals(s, _s);
            List<? extends TIPPFile> o2 = _s.getFileResources();
            assertNotNull(o1);
            assertNotNull(o2);
            assertEquals(o1, o2);
            Iterator<? extends TIPPFile> fit1 = o1.iterator();
            Iterator<? extends TIPPFile> fit2 = o2.iterator();
            while (fit1.hasNext()) {
                TIPPFile f1 = fit1.next();
                assertTrue(fit2.hasNext());
                TIPPFile f2 = fit2.next();
                assertEquals(f1, f2);
                try (InputStream is1 = p1.getFile(f1);
                        InputStream is2 = p2.getFile(f2)) {
                    verifyBytes(is1, is2);
                }
            }
        }
    }

    static void verifyRequestPackage(TIPP tip) {
        assertTrue(tip.isRequest());
        verifySamplePackage(tip,
                "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    static void verifySamplePackage(TIPP tip, String packageId) {
        assertEquals(packageId, tip.getPackageId());
        assertEquals(
                new TIPPCreator("Test Company", "http://127.0.0.1/test",
                        TestManifest.getDate(2011, 4, 9, 22, 45, 0),
                        new TIPPTool("TestTool",
                                "http://interoperability-now.org/", "1.0")),
                tip.getCreator());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT, tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, TIPPSectionType.BILINGUAL,
                new ArrayList<TIPPFile>() {
                    {
                        add(new TIPPFile(TIPPSectionType.BILINGUAL,
                                "Peanut_Butter.xlf", 1));
                    }
                });
        expectObjectSection(tip, TIPPSectionType.PREVIEW,
                new ArrayList<TIPPFile>() {
                    {
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "Peanut_Butter.html.skl", 1));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/20px-Padlock-silver.svg.png", 2));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/load.php", 3));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/290px-PeanutButter.jpg", 4));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/load_1.php", 5));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/magnify-clip.png", 6));
                    }
                });
    }

    static void verifyResponsePackage(ResponseTIPP tip) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239",
                tip.getPackageId());
        assertEquals(
                new TIPPCreator("Test Testerson",
                        "http://interoperability-now.org",
                        TestManifest.getDate(2011, 4, 18, 19, 3, 15),
                        new TIPPTool("Test Workbench",
                                "http://interoperability-now.org", "2.0")),
                tip.getCreator());
        assertEquals(
                new TIPPCreator("Test Company", "http://127.0.0.1/test",
                        TestManifest.getDate(2011, 4, 9, 22, 45, 0),
                        new TIPPTool("TestTool",
                                "http://interoperability-now.org/", "1.0")),
                tip.getRequestCreator());
        assertEquals("urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
                tip.getRequestPackageId());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT,
                tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        expectObjectSection(tip, TIPPSectionType.BILINGUAL,
                new ArrayList<TIPPFile>() {
                    {
                        add(new TIPPFile(TIPPSectionType.BILINGUAL,
                                "Peanut_Butter.xlf", 1));
                    }
                });
    }

    private static void expectObjectSection(TIPP tipp, TIPPSectionType type, List<TIPPFile> files) {
        List<? extends TIPPFile> found = tipp.getSection(type)
                .getFileResources();
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
