package org.iplantc.core.client.pipelines.models;

import java.util.ArrayList;
import java.util.List;

import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.metadata.client.property.DataObject;
import org.iplantc.core.metadata.client.property.Property;
import org.iplantc.core.metadata.client.property.PropertyData;
import org.iplantc.core.uiapplications.client.models.Analysis;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * A BaseModelData class for Apps in a Pipeline. Contains methods for Input-Output mappings and
 * generating JSON for submitting to the Import Workflow service.
 * 
 * @author psarando
 * 
 */
@SuppressWarnings("serial")
public class PipelineAppModel extends BaseModelData {
    private static final String ANALYSIS = "Analysis";

    public static final String APP = "app"; //$NON-NLS-1$

    // JSON keys used in toJson objects
    public static final String STEP_NAME = "step_name"; //$NON-NLS-1$
    public static final String TEMPLATE_ID = "template_id"; //$NON-NLS-1$
    public static final String CONFIG = "config"; //$NON-NLS-1$
    public static final String SOURCE_STEP = "source_step"; //$NON-NLS-1$
    public static final String TARGET_STEP = "target_step"; //$NON-NLS-1$
    public static final String MAP = "map"; //$NON-NLS-1$

    // JSON keys and values used internally
    private static final String ID_KEY = "auto-gen"; //$NON-NLS-1$
    private static final String INPUTS = "inputs"; //$NON-NLS-1$
    private static final String OUTPUTS = "outputs"; //$NON-NLS-1$

    /**
     * A mapping of SourceStepIDs to InputOutputMaps, where InputOutputMap is a mapping of this App's
     * Input IDs to the SourceStep's Output IDs.
     */
    private FastMap<FastMap<String>> mapInputsOutputs;

    private Analysis app;

    /**
     * Constructs a PipelineAppModel from JSON which should have keys for an "id" string, "name" string,
     * and "inputs" and "outputs" arrays. Additionally, this constructor takes an Analysis object whose
     * id should match the id value in the json parameter.
     * 
     * @param json
     * @param app
     */
    public PipelineAppModel(JSONObject json, Analysis app) {

        mapInputsOutputs = new FastMap<FastMap<String>>();

        setApp(app);
        setId(JsonUtil.getString(json, JSONMetaDataObject.ID));
        setName(JsonUtil.getString(json, JSONMetaDataObject.NAME));
        setInputs(getPropertyDataList(json, INPUTS));
        setOutputs(getPropertyDataList(json, OUTPUTS));
    }

    private List<PropertyData> getPropertyDataList(JSONObject json, String type) {
        List<PropertyData> dataObjects = new ArrayList<PropertyData>();

        JSONArray jsonDataObjects = JsonUtil.getArray(json, type);
        if (jsonDataObjects != null) {
            for (int i = 0; i < jsonDataObjects.size(); i++) {
                Property dataObjectProperty = new Property(JsonUtil.getObjectAt(jsonDataObjects, i));

                PropertyData propertyModel = new PropertyData(dataObjectProperty);
                propertyModel.set(PropertyData.LABEL, getDataObjectLabel(propertyModel));

                dataObjects.add(propertyModel);
            }
        }

        return dataObjects;
    }

    private String getDataObjectLabel(PropertyData value) {
        if (value == null) {
            return null;
        }

        Property property = value.getProperty();
        if (property != null) {
            DataObject data = property.getDataObject();

            if (data != null) {
                if (!data.getLabel().isEmpty()) {
                    return data.getLabel();
                }

                if (!data.getOutputFilename().isEmpty()) {
                    return data.getOutputFilename();
                }

                if (!data.getName().isEmpty()) {
                    return data.getName();
                }
            }

            if (!property.getLabel().isEmpty()) {
                return property.getLabel();
            }
        }

        return value.getLabel();
    }

    /**
     * Gets the ID for this App in the Workflow.
     * 
     * @return The name of this App in the Workflow.
     */
    public String getId() {
        return get(JSONMetaDataObject.ID) != null ? get(JSONMetaDataObject.ID).toString() : ""; //$NON-NLS-1$
    }

    /**
     * Sets the ID for this App in the Workflow.
     * 
     * @param id
     */
    public void setId(String id) {
        set(JSONMetaDataObject.ID, id);
    }

    /**
     * Gets the "name" identifier for this App in the Workflow.
     * 
     * @return The name of this App in the Workflow.
     */
    public String getName() {
        return get(JSONMetaDataObject.NAME) != null ? get(JSONMetaDataObject.NAME).toString() : ""; //$NON-NLS-1$
    }

    /**
     * Sets the "name" identifier for this App in the Workflow.
     * 
     * @param name
     */
    public void setName(String name) {
        set(JSONMetaDataObject.NAME, name);
    }

