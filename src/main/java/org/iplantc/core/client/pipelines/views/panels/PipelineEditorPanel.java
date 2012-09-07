package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.List;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.events.PipelineStepValidationEvent;
import org.iplantc.core.client.pipelines.events.PipelineStepValidationEventHandler;
import org.iplantc.core.client.pipelines.images.Resources;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.uiapplications.client.events.AnalysisGroupCountUpdateEvent;
import org.iplantc.core.uiapplications.client.services.AppTemplateUserServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.core.uicommons.client.models.UserInfo;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.CardPanel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.shared.HandlerRegistration;
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
    protected BorderLayoutData dataWest;
    protected BorderLayoutData dataCenter;

    private CardPanel pnlMain;
    private PipelineStep pnlInfo;
    private PipelineBuilderPanel pnlMapping;
    private List<ActionsToggleButton> actions;

    private final AbstractCatalogCategoryPanel categoryPanel;
    private final AppTemplateUserServiceFacade service;
    private ToolBar toolbar;
    private ContentPanel noteContainer;
    private final Command publishCallback;
    private final String tag;

    private ArrayList<HandlerRegistration> handlers;

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
        pnlMain = new CardPanel();
        pnlMain.setScrollMode(Scroll.NONE);

        pnlInfo = new PipelineInfoPanel(I18N.DISPLAY.workflowInfo());

        pnlMapping = new PipelineBuilderPanel(I18N.DISPLAY.selectAndOrderApps(), tag, categoryPanel,
                service);

        pnlMain.add(pnlInfo);
        pnlMain.add(pnlMapping);

        initListeners();
        initLayout();
        setHeaderVisible(false);
        toolbar = new ToolBar();
        buildPublishButton();
        setBottomComponent(toolbar);

    }

    private void initListeners() {
        EventBus bus = EventBus.getInstance();
        handlers = new ArrayList<HandlerRegistration>();

        handlers.add(bus.addHandler(PipelineStepValidationEvent.TYPE,
                new PipelineStepValidationEventHandler() {
                    @Override
                    public void onValidate(PipelineStepValidationEvent event) {
                        validateSteps();
                    }
                }));
    }

    private void buildPublishButton() {
        Button btnPublish = new Button(I18N.DISPLAY.publishToWorkspace());
        btnPublish.setId(ID_BTN_PUBLISH);
        btnPublish.disable();
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

        dataWest = initLayoutRegion(LayoutRegion.WEST, 175, false);
        dataCenter = initLayoutRegion(LayoutRegion.CENTER, 0, false);
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
        buildNotePanel();
        LayoutContainer btns = new ActionsPanel();

        add(btns, dataWest);
        add(pnlMain, dataCenter);
    }

    private final class PublishButtonSelectionListener extends SelectionListener<ButtonEvent> {

        @Override
        public void componentSelected(ButtonEvent ce) {
            ((Button)ce.getSource()).disable();

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

                }

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(I18N.ERROR.workflowPublishError(), caught);

                }
            });

        }
    }

    private class ActionsToggleButton extends ToggleButton {
        private final PipelineStep step;
        private final String qtip;

        public ActionsToggleButton(String label, final PipelineStep step, final String qtip) {
            super(label, new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    validateSteps();
                    toggleButtons(step, (ToggleButton)ce.getButton(), qtip);
                }
            });

            setSize(150, 30);
            this.step = step;
            this.qtip = qtip;
            setStyleAttribute("outline", "none"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public PipelineStep getStep() {
            return step;
        }

        @Override
        protected void toggle(boolean state, boolean silent) {
            super.toggle(state, silent);
            // blur so no dotted line is shown around the button in Firefox
            if (buttonEl != null) {
                buttonEl.blur();
            }
        }

    }

    private void validateSteps() {
        for (int i = 0; i < actions.size(); i++) {
            ActionsToggleButton btn = actions.get(i);
            if (btn.getStep().isValid()) {
                btn.setIcon(AbstractImagePrototype
                        .create(org.iplantc.core.client.pipelines.images.Resources.ICONS.stepComplete()));
            } else {
                btn.setIcon(AbstractImagePrototype
                        .create(org.iplantc.core.client.pipelines.images.Resources.ICONS.stepError()));
            }
        }

        setPublishButtonState();

    }

    private void setPublishButtonState() {
        Button b = (Button)toolbar.getItemByItemId(ID_BTN_PUBLISH);
        b.setEnabled(pnlInfo.isValid() && pnlMapping.isValid());
    }

    private void toggleButtons(PipelineStep contents, ToggleButton btnActive, String qtip) {
        for (ToggleButton btn : actions) {
            btn.toggle(false);
        }
        btnActive.toggle(true);

        pnlMain.setActiveItem(contents);

        noteContainer.removeAll();
        noteContainer.addText(qtip);
        noteContainer.layout();
    }

    private class ActionsPanel extends LayoutContainer {

        public ActionsPanel() {
            init();
            compose();
            setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
            // over ride top padding to match center panel header
            setStyleAttribute("padding-top", "20px"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        private void init() {
            actions = new ArrayList<ActionsToggleButton>();
        }

        private void compose() {
            actions.add(new ActionsToggleButton(I18N.DISPLAY.workflowInfo(), pnlInfo, I18N.DISPLAY
                    .infoPnlTip()));
            actions.add(new ActionsToggleButton(I18N.DISPLAY.selectAndOrderApps(), pnlMapping,
                    I18N.DISPLAY.selectOrderPnlTip()));

            for (ToggleButton btn : actions) {
                add(btn);
                add(new Html("<br/>")); //$NON-NLS-1$
            }

            add(noteContainer);

            toggleButtons(actions.get(0).step, actions.get(0), actions.get(0).qtip);
        }
    }

    /**
     * Get the JSON of this pipeline required for publishing.
     * 
     * @return JSONObject required for publishing.
     */
    public JSONObject getPublishJson() {
        JSONObject ret = new JSONObject();

        JSONObject publishJson = pnlInfo.toJson().isObject();

        JSONObject stepsMappingsJson = pnlMapping.getPublishJson();
        JSONArray steps = JsonUtil.getArray(stepsMappingsJson, PipelineBuilderPanel.STEPS);
        JSONArray mappings = JsonUtil.getArray(stepsMappingsJson, PipelineBuilderPanel.MAPPINGS);

        if (publishJson == null || steps == null || mappings == null) {
            // something went wrong, abort
            return null;
        }

        publishJson.put(PipelineBuilderPanel.STEPS, steps);
        publishJson.put(PipelineBuilderPanel.MAPPINGS, mappings);
        publishJson.put("implementation", getImplementorDetails()); //$NON-NLS-1$
        publishJson.put("full_username", new JSONString(UserInfo.getInstance().getFullUsername())); //$NON-NLS-1$

        JSONArray analyses = new JSONArray();
        analyses.set(0, publishJson);
        ret.put("analyses", analyses); //$NON-NLS-1$

        return ret;
    }

    private void buildNotePanel() {
        noteContainer = new ContentPanel();
        noteContainer.setHeading(I18N.DISPLAY.quickTipsHeading());
        noteContainer.setSize(153, 190);
        noteContainer.setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        noteContainer.setCollapsible(true);
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

        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }

        handlers.clear();
    }

    /**
     * get json representation of the form data
     * 
     * @return JSONObject containing form data
     */
    public JSONObject toJson() {
        JSONObject ret = new JSONObject();

        ret.put("info", pnlInfo.toJson()); //$NON-NLS-1$
        ret.put("pipeline", pnlMapping.toJson()); //$NON-NLS-1$

        return ret;
    }

    /**
     * Restore state from the given json representation of the form data.
     * 
     * @param obj json representation of the form data
     */
    public void configure(JSONObject obj) {
        pnlInfo.setData(JsonUtil.getObject(obj, "info")); //$NON-NLS-1$
        pnlMapping.setData(JsonUtil.getObject(obj, "pipeline")); //$NON-NLS-1$
    }
}
