package org.iplantc.core.client.pipelines.views.panels;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.json.client.JSONObject;

/**
 * Defines a common view of a Pipeline Editor.
 * 
 * @author psarando
 * 
 */
public abstract class PipelineEditorView extends ContentPanel {

    public PipelineEditorView() {
        setScrollMode(Scroll.AUTO);
    }

    /**
     * Checks if the Pipeline is valid.
     * 
     * @return boolean true or false
     */
    public abstract boolean isValid();

    /**
     * Gets a JSON representation of the Pipeline.
     * 
     * @return JSONObject representing the Pipeline.
     */
    public abstract JSONObject toJson();

    /**
     * Get the JSON of this pipeline required for publishing.
     * 
     * @return JSONObject required for publishing.
     */
    public abstract JSONObject getPublishJson();

    /**
     * Initializes the Pipeline from the given JSON.
     * 
     * @param obj JSON representation of the Pipeline.
     */
    public abstract void configure(JSONObject obj);

    /**
     * Remove event handlers and free-up resources.
     */
    public abstract void cleanup();
}
