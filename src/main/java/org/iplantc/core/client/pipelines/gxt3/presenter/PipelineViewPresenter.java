package org.iplantc.core.client.pipelines.gxt3.presenter;

import org.iplant.pipeline.client.json.autobeans.Pipeline;
import org.iplant.pipeline.client.json.autobeans.PipelineAutoBeanFactory;
import org.iplantc.core.client.pipelines.gxt3.views.PipelineView;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbar;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbarImpl;
import org.iplantc.core.uicommons.client.presenter.Presenter;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * The Presenter for the Pipeline View.
 * 
 * @author psarando
 * 
 */
public class PipelineViewPresenter implements Presenter, PipelineView.Presenter,
        PipelineViewToolbar.Presenter {

    private final PipelineView view;
    private final PipelineViewToolbar toolbar;
    private final Command onPublishCallback;
    private final PipelineAutoBeanFactory factory = GWT.create(PipelineAutoBeanFactory.class);

    public PipelineViewPresenter(PipelineView view, Command onPublishCallback) {
        this.view = view;
        this.onPublishCallback = onPublishCallback;

        toolbar = new PipelineViewToolbarImpl();

        view.setPresenter(this);
        toolbar.setPresenter(this);

        view.setNorthWidget(toolbar);
    }

    @Override
    public void go(HasOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onPublishClicked() {
        if (onPublishCallback != null) {
            onPublishCallback.execute();
        }
    }

    @Override
    public void onSwapViewClicked() {
        IsWidget activeView = view.getActiveView();

        if (activeView == view.getStepEditorPanel()) {
            activeView = view.getBuilderPanel();
        } else {
            activeView = view.getStepEditorPanel();
        }

        view.setActiveView(activeView);
    }

    @Override
    public Pipeline getPipeline() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoClick() {
        view.getStepPanel().setActiveWidget(view.getInfoPanel());
        view.setAppOrderButtonPressed(false);
        view.setMappingButtonPressed(false);
    }

    @Override
    public void onAppOrderClick() {
        view.getStepPanel().setActiveWidget(view.getAppOrderPanel());
        view.setInfoButtonPressed(false);
        view.setMappingButtonPressed(false);
    }

    @Override
    public void onMappingClick() {
        view.getStepPanel().setActiveWidget(view.getMappingPanel());
        view.setInfoButtonPressed(false);
        view.setAppOrderButtonPressed(false);
    }
}
