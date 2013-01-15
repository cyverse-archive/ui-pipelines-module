package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.models.PipelineAppModel;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Defines a common view of a Pipeline Editor.
 * 
 * @author psarando
 * 
 */
public abstract class PipelineEditorView extends ContentPanel {
    // JSON keys used in getPublishJson objects
    public static final String ANALYSIS_NAME = "analysis_name"; //$NON-NLS-1$
    public static final String STEPS = "steps"; //$NON-NLS-1$
    public static final String MAPPINGS = "mappings"; //$NON-NLS-1$
    public static final String PIPELINE_CREATOR_STEPS = "apps"; //$NON-NLS-1$
    public static final String TEMPLATE_ID = "template_id"; //$NON-NLS-1$
    public static final String CONFIG = "config"; //$NON-NLS-1$
    public static final String SOURCE_STEP = "source_step"; //$NON-NLS-1$
    public static final String TARGET_STEP = "target_step"; //$NON-NLS-1$

    // JSON keys and values used internally
    private static final String ID_KEY = "auto-gen"; //$NON-NLS-1$

    public PipelineEditorView() {
        setScrollMode(Scroll.AUTO);
    }

    /**
     * Checks if the Pipeline is valid.
     * 
     * @return boolean true or false
     */
    public abstract boolean isValid();

    /**
     * Gets a JSON representation of the Pipeline.
     * 
     * @return JSONObject representing the Pipeline.
     */
    public abstract JSONObject toJson();

    /**
     * Get the JSON of this pipeline required for publishing.
     * 
     * @return JSONObject required for publishing.
     */
    public JSONObject getPublishJson() {
        JSONObject pipelineJson = toJson();
        JSONArray steps = JsonUtil.getArray(pipelineJson, PIPELINE_CREATOR_STEPS);

        if (steps == null) {
            return null;
        }

        JSONObject publishJson = new JSONObject();

        publishJson.put(JSONMetaDataObject.ID, new JSONString(ID_KEY));
        publishJson.put(ANALYSIS_NAME,
                new JSONString(JsonUtil.getString(pipelineJson, JSONMetaDataObject.NAME)));
        publishJson.put(JSONMetaDataObject.DESCRIPTION,
                new JSONString(JsonUtil.getString(pipelineJson, JSONMetaDataObject.DESCRIPTION)));

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

            JSONObject stepMap = JsonUtil.getObject(sourceStepMapping, PipelineAppModel.MAP);
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
                    mapping.put(PipelineAppModel.MAP, map);

                    return mapping;
                }
            }
        }

        // No mappings were found in the given sourceStepMapping.
        return null;
    }

    /**
     * Initializes the Pipeline from the given JSON.
     * 
     * @param obj JSON representation of the Pipeline.
     */
    public abstract void configure(JSONObject obj);

    /**
     * Remove event handlers and free-up resources.
     */
    public abstract void cleanup();
}
