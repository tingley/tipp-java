package com.spartansoftwareinc.tipp;

import java.util.Objects;

abstract class TIPPTask {

    private TIPPTaskType taskType;
    private String sourceLocale, targetLocale;

    TIPPTask() { }
    
    public TIPPTask(TIPPTaskType taskType, String sourceLocale, String targetLocale) {
        this.taskType = taskType;
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
    }
    
    public TIPPTaskType getTaskType() {
        return taskType;
    }
    public String getSourceLocale() {
        return sourceLocale;
    }
    public String getTargetLocale() {
        return targetLocale;
    }

    /**
     * TIPCreator objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPTask)) return false;
        TIPPTask t = (TIPPTask)o;
        return Objects.equals(taskType, t.taskType) &&
               Objects.equals(sourceLocale, t.sourceLocale) &&
               Objects.equals(targetLocale, t.targetLocale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType, sourceLocale, targetLocale);
    }

    @Override
    public String toString() {
        return getTaskType() + "[" + getSourceLocale() + "->" + getTargetLocale()
                + "]";
    }
}
