package com.spartansoftwareinc.tipp;

/**
 * Represents one of the section types allowed by the specification.
 */
public enum TIPPSectionType {
    BILINGUAL("Bilingual", "bilingual"),
    INPUT("Input", "input"),
    OUTPUT("Output", "output"),
    STS("STS", "sts"),
    TM("Tm", "tm"),
    TERMINOLOGY("Terminology", "terminology"),
    REFERENCE("Reference", "reference"),
    PREVIEW("Preview", "preview"),
    METRICS("Metrics", "metrics"),
    EXTRAS("Extras", "extras");

    private String elementName;
    private String defaultName;
    
    TIPPSectionType(String elementName, String defaultName) {
        this.elementName = elementName;
        this.defaultName = defaultName;
    }
    
    /**
     * Name of the element for this section type.
     * @return element name
     */
    public String getElementName() {
        return elementName;
    }
    
    /**
     * The default value for the @name attribute to use when
     * writing out a section of this type.
     * @return default section name
     */
    public String getDefaultName() {
        return defaultName;
    }
    
    public static TIPPSectionType byElementName(String elementName) {
        for (TIPPSectionType t : values()) {
            if (t.elementName.equals(elementName)) {
                return t;
            }
        }
        return null;
    }
}
