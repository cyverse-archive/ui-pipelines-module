package org.iplantc.core.client.pipelines.models;

import java.util.ArrayList;
import java.util.List;

import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.metadata.client.property.DataObject;
import org.iplantc.core.metadata.client.property.Property;
import org.iplantc.core.metadata.client.property.PropertyData;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
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
    // JSON keys used in toJson objects
    public static final String STEP = "step"; //$NON-NLS-1$
    public static final String MAPPINGS = "mappings"; //$NON-NLS-1$
    public static final String MAP = "map"; //$NON-NLS-1$

    // JSON keys and values used internally
    private static final String INPUTS = "inputs"; //$NON-NLS-1$
    private static final String OUTPUTS = "outputs"; //$NON-NLS-1$

    /**
     * A mapping of SourceStepIDs to InputOutputMaps, where InputOutputMap is a mapping of this App's
     * Input IDs to the SourceStep's Output IDs.
     */
    private FastMap<JSONObject> mapInputsOutputs;

    private int step;

    /**
     * Constructs a PipelineAppModel from JSON which should have keys for an "id" string, "name" string,
     * and "inputs" and "outputs" arrays. Additionally, this constructor takes an Analysis object whose
     * id should match the id value in the json parameter.
     * 
     * @param json
     * @param app
     */
    public PipelineAppModel(JSONObject json) {

        mapInputsOutputs = new FastMap<JSONObject>();

        setId(JsonUtil.getString(json, JSONMetaDataObject.ID));
        setName(JsonUtil.getString(json, JSONMetaDataObject.NAME));
        setDescription(JsonUtil.getString(json, JSONMetaDataObject.DESCRIPTION));
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
     * Sets this App's position in the workflow.
     * 
     * @param step
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Gets this App's position in the workflow.
     * 
     * @return the App's position in the workflow
     */
    public int getStep() {
        return step;
    }

    /**
     * Gets this App's workflow step name, based on its position in the workflow and its ID.
     * 
     * @return
     */
    public String getStepName() {
        // steps start at 0.
        return "step_" + (step + 1) + "_" + getId(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getDescription() {
        return get(JSONMetaDataObject.DESCRIPTION) != null ? get(JSONMetaDataObject.DESCRIPTION)
                .toString() : ""; //$NON-NLS-1$
    }

    public void setDescription(String description) {
        set(JSONMetaDataObject.DESCRIPTION, description);
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

    public void setSourceMappings(PipelineAppModel sourceStep, JSONObject mapping) {
        if (sourceStep != null) {
            mapInputsOutputs.put(sourceStep.getStepName(), mapping);
        }
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
    public void setInputOutputMapping(PipelineAppModel sourceStep, String sourceOutputId,
            String targetInputId) {
        String sourceStepName = sourceStep.getStepName();

        // Find the input->output mappings for sourceStepName.
        JSONObject ioMapping = mapInputsOutputs.get(sourceStepName);

        if (ioMapping == null) {
            // There are no input->output mappings for this sourceStepName yet.
            if (sourceOutputId == null || sourceOutputId.isEmpty()) {
                // nothing to do in order to clear this mapping.
                return;
            }

            // Create a new input->output mapping for sourceStepName.
            ioMapping = new JSONObject();
            ioMapping.put(STEP, new JSONNumber(sourceStep.getStep()));
            ioMapping.put(JSONMetaDataObject.ID, new JSONString(sourceStep.getId()));
            ioMapping.put(MAP, new JSONObject());

            mapInputsOutputs.put(sourceStepName, ioMapping);
        }

        // TODO validate targetInputId belongs to one of this App's Inputs?
        JSONObject map = JsonUtil.getObject(ioMapping, MAP);
        if (sourceOutputId == null || sourceOutputId.isEmpty()) {
            // clear the mapping for this Input ID.
            map.put(targetInputId, null);
        } else {
            // Map sourceOutputId to this App's given targetInputId.
            map.put(targetInputId, new JSONString(sourceOutputId));
        }
    }

    public FastMap<JSONObject> getInputOutputMapping() {
        return mapInputsOutputs;
    }

    /**
     * Clears the Output-Input mapping for this App in the Workflow.
     */
    public void resetInputOutputMapping() {
        mapInputsOutputs = new FastMap<JSONObject>();
    }

    /**
     * Gets a JSON object representing this App model as a workflow step.
     * 
     * @return A JSON object this App as a workflow step.
     */
    public JSONObject toJson() {
        JSONObject ret = new JSONObject();

        ret.put(JSONMetaDataObject.ID, new JSONString(getId()));
        ret.put(JSONMetaDataObject.NAME, new JSONString(getName()));
        ret.put(JSONMetaDataObject.DESCRIPTION, new JSONString(getDescription()));
        ret.put(STEP, new JSONNumber(getStep()));
        ret.put(MAPPINGS, ioMappingToJson());
        ret.put(INPUTS, buildPropertyDataArrayFromList(getInputs()));
        ret.put(OUTPUTS, buildPropertyDataArrayFromList(getOutputs()));

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
            JSONObject ioMapping = mapInputsOutputs.get(sourceStepName);

            // Ensure at least one output->input is set for sourceStepName in the JSON map object.
            JSONObject map = JsonUtil.getObject(ioMapping, MAP);

            if (map != null && !map.keySet().isEmpty()) {
                ret.set(mappingIndex, ioMapping);
                mappingIndex++;
            }
        }

        return ret;
    }
}
