package com.spartansoftwareinc.tipp;

import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests things related to resource sequencing, mostly.
 */
public class TestTIPPSection {

    @Test
    public void testClearSection() {
        TIPPSection s = new TIPPSection(TIPPSectionType.BILINGUAL);
        s.addFile("test1");
        s.addFile("test2");
        List<? extends TIPPFile> l = s.getResources();
        assertEquals(2, l.size());
        checkFile(1, "test1", "test1", l.get(0));
        checkFile(2, "test2", "test2", l.get(1));
        s.clear();
        assertNotNull(s.getResources());
        assertEquals(0, s.getResources().size());
        // Now make sure the sequence reset
        s.addFile("test1");
        l = s.getResources();
        assertEquals(1, l.size());
        checkFile(1, "test1", "test1", l.get(0));
    }

    @Test
    public void testRemoveResource() {
        TIPPSection s = new TIPPSection(TIPPSectionType.BILINGUAL);
        s.addFile("test1");
        s.addFile("test2");
        s.removeResource("test1");
        List<? extends TIPPFile> l = s.getResources();
        assertEquals(1, l.size());
        checkFile(2, "test2", "test2", l.get(0));
        
        // If we remove the remaining resource, the "next sequence" value
        // should revert to 1
        s.removeResource("test2");
        assertEquals(0, s.getResources().size());
        
        s.addFile("test3");
        assertEquals(1, s.getResources().size());
        checkFile(1, "test3", "test3", s.getResources().get(0)); 
    }
    
    private void checkFile(int sequence, String name, String location, TIPPFile f) {
        assertEquals(sequence, f.getSequence());
        assertEquals(name, f.getName());
        assertEquals(location, f.getLocation());
    }
}
