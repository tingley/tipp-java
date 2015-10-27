package com.spartansoftwareinc.tipp;

import java.util.Date;
import java.util.Objects;

public class TIPPCreator {

    private String name;
    private String id;
    private Date date;
    private TIPPTool tool;

    TIPPCreator(String name, String id, Date date, TIPPTool tool) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.tool = tool;
    }
    
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public Date getDate() {
        return date;
    }
    public TIPPTool getTool() {
        return tool;
    }

    /**
     * TIPCreator objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPCreator)) return false;
        TIPPCreator c = (TIPPCreator)o;
        return Objects.equals(name, c.name) &&
               Objects.equals(id, c.id) &&
               Objects.equals(date, c.date) &&
               Objects.equals(tool, c.tool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, date, tool);
    }

    @Override
    public String toString() {
        return "TIPCreator(name=" + getName() + ", id=" + getId() +
                ", date=" + getDate() + ", tool=" + getTool() + ")";
    }
}
