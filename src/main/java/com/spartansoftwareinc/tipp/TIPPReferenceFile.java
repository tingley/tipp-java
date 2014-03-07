package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.FILE_RESOURCE;
import static com.spartansoftwareinc.tipp.TIPPConstants.REFERENCE_FILE_RESOURCE;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.ObjectFile;

public class TIPPReferenceFile extends TIPPFile {

    public enum LanguageChoice {
        source,
        target;
    }
    
    private LanguageChoice languageChoice;
    
    TIPPReferenceFile() {
        super();
    }
    
    TIPPReferenceFile(String location, String name, int sequence) {
        super(location, name, sequence);
    }
    
    public LanguageChoice getLanguageChoice() {
        return languageChoice;
    }
    
    public void setLanguageChoice(LanguageChoice choice) {
        this.languageChoice = choice;
    }

    Element toElement(Document doc) {
        Element el = doc.createElement(REFERENCE_FILE_RESOURCE);
        if (getLanguageChoice() != null) {
            el.setAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE, 
                            getLanguageChoice().name());
        }
        return addChildren(doc, el);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 +
            (languageChoice != null ? languageChoice.hashCode() : 0);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPReferenceFile)) {
            return false;
        }
        TIPPReferenceFile f = (TIPPReferenceFile)o;
        if (((f.languageChoice == null && languageChoice == null) ||
             (f.languageChoice != null && f.languageChoice.equals(languageChoice))) &&
            super.equals(o)) {
            return true;
        }
        return false;
    }
}
