package com.spartansoftwareinc.tipp;

import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests things related to resource sequencing, mostly.
 */
public class TestTIPPSection {

    private TIPPSection sectionWithFiles(TIPPSectionType type, String...names) {
        SectionBuilder b = new SectionBuilder(type);
        for (String name : names) {
            b.addFile(name);
        }
        return b.build();
    }

    @Test
    public void testSection() {
        TIPPSection s = sectionWithFiles(TIPPSectionType.BILINGUAL, "test1", "test2");
        List<? extends TIPPResource> l = s.getResources();
        assertEquals(2, l.size());
        checkFile(1, "test1", (TIPPFile)l.get(0));
        checkFile(2, "test2", (TIPPFile)l.get(1));
    }

    private void checkFile(int sequence, String name, TIPPFile f) {
        assertEquals(sequence, f.getSequence());
        assertEquals(name, f.getName());
    }
}
