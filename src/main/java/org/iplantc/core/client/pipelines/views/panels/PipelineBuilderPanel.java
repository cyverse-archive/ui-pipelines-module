package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.jsonutil.JsonUtil;
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
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
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

    private final String tag;
    private final AppTemplateUserServiceFacade service;
    private final AbstractCatalogCategoryPanel categoryPanel;
    private BaseCatalogMainPanel appsListPanel;
    private LayoutContainer builderPanel;
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

        appsListPanel = new PipelineCatalogMainPanel(tag, service);

        builder = new PipelineCreator();
        builder.setStyleName("pipeline-builder"); //$NON-NLS-1$

        builderPanel = new LayoutContainer(new FitLayout());
        builderPanel.setScrollMode(Scroll.AUTO);
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
        BorderLayoutData dataEast = initLayoutRegion(LayoutRegion.EAST, 265, false);

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
        JSONArray apps = JsonUtil.getArray(builder.getPipelineJson(), "apps"); //$NON-NLS-1$

        // A pipline needs at least 2 apps and each app after the first one should have at least one
        // output-to-input mapping
        if (apps == null || apps.size() < 2) {
            return false;
        }

        for (int i = 1; i < apps.size(); i++) {
            JSONObject targetModel = JsonUtil.getObjectAt(apps, i);

            JSONArray ioMappingArray = JsonUtil.getArray(targetModel, "mappings"); //$NON-NLS-1$
            if (ioMappingArray == null || ioMappingArray.size() < 1) {
                return false;
            }

            for (int j = 0; j < ioMappingArray.size(); j++) {
                JSONObject mapping = JsonUtil.getObjectAt(ioMappingArray, j);
                JSONObject map = JsonUtil.getObject(mapping, "map"); //$NON-NLS-1$

                if (map == null || map.keySet().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public JSONValue toJson() {
        JSONArray ret = new JSONArray();
        JSONArray apps = JsonUtil.getArray(builder.getPipelineJson(), "apps"); //$NON-NLS-1$

        int retIndex = 0;
        for (int i = 1; i < apps.size(); i++) {
            JSONObject targetModel = JsonUtil.getObjectAt(apps, i);
            JSONArray mapping = JsonUtil.getArray(targetModel, "mappings"); //$NON-NLS-1$

            // TODO correct format of mappings (generate step_name, etc.)
            for (int j = 0; j < mapping.size(); j++) {
                ret.set(retIndex, mapping.get(j));
                retIndex++;
            }
        }

        return ret;
    }

    @Override
    protected void setData(JSONObject obj) {
        // TODO parse saved state
        // builder.loadPipeline(obj);
    }

    private void validateDNDEventApps(DNDEvent e) {
        if (e.isCancelled()) {
            return;
        }

        List<Analysis> selected = e.getData();
        if (selected == null || selected.isEmpty()) {
            // e.setCancelled(true);
            e.getStatus().setStatus(false);
            return;
        }

        for (final Analysis app : selected) {
            if (!app.getPipelineEligibility().isValid()) {
                // e.setCancelled(true);
                e.getStatus().setStatus(false);
                e.getStatus().update(app.getPipelineEligibility().getReason());
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
                            appJson.put("inputs", JsonUtil.getArray(obj, "inputs")); //$NON-NLS-1$ //$NON-NLS-2$
                            appJson.put("outputs", JsonUtil.getArray(obj, "outputs")); //$NON-NLS-1$ //$NON-NLS-2$

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
