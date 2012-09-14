package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.uiapplications.client.events.AnalysisCategorySelectedEvent;
import org.iplantc.core.uiapplications.client.events.AnalysisCategorySelectedEventHandler;
import org.iplantc.core.uiapplications.client.events.AppSearchResultSelectedEvent;
import org.iplantc.core.uiapplications.client.events.AppSearchResultSelectedEventHandler;
import org.iplantc.core.uiapplications.client.models.Analysis;
import org.iplantc.core.uiapplications.client.models.AnalysisGroup;
import org.iplantc.core.uiapplications.client.services.AppTemplateServiceFacade;
import org.iplantc.core.uiapplications.client.services.AppTemplateUserServiceFacade;
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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A panel that allows the user to drag Apps from a listing grid and into the PipelineCreator, where they
 * can rearrange the Apps in the pipeline and map their outputs and inputs.
 * 
 * @author psarando
 * 
 */
public class PipelineBuilderPanel extends PipelineStep {
    // JSON keys used in toJson objects
    public static final String STEPS = "steps"; //$NON-NLS-1$
    public static final String MAPPINGS = "mappings"; //$NON-NLS-1$
    public static final String PIPELINE_CREATOR_STEPS = "apps"; //$NON-NLS-1$
    public static final String TEMPLATE_ID = "template_id"; //$NON-NLS-1$
    public static final String CONFIG = "config"; //$NON-NLS-1$
    public static final String SOURCE_STEP = "source_step"; //$NON-NLS-1$
    public static final String TARGET_STEP = "target_step"; //$NON-NLS-1$
    public static final String MAP = "map"; //$NON-NLS-1$

    // JSON keys and values used internally
    private static final String ID_KEY = "auto-gen"; //$NON-NLS-1$
    private static final String INPUTS = "inputs"; //$NON-NLS-1$
    private static final String OUTPUTS = "outputs"; //$NON-NLS-1$

    private final String tag;
    private final AppTemplateUserServiceFacade service;
    private final AbstractCatalogCategoryPanel categoryPanel;
    private BaseCatalogMainPanel appsListPanel;
    private ContentPanel builderPanel;
    private PipelineCreator builder;
    private ArrayList<HandlerRegistration> handlers;

