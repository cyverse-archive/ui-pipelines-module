package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.events.PipelineStepValidationEvent;
import org.iplantc.core.client.pipelines.models.PipelineAppModel;
import org.iplantc.core.client.pipelines.models.PipelineMapping;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.metadata.client.property.DataObject;
import org.iplantc.core.metadata.client.property.PropertyData;
import org.iplantc.core.uicommons.client.events.EventBus;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.ListModelPropertyEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * A panel that allows the user to map outputs to inputs for the Pipeline.
 * 
 * @author psarando
 * 
 */
public class InputOutputMappingPanel extends PipelineStep {

    private List<PipelineAppModel> apps;
    private Grid<PipelineAppModel> grid;

    public InputOutputMappingPanel(String title) {
        super(title);
        init();
    }

    private void init() {
        setLayout(new FitLayout());
        setSize(500, 300);

        grid = new Grid<PipelineAppModel>(new ListStore<PipelineAppModel>(), buildColumnModel());
        grid.getView().setEmptyText(I18N.DISPLAY.noApps());

        add(grid);
    }

    private ColumnModel buildColumnModel() {
        ColumnConfig name = new ColumnConfig(JSONMetaDataObject.DESCRIPTION, I18N.DISPLAY.name(), 125);
        name.setSortable(false);

        ColumnConfig inputLabel = new ColumnConfig(DataObject.INPUT_TYPE, I18N.DISPLAY.inputLabel(), 200);
        inputLabel.setSortable(false);
        inputLabel.setRenderer(new InputLabelGridCellRenderer());

        ColumnConfig inputSelect = new ColumnConfig(DataObject.OUTPUT_TYPE, I18N.DISPLAY.selectInputs(),
                200);
        inputSelect.setSortable(false);
        inputSelect.setRenderer(new InputSelectGridCellRenderer());

        ColumnConfig outputs = new ColumnConfig(DataObject.OUTPUT_FILENAME, I18N.DISPLAY.outputs(), 200);
        outputs.setSortable(false);
        outputs.setRenderer(new OutputsCellRenderer());

        return new ColumnModel(Arrays.asList(name, inputLabel, inputSelect, outputs));
    }

    private ComboBox<PropertyData> buildOutputsComboBox(final List<PipelineAppModel> sourceSteps,
            final PipelineAppModel targetStep, final String targetInputId) {
        ListStore<PropertyData> comboStore = new ListStore<PropertyData>();
        final List<PipelineMapping> mappings = new ArrayList<PipelineMapping>();

        PropertyData blankOption = new PropertyData(null);
        blankOption.set(PropertyData.LABEL, I18N.DISPLAY.userProvided());

        comboStore.add(blankOption);
        for (PipelineAppModel model : sourceSteps) {
            List<PropertyData> pd = model.getOutputs();
            comboStore.add(pd);
            mappings.add(new PipelineMapping(model, pd));
        }

        ComboBox<PropertyData> combo = new ComboBox<PropertyData>();
        combo.setStore(comboStore);
        combo.setEditable(false);
        combo.setTriggerAction(TriggerAction.ALL);
        combo.setPropertyEditor(new ListModelPropertyEditor<PropertyData>(PropertyData.LABEL));
        combo.setSimpleTemplate(Format.substitute("<span qtip='{{0}}'>{{0}}</span>", PropertyData.LABEL)); //$NON-NLS-1$

        // default to the "blank" selection
        combo.setSelection(Arrays.asList(blankOption));

        for (PipelineAppModel model : sourceSteps) {
            FastMap<String> mapping = targetStep.getInputOutputMapping().get(model.getStepName());
            if (mapping != null && !mapping.isEmpty()) {
                for (PropertyData output : model.getOutputs()) {
                    if (output.getProperty().getDataObject().getId().equals(mapping.get(targetInputId))) {
                        combo.setSelection(Arrays.asList(output));
                        break;
                    }
                }

            }
        }

        combo.addSelectionChangedListener(new MappingComboSelectionChangeListener(mappings, targetStep, targetInputId));

        return combo;
    }

    /**
     * Rebuilds this panel with the given PipelineAppModel list.
     * 
     * @param listAppModels
     */
    public void reconfigure(List<PipelineAppModel> listAppModels) {
        apps = listAppModels;

        reInitModels();

        ListStore<PipelineAppModel> store = new ListStore<PipelineAppModel>();
        store.add(apps);

        grid.reconfigure(store, grid.getColumnModel());
    }

    private void reInitModels() {
        int step_no = 1;
        if (apps != null) {
            for (PipelineAppModel model : apps) {
                model.resetInputOutputMapping();
                model.setStepName(step_no++);
            }
        }

    }

