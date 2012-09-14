package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.images.Resources;
import org.iplantc.core.uiapplications.client.events.AnalysisGroupCountUpdateEvent;
import org.iplantc.core.uiapplications.client.services.AppTemplateUserServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.core.uicommons.client.models.UserInfo;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
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
    protected BorderLayoutData dataCenter;
    protected BorderLayoutData dataEast;

    private PipelineBuilderPanel pnlMapping;

    private final AbstractCatalogCategoryPanel categoryPanel;
    private final AppTemplateUserServiceFacade service;
    private ToolBar toolbar;
    private final Command publishCallback;
    private final String tag;

    public PipelineEditorPanel(String tag, AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateUserServiceFacade service, Command publishCallback) {
        this.tag = tag;
        this.categoryPanel = categoryPanel;
        this.service = service;
        this.publishCallback = publishCallback;
        init();
        compose();
    }

    private void init() {
        pnlMapping = new PipelineBuilderPanel(I18N.DISPLAY.selectAndOrderApps(), tag, categoryPanel,
                service);

        initLayout();
        setHeaderVisible(false);

        toolbar = new ToolBar();
        buildPublishButton();
        setBottomComponent(toolbar);
    }

    private void buildPublishButton() {
        Button btnPublish = new Button(I18N.DISPLAY.publishToWorkspace());
        btnPublish.setId(ID_BTN_PUBLISH);
        btnPublish.setIcon(AbstractImagePrototype.create(Resources.ICONS.publish()));
        btnPublish.addSelectionListener(new PublishButtonSelectionListener());

        toolbar.add(new FillToolItem());
        toolbar.add(btnPublish);
    }

    private void initLayout() {
        BorderLayout layout = new BorderLayout();

        // make sure we re-draw when a panel expands
        layout.addListener(Events.Expand, new Listener<BorderLayoutEvent>() {
            @Override
            public void handleEvent(BorderLayoutEvent be) {
                layout();
            }
        });

        setLayout(layout);

        dataCenter = initLayoutRegion(LayoutRegion.CENTER, 0, false);
        dataEast = initLayoutRegion(LayoutRegion.EAST, 175, false);
    }

    private BorderLayoutData initLayoutRegion(LayoutRegion region, float size, boolean collapsible) {
        BorderLayoutData ret = new BorderLayoutData(region);

        if (size > 0) {
            ret.setSize(size);
        }

        ret.setCollapsible(collapsible);
        ret.setSplit(false);

        return ret;
    }

    private void compose() {
        add(pnlMapping, dataCenter);
    }

    private final class PublishButtonSelectionListener extends SelectionListener<ButtonEvent> {

        @Override
        public void componentSelected(ButtonEvent ce) {
            if (!pnlMapping.isValid()) {
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
                    MessageBox.info(I18N.DISPLAY.publishToWorkspace(),
                            I18N.DISPLAY.publishWorkflowSuccess(), null);
                    AnalysisGroupCountUpdateEvent event = new AnalysisGroupCountUpdateEvent(true, null);
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

        JSONObject publishJson = pnlMapping.getPublishJson();

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
        pnlMapping.cleanup();
    }

    /**
     * get json representation of the form data
     * 
     * @return JSONObject containing form data
     */
    public JSONObject toJson() {
        return (JSONObject)pnlMapping.toJson();
    }

    /**
     * Restore state from the given json representation of the form data.
     * 
     * @param obj json representation of the form data
     */
    public void configure(JSONObject obj) {
        pnlMapping.setData(obj);
    }
}
