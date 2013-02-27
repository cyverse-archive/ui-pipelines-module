package org.iplantc.core.client.pipelines.gxt3.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.gxt3.dnd.AppsGridDragHandler;
import org.iplantc.core.client.pipelines.gxt3.dnd.PipelineBuilderDNDHandler;
import org.iplantc.core.client.pipelines.gxt3.dnd.PipelineBuilderDropHandler;
import org.iplantc.core.client.pipelines.gxt3.util.PipelineAutoBeanUtil;
import org.iplantc.core.client.pipelines.gxt3.views.AppSelectionDialog;
import org.iplantc.core.client.pipelines.gxt3.views.PipelineAppMappingForm;
import org.iplantc.core.client.pipelines.gxt3.views.PipelineAppMappingView;
import org.iplantc.core.client.pipelines.gxt3.views.PipelineView;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbar;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbarImpl;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineAppMapping;
import org.iplantc.core.uiapps.client.models.autobeans.App;
import org.iplantc.core.uiapps.client.presenter.AppsViewPresenter;
import org.iplantc.core.uiapps.client.views.AppsView;
import org.iplantc.core.uiapps.client.views.AppsViewImpl;
import org.iplantc.core.uicommons.client.presenter.Presenter;

import com.google.common.base.Strings;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * The Presenter for the Pipeline View.
 * 
 * @author psarando
 * 
 */
