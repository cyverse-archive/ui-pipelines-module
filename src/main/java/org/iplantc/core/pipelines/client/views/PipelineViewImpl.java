package org.iplantc.core.pipelines.client.views;

import java.util.ArrayList;
import java.util.List;

import org.iplantc.core.pipelineBuilder.client.builder.PipelineCreator;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;
import org.iplantc.core.resources.client.messages.I18N;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.tips.ToolTip;

/**
 * The main PipelineView implementation.
 *
 * @author psarando
 *
 */
public class PipelineViewImpl implements PipelineView {

    private static PipelineViewUiBinder uiBinder = GWT.create(PipelineViewUiBinder.class);
    private final Driver driver = GWT.create(Driver.class);
    private final Widget widget;
    private Presenter presenter;

    @UiTemplate("PipelineView.ui.xml")
    interface PipelineViewUiBinder extends UiBinder<Widget, PipelineViewImpl> {
    }

    interface Driver extends SimpleBeanEditorDriver<Pipeline, PipelineViewImpl> {
    }

    public PipelineViewImpl() {
        widget = uiBinder.createAndBindUi(this);
        driver.initialize(this);

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
    @Path("")
    PipelineInfoEditor infoPanel;

    @UiField
    @Path("")
    PipelineAppOrderViewImpl appOrderPanel;

    @UiField
    @Path("apps")
    PipelineAppMappingForm mappingPanel;

    @UiField
    @Editor.Ignore
    ToggleButton infoBtn;

    @UiField
    @Editor.Ignore
    ToggleButton appOrderBtn;

    @UiField
    @Editor.Ignore
    ToggleButton mappingBtn;

    @UiField
    HtmlLayoutContainer helpContainer;

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

    @UiFactory
    public HtmlLayoutContainer buildHelpContainer() {
        return new HtmlLayoutContainer(I18N.DISPLAY.infoPnlTip());
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
    public void setPipeline(Pipeline pipeline) {
        if (pipeline.getApps() == null) {
            pipeline.setApps(new ArrayList<PipelineApp>());
        }

        driver.edit(pipeline);
    }

    @Override
    public boolean isValid() {
        return driver.flush() != null && !driver.hasErrors();
    }

    @Override
    public List<EditorError> getErrors() {
        if (driver.hasErrors()) {
            return driver.getErrors();
        }

        return null;
    }

    @Override
    public void clearInvalid() {
        clearInvalid(infoBtn);
        clearInvalid(appOrderBtn);
        clearInvalid(mappingBtn);

        infoPanel.clearInvalid();
        mappingPanel.clearInvalid();
    }

    private void clearInvalid(ToggleButton btn) {
        btn.setIcon(null);
        clearErrorTip(btn);
    }

    private void clearErrorTip(ToggleButton btn) {
        ToolTip toolTip = btn.getToolTip();
        if (toolTip != null) {
            toolTip.disable();
        }
    }

    @Override
    public Pipeline getPipeline() {
        return driver.flush();
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
    public PipelineAppOrderView getAppOrderPanel() {
        return appOrderPanel;
    }

    @Override
    @Editor.Ignore
    public PipelineAppMappingView getMappingPanel() {
        return mappingPanel;
    }

    @Override
    public ListStore<PipelineApp> getPipelineAppStore() {
        return appOrderPanel.getPipelineAppStore();
    }

    @Override
    public PipelineApp getOrderGridSelectedApp() {
        return appOrderPanel.getOrderGridSelectedApp();
    }

    @Override
    @Editor.Ignore
    public ToggleButton getInfoBtn() {
        return infoBtn;
    }

    @Override
    @Editor.Ignore
    public ToggleButton getAppOrderBtn() {
        return appOrderBtn;
    }

    @Override
    @Editor.Ignore
    public ToggleButton getMappingBtn() {
        return mappingBtn;
    }

    @Override
    public HtmlLayoutContainer getHelpContainer() {
        return helpContainer;
    }

    @Override
    public void markInfoBtnValid() {
        markValid(infoBtn);
    }

    @Override
    public void markInfoBtnInvalid(String error) {
        markInvalid(infoBtn, error);
    }

    @Override
    public void markAppOrderBtnValid() {
        markValid(appOrderBtn);
    }

    @Override
    public void markAppOrderBtnInvalid(String error) {
        markInvalid(appOrderBtn, error);
    }

    @Override
    public void markMappingBtnValid() {
        markValid(mappingBtn);
    }

    @Override
    public void markMappingBtnInvalid(String error) {
        markInvalid(mappingBtn, error);
    }

    private void markValid(ToggleButton btn) {
     //   btn.setIcon(IplantResources.ICON.stepComplete());
        clearErrorTip(btn);
    }

    private void markInvalid(ToggleButton btn, String error) {
       // btn.setIcon(IplantResources.ICON.stepError());
        btn.setToolTip(error);
        btn.getToolTip().enable();
    }
}
