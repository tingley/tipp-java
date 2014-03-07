package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.TASK_REQUEST;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class TIPPTaskRequest extends TIPPTask {

    TIPPTaskRequest() { super(); }
    
    public TIPPTaskRequest(String taskType, String sourceLocale, String targetLocale) {
        super(taskType, sourceLocale, targetLocale);
    }

    @Override
    Element toElement(Document doc) {
        Element requestEl = doc.createElement(TASK_REQUEST);
        return addTaskData(doc, requestEl);
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o) && 
                (o instanceof TIPPTaskRequest);
    }
    
    @Override
    public String toString() {
        return "TaskRequest(" + super.toString() + ")";
    }
}
