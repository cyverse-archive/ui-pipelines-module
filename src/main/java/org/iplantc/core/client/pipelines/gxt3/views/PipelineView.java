package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplant.pipeline.client.json.autobeans.Pipeline;
import org.iplant.pipeline.client.json.autobeans.PipelineApp;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.grid.Grid;

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

    public Grid<PipelineApp> getAppOrderGrid();
}
