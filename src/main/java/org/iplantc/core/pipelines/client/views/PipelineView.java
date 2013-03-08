package org.iplantc.core.pipelines.client.views;

import java.util.List;

import org.iplantc.core.pipelineBuilder.client.builder.PipelineCreator;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * A View for Pipeline editors.
 * 
 * @author psarando
 * 
 */
public interface PipelineView extends IsWidget, Editor<Pipeline> {

    public interface Presenter extends org.iplantc.core.uicommons.client.presenter.Presenter {
        public void go(HasOneWidget container, Pipeline pipeline);

        public Pipeline getPipeline();

        public void onInfoClick();

        public void onAppOrderClick();

        public void onMappingClick();
    }

    void setPresenter(final Presenter presenter);

    /**
     * Checks if the current state of the Pipeline is valid.
     * 
     * @return boolean true if the Pipeline is valid, false otherwise.
     */
    public boolean isValid();

    public List<EditorError> getErrors();

    public void clearInvalid();

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

    public PipelineAppOrderView getAppOrderPanel();

    @Editor.Ignore
    public PipelineAppMappingView getMappingPanel();

    public ListStore<PipelineApp> getPipelineAppStore();

    public PipelineApp getOrderGridSelectedApp();

    @Editor.Ignore
    public ToggleButton getInfoBtn();

    @Editor.Ignore
    public ToggleButton getAppOrderBtn();

    @Editor.Ignore
    public ToggleButton getMappingBtn();

    public HtmlLayoutContainer getHelpContainer();

    public void markInfoBtnValid();

    public void markInfoBtnInvalid(String error);

    public void markAppOrderBtnValid();

    public void markAppOrderBtnInvalid(String error);

    public void markMappingBtnValid();

    public void markMappingBtnInvalid(String error);
}
