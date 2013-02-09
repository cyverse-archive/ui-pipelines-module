package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.models.PipelineAppModel;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.uiapplications.client.events.AppGroupSelectedEvent;
import org.iplantc.core.uiapplications.client.events.AppSearchResultSelectedEvent;
import org.iplantc.core.uiapplications.client.events.handlers.AppGroupSelectedEventHandler;
import org.iplantc.core.uiapplications.client.events.handlers.AppSearchResultSelectedEventHandler;
import org.iplantc.core.uiapplications.client.models.Analysis;
import org.iplantc.core.uiapplications.client.models.AnalysisGroup;
import org.iplantc.core.uiapplications.client.services.AppServiceFacade;
import org.iplantc.core.uiapplications.client.services.AppUserServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uiapplications.client.views.panels.BaseCatalogMainPanel;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.de.client.DeCommonI18N;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A panel that allows the user to drag Apps from a listing grid and into the PipelineCreator, where they
 * can rearrange the Apps in the pipeline and map their outputs and inputs.
 * 
 * @author psarando
 * 
 */
public class PipelineBuilderPanel extends PipelineEditorView {
    // JSON keys and values used internally
    private static final String INPUTS = "inputs"; //$NON-NLS-1$
    private static final String OUTPUTS = "outputs"; //$NON-NLS-1$

    private final String tag;
    private final AppUserServiceFacade service;
    private final AbstractCatalogCategoryPanel categoryPanel;
    private BaseCatalogMainPanel appsListPanel;
    private ContentPanel builderPanel;
    private PipelineCreator builder;
    private ArrayList<HandlerRegistration> handlers;

    public PipelineBuilderPanel(String tag, AbstractCatalogCategoryPanel categoryPanel,
            AppUserServiceFacade service) {
        this.tag = tag;
        this.categoryPanel = categoryPanel;
        this.service = service;

        init();
        initHandlers();
        compose();
    }

    private void init() {
        setLayout(new BorderLayout());
        setSize(500, 300);
        setHeaderVisible(false);

        appsListPanel = new PipelineCatalogMainPanel(tag, service);

        builder = new PipelineCreator();

        builderPanel = new ContentPanel(new FitLayout());
        builderPanel.setScrollMode(Scroll.AUTO);
        builderPanel.setHeading(I18N.DISPLAY.dragDropAppsToCreator());
        builderPanel.add(builder);

        DropTarget target = new DropTarget(builderPanel);
        target.addDNDListener(new PipelineDNDListener(builder));
        target.setOperation(Operation.COPY);
    }