public class PipelineViewPresenter implements Presenter, PipelineView.Presenter,
        PipelineViewToolbar.Presenter, PipelineBuilderDNDHandler.Presenter,
        AppSelectionDialog.Presenter, PipelineAppMappingView.Presenter {

    private final PipelineView view;
    private final PipelineViewToolbar toolbar;
    private AppsViewPresenter appsPresenter;
    private AppSelectionDialog appSelectView;
    private final Command onPublishCallback;
    private Pipeline pipeline;

    public PipelineViewPresenter(PipelineView view, Command onPublishCallback) {
        this.view = view;
        this.onPublishCallback = onPublishCallback;

        pipeline = PipelineAutoBeanUtil.getPipelineAutoBeanFactory().pipeline().as();
        view.setPipeline(pipeline);

        toolbar = new PipelineViewToolbarImpl();

        view.setPresenter(this);
        toolbar.setPresenter(this);

        view.setNorthWidget(toolbar);

        initAppMappingView();
        initAppsView();
    }

    private void initAppMappingView() {
        PipelineAppMappingForm mappingForm = (PipelineAppMappingForm)view.getMappingPanel();
        mappingForm.setPresenter(this);
        mappingForm.setPipeline(pipeline);
    }

    private void initAppsView() {
        appSelectView = new AppSelectionDialog();
        appSelectView.setPresenter(this);

        AppsView appsView = new AppsViewImpl();
        appsPresenter = new AppsViewPresenter(appsView);

        initAppsGridDragHandler(appsView.getAppsGrid());
        initPipelineBuilderDropHandler(view.getBuilderDropContainer());

        appsPresenter.builder()
                .hideToolbarButtonCopy()
                .hideToolbarButtonCreate()
                .hideToolbarButtonDelete()
                .hideToolbarButtonEdit()
                .hideToolbarButtonRequestTool()
                .hideToolbarButtonSubmit()
                .go(appSelectView);
    }

    private void initAppsGridDragHandler(Grid<App> grid) {
        AppsGridDragHandler handler = new AppsGridDragHandler();
        handler.setPresenter(this);

        GridDragSource<App> source = new GridDragSource<App>(grid);
        source.addDragStartHandler(handler);
        source.addDragCancelHandler(handler);
    }

    private void initPipelineBuilderDropHandler(Container builderPanel) {
        PipelineBuilderDropHandler handler = new PipelineBuilderDropHandler();
        handler.setPresenter(this);

        DropTarget target = new DropTarget(builderPanel);
        target.setOperation(Operation.COPY);
        target.addDragEnterHandler(handler);
        target.addDragLeaveHandler(handler);
        target.addDragCancelHandler(handler);
        target.addDropHandler(handler);
    }

    @Override
    public void go(HasOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onPublishClicked() {
        if (onPublishCallback != null) {
            onPublishCallback.execute();
        }
    }

    @Override
    public void onSwapViewClicked() {
        IsWidget activeView = view.getActiveView();

        if (activeView == view.getStepEditorPanel()) {
            activeView = view.getBuilderPanel();

            if (pipeline != null) {
                view.getPipelineCreator().loadPipeline(pipeline);
            }

            appsPresenter.go(view.getAppsContainer());
        } else {
            activeView = view.getStepEditorPanel();

            pipeline = view.getPipelineCreator().getPipeline();
            if (pipeline != null) {
                view.setPipeline(pipeline);

                updatePipelineAppMappingForm();

                ListStore<PipelineApp> store = view.getPipelineAppStore();
                store.clear();
                List<PipelineApp> apps = pipeline.getApps();
                if (apps != null) {
                    store.addAll(apps);
                }
            }

            appsPresenter.go(appSelectView);
        }

        view.setActiveView(activeView);
    }

    private void reconfigurePipelineAppMappingForm(int startingStep) {
        List<PipelineApp> apps = pipeline.getApps();
        if (apps != null) {
            for (PipelineApp app : apps) {
                if (app.getStep() >= startingStep) {
                    resetAppMappings(app);
                }
            }
        }

        updatePipelineAppMappingForm();
    }

    private void updatePipelineAppMappingForm() {
        PipelineAppMappingForm mappingForm = (PipelineAppMappingForm)view.getMappingPanel();
        mappingForm.setPipeline(pipeline);
    }

    @Override
    public Pipeline getPipeline() {
        if (view.getActiveView() == view.getBuilderPanel()) {
            pipeline = view.getPipelineCreator().getPipeline();
        }

        return pipeline;
    }

    @Override
    public void onInfoClick() {
        view.getStepPanel().setActiveWidget(view.getInfoPanel());
    }

    @Override
    public void onAppOrderClick() {
        view.getStepPanel().setActiveWidget(view.getAppOrderPanel());
    }

    @Override
    public void onMappingClick() {
        view.getStepPanel().setActiveWidget(view.getMappingPanel());
    }

    @Override
    public void onAddAppsClicked() {
        appSelectView.show();
    }

    @Override
    public void onMoveUpClicked() {
        PipelineApp selectedApp = view.getOrderGridSelectedApp();
        if (selectedApp == null) {
            return;
        }

        ListStore<PipelineApp> store = view.getPipelineAppStore();

        int selectedStep = selectedApp.getStep();
        if (selectedStep > 1) {
            int stepUp = selectedStep - 1;
            PipelineApp prevApp = store.get(stepUp - 1);
            prevApp.setStep(selectedStep);
            selectedApp.setStep(stepUp);

            store.update(selectedApp);
            store.update(prevApp);

            store.applySort(false);

            pipeline.setApps(store.getAll());

            reconfigurePipelineAppMappingForm(stepUp);
        }
    }

    @Override
    public void onMoveDownClicked() {
        PipelineApp selectedApp = view.getOrderGridSelectedApp();
        if (selectedApp == null) {
            return;
        }

        ListStore<PipelineApp> store = view.getPipelineAppStore();

        int selectedStep = selectedApp.getStep();
        if (selectedStep < store.size()) {
            int stepDown = selectedStep + 1;
            PipelineApp nextApp = store.get(stepDown - 1);
            nextApp.setStep(selectedStep);
            selectedApp.setStep(stepDown);

            store.update(selectedApp);
            store.update(nextApp);

            store.applySort(false);

            pipeline.setApps(store.getAll());

            reconfigurePipelineAppMappingForm(stepDown);
        }
    }

    @Override
    public void onRemoveAppClicked() {
        PipelineApp selectedApp = view.getOrderGridSelectedApp();

        if (selectedApp != null) {
            ListStore<PipelineApp> store = view.getPipelineAppStore();

            store.remove(selectedApp);

            for (int step = 1; step <= store.size(); step++) {
                PipelineApp app = store.get(step - 1);
                app.setStep(step);
                store.update(app);
            }

            pipeline.setApps(store.getAll());

            reconfigurePipelineAppMappingForm(selectedApp.getStep());
        }
    }

    @Override
    public void onAddAppClick() {
        App selectedApp = appsPresenter.getSelectedApp();
        PipelineAutoBeanUtil.appToPipelineApp(selectedApp, new AsyncCallback<PipelineApp>() {

            @Override
            public void onSuccess(PipelineApp result) {
                if (result != null) {
                    ListStore<PipelineApp> store = view.getPipelineAppStore();

                    result.setStep(store.size() + 1);
                    store.add(result);

                    pipeline.setApps(store.getAll());

                    appSelectView.updateStatusBar(store.size(), I18N.DISPLAY.lastApp(result.getName()));

                    updatePipelineAppMappingForm();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                ListStore<PipelineApp> store = view.getPipelineAppStore();
                appSelectView.updateStatusBar(store.size(), caught.getMessage());
            }
        });

    }

    @Override
    public void addAppToPipeline(final App app) {
        PipelineAutoBeanUtil.appToPipelineApp(app, new AsyncCallback<PipelineApp>() {

            @Override
            public void onSuccess(PipelineApp result) {
                if (result != null) {
                    view.getPipelineCreator().appendApp(result);
                    unmaskPipelineBuilder();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                unmaskPipelineBuilder();
            }
        });
    }

    @Override
    public void maskPipelineBuilder(String message) {
        view.getBuilderDropContainer().mask(message);
    }

    @Override
    public void unmaskPipelineBuilder() {
        view.getBuilderDropContainer().unmask();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStepName(PipelineApp app) {
        return app == null ? "" : getStepName(app.getStep(), app.getId()); //$NON-NLS-1$
    }

    private String getStepName(int step, String id) {
        return Format.substitute("step_{0}_{1}", step, id); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputOutputMapping(PipelineApp targetStep, String targetInputId,
            PipelineApp sourceStep, String sourceOutputId) {
        String sourceStepName = getStepName(sourceStep);

        // Find the input->output mappings for sourceStepName.
        FastMap<PipelineAppMapping> mapInputsOutputs = getTargetMappings(targetStep);
        PipelineAppMapping targetAppMapping = mapInputsOutputs.get(sourceStepName);

        if (targetAppMapping == null) {
            // There are no input->output mappings for this sourceStepName yet.
            if (sourceOutputId == null || sourceOutputId.isEmpty()) {
                // nothing to do in order to clear this mapping.
                return;
            }

            // Create a new input->output mapping for sourceStepName.
            targetAppMapping = PipelineAutoBeanUtil.getPipelineAutoBeanFactory().appMapping().as();
            targetAppMapping.setStep(sourceStep.getStep());
            targetAppMapping.setId(sourceStep.getId());
            targetAppMapping.setMap(new FastMap<String>());

            mapInputsOutputs.put(sourceStepName, targetAppMapping);
        }

        // TODO validate targetInputId belongs to one of this App's Inputs?
        Map<String, String> map = targetAppMapping.getMap();
        if (Strings.isNullOrEmpty(sourceOutputId)) {
            // clear the mapping for this Input ID.
            map.put(targetInputId, null);
        } else {
            // Map sourceOutputId to this App's given targetInputId.
            map.put(targetInputId, sourceOutputId);
        }

        targetStep.setMappings(new ArrayList<PipelineAppMapping>(mapInputsOutputs.values()));
    }

    private FastMap<PipelineAppMapping> getTargetMappings(PipelineApp targetStep) {
        AutoBean<PipelineApp> targetBean = AutoBeanUtils.getAutoBean(targetStep);
        FastMap<PipelineAppMapping> mapInputsOutputs = targetBean.getTag("stepMappings"); //$NON-NLS-1$

        if (mapInputsOutputs == null) {
            mapInputsOutputs = new FastMap<PipelineAppMapping>();
            targetBean.setTag("stepMappings", mapInputsOutputs); //$NON-NLS-1$

            List<PipelineAppMapping> appMappings = targetStep.getMappings();
            if (appMappings != null) {
                for (PipelineAppMapping mapping : appMappings) {
                    String sourceStepName = getStepName(mapping.getStep(), mapping.getId());
                    mapInputsOutputs.put(sourceStepName, mapping);
                }
            }
        }

        return mapInputsOutputs;
    }

    private void resetAppMappings(PipelineApp targetStep) {
        AutoBean<PipelineApp> targetBean = AutoBeanUtils.getAutoBean(targetStep);
        targetBean.setTag("stepMappings", null); //$NON-NLS-1$

        targetStep.setMappings(null);
    }
}
