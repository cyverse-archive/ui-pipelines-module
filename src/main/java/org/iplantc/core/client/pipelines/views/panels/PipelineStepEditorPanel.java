package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.List;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.events.PipelineChangeEvent;
import org.iplantc.core.client.pipelines.events.PipelineChangeEventHandler;
import org.iplantc.core.client.pipelines.events.PipelineStepValidationEvent;
import org.iplantc.core.client.pipelines.events.PipelineStepValidationEventHandler;
import org.iplantc.core.client.pipelines.images.Resources;
import org.iplantc.core.jsonutil.JsonUtil;
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
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A PipelineEditorView that contains and coordinates PipelineStep panels.
 * 
 * @author psarando
 * 
 */
public class PipelineStepEditorPanel extends PipelineEditorView {

    protected BorderLayoutData dataWest;
    protected BorderLayoutData dataCenter;

    private CardPanel pnlMain;
    private PipelineStep pnlInfo;
    private PipelineStep pnlSelect;
    private InputOutputMappingPanel pnlMapping;
    private List<ActionsToggleButton> actions;

    private ContentPanel noteContainer;

    private ArrayList<HandlerRegistration> handlers;

    public PipelineStepEditorPanel(String tag, AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateUserServiceFacade service) {
        init(tag, categoryPanel, service);
        compose();
    }

    private void init(String tag, AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateUserServiceFacade service) {
        pnlMain = new CardPanel();
        pnlMain.setScrollMode(Scroll.NONE);

        pnlInfo = new PipelineInfoPanel(I18N.DISPLAY.workflowInfo());

        pnlSelect = new SelectAndOrderPanel(I18N.DISPLAY.selectAndOrderApps(), tag, categoryPanel,
                service);

        pnlMapping = new InputOutputMappingPanel(I18N.DISPLAY.mapOutputsToInputs());

        pnlMain.add(pnlInfo);
        pnlMain.add(pnlSelect);
        pnlMain.add(pnlMapping);

        initListeners();
        initLayout();
        setHeaderVisible(false);
    }

    private void initListeners() {
        EventBus bus = EventBus.getInstance();
        handlers = new ArrayList<HandlerRegistration>();

        handlers.add(bus.addHandler(PipelineChangeEvent.TYPE, new PipelineChangeEventHandler() {
            @Override
            public void onChange(PipelineChangeEvent event) {
                pnlMapping.reconfigure(event.getAppModels());
            }
        }));

        handlers.add(bus.addHandler(PipelineStepValidationEvent.TYPE,
                new PipelineStepValidationEventHandler() {
                    @Override
                    public void onValidate(PipelineStepValidationEvent event) {
                        validateSteps();
                    }
                }));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return validateSteps();
    }

    private boolean validateSteps() {
        boolean stepsValid = true;

        for (ActionsToggleButton btn : actions) {
            if (btn.getStep().isValid()) {
                btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.stepComplete()));
            } else {
                btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.stepError()));
                stepsValid = false;
            }
        }

        return stepsValid;
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
            actions.add(new ActionsToggleButton(I18N.DISPLAY.selectAndOrderApps(), pnlSelect,
                    I18N.DISPLAY.selectOrderPnlTip()));
            actions.add(new ActionsToggleButton(I18N.DISPLAY.mapOutputsToInputs(), pnlMapping,
                    I18N.DISPLAY.inputsOutputsPnlTip()));

            for (ToggleButton btn : actions) {
                add(btn);
                add(new Html("<br/>")); //$NON-NLS-1$
            }

            add(noteContainer);

            toggleButtons(actions.get(0).step, actions.get(0), actions.get(0).qtip);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getPublishJson() {
        // TODO Refactor
        return toJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        JSONObject obj = pnlInfo.toJson().isObject();

        if (obj != null) {
            obj.put("steps", pnlSelect.toJson()); //$NON-NLS-1$
            obj.put("mappings", pnlMapping.toJson()); //$NON-NLS-1$
            obj.put("implementation", getImplementorDetails()); //$NON-NLS-1$
            obj.put("full_username", new JSONString(UserInfo.getInstance().getFullUsername())); //$NON-NLS-1$
            JSONArray arr = new JSONArray();
            arr.set(0, obj);
            jsonObj.put("analyses", arr); //$NON-NLS-1$
            return jsonObj;
        } else {
            ErrorHandler.post(I18N.ERROR.workflowPublishError());
            return null;
        }

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
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }

        handlers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(JSONObject obj) {
        // TODO Refactor
        JSONArray apps = JsonUtil.getArray(obj, "analyses"); //$NON-NLS-1$
        if (apps != null) {
            final JSONObject pipeline_config = JsonUtil.getObjectAt(apps, 0);
            if (pipeline_config != null) {
                pnlInfo.setData(pipeline_config);
                pnlSelect.setData(pipeline_config);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                            pnlMapping.setData(pipeline_config);
                    }
                });
        }
       }
    }
}