    @Override
    public boolean isValid() {
        // A pipline needs at least 2 apps and each app after the first one should have at least one
        // output-to-input mapping
        if (apps == null || apps.size() < 2) {
            return false;
        }

        for (int i = 1; i < apps.size(); i++) {
            PipelineAppModel targetModel = apps.get(i);

            JSONArray ioMappingArray = targetModel.ioMappingToJson();
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

        if (apps == null || apps.size() < 2) {
            return ret;
        }

        int retIndex = 0;
        for (int i = 1; i < apps.size(); i++) {
            PipelineAppModel targetModel = apps.get(i);
            JSONArray mapping = targetModel.ioMappingToJson();

            for (int j = 0; j < mapping.size(); j++) {
                ret.set(retIndex, mapping.get(j));
                retIndex++;
            }
        }

        return ret;
    }
    
    @Override
    protected void onShow() {
        super.onShow();
        grid.getView().refresh(false);
    }
    
    private final class OutputsCellRenderer implements GridCellRenderer<PipelineAppModel> {
        @Override
        public Object render(PipelineAppModel model, String property, ColumnData config,
                int rowIndex, int colIndex, ListStore<PipelineAppModel> store,
                Grid<PipelineAppModel> grid) {
            VerticalPanel ret = new VerticalPanel();
            ret.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
            ret.setSpacing(10);

            for (PropertyData propertyModel : model.getOutputs()) {
                ret.add(new Label((String)propertyModel.get(PropertyData.LABEL)));
            }

            return ret;
        }
    }

    private final class InputSelectGridCellRenderer implements GridCellRenderer<PipelineAppModel> {
        @Override
        public Object render(PipelineAppModel model, String property, ColumnData config,
                int rowIndex, int colIndex, ListStore<PipelineAppModel> store,
                Grid<PipelineAppModel> grid) {
            VerticalPanel ret = new VerticalPanel();
            ret.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

            if (rowIndex == 0) {
                ret.setSpacing(10);
            } else {
                ret.setSpacing(4);
            }
            
            List<PipelineAppModel> prevModels = new ArrayList<PipelineAppModel>();
            int index = 0;
            for (PipelineAppModel prevModel : store.getModels()) {
                if (index < rowIndex) {
                    prevModels.add(prevModel);
                    index++;
                }
            }

            for (PropertyData propertyModel : model.getInputs()) {
                if (rowIndex == 0) {
                    ret.add(new Label(I18N.DISPLAY.userProvided()));
                } else {
                    ret.add(buildOutputsComboBox(prevModels, model, propertyModel.getProperty()
                            .getDataObject().getId()));
                }
            }

            return ret;
        }
    }

    private final class InputLabelGridCellRenderer implements GridCellRenderer<PipelineAppModel> {
        @Override
        public Object render(PipelineAppModel model, String property, ColumnData config,
                int rowIndex, int colIndex, ListStore<PipelineAppModel> store,
                Grid<PipelineAppModel> grid) {
            VerticalPanel ret = new VerticalPanel();
            ret.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
            ret.setSpacing(10);

            for (PropertyData propertyModel : model.getInputs()) {
                ret.add(new Label((String)propertyModel.get(PropertyData.LABEL)));
            }

            return ret;
        }
    }

    private final class MappingComboSelectionChangeListener extends
            SelectionChangedListener<PropertyData> {
        private final List<PipelineMapping> mappings;
        private final PipelineAppModel targetStep;
        private final String targetInputId;

        private MappingComboSelectionChangeListener(List<PipelineMapping> mappings,
                PipelineAppModel targetStep, String targetInputId) {
            this.mappings = mappings;
            this.targetStep = targetStep;
            this.targetInputId = targetInputId;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent<PropertyData> se) {
            // set the input-output mapping based on the selected Output value
            String outputId = null;
            PropertyData selectedOutput = se.getSelectedItem();

            if (selectedOutput != null && selectedOutput.getProperty() != null) {
                outputId = selectedOutput.getProperty().getDataObject().getId();
            }

            for (PipelineMapping mapping : mappings) {
                for (PropertyData data : mapping.getPropertyDataList()) {
                    if (selectedOutput.equals(data)) {
                        targetStep.setInputOutputMapping(mapping.getAppModel().getStepName(), outputId,
                                targetInputId);
                        break;
                    }
                }
            }

            EventBus.getInstance().fireEvent(new PipelineStepValidationEvent(isValid()));
        }
    }

    @Override
    protected void setData(JSONObject obj) {
       JSONArray mappings = JsonUtil.getArray(obj, "mappings");
        if (mappings != null) {
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject temp = mappings.get(i).isObject();
                String target_step = JsonUtil.getString(temp, "target_step");
                String source_step = JsonUtil.getString(temp, "source_step");
                for (PipelineAppModel model : grid.getStore().getModels()) {
                    if(model.getStepName().equals(target_step)) {
                        JSONObject map = JsonUtil.getObject(temp, "map");
                        for (String key : map.keySet()) {
                            String value = JsonUtil.getString(map, key);
                            model.setInputOutputMapping(source_step, value, key);
                        }
                    }
                }

            }
            grid.getView().refresh(false);
        }
    }
    
}
