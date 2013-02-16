package org.iplantc.core.client.pipelines.gxt3.presenter;

import java.util.ArrayList;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplant.pipeline.client.json.autobeans.Pipeline;
import org.iplant.pipeline.client.json.autobeans.PipelineApp;
import org.iplant.pipeline.client.json.autobeans.PipelineAppMapping;
import org.iplantc.core.client.pipelines.gxt3.dnd.AppsGridDragHandler;
import org.iplantc.core.client.pipelines.gxt3.dnd.PipelineBuilderDropHandler;
import org.iplantc.core.client.pipelines.gxt3.util.PipelineAutoBeanUtil;
import org.iplantc.core.client.pipelines.gxt3.views.PipelineView;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbar;
import org.iplantc.core.client.pipelines.gxt3.views.widgets.PipelineViewToolbarImpl;
import org.iplantc.core.uiapplications.client.models.autobeans.App;
import org.iplantc.core.uiapplications.client.presenter.AppsViewPresenter;
import org.iplantc.core.uiapplications.client.views.AppsView;
import org.iplantc.core.uiapplications.client.views.AppsViewImpl;
import org.iplantc.core.uicommons.client.presenter.Presenter;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.shared.FastMap;
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
        PipelineViewToolbar.Presenter {

    private final PipelineView view;
    private final PipelineViewToolbar toolbar;
    private final Command onPublishCallback;

    public PipelineViewPresenter(PipelineView view, Command onPublishCallback) {
        this.view = view;
        this.onPublishCallback = onPublishCallback;

        toolbar = new PipelineViewToolbarImpl();

        view.setPresenter(this);
        toolbar.setPresenter(this);

        view.setNorthWidget(toolbar);

        Container builderPanel = view.getBuilderDropContainer();

        AppsView appsView = new AppsViewImpl();
        AppsViewPresenter appsPresenter = new AppsViewPresenter(appsView);

        initAppsGridDragHandler(builderPanel, appsView.getAppsGrid());
        initPipelineBuilderDropHandler(builderPanel, view.getPipelineCreator());

        appsPresenter.builder()
                .hideToolbarButtonCopy()
                .hideToolbarButtonCreate()
                .hideToolbarButtonDelete()
                .hideToolbarButtonEdit()
                .hideToolbarButtonRequestTool()
                .hideToolbarButtonSubmit()
                .go(view.getAppsContainer());
    }

    private void initAppsGridDragHandler(Container builderPanel, Grid<App> grid) {
        AppsGridDragHandler handler = new AppsGridDragHandler(builderPanel);

        GridDragSource<App> source = new GridDragSource<App>(grid);
        source.addDragStartHandler(handler);
        source.addDragCancelHandler(handler);
    }

    private void initPipelineBuilderDropHandler(Container builderPanel, PipelineCreator creator) {
        PipelineBuilderDropHandler handler = new PipelineBuilderDropHandler(builderPanel, creator);

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

            Pipeline pipeline = view.getPipeline();
            if (pipeline != null) {
                view.getPipelineCreator().loadPipeline(pipeline);
            }
        } else {
            activeView = view.getStepEditorPanel();

            Pipeline pipeline = view.getPipelineCreator().getPipeline();
            if (pipeline != null) {
                view.setPipeline(pipeline);
            }
        }

        view.setActiveView(activeView);
    }

    @Override
    public Pipeline getPipeline() {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * Gets the given App's workflow step name, based on its position in the workflow and its ID.
     * 
     * @return
     */
    private String getStepName(PipelineApp app) {
        return app == null ? "" : getStepName(app.getStep(), app.getId()); //$NON-NLS-1$
    }

    private String getStepName(int step, String id) {
        // steps start at 0.
        return Format.substitute("step_{0}_{1}", (step + 1), id); //$NON-NLS-1$
    }

    /**
     * Sets a mapping for this "target_step" App's Input DataObject, with the given targetInputId, to
     * sourceStepName's Output DataObject with the given sourceOutputId. A null sourceOutputId will clear
     * the mapping for the given targetInputId.
     * 
     * @param sourceStepName
     * @param sourceOutputId
     * @param targetInputId
     */
    private void setInputOutputMapping(PipelineApp sourceStep, String sourceOutputId,
            PipelineApp targetStep, String targetInputId) {
        String sourceStepName = getStepName(sourceStep);

        // Find the input->output mappings for sourceStepName.
        FastMap<PipelineAppMapping> mapInputsOutputs = getTargetMappings(targetStep);
        PipelineAppMapping ioMapping = mapInputsOutputs.get(sourceStepName);

        if (ioMapping == null) {
            // There are no input->output mappings for this sourceStepName yet.
            if (sourceOutputId == null || sourceOutputId.isEmpty()) {
                // nothing to do in order to clear this mapping.
                return;
            }

            // Create a new input->output mapping for sourceStepName.
            ioMapping = PipelineAutoBeanUtil.getPipelineAutoBeanFactory().appMapping().as();
            ioMapping.setStep(sourceStep.getStep());
            ioMapping.setId(sourceStep.getId());
            ioMapping.setMap(new FastMap<String>());

            mapInputsOutputs.put(sourceStepName, ioMapping);
        }

        // TODO validate targetInputId belongs to one of this App's Inputs?
        FastMap<String> map = (FastMap<String>)ioMapping.getMap();
        if (sourceOutputId == null || sourceOutputId.isEmpty()) {
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

            if (targetStep.getMappings() != null) {
                for (PipelineAppMapping mapping : targetStep.getMappings()) {
                    mapInputsOutputs.put(getStepName(mapping.getStep(), mapping.getId()), mapping);
                }
            }
        }

        return mapInputsOutputs;
    }
}
