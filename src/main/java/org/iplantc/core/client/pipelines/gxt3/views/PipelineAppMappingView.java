package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;

/**
 * A View for displaying and editing Pipeline Output to Input mappings.
 * 
 * @author psarando
 * 
 */
public interface PipelineAppMappingView extends PipelineStepEditorView {

    public interface Presenter {
        /**
         * Sets a mapping for targetStep's Input DataObject, with the given targetInputId, to
         * sourceStep's Output DataObject with the given sourceOutputId. A null sourceOutputId will clear
         * the mapping for the given targetInputId.
         * 
         * @param targetStep
         * @param targetInputId
         * @param sourceStep
         * @param sourceOutputId
         */
        public void setInputOutputMapping(PipelineApp targetStep, String targetInputId,
                PipelineApp sourceStep, String sourceOutputId);
    }

    public void setPresenter(Presenter presenter);
}