    public PipelineBuilderPanel(String title, String tag, AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateUserServiceFacade service) {
        super(title);

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

        handlers.add(eventbus.addHandler(AnalysisCategorySelectedEvent.TYPE,
                new AnalysisCategorySelectedEventHandler() {
                    @Override
                    public void onSelection(AnalysisCategorySelectedEvent event) {
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
                            categoryPanel.selectCategory(event.getCategoryId());
                            appsListPanel.selectTool(event.getAppId());
                        }
                    }
                }));
    }

    /**
     * Remove event handlers and free-up resources.
     */
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
        BorderLayoutData dataEast = initLayoutRegion(LayoutRegion.EAST, 300, false);

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
        service.getAnalysis(group.getId(), new AsyncCallback<String>() {
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

    @Override
    public boolean isValid() {
        JSONArray steps = JsonUtil.getArray(builder.getPipelineJson(), PIPELINE_CREATOR_STEPS);

        // A pipline needs at least 2 apps and each app after the first one should have at least one
        // output-to-input mapping
        if (steps == null || steps.size() < 2) {
            return false;
        }

        for (int i = 1; i < steps.size(); i++) {
            JSONObject targetStep = JsonUtil.getObjectAt(steps, i);

            JSONArray ioMappingArray = JsonUtil.getArray(targetStep, MAPPINGS);
            if (ioMappingArray == null || ioMappingArray.size() < 1) {
                return false;
            }

            for (int j = 0; j < ioMappingArray.size(); j++) {
                JSONObject mapping = JsonUtil.getObjectAt(ioMappingArray, j);
                JSONObject map = JsonUtil.getObject(mapping, MAP);

                if (map == null || map.keySet().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public JSONValue toJson() {
        return builder.getPipelineJson();
    }

    /**
     * Get the JSON of this pipeline required for publishing.
     * 
     * @return JSONObject required for publishing.
     */
    public JSONObject getPublishJson() {
        JSONArray steps = JsonUtil.getArray(builder.getPipelineJson(), PIPELINE_CREATOR_STEPS);

        if (steps == null) {
            return null;
        }

        JSONObject publishJson = new JSONObject();
        JSONArray publishSteps = new JSONArray();
        JSONArray publishMappings = new JSONArray();

        for (int i = 0,mappingsIndex = 0; i < steps.size(); i++) {
            JSONObject step = JsonUtil.getObjectAt(steps, i);

            // Convert the PipelineCreator step to a metadactyl step.
            JSONObject publishStep = getStepJson(step);

            if (publishStep != null) {
                publishSteps.set(i, publishStep);

                // The first step should not have any input mappings.
                if (i > 0) {
                    JSONArray stepMappingArray = JsonUtil.getArray(step, MAPPINGS);

                    if (stepMappingArray != null) {
                        // Convert the PipelineCreator input->output mappings to metadactyl mappings.
                        String targetStepName = getStepName(publishStep);

                        for (int j = 0; j < stepMappingArray.size(); j++) {
                            JSONObject stepMapping = JsonUtil.getObjectAt(stepMappingArray, j);

                            JSONObject publishMapping = getMappingJson(targetStepName, stepMapping);

                            if (publishMapping != null) {
                                publishMappings.set(mappingsIndex, publishMapping);
                                mappingsIndex++;
                            }
                        }
                    }
                }
            }
        }

        publishJson.put(STEPS, publishSteps);
        publishJson.put(MAPPINGS, publishMappings);

        return publishJson;
    }

    /**
     * Gets a JSON object representing this Pipeline step as a Workflow step.
     * 
     * @return A Workflow step JSON object.
     */
    private JSONObject getStepJson(JSONObject pipelineStep) {
        JSONObject ret = new JSONObject();

        String appId = JsonUtil.getString(pipelineStep, JSONMetaDataObject.ID);
        Number step = JsonUtil.getNumber(pipelineStep, "step"); //$NON-NLS-1$

        if (step == null) {
            return null;
        }

        ret.put(JSONMetaDataObject.ID, new JSONString(ID_KEY));
        ret.put(TEMPLATE_ID, new JSONString(appId));
        ret.put(JSONMetaDataObject.NAME, new JSONString(buildStepName(step.intValue(), appId)));
        ret.put(JSONMetaDataObject.DESCRIPTION, pipelineStep.get(JSONMetaDataObject.NAME));
        ret.put(CONFIG, new JSONObject());

        return ret;
    }

    private String buildStepName(int step, String appId) {
        // PipelineCreator steps start at 0.
        return "step_" + (step + 1) + "_" + appId; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getStepName(JSONObject step) {
        return JsonUtil.getString(step, JSONMetaDataObject.NAME);
    }

    /**
     * Formats the Output->Input mappings for the given source step mapping to the target step name, as a
     * JSON object of a mapping array for the Import Workflow service.
     * 
     * @return A JSON object of Output->Input mappings.
     */
    private JSONObject getMappingJson(String targetStepName, JSONObject sourceStepMapping) {
        Number mappedStep = JsonUtil.getNumber(sourceStepMapping, "step"); //$NON-NLS-1$

        if (mappedStep != null) {
            String sourceStepName = buildStepName(mappedStep.intValue(),
                    JsonUtil.getString(sourceStepMapping, JSONMetaDataObject.ID));

            JSONObject stepMap = JsonUtil.getObject(sourceStepMapping, MAP);
            if (stepMap != null) {
                // Build the JSON output->input map object.
                JSONObject map = new JSONObject();

                for (String inputId : stepMap.keySet()) {
                    String outputId = JsonUtil.getString(stepMap, inputId);

                    if (!outputId.isEmpty()) {
                        map.put(outputId, new JSONString(inputId));
                    }
                }

                // Ensure at least one output->input is set for sourceStepName in the JSON map object.
                if (!map.keySet().isEmpty()) {
                    // Return the mappings from sourceStepName to targetStepName.
                    JSONObject mapping = new JSONObject();

                    mapping.put(SOURCE_STEP, new JSONString(sourceStepName));
                    mapping.put(TARGET_STEP, new JSONString(targetStepName));
                    mapping.put(MAP, map);

                    return mapping;
                }
            }
        }

        // No mappings were found in the given sourceStepMapping.
        return null;
    }

    @Override
    protected void setData(JSONObject obj) {
        if (obj != null) {
            builder.loadPipeline(obj);
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
        }
    }

    private class PipelineCatalogMainPanel extends BaseCatalogMainPanel {

        public PipelineCatalogMainPanel(String tag, AppTemplateServiceFacade templateService) {
            super(tag, templateService);

            GridDragSource dragSource = new GridDragSource(analysisGrid);
            dragSource.addDNDListener(new AppDNDListener());
        }

    }

    private class AppDNDListener extends DNDListener {

        @Override
        public void dragStart(DNDEvent e) {
            validateDNDEventApps(e);
        }

        @Override
        public void dragMove(DNDEvent e) {
            validateDNDEventApps(e);
        }
    }

    private class PipelineDNDListener extends DNDListener {
        private final PipelineCreator builder;

        PipelineDNDListener(PipelineCreator builder) {
            this.builder = builder;
        }

        @Override
        public void dragMove(DNDEvent e) {
            validateDNDEventApps(e);
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

                    service.getDataObjectsForAnalysis(app.getId(), new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            JSONObject obj = JsonUtil.getObject(result);

                            // Set the ID to the returned template ID, so that it's the correct ID for
                            // getStepJson.
                            appJson.put(JSONMetaDataObject.ID, obj.get(JSONMetaDataObject.ID));
                            appJson.put(INPUTS, JsonUtil.getArray(obj, INPUTS));
                            appJson.put(OUTPUTS, JsonUtil.getArray(obj, OUTPUTS));

                            builder.appendApp(appJson);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            ErrorHandler.post(I18N.ERROR.dataObjectsRetrieveError(), caught);
                        }
                    });
                }
            }
        }
    }
}