    private void initHandlers() {
        EventBus eventbus = EventBus.getInstance();
        handlers = new ArrayList<HandlerRegistration>();

        handlers.add(eventbus.addHandler(AppGroupSelectedEvent.TYPE, new AppGroupSelectedEventHandler() {
                    @Override
            public void onSelection(AppGroupSelectedEvent event) {
                        if (event.getSourcePanel() == categoryPanel && appsListPanel != null) {
                            appsListPanel.setHeading(event.getGroup().getName());
                            updateAnalysesListing(event.getGroup());
                        }
                    }
                }));

        handlers.add(eventbus.addHandler(AppSearchResultSelectedEvent.TYPE,
                new AppSearchResultSelectedEventHandler() {
                    @Override
                    public void onSelection(AppSearchResultSelectedEvent event) {
                        if (event.getSourceTag().equals(tag)) {
                            categoryPanel.selectCategory(event.getAppGroupId());
                            appsListPanel.selectTool(event.getAppId());
                        }
                    }
                }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        categoryPanel.cleanup();
        appsListPanel.cleanup();

        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }

        handlers.clear();
    }

    private void compose() {
        BorderLayoutData dataWest = initLayoutRegion(LayoutRegion.WEST, 220, true);
        BorderLayoutData dataCenter = initLayoutRegion(LayoutRegion.CENTER, 0, false);
        BorderLayoutData dataEast = initLayoutRegion(LayoutRegion.EAST, 400, false);

        add(categoryPanel, dataWest);
        add(appsListPanel, dataCenter);
        add(builderPanel, dataEast);
    }

    private BorderLayoutData initLayoutRegion(LayoutRegion region, float size, boolean collapsible) {
        BorderLayoutData ret = new BorderLayoutData(region);

        if (size > 0) {
            ret.setSize(size);
        }

        ret.setCollapsible(collapsible);
        ret.setSplit(true);

        return ret;
    }

    private void updateAnalysesListing(final AnalysisGroup group) {
        appsListPanel.mask(DeCommonI18N.DISPLAY.loadingMask());
        service.getApps(group.getId(), new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ArrayList<Analysis> analyses = new ArrayList<Analysis>();

                JSONArray templates = JsonUtil.getArray(JsonUtil.getObject(result), "templates"); //$NON-NLS-1$
                if (templates != null) {
                    for (int i = 0; i < templates.size(); i++) {
                        Analysis analysis = new Analysis(JsonUtil.getObjectAt(templates, i));
                        analyses.add(analysis);
                    }
                }

                appsListPanel.seed(analyses, group);
                appsListPanel.unmask();
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
                appsListPanel.unmask();
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        JSONObject pipelineJson = toJson();

        String name = JsonUtil.getString(pipelineJson, JSONMetaDataObject.NAME);
        String description = JsonUtil.getString(pipelineJson, JSONMetaDataObject.DESCRIPTION);
        JSONArray steps = JsonUtil.getArray(pipelineJson, PIPELINE_CREATOR_STEPS);

        // A pipline needs a name, description, and at least 2 apps.
        if (name.isEmpty() || description.isEmpty() || steps == null || steps.size() < 2) {
            return false;
        }

        // Each app after the first one should have at least one of its inputs mapped to an output.
        for (int i = 1; i < steps.size(); i++) {
            JSONObject targetStep = JsonUtil.getObjectAt(steps, i);

            JSONArray ioMappingArray = JsonUtil.getArray(targetStep, MAPPINGS);
            if (ioMappingArray == null || ioMappingArray.size() < 1) {
                return false;
            }

            for (int j = 0; j < ioMappingArray.size(); j++) {
                JSONObject mapping = JsonUtil.getObjectAt(ioMappingArray, j);
                JSONObject map = JsonUtil.getObject(mapping, PipelineAppModel.MAP);

                if (map == null || map.keySet().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        return builder.getPipelineJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(JSONObject obj) {
        if (obj != null) {
            builder.loadPipeline(obj);
        }
    }

    private void maskPipelineBuilder(String message) {
        if (builderPanel != null) {
            builderPanel.mask(message);
        }
    }

    private void unmaskPipelineBuilder() {
        if (builderPanel != null) {
            builderPanel.unmask();
        }
    }

    private void validateDNDEventApps(DNDEvent e) {
        StatusProxy eventStatus = e.getStatus();
        List<Analysis> selected = e.getData();

        if (selected == null || selected.isEmpty()) {
            eventStatus.setStatus(false);
            return;
        }

        for (final Analysis app : selected) {
            if (!app.getPipelineEligibility().isValid()) {
                eventStatus.setStatus(false);
                eventStatus.update(app.getPipelineEligibility().getReason());
                return;
            }

            eventStatus.update(I18N.DISPLAY.appendAppToWorkflow(app.getName()));
        }
    }

    private class PipelineCatalogMainPanel extends BaseCatalogMainPanel {

        public PipelineCatalogMainPanel(String tag, AppServiceFacade templateService) {
            super(tag, templateService);

            GridDragSource dragSource = new GridDragSource(analysisGrid);

            // GridDragSource does not add listeners to DragCancel events by default.
            AppDNDListener listener = new AppDNDListener();
            dragSource.addDNDListener(listener);
            dragSource.addListener(Events.DragCancel, listener);
        }
    }

    private class AppDNDListener extends DNDListener {

        @Override
        public void handleEvent(DNDEvent e) {
            EventType type = e.getType();
            if (type == Events.DragCancel) {
                dragCancel(e);
            } else {
                super.handleEvent(e);
            }
        }

        public void dragCancel(DNDEvent e) {
            unmaskPipelineBuilder();
        }

        @Override
        public void dragStart(DNDEvent e) {
            validateDNDEventApps(e);
        }
    }

    private class PipelineDNDListener extends DNDListener {
        private final PipelineCreator builder;

        PipelineDNDListener(PipelineCreator builder) {
            this.builder = builder;
        }

        @Override
        public void dragEnter(DNDEvent e) {
            validateDNDEventApps(e);

            if (e.getStatus().getStatus()) {
                // Event Status is true, so we have a valid App
                List<Analysis> selected = e.getData();
                String appName = selected.get(0).getName();

                maskPipelineBuilder(I18N.DISPLAY.appendAppToWorkflow(appName));
            }
        }

        @Override
        public void dragLeave(DNDEvent e) {
            unmaskPipelineBuilder();
        }

        @Override
        public void dragDrop(DNDEvent e) {
            List<Analysis> data = e.getData();

            if (data == null || data.isEmpty()) {
                return;
            }

            for (Analysis app : data) {
                if (app.getPipelineEligibility().isValid()) {
                    final JSONObject appJson = app.toJson();

                    service.getDataObjectsForApp(app.getId(), new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            JSONObject obj = JsonUtil.getObject(result);

                            // Set the ID to the returned template ID, so that it's the correct ID for
                            // getStepJson.
                            appJson.put(JSONMetaDataObject.ID, obj.get(JSONMetaDataObject.ID));
                            appJson.put(INPUTS, JsonUtil.getArray(obj, INPUTS));
                            appJson.put(OUTPUTS, JsonUtil.getArray(obj, OUTPUTS));

                            builder.appendApp(appJson);
                            unmaskPipelineBuilder();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            ErrorHandler.post(I18N.ERROR.dataObjectsRetrieveError(), caught);
                            unmaskPipelineBuilder();
                        }
                    });
                }
            }
        }
    }
}