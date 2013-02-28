package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A common IsWidget interface for each step in the PipelineView.
 * 
 * @author psarando
 * 
 */
public interface PipelineStepEditorView extends IsWidget {

    /**
     * Initializes this step from the given pipeline.
     * 
     * @param pipeline
     */
    public void setPipeline(Pipeline pipeline);

    /**
     * Checks if this step of the Pipeline is valid.
     * 
     * @return boolean true if this step of the Pipeline is valid, false otherwise.
     */
    public boolean isValid();
}
