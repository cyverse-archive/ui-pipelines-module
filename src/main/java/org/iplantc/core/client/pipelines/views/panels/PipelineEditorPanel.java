package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.images.Resources;
import org.iplantc.core.client.pipelines.models.PipelineAppModel;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.uiapplications.client.Services;
import org.iplantc.core.uiapplications.client.events.AppGroupCountUpdateEvent;
import org.iplantc.core.uiapplications.client.services.AppUserServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.core.uicommons.client.models.UserInfo;
import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IplantInfoBox;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.CardPanel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A panel that contains all individual Pipeline panels and acts as their controller.
 * 
 * @author psarando
 * 
 */
public class PipelineEditorPanel extends ContentPanel {

    private static final String ID_BTN_PUBLISH = "idBtnPublish"; //$NON-NLS-1$

    private CardPanel pnlMain;
    private PipelineBuilderPanel pnlBuilder;
    private PipelineEditorView pnlStepEditor;

    private final AbstractCatalogCategoryPanel categoryPanel;
    private final AppUserServiceFacade service;
    private final Command publishCallback;
    private final String tag;

    public PipelineEditorPanel(String tag, AbstractCatalogCategoryPanel categoryPanel,
            Command publishCallback) {
        this.tag = tag;
        this.categoryPanel = categoryPanel;
        this.service = Services.USER_APP_SERVICE;
        this.publishCallback = publishCallback;
        init();
        compose();
    }

    private void init() {
        pnlMain = new CardPanel();
        pnlMain.setScrollMode(Scroll.NONE);

        pnlBuilder = new PipelineBuilderPanel(tag, categoryPanel, service);
        pnlStepEditor = new PipelineStepEditorPanel(tag, categoryPanel, service);

        pnlMain.add(pnlBuilder);
        pnlMain.add(pnlStepEditor);

        initLayout();
        setHeaderVisible(false);

        setBottomComponent(buildToolBar());
    }

    private ToolBar buildToolBar() {
        ToolBar toolbar = new ToolBar();

        toolbar.add(buildSwitchViewButton());
        toolbar.add(new FillToolItem());
        toolbar.add(buildPublishButton());

        return toolbar;
    }

    private Button buildPublishButton() {
        Button btnPublish = new Button(I18N.DISPLAY.publishToWorkspace());

        btnPublish.setId(ID_BTN_PUBLISH);
        btnPublish.setIcon(AbstractImagePrototype.create(Resources.ICONS.publish()));
        btnPublish.addSelectionListener(new PublishButtonSelectionListener());

        return btnPublish;
    }

    private Button buildSwitchViewButton() {
        Button btnPublish = new Button("Switch View");

        btnPublish.setId("idBtnSwitchView");
        btnPublish.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                PipelineEditorView pnlActive = getActiveView();
                JSONObject pipelineState = pnlActive.toJson();

                if (pnlActive == pnlBuilder) {
                    pnlActive = pnlStepEditor;
                } else {
                    pnlActive = pnlBuilder;
                }

                pnlMain.setActiveItem(pnlActive);
                pnlActive.configure(pipelineState);
            }
        });

        return btnPublish;
    }

    private void initLayout() {
        setLayout(new FitLayout());
    }

    private void compose() {
        add(pnlMain);
    }

    /**
     * @return The active PipelineEditorView currently displayed by the presenter.
     */
    private PipelineEditorView getActiveView() {
        return (PipelineEditorView)pnlMain.getActiveItem();
    }

    private final class PublishButtonSelectionListener extends SelectionListener<ButtonEvent> {

        @Override
        public void componentSelected(ButtonEvent ce) {
            if (!getActiveView().isValid()) {
                ErrorHandler.post(I18N.ERROR.workflowValidationError());
                return;
            }

            final Button btnPublish = (Button)ce.getSource();

            btnPublish.disable();

            JSONObject publishJson = getPublishJson();
            if (publishJson == null) {
                ErrorHandler.post(I18N.ERROR.workflowPublishError());
                return;
            }

            service.publishWorkflow(publishJson.toString(), new AsyncCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    new IplantInfoBox(I18N.DISPLAY.publishToWorkspace(), I18N.DISPLAY.publishWorkflowSuccess()).show();
                    AppGroupCountUpdateEvent event = new AppGroupCountUpdateEvent(true, null);
                    EventBus.getInstance().fireEvent(event);
                    if (publishCallback != null) {
                        publishCallback.execute();
                    }

                    btnPublish.enable();
                }

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(I18N.ERROR.workflowPublishError(), caught);
                    btnPublish.enable();
                }
            });

        }
    }

    /**
     * Get the JSON of this pipeline required for publishing.
     * 
     * @return JSONObject required for publishing.
     */
    public JSONObject getPublishJson() {
        JSONObject ret = new JSONObject();

        JSONObject publishJson = getActiveView().getPublishJson();

        if (publishJson == null) {
            // something went wrong, abort
            return null;
        }

        publishJson.put("implementation", getImplementorDetails()); //$NON-NLS-1$
        publishJson.put("full_username", new JSONString(UserInfo.getInstance().getFullUsername())); //$NON-NLS-1$

        JSONArray analyses = new JSONArray();
        analyses.set(0, publishJson);
        ret.put("analyses", analyses); //$NON-NLS-1$

        return ret;
    }

    private JSONObject getImplementorDetails() {
        UserInfo user = UserInfo.getInstance();
        JSONObject obj = new JSONObject();
        obj.put("implementor_email", new JSONString(user.getEmail())); //$NON-NLS-1$
        obj.put("implementor", new JSONString(user.getUsername())); //$NON-NLS-1$
        JSONObject params = new JSONObject();
        params.put("params", new JSONArray()); //$NON-NLS-1$
        obj.put("test", params); //$NON-NLS-1$
        return obj;
    }

    /**
     * 
     * Remove event handlers and free-up resources
     * 
     */
    public void cleanup() {
        pnlStepEditor.cleanup();
        pnlBuilder.cleanup();
    }

    /**
     * get json representation of the form data
     * 
     * @return JSONObject containing form data
     */
    public JSONObject toJson() {
        return getActiveView().toJson();
    }

    /**
     * Restore state from the given json representation of the form data.
     * 
     * @param obj json representation of the form data
     */
    public void configure(JSONObject obj) {
        getActiveView().configure(obj);
    }
}
