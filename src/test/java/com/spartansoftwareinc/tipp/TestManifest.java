package com.spartansoftwareinc.tipp;

import org.junit.*;

import static com.spartansoftwareinc.tipp.TIPPErrorType.*;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.KeySelector;

public class TestManifest {

	@Test
	public void testEmptyManifest() throws Exception {
		Manifest manifest = new ManifestBuilder().build();
		assertNotNull(manifest.getCreator());
		assertNotNull(manifest.getCreator().getTool());
		assertNotNull(manifest.getSections());
	}

	private Manifest loadManifestFromResource(String resource, TIPPErrorHandler status) throws IOException {
	    return new ManifestLoader()
	            .loadFromStream(getClass().getResourceAsStream(resource), status);
	}

    @Test
    public void testManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();   
        Manifest manifest = loadManifestFromResource("data/peanut_butter.xml", status);
        TestUtils.expectLoadStatus(status, 0, TIPPErrorSeverity.NONE);
        verifyRequestManifest(manifest);
    }

    @Test
    public void testInvalidResponseMessage() throws Exception {
        Manifest manifest = null;
        CollectingErrorHandler status = new CollectingErrorHandler();
        try {
            manifest = loadManifestFromResource("data/invalid_repsonse_message.xml", status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertNull(manifest);
        assertEquals(1, status.getErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
        assertEquals(INVALID_MANIFEST, status.getErrors().get(0).getErrorType());
    }
    
    @Test
    public void testInvalidSequenceValue() throws Exception {
        Manifest manifest = null;
        CollectingErrorHandler status = new CollectingErrorHandler();
        try {
            manifest = loadManifestFromResource("data/invalid_sequence.xml", status);
        }
        catch (ReportedException e) {
            // expected
        }
        // This shows up as a validation error
        assertNull(manifest);
        assertEquals(1, status.getErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
        assertEquals(INVALID_MANIFEST, status.getErrors().get(0).getErrorType());
    }

    @Test
    public void testCustomTaskType() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/custom_task.xml", status);
        assertNotNull(manifest);
        assertEquals(0, status.getErrors().size());
        assertEquals(new CustomTaskType("http://spartansoftware.com/tasks/test", TIPPSectionType.ALL_SECTIONS),
                     manifest.getTask().getTaskType());
    }
    
    @Test
    public void testDuplicateSectionInManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try {
            loadManifestFromResource("data/duplicate_section_request.xml", status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getErrors().size());
        assertEquals(TIPPErrorSeverity.ERROR, status.getMaxSeverity());
        assertEquals(DUPLICATE_SECTION_IN_MANIFEST, status.getErrors().get(0).getErrorType());
    }
    
    @Test
    public void testDuplicateResourcesInManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/duplicate_resources.xml", status);
        Map<String, Path> files = Collections.singletonMap("bilingual/Peanut_Butter.xlf", Paths.get("/"));
        new PayloadValidator().validate(manifest, new Payload(files), status);
        TestTIPPackage.checkErrors(status, 1);
        assertEquals(DUPLICATE_RESOURCE_LOCATION_IN_MANIFEST, 
                status.getErrors().get(0).getErrorType());
    }

    @Test
    public void testDuplicateResourceSequencesInManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        loadManifestFromResource("data/duplicate_sequences.xml", status);
        TestTIPPackage.checkErrors(status, 1);
        assertEquals(DUPLICATE_RESOURCE_SEQUENCE_IN_MANIFEST, 
                status.getErrors().get(0).getErrorType());
    }

    @Test
    public void testInvalidLocationsInManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        loadManifestFromResource("data/invalid_location.xml", status);
        TestTIPPackage.checkErrors(status, 7);
        for (int i = 0; i < 7; i++) {
            assertEquals(INVALID_RESOURCE_LOCATION_IN_MANIFEST,
                        status.getErrors().get(i).getErrorType());
        }
    }

    @Test
    public void testInvalidSectionInManifest() throws Exception {
        Manifest manifest = null;
        CollectingErrorHandler status = new CollectingErrorHandler();
        try {
            manifest = loadManifestFromResource("data/invalid_section_request.xml", status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertNull(manifest);
        assertEquals(1, status.getErrors().size());
        assertEquals(INVALID_MANIFEST, status.getErrors().get(0).getErrorType());
        assertEquals(TIPPErrorSeverity.FATAL, status.getMaxSeverity());
    }

    @Test
    public void testInvalidSectionForTaskType() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        try {
            loadManifestFromResource("data/invalid_section_strict_bitext.xml", status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getErrors().size());
        assertEquals(INVALID_SECTION_FOR_TASK, status.getErrors().get(0).getErrorType());
        assertEquals(TIPPErrorSeverity.ERROR, status.getMaxSeverity());
    }
    
    @Test
    public void testManifestSave() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/peanut_butter.xml", status);
        assertEquals(0, status.getErrors().size());
        status = new CollectingErrorHandler();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getErrors().size());
        verifyRequestManifest(roundtrip);
    }

    @Test
    public void testResponseManifest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/peanut_butter_response.xml", status);
        assertEquals(0, status.getErrors().size());
        verifySampleResponseManifest(manifest);
        status = new CollectingErrorHandler();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getErrors().size());
        verifySampleResponseManifest(roundtrip);
    }

    @Test
    public void testResponseCreationFromRequest() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        TIPP requestPackage = getSamplePackage("data/test_package.zip", status);
        TestTIPPackage.checkErrors(status, 0);
        ResponseTIPPBuilder responseBuilder = new ResponseTIPPBuilder((RequestTIPP) requestPackage);
        ResponseTIPP responsePackage = responseBuilder.build();
        assertFalse(responsePackage.isRequest());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT, responsePackage.getTaskType());
        assertEquals("en-US", responsePackage.getSourceLocale());
        assertEquals("fr-FR", responsePackage.getTargetLocale());
        // Make sure the internal object was set correctly
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT, responsePackage.getTaskType());
        assertEquals(requestPackage.getCreator(), responsePackage.getRequestCreator());
        assertEquals(requestPackage.getPackageId(), responsePackage.getRequestPackageId());
    }
    
    // Disabled - signatures are currently broken for some reason
    //@Test
    public void testManifestSignature() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/peanut_butter.xml", status);
        assertEquals(0, status.getErrors().size());
        status = new CollectingErrorHandler();
        File temp = File.createTempFile("tipp", ".xml");
        System.out.println("Using: " + temp);
        FileOutputStream fos = new FileOutputStream(temp);
        ManifestWriter mw = new ManifestWriter();
        // Generate a dummy keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        mw.setKeyPair(kp);
        mw.saveToStream(manifest, fos);
        fos.flush();
        fos.close();
        
        // Make sure that the signer gives us a warning if the signature
        // exists but we don't provide the key
        CollectingErrorHandler roundtripStatus = new CollectingErrorHandler();
        FileInputStream fis = new FileInputStream(temp);
        new ManifestLoader().loadFromStream(fis, roundtripStatus);
        TestUtils.expectLoadStatus(roundtripStatus, 1, TIPPErrorSeverity.WARN);
        assertEquals(UNABLE_TO_VERIFY_SIGNATURE, 
                roundtripStatus.getErrors().get(0).getErrorType());
        
        // Now verify the signature for real
        roundtripStatus = new CollectingErrorHandler();
        fis = new FileInputStream(temp);
        new ManifestLoader().loadFromStream(fis, roundtripStatus, 
                KeySelector.singletonKeySelector(kp.getPublic()), null);
        TestUtils.expectLoadStatus(roundtripStatus, 0, TIPPErrorSeverity.NONE);
    }
    
    @Test
    public void testNewManifest() throws Exception {
        ManifestBuilder manifestBuilder = new ManifestBuilder();
        manifestBuilder.setTaskType(StandardTaskType.TRANSLATE_STRICT_BITEXT);
        manifestBuilder.setPackageId("urn:uuid:12345");
        manifestBuilder.setCreator(new TIPPCreator("Test", "Test Testerson", getDate(
                2011, 3, 14, 6, 55, 11), new TIPPTool("TestTool", "urn:test",
                "1.0")));
        manifestBuilder.setSourceLocale("en-US");
        manifestBuilder.setTargetLocale("jp-JP");
        // Add a section
        manifestBuilder.addFile(TIPPSectionType.BILINGUAL, "test.xlf");
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = manifestBuilder.build();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getErrors().size());
        assertEquals("urn:uuid:12345", roundtrip.getPackageId());
        assertEquals(manifest.getCreator(), roundtrip.getCreator());
        assertEquals(manifest.getTask(), roundtrip.getTask());
        expectObjectSection(roundtrip, 
                TIPPSectionType.BILINGUAL,
                Collections.singletonList(new TIPPFile(TIPPSectionType.BILINGUAL, "test.xlf", 1)));
    }

    @Test
    public void testReferenceResources() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = new ManifestLoader()
            .loadFromStream(getClass().getResourceAsStream(
                "data/reference-request.xml"), status);
        TestTIPPackage.checkErrors(status, 0);
        TIPPReferenceSection refSection = manifest.getReferenceSection();
        assertNotNull(refSection);
        // TODO: more tests
    }

    @Test
    public void testSectionOrdering() throws Exception {
        CollectingErrorHandler status = new CollectingErrorHandler();
        Manifest manifest = loadManifestFromResource("data/out_of_order_resources.xml", status);
        TestTIPPackage.checkErrors(status, 0);
        TIPPSection section = manifest.getSection(TIPPSectionType.BILINGUAL);
        assertNotNull(section);
        List<TIPPFile> l = Arrays.asList(section.getResources().toArray(new TIPPFile[0]));
        assertEquals(2, l.size());
        assertEquals("1.xlf", manifest.getLocationForFile((TIPPFile)l.get(0)));
        assertEquals("2.xlf", manifest.getLocationForFile((TIPPFile)l.get(1)));
    }

    private Manifest roundtripManifest(Manifest src, CollectingErrorHandler status) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ManifestWriter().saveToStream(src, output);
        // TODO: write it out to take a look
        // - failing at a minimum because I'm putting the task inside the descriptor
        Manifest roundtrip = new ManifestLoader()
            .loadFromStream(new ByteArrayInputStream(output.toByteArray()), status);
        return roundtrip;
    }

    static void verifyRequestManifest(Manifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    static void verifyResponseManifest(Manifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239");
    }

    @SuppressWarnings("serial")
    static void verifySampleManifest(Manifest manifest, String packageId) {
        assertEquals(packageId, manifest.getPackageId());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),manifest.getCreator());
        assertEquals(new TIPPTaskRequest(StandardTaskType.TRANSLATE_STRICT_BITEXT,
                     "en-US", "fr-FR"), manifest.getTask());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(manifest, TIPPSectionType.BILINGUAL, Collections.singletonList(
                        new TIPPFile(TIPPSectionType.BILINGUAL, "Peanut_Butter.xlf", 1)));
        expectObjectSection(manifest, TIPPSectionType.PREVIEW,
                new ArrayList<TIPPFile>() {
                    {
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "Peanut_Butter.html.skl", 1));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/20px-Padlock-silver.svg.png", 2));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,"resources/load.php", 3));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/290px-PeanutButter.jpg", 4));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/load_1.php", 5));
                        add(new TIPPFile(TIPPSectionType.PREVIEW,
                                "resources/magnify-clip.png", 6));
                    }
                });
    }

    static void verifySampleResponseManifest(Manifest manifest) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", 
                     manifest.getPackageId());
        assertEquals(new TIPPCreator("Test Testerson", "http://interoperability-now.org",
                getDate(2011, 4, 18, 19, 03, 15), new TIPPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                manifest.getCreator());
        // Then verify the response
        assertNotNull(manifest.getTask());
        assertTrue(manifest.getTask() instanceof TIPPTaskResponse);
        assertEquals(new TIPPTaskResponse(
                        StandardTaskType.TRANSLATE_STRICT_BITEXT,
                        "en-US", 
                        "fr-FR",
                        "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
                        new TIPPCreator("Test Company",
                                        "http://127.0.0.1/test",
                                        getDate(2011, 4, 9, 22, 45, 0),
                                        new TIPPTool("TestTool", "http://interoperability-now.org/", "1.0")),
                        TIPPResponseCode.Success, ""),
                     manifest.getTask());
        assertEquals(new TIPPCreator("Test Testerson", 
                                "http://interoperability-now.org", 
                                getDate(2011, 4, 18, 19, 3, 15), 
                                new TIPPTool("Test Workbench", 
                                        "http://interoperability-now.org", "2.0")),
                     manifest.getCreator());
        TIPPTaskResponse response = ((TIPPTaskResponse)manifest.getTask());
        assertEquals(TIPPResponseCode.Success, 
                response.getMessage());
        assertEquals("", response.getComment());
        // TODO: verify response
    }

    /**
     * This follows the Calendar.set() parameter conventions. Note that month is
     * zero-indexed!
     */
    public static Date getDate(int y, int mon, int d, int h, int min, int s) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(0); // Zero out the ms field or comparison may fail!
        c.set(y, mon, d, h, min, s); // note 0-indexed month
        return c.getTime();
    }

    private static void expectObjectSection(Manifest manifest,
            TIPPSectionType type, List<TIPPFile> files) {
        TIPPSection section = manifest.getSection(type);
        assertNotNull(section);
        assertEquals(type, section.getType());
        assertEquals(files, new ArrayList<TIPPResource>(section.getResources()));
    }
    
    private TIPP getSamplePackage(String path, CollectingErrorHandler status) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return TestUtils.createFactory(status).openFromStream(is);
    }
}
