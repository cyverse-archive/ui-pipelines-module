package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.events.PipelineStepValidationEvent;
import org.iplantc.core.uicommons.client.events.EventBus;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.json.client.JSONValue;

/**
 * 
 * @author sriram
 * 
 */
public abstract class PipelineStep extends ContentPanel {

    public PipelineStep(String title) {
        setHeading(title);
        setFrame(false);
        setScrollMode(Scroll.AUTO);
    }

    public abstract boolean isValid();

    public abstract JSONValue toJson();

    protected void firePipelineStepValidationEvent(boolean valid) {
        PipelineStepValidationEvent event = new PipelineStepValidationEvent(valid);
        EventBus.getInstance().fireEvent(event);
    }
}
