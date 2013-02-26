package org.iplantc.core.client.pipelines.gxt3.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iplant.pipeline.client.json.autobeans.Pipeline;
import org.iplant.pipeline.client.json.autobeans.PipelineApp;
import org.iplant.pipeline.client.json.autobeans.PipelineAppData;
import org.iplant.pipeline.client.json.autobeans.PipelineAppMapping;
import org.iplantc.core.client.pipelines.I18N;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.ListField;

/**
 * A PipelineAppMappingView that displays input mappings as FieldLabels with a ComboBox for mapping an
 * output to an input.
 * 
 * @author psarando
 * 
 */
public class PipelineAppMappingForm implements PipelineAppMappingView {
    private Presenter presenter;
    private Pipeline pipeline;

    private final VerticalLayoutContainer container;
    private final LabelProvider<PipelineMappingOutputWrapper> labelProvider;
    private final ModelKeyProvider<PipelineMappingOutputWrapper> outputsKeyProvider;
    private final ValueProvider<PipelineMappingOutputWrapper, String> outputsValueProvider;

    public PipelineAppMappingForm() {
        container = new VerticalLayoutContainer();
        container.setScrollMode(ScrollMode.AUTO);

        labelProvider = new OutputComboLabelProvider();
        outputsKeyProvider = new OutputWrapperKeyProvider();
        outputsValueProvider = new OutputWrapperValueProvider();
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;

        container.clear();

        List<PipelineApp> apps = pipeline.getApps();
        if (apps == null) {
            return;
        }

        // Keep a list of all previous steps' outputs.
        List<PipelineMappingOutputWrapper> outputs = new ArrayList<PipelineMappingOutputWrapper>();

        // Build a FieldSet for each step.
        for (PipelineApp app : apps) {
            FieldSet step = buildStepFieldSet(app);
            container.add(step);

            VerticalLayoutContainer panel = new VerticalLayoutContainer();
            step.add(panel);

            // Add this step's available inputs each with a ComboBox of all previous steps' outputs.
            List<PipelineAppData> appInputs = app.getInputs();
            if (appInputs != null) {
                for (PipelineAppData input : appInputs) {
                    ComboBox<PipelineMappingOutputWrapper> combo = buildOutputCombo(app, input, outputs);

                    FieldLabel inputField = new FieldLabel(combo, input.getName());
                    panel.add(inputField);
                }
            }

            // Add a list of this step's available outputs.
            List<PipelineMappingOutputWrapper> outputWrappers = wrapOutputs(app);
            if (outputWrappers != null) {
                outputs.addAll(outputWrappers);

                FieldLabel outputList = new FieldLabel(buildOutputsListField(outputWrappers),
                        I18N.DISPLAY.outputs());
                panel.add(outputList);
            }
        }
    }

    private List<PipelineMappingOutputWrapper> wrapOutputs(PipelineApp app) {
        List<PipelineMappingOutputWrapper> outputWrappers = null;

        List<PipelineAppData> appOutputs = app.getOutputs();
        if (appOutputs != null) {
            outputWrappers = new ArrayList<PipelineMappingOutputWrapper>();

            for (PipelineAppData output : appOutputs) {
                outputWrappers.add(new PipelineMappingOutputWrapper(app, output));
            }
        }

        return outputWrappers;
    }

    private FieldSet buildStepFieldSet(PipelineApp app) {
        FieldSet step = new FieldSet();
        String stepLabel = I18N.DISPLAY.stepWithValue(app.getStep());
        step.setHeadingText(Format.substitute("{0}: {1}", stepLabel, app.getName())); //$NON-NLS-1$
        step.setCollapsible(true);

        return step;
    }

    private ComboBox<PipelineMappingOutputWrapper> buildOutputCombo(PipelineApp app,
            PipelineAppData input, List<PipelineMappingOutputWrapper> outputs) {
        ListStore<PipelineMappingOutputWrapper> store = new ListStore<PipelineMappingOutputWrapper>(
                outputsKeyProvider);

        store.addAll(outputs);

        ComboBox<PipelineMappingOutputWrapper> combo = new ComboBox<PipelineMappingOutputWrapper>(store,
                labelProvider);
        combo.setEmptyText(I18N.DISPLAY.userProvided());
        combo.setAllowBlank(true);
        combo.setForceSelection(true);
        combo.setTriggerAction(TriggerAction.ALL);
        combo.setWidth(200);
        combo.addSelectionHandler(new OutputComboSelectionHandler(presenter, app, input.getId()));

        return combo;
    }

