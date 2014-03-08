package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.PACKAGE_CREATOR;
import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.Creator;

public class TIPPCreator {

    private String name;
    private String id;
    private Date date;
    private TIPPTool tool = new TIPPTool();

    public TIPPCreator() { }
    
    TIPPCreator(String name, String id, Date date, TIPPTool tool) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.tool = tool;
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
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public TIPPTool getTool() {
        return tool;
    }
    public void setTool(TIPPTool tool) {
        this.tool = tool;
    }

    Element toElement(Document doc) {
        Element creatorEl = doc.createElement(PACKAGE_CREATOR);
        appendElementChildWithText(doc, 
                creatorEl, Creator.NAME, getName());
        appendElementChildWithText(doc, 
                creatorEl, Creator.ID, getId());
        appendElementChildWithText(doc, creatorEl, Creator.UPDATE, 
                TIPPFormattingUtil.writeTIPPDate(getDate()));
        creatorEl.appendChild(getTool().toElement(doc));
        return creatorEl;
    }

    /**
     * TIPCreator objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPCreator)) return false;
        TIPPCreator c = (TIPPCreator)o;
        return c.getName().equals(getName()) &&
                c.getId().equals(getId()) &&
                c.getDate().equals(getDate()) &&
                c.getTool().equals(getTool());
    }
    
    @Override
    public String toString() {
        return "TIPCreator(name=" + getName() + ", id=" + getId() +
                ", date=" + getDate() + ", tool=" + getTool() + ")";
    }
}
