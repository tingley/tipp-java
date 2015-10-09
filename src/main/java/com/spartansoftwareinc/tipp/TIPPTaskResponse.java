package com.spartansoftwareinc.tipp;

import static com.spartansoftwareinc.tipp.TIPPConstants.TASK_RESPONSE;
import static com.spartansoftwareinc.tipp.TIPPConstants.UNIQUE_PACKAGE_ID;
import static com.spartansoftwareinc.tipp.XMLUtil.appendElementChildWithText;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spartansoftwareinc.tipp.TIPPConstants.TaskResponse;

class TIPPTaskResponse extends TIPPTask {
    private String requestPackageId;
    private TIPPCreator requestCreator;
    private TIPPResponseCode message;
    private String comment;
    

    TIPPTaskResponse() { super(); }
    
    public TIPPTaskResponse(String taskType, String sourceLocale, String targetLocale,
                            String requestPackageId, TIPPCreator requestCreator,
                            TIPPResponseCode message, String comment) {
        super(taskType, sourceLocale, targetLocale);
        this.requestPackageId = requestPackageId;
        this.requestCreator = requestCreator;
        this.message = message;
        this.comment = comment;
    }
    
    /**
     * Create a response header based on an existing request Manifest.
     * @param request
     */
    public TIPPTaskResponse(TIPPTaskRequest request, 
    		String requestPackageId, TIPPCreator requestCreator) {
    	super(request.getTaskType(), request.getSourceLocale(), 
    		  request.getTargetLocale());
    	this.requestPackageId = requestPackageId;
    	this.requestCreator = requestCreator;
    }

    public String getRequestPackageId() {
        return requestPackageId;
    }

    public void setRequestPackageId(String requestPackageId) {
        this.requestPackageId = requestPackageId;
    }

    public TIPPCreator getRequestCreator() {
        return requestCreator;
    }

    public void setRequestCreator(TIPPCreator requestCreator) {
        this.requestCreator = requestCreator;
    }

    public TIPPResponseCode getMessage() {
        return message;
    }

    public void setMessage(TIPPResponseCode message) {
        this.message = message;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    Element toElement(Document doc) {
        Element responseEl = doc.createElement(TASK_RESPONSE);
        responseEl.appendChild(makeInResponseTo(doc));
        appendElementChildWithText(doc, responseEl,
                TaskResponse.MESSAGE, getMessage().toString());
        String comment = getComment() != null ? getComment() : "";
        appendElementChildWithText(doc, responseEl,
                TaskResponse.COMMENT, comment);        
        return responseEl;
    }

    private Element makeInResponseTo(Document doc) {
        Element inReEl = doc.createElement(TaskResponse.IN_RESPONSE_TO);
        addTaskData(doc, inReEl);
        appendElementChildWithText(doc, inReEl,
                UNIQUE_PACKAGE_ID, getRequestPackageId());
        inReEl.appendChild(getRequestCreator().toElement(doc));
        return inReEl;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o) || 
            !(o instanceof TIPPTaskResponse)) return false;
        TIPPTaskResponse r = (TIPPTaskResponse)o;
        return Objects.equals(message, r.message) &&
               Objects.equals(comment, r.comment) &&
               Objects.equals(requestPackageId, r.requestPackageId) &&
               Objects.equals(requestCreator, r.requestCreator);
    }
    
    @Override
    public String toString() {
        return "TaskResponse(task=" + super.toString() + ", message=" + getMessage() +
                ", commment='" + getComment() + "', requestId=" + 
                getRequestPackageId() + ", requestCreator=" + requestCreator + ")";
    }

}
