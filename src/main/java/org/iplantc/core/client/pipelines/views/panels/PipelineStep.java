package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.events.PipelineStepValidationEvent;
import org.iplantc.core.uicommons.client.events.EventBus;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * 
 * A base class for the all pipeline steps
 * 
 * @author sriram
 * 
 */
public abstract class PipelineStep extends ContentPanel {

    /**
     * Construct a new pipeline step
     * 
     * @param title a title for the pipeline step panel
     */
    public PipelineStep(String title) {
        setHeading(title);
        setFrame(false);
        setScrollMode(Scroll.AUTO);
    }

    /**
     * check if the step is valid
     * 
     * @return boolean true or false
     */
    public abstract boolean isValid();

    /**
     * Return step data as JSONValue
     * 
     * @return JSONValue containing data from that pipeline step
     */
    public abstract JSONValue toJson();

    protected void firePipelineStepValidationEvent(boolean valid) {
        PipelineStepValidationEvent event = new PipelineStepValidationEvent(valid);
        EventBus.getInstance().fireEvent(event);
    }

    /**
     * Set data for the pipeline step
     * 
     * @param pipelineConfig JSON representation of the Pipeline.
     */
    protected abstract void setData(JSONObject pipelineConfig);
}
