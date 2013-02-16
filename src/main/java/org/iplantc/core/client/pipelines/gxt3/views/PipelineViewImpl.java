package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplant.pipeline.client.json.autobeans.Pipeline;
import org.iplantc.core.client.pipelines.gxt3.util.PipelineAutoBeanUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

/**
 * The main PipelineView implementation.
 * 
 * @author psarando
 * 
 */
public class PipelineViewImpl implements PipelineView {

    private static PipelineViewUiBinder uiBinder = GWT.create(PipelineViewUiBinder.class);
    private final Widget widget;
    private Presenter presenter;
    private Pipeline pipeline;

    @UiTemplate("PipelineView.ui.xml")
    interface PipelineViewUiBinder extends UiBinder<Widget, PipelineViewImpl> {
    }

    public PipelineViewImpl() {
        widget = uiBinder.createAndBindUi(this);

        setPipeline(PipelineAutoBeanUtil.getPipelineAutoBeanFactory().pipeline().as());

        ToggleGroup group = new ToggleGroup();
        group.add(infoBtn);
        group.add(appOrderBtn);
        group.add(mappingBtn);
    }

    @UiField
    BorderLayoutContainer borders;

    @UiField
    BorderLayoutData northData;

    @UiField
    CardLayoutContainer centerPanel;

    @UiField
    BorderLayoutContainer builderPanel;

    @UiField
    SimpleContainer builderDropWrapper;

    @UiField
    PipelineCreator builder;

    @UiField
    SimpleContainer appsContainer;

    @UiField
    BorderLayoutContainer stepEditorPanel;

    @UiField
    CardLayoutContainer stepPanel;

    @UiField
    PipelineInfoEditor infoPanel;

    @UiField
    SimpleContainer appOrderPanel;

    @UiField
    SimpleContainer mappingPanel;

    @UiField
    ToggleButton infoBtn;

    @UiField
    ToggleButton appOrderBtn;

    @UiField
    ToggleButton mappingBtn;

    @UiHandler("infoBtn")
    public void onInfoClick(SelectEvent e) {
        presenter.onInfoClick();
    }

    @UiHandler("appOrderBtn")
    public void onAppOrderClick(SelectEvent e) {
        presenter.onAppOrderClick();
    }

    @UiHandler("mappingBtn")
    public void onMappingClick(SelectEvent e) {
        presenter.onMappingClick();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Pipeline getPipeline() {
        return infoPanel.getPipeline();
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        infoPanel.setPipeline(pipeline);
    }

    @Override
    public boolean isValid() {
        return !infoPanel.isValid();
    }

    @Override
    public void setNorthWidget(IsWidget widget) {
        borders.setNorthWidget(widget, northData);
    }

    @Override
    public IsWidget getActiveView() {
        return centerPanel.getActiveWidget();
    }

    @Override
    public void setActiveView(IsWidget view) {
        centerPanel.setActiveWidget(view);
    }

    @Override
    public BorderLayoutContainer getBuilderPanel() {
        return builderPanel;
    }

    @Override
    public SimpleContainer getBuilderDropContainer() {
        return builderDropWrapper;
    }

    @Override
    public PipelineCreator getPipelineCreator() {
        return builder;
    }

    @Override
    public SimpleContainer getAppsContainer() {
        return appsContainer;
    }

    @Override
    public BorderLayoutContainer getStepEditorPanel() {
        return stepEditorPanel;
    }

    @Override
    public CardLayoutContainer getStepPanel() {
        return stepPanel;
    }

    @Override
    public IsWidget getInfoPanel() {
        return infoPanel;
    }

    @Override
    public IsWidget getAppOrderPanel() {
        return appOrderPanel;
    }

    @Override
    public IsWidget getMappingPanel() {
        return mappingPanel;
    }
}
