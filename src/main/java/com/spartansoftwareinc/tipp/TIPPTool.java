package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.TOOL;
import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.ContributorTool;

public class TIPPTool {

    private String name;
    private String id;
    private String version;
    
    TIPPTool() { }

    public TIPPTool(String name, String id, String version) {
        this.name = name;
        this.id = id;
        this.version = version;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    Element toElement(Document doc) {
        Element toolEl = doc.createElement(TOOL);
        appendElementChildWithText(doc,
                toolEl, ContributorTool.NAME, getName());
        appendElementChildWithText(doc,
                toolEl, ContributorTool.ID, getId());
        appendElementChildWithText(doc,
                toolEl, ContributorTool.VERSION, getVersion());
        return toolEl;
    }

    /**
     * TIPTool objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPTool)) return false;
        TIPPTool t = (TIPPTool)o;
        return Objects.equals(name, t.name) &&
               Objects.equals(id, t.id) &&
               Objects.equals(version, t.version);
    }
    
    @Override
    public String toString() {
        return "TIPTool(name='" + name + "', id='" + id + 
                    "', version='" + version + "')";
    }
}