    /**
     * Sets this App's step name based on the step number of the App in the workflow.
     * 
     * @param step
     */
    public void setStepName(int step) {
        set(STEP_NAME, "step_" + step + "_" + getId()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Gets this App's workflow step name.
     * 
     * @return
     */
    public String getStepName() {
        return get(STEP_NAME) != null ? get(STEP_NAME).toString() : ""; //$NON-NLS-1$
    }

    /**
     * Gets the list of PropertyData Inputs for this App.
     * 
     * @return This App's list of PropertyData Inputs.
     */
    public List<PropertyData> getInputs() {
        return get(DataObject.INPUT_TYPE);
    }

    /**
     * Sets the list of PropertyData Inputs for this App.
     * 
     * @param inputs
     */
    public void setInputs(List<PropertyData> inputs) {
        set(DataObject.INPUT_TYPE, inputs);
    }

    /**
     * Gets the list of PropertyData Outputs for this App.
     * 
     * @return This App's list of PropertyData Outputs.
     */
    public List<PropertyData> getOutputs() {
        return get(DataObject.OUTPUT_TYPE);
    }

    /**
     * Sets the list of PropertyData Outputs for this App.
     * 
     * @param inputs
     */
    public void setOutputs(List<PropertyData> inputs) {
        set(DataObject.OUTPUT_TYPE, inputs);
    }

    /**
     * Sets a mapping for this "target_step" App's Input DataObject, with the given targetInputId, to
     * sourceStepName's Output DataObject with the given sourceOutputId. A null sourceOutputId will clear
     * the mapping for the given targetInputId.
     * 
     * @param sourceStepName
     * @param sourceOutputIdValue
     * @param targetInputIdKey
     */
    public void setInputOutputMapping(String sourceStepName, String sourceOutputIdValue, String targetInputIdKey) {
        // TODO validate targetInputId belongs to one of this App's Inputs?

        // Find the input->output mappings for sourceStepName.
        FastMap<String> ioMapping = mapInputsOutputs.get(sourceStepName);

        if (ioMapping == null) {
            // There are no input->output mappings for this sourceStepName yet.
            if (sourceOutputIdValue == null || sourceOutputIdValue.isEmpty()) {
                // nothing to do in order to clear this mapping.
                return;
            }

            // Create a new input->output mapping for sourceStepName.
            ioMapping = new FastMap<String>();
            mapInputsOutputs.put(sourceStepName, ioMapping);
        }

        if (sourceOutputIdValue == null || sourceOutputIdValue.isEmpty()) {
            // clear the mapping for this Input ID.
            ioMapping.remove(targetInputIdKey);
        } else {
            // Map sourceOutputId to this App's given targetInputId.
            ioMapping.put(targetInputIdKey, sourceOutputIdValue);
        }
    }

    public FastMap<FastMap<String>> getInputOutputMapping() {
        return mapInputsOutputs;
    }

    /**
     * @param app the app to set
     */
    public void setApp(Analysis app) {
        set(APP, app);
        this.app = app;
    }

    /**
     * @return the app
     */
    public Analysis getApp() {
        return app;
    }

    /**
     * Clears the Output-Input mapping for this App in the Workflow.
     */
    public void resetInputOutputMapping() {
        mapInputsOutputs = new FastMap<FastMap<String>>();
    }

    /**
     * Gets a JSON object representing this App model as a workflow step.
     * 
     * @return A JSON object this App as a workflow step.
     */
    public JSONObject stepToJson() {
        JSONObject ret = new JSONObject();
        ret.put(JSONMetaDataObject.ID, new JSONString(ID_KEY));
        ret.put(JSONMetaDataObject.NAME, new JSONString(getStepName()));
        ret.put(TEMPLATE_ID, new JSONString(getId()));
        ret.put(JSONMetaDataObject.DESCRIPTION, new JSONString(getName()));
        JSONObject appObj = app.toJson();
        appObj.put(INPUTS, buildPropertyDataArrayFromList(getInputs()));
        appObj.put(OUTPUTS, buildPropertyDataArrayFromList(getOutputs()));
        ret.put(ANALYSIS, appObj);
        ret.put(CONFIG, new JSONObject());

        return ret;
    }

    private JSONArray buildPropertyDataArrayFromList(List<PropertyData> propertyDataList) {
        JSONArray arr = new JSONArray();
        int index = 0;
        for (PropertyData data : propertyDataList) {
            arr.set(index++, data.getProperty().toJson());
        }
        return arr;
    }

    /**
     * Formats the Output->Input mappings for this "target_step" App as a JSON array for the Import
     * Workflow service.
     * 
     * @return A JSON array of Output-Input mappings for this App.
     */
    public JSONArray ioMappingToJson() {
        JSONArray ret = new JSONArray();

        int mappingIndex = 0;
        for (String sourceStepName : mapInputsOutputs.keySet()) {
            // Get the input->output mappings for sourceStepName.
            FastMap<String> ioMap = mapInputsOutputs.get(sourceStepName);

            if (ioMap != null && !ioMap.isEmpty()) {
                // Build the JSON output->input map object.
                JSONObject map = new JSONObject();

                for (String inputId : ioMap.keySet()) {
                    String outputId = ioMap.get(inputId);

                    if (outputId != null && !outputId.isEmpty()) {
                        map.put(outputId, new JSONString(inputId));
                    }
                }

                // Ensure at least one output->input is set for sourceStepName in the JSON map object.
                if (!map.keySet().isEmpty()) {
                    // Add the mappings (from sourceStepName to this App) to the JSON array.
                    JSONObject mapping = new JSONObject();

                    mapping.put(SOURCE_STEP, new JSONString(sourceStepName));
                    mapping.put(TARGET_STEP, new JSONString(getStepName()));
                    mapping.put(MAP, map);

                    ret.set(mappingIndex, mapping);
                    mappingIndex++;
                }
            }
        }

        return ret;
    }
}
