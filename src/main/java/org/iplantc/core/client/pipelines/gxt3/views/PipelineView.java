package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplantc.core.pipelineBuilder.client.builder.PipelineCreator;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * A View for Pipeline editors.
 * 
 * @author psarando
 * 
 */
public interface PipelineView extends IsWidget {

    public interface Presenter extends org.iplantc.core.uicommons.client.presenter.Presenter {
        Pipeline getPipeline();

        public void onInfoClick();

        public void onAppOrderClick();

        public void onMappingClick();

        public void onAddAppsClicked();

        public void onRemoveAppClicked();

        public void onMoveUpClicked();

        public void onMoveDownClicked();

        /**
         * Gets the given App's workflow step name, based on its position in the workflow and its ID.
         * 
         * @param app
         * @return the PipelineApp's step name.
         */
        public String getStepName(PipelineApp app);
    }

    void setPresenter(final Presenter presenter);

    /**
     * Checks if the current state of the Pipeline is valid.
     * 
     * @return boolean true if the Pipeline is valid, false otherwise.
     */
    public boolean isValid();

    /**
     * Gets the current state of the Pipeline.
     * 
     * @return Pipeline current state.
     */
    public Pipeline getPipeline();

    /**
     * Initializes the Pipeline from the given state.
     * 
     * @param pipeline
     */
    public void setPipeline(Pipeline pipeline);

    public void setNorthWidget(IsWidget widget);

    public IsWidget getActiveView();

    public void setActiveView(IsWidget view);

    public BorderLayoutContainer getBuilderPanel();

    public SimpleContainer getBuilderDropContainer();

    public PipelineCreator getPipelineCreator();

    public BorderLayoutContainer getStepEditorPanel();

    public SimpleContainer getAppsContainer();

    public CardLayoutContainer getStepPanel();

    public IsWidget getInfoPanel();

    public IsWidget getAppOrderPanel();

    public IsWidget getMappingPanel();

    public ListStore<PipelineApp> getPipelineAppStore();

    public PipelineApp getOrderGridSelectedApp();
}