    private ListField<PipelineMappingOutputWrapper, String> buildOutputsListField(
            List<PipelineMappingOutputWrapper> outputs) {
        ListStore<PipelineMappingOutputWrapper> outputListStore = new ListStore<PipelineMappingOutputWrapper>(
                outputsKeyProvider);
        outputListStore.addAll(outputs);

        ListView<PipelineMappingOutputWrapper, String> outputsView = new ListView<PipelineMappingOutputWrapper, String>(
                outputListStore, outputsValueProvider);

        ListField<PipelineMappingOutputWrapper, String> outputListField = new ListField<PipelineMappingOutputWrapper, String>(outputsView);
        outputListField.setWidth(200);

        return outputListField;
    }

    @Override
    public boolean isValid() {
        // A pipline needs at least 2 apps and each app after the first one should have at least one
        // output-to-input mapping
        List<PipelineApp> apps = pipeline.getApps();
        if (apps == null || apps.size() < 2) {
            return false;
        }

        for (int i = 1; i < apps.size(); i++) {
            PipelineApp targetApp = apps.get(i);

            List<PipelineAppMapping> mappings = targetApp.getMappings();
            if (mappings == null || mappings.size() < 1) {
                return false;
            }

            for (PipelineAppMapping mapping : mappings) {
                Map<String, String> map = mapping.getMap();

                if (map == null || map.keySet().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * A convenience wrapper to hold a reference to a Pipeline App and one of its Outputs.
     * 
     * @author psarando
     * 
     */
    private class PipelineMappingOutputWrapper {
        private final PipelineApp sourceApp;
        private final PipelineAppData sourceOutput;

        public PipelineMappingOutputWrapper(PipelineApp sourceApp, PipelineAppData sourceOutput) {
            this.sourceApp = sourceApp;
            this.sourceOutput = sourceOutput;
        }

        /**
         * @return the sourceApp
         */
        public PipelineApp getApp() {
            return sourceApp;
        }

        /**
         * @return the sourceOutput
         */
        public PipelineAppData getOutput() {
            return sourceOutput;
        }
    }

    /**
     * A SelectionHandler for the output ComboBoxes that sets the mapping when an output is selected.
     * 
     * @author psarando
     * 
     */
    private class OutputComboSelectionHandler implements SelectionHandler<PipelineMappingOutputWrapper> {

        private final Presenter presenter;
        private final PipelineApp targetApp;
        private final String targetInputId;

        OutputComboSelectionHandler(Presenter presenter, PipelineApp targetApp, String targetInputId) {
            this.presenter = presenter;
            this.targetApp = targetApp;
            this.targetInputId = targetInputId;
        }

        @Override
        public void onSelection(SelectionEvent<PipelineMappingOutputWrapper> event) {
            PipelineMappingOutputWrapper selectedWrapper = event.getSelectedItem();

            if (selectedWrapper != null) {
                PipelineApp sourceApp = selectedWrapper.getApp();
                String sourceOutputId = selectedWrapper.getOutput().getId();

                presenter.setInputOutputMapping(targetApp, targetInputId, sourceApp, sourceOutputId);
            }
        }
    }

    private class OutputComboLabelProvider implements LabelProvider<PipelineMappingOutputWrapper> {

        @Override
        public String getLabel(PipelineMappingOutputWrapper item) {
            String stepLabel = I18N.DISPLAY.stepWithValue(item.getApp().getStep());
            return Format.substitute("{0}: {1}", stepLabel, item.getOutput().getName()); //$NON-NLS-1$
        }
    }

    private class OutputWrapperKeyProvider implements ModelKeyProvider<PipelineMappingOutputWrapper> {

        @Override
        public String getKey(PipelineMappingOutputWrapper item) {
            return item.getOutput().getId();
        }
    }

    private class OutputWrapperValueProvider implements ValueProvider<PipelineMappingOutputWrapper, String> {

        @Override
        public String getValue(PipelineMappingOutputWrapper object) {
            return object.getOutput().getName();
        }

        @Override
        public void setValue(PipelineMappingOutputWrapper object, String value) {
        }

        @Override
        public String getPath() {
            return null;
        }
    }
}
