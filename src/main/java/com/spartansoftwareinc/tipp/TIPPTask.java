package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.Task;

abstract class TIPPTask {

    private String taskType;
    private String sourceLocale, targetLocale;

    TIPPTask() { }
    
    public TIPPTask(String taskType, String sourceLocale, String targetLocale) {
        this.taskType = taskType;
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
    }
    
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public String getSourceLocale() {
        return sourceLocale;
    }
    public void setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
    }
    public String getTargetLocale() {
        return targetLocale;
    }
    public void setTargetLocale(String targetLocale) {
        this.targetLocale = targetLocale;
    }

    abstract Element toElement(Document doc);

    protected Element addTaskData(Document doc, Element el) {
        appendElementChildWithText(doc, el, 
                Task.TYPE, getTaskType());
        appendElementChildWithText(doc, el, 
                Task.SOURCE_LANGUAGE, getSourceLocale());        
        appendElementChildWithText(doc, el, 
                Task.TARGET_LANGUAGE, getTargetLocale());
        return el;
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
