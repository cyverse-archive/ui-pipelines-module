package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ListStore;

/**
 * A PipelineStepEditorView for adding, removing, and ordering Apps in the Pipeline.
 * 
 * @author psarando
 * 
 */
public interface PipelineAppOrderView extends IsWidget {
    public interface Presenter extends org.iplantc.core.uicommons.client.presenter.Presenter {

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

    public void setPresenter(Presenter presenter);

    public ListStore<PipelineApp> getPipelineAppStore();

    public PipelineApp getOrderGridSelectedApp();
}
