package org.iplantc.core.client.pipelines.gxt3.views;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.IsWidget;
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
        JSONObject toJson();

        public void onInfoClick();

        public void onAppOrderClick();

        public void onMappingClick();
    }

    void setPresenter(final Presenter presenter);

    public void setNorthWidget(IsWidget widget);

    public IsWidget getActiveView();

    public void setActiveView(IsWidget view);

    public SimpleContainer getBuilderPanel();

    public BorderLayoutContainer getStepEditorPanel();

    public CardLayoutContainer getStepPanel();

    public SimpleContainer getInfoPanel();

    public SimpleContainer getAppOrderPanel();

    public SimpleContainer getMappingPanel();

    public void setInfoButtonPressed(boolean pressed);

    public void setAppOrderButtonPressed(boolean pressed);

    public void setMappingButtonPressed(boolean pressed);
}
