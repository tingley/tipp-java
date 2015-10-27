package com.spartansoftwareinc.tipp;

import java.util.Objects;

public class TIPPTool {

    private String name;
    private String id;
    private String version;

    public TIPPTool(String name, String id, String version) {
        this.name = name;
        this.id = id;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
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
    public int hashCode() {
        return Objects.hash(name, id, version);
    }

    @Override
    public String toString() {
        return "TIPTool(name='" + name + "', id='" + id + "', version='" + version + "')";
    }
}
