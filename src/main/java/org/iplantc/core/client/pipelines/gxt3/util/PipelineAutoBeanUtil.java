package org.iplantc.core.client.pipelines.gxt3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.gxt3.models.ImplementorDetailTest;
import org.iplantc.core.client.pipelines.gxt3.models.ImplementorDetails;
import org.iplantc.core.client.pipelines.gxt3.models.ServicePipeline;
import org.iplantc.core.client.pipelines.gxt3.models.ServicePipelineAnalysis;
import org.iplantc.core.client.pipelines.gxt3.models.ServicePipelineAutoBeanFactory;
import org.iplantc.core.client.pipelines.gxt3.models.ServicePipelineMapping;
import org.iplantc.core.client.pipelines.gxt3.models.ServicePipelineStep;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineAppData;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineAppMapping;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineAutoBeanFactory;
import org.iplantc.core.uiapps.client.Services;
import org.iplantc.core.uiapps.client.models.autobeans.App;
import org.iplantc.core.uiapps.client.models.autobeans.AppDataObject;
import org.iplantc.core.uiapps.client.models.autobeans.DataObject;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.models.UserInfo;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.shared.FastMap;

/**
 * A Utility class for Pipeline AutoBeans and converting them to/from the service JSON.
 * 
 * @author psarando
 * 
 */
public class PipelineAutoBeanUtil {

    private final PipelineAutoBeanFactory factory = GWT.create(PipelineAutoBeanFactory.class);
    private final ServicePipelineAutoBeanFactory serviceFactory = GWT
            .create(ServicePipelineAutoBeanFactory.class);

    private static final String AUTO_GEN_ID = "auto-gen"; //$NON-NLS-1$

    /**
     * @return A singleton instance of the PipelineAutoBeanFactory.
     */
    public PipelineAutoBeanFactory getPipelineFactory() {
        return factory;
    }

    /**
     * Clones the data contained in the given App into a PipelineApp, calling the App service to fetch
     * the input and output data objects for inclusion in the PipelineApp result.
     * 
     * @param app Must be eligible for pipelines, otherwise the callback's onFailure method is called.
     * @param callback Receives the PipelineApp result on success, cloned from the data contained in the
     *            given app plus the data objects returned from the App service.
     */
    public void appToPipelineApp(final App app, final AsyncCallback<PipelineApp> callback) {
        if (app == null) {
            callback.onFailure(new NullPointerException());
            return;
        }

        if (!app.getPipelineEligibility().isValid()) {
            callback.onFailure(new Exception(app.getPipelineEligibility().getReason()));
            return;
        }

        Services.USER_APP_SERVICE.getDataObjectsForApp(app.getId(), new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                AutoBean<App> appBean = AutoBeanUtils.getAutoBean(app);

                // Clone the App AutoBean so we don't modify the original.
                Splittable appJson = AutoBeanCodex.encode(appBean);
                appBean = AutoBeanCodex.decode(appBean.getFactory(), App.class, appJson.getPayload());

                Splittable json = StringQuoter.split(result);
                AutoBeanCodex.decodeInto(json, appBean);

                callback.onSuccess(appBeanToPipelineApp(appBean));
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.dataObjectsRetrieveError(), caught);
                callback.onFailure(caught);
            }
        });
    }

    /**
     * Converts an App AutoBean into a PipelineApp.
     * 
     * @param appBean
     * @return A PipelineApp cloned from the data contained in the given appBean.
     */
    public PipelineApp appBeanToPipelineApp(AutoBean<App> appBean) {
        if (appBean == null) {
            return null;
        }

        App app = appBean.as();

        PipelineApp ret = AutoBeanCodex.decode(factory, PipelineApp.class,
                AutoBeanCodex.encode(appBean).getPayload()).as();

        List<PipelineAppData> inputs = new ArrayList<PipelineAppData>();
        List<AppDataObject> appDataObjInputs = app.getInputs();
        if (appDataObjInputs != null) {
            for (AppDataObject appDataObj : appDataObjInputs) {
                DataObject dataObject = appDataObj.getDataObject();

                PipelineAppData input = factory.appData().as();
                input.setId(dataObject.getId());
                input.setName(dataObject.getName());
                input.setDescription(dataObject.getDescription());
                input.setRequired(dataObject.getRequired());
                input.setFormat(dataObject.getFormat());

                inputs.add(input);
            }
        }
        ret.setInputs(inputs);

        List<PipelineAppData> outputs = new ArrayList<PipelineAppData>();
        List<AppDataObject> appDataObjOutputs = app.getOutputs();
        if (appDataObjOutputs != null) {
            for (AppDataObject appDataObj : appDataObjOutputs) {
                DataObject dataObject = appDataObj.getDataObject();

                PipelineAppData output = factory.appData().as();
                output.setId(dataObject.getId());
                output.setName(dataObject.getName());
                output.setDescription(dataObject.getDescription());
                output.setRequired(dataObject.getRequired());
                output.setFormat(dataObject.getFormat());

                outputs.add(output);
            }
        }
        ret.setOutputs(outputs);

        return ret;
    }

    /**
     * Get the JSON of the given pipeline required for publishing.
     * 
     * @return JSON string required for publishing the given pipeline.
     */
    public String getPublishJson(Pipeline pipeline) {
        if (pipeline == null) {
            return null;
        }

        List<PipelineApp> steps = pipeline.getApps();

        if (steps == null) {
            return null;
        }

        ServicePipelineAnalysis pipelineAnalysis = serviceFactory.servicePipelineAnalysis().as();
        pipelineAnalysis.setId(AUTO_GEN_ID);
        pipelineAnalysis.setAnalysisName(pipeline.getName());
        pipelineAnalysis.setDescription(pipeline.getDescription());
        pipelineAnalysis.setImplementation(getImplementorDetails());
        pipelineAnalysis.setFullUsername(UserInfo.getInstance().getFullUsername());

        List<ServicePipelineStep> publishSteps = new ArrayList<ServicePipelineStep>();
        List<ServicePipelineMapping> publishMappings = new ArrayList<ServicePipelineMapping>();

        for (PipelineApp app : pipeline.getApps()) {
            // Convert the Pipeline step to a service step.
            ServicePipelineStep step = getServiceStep(app);

            if (step != null) {
                publishSteps.add(step);

                // The first step should not have any input mappings.
                if (app.getStep() > 1) {
                    List<PipelineAppMapping> appMappings = app.getMappings();

                    if (appMappings != null) {
                        // Convert the Pipeline output->input mappings to service input->output mappings.
                        String targetStepName = getStepName(app);

                        for (PipelineAppMapping mapping : appMappings) {
                            ServicePipelineMapping publishMapping = getServiceMapping(targetStepName,
                                    mapping);

                            if (publishMapping != null) {
                                publishMappings.add(publishMapping);
                            }
                        }
                    }
                }
            }
        }

        pipelineAnalysis.setSteps(publishSteps);
        pipelineAnalysis.setMappings(publishMappings);

        AutoBean<ServicePipeline> servicePipeline = serviceFactory.servicePipeline();
        servicePipeline.as().setAnalyses(Collections.singletonList(pipelineAnalysis));

        return AutoBeanCodex.encode(servicePipeline).getPayload();
    }

    private ImplementorDetails getImplementorDetails() {
        UserInfo user = UserInfo.getInstance();

        ImplementorDetailTest test = serviceFactory.implementorDetailTest().as();
        test.setParams(new ArrayList<String>());

        ImplementorDetails details = serviceFactory.implementorDetails().as();
        details.setImplementor(user.getUsername());
        details.setImplementorEmail(user.getEmail());
        details.setTest(test);

        return details;
    }

    /**
     * Gets a ServicePipelineStep representing the given PipelineApp step.
     * 
     * @return The PipelineApp as a workflow ServicePipelineStep.
     */
    private ServicePipelineStep getServiceStep(PipelineApp app) {
        ServicePipelineStep step = serviceFactory.servicePipelineStep().as();

        step.setId(AUTO_GEN_ID);
        step.setTemplateId(app.getId());
        step.setName(getStepName(app));
        step.setDescription(app.getName());
        step.setConfig(serviceFactory.servicePipelineMappingConfig().as());

        return step;
    }

    /**
     * Gets the given App's workflow step name, based on its position in the workflow and its ID.
     * 
     * @param app
     * @return the PipelineApp's step name.
     */
    public String getStepName(PipelineApp app) {
        return app == null ? "" : getStepName(app.getStep(), app.getId()); //$NON-NLS-1$
    }

    /**
     * Gets a workflow step name, based on the given workflow step position and App ID.
     * 
     * @param step A position in the workflow.
     * @param id An App ID.
     * @return A workflow step name.
     */
    public String getStepName(int step, String id) {
        return Format.substitute("step_{0}_{1}", step, id); //$NON-NLS-1$
    }

    /**
     * Formats the output->input mappings for the given source PipelineAppMapping to the targetStepName,
     * as a ServicePipelineMapping for the Import Workflow service.
     * 
     * @return A ServicePipelineMapping of input->output mappings.
     */
    private ServicePipelineMapping getServiceMapping(String targetStepName,
            PipelineAppMapping sourceStepMapping) {
        if (sourceStepMapping != null) {
            String sourceStepName = getStepName(sourceStepMapping.getStep(), sourceStepMapping.getId());

            Map<String, String> stepMap = sourceStepMapping.getMap();
            if (stepMap != null) {
                // Build the service input->output mapping.
                Map<String, String> map = new FastMap<String>();

                for (String inputId : stepMap.keySet()) {
                    String outputId = stepMap.get(inputId);

                    if (!Strings.isNullOrEmpty(outputId)) {
                        map.put(outputId, inputId);
                    }
                }

                // Ensure at least one input->output is set for sourceStepName in the service mapping.
                if (!map.keySet().isEmpty()) {
                    // Return the mappings from sourceStepName to targetStepName.
                    ServicePipelineMapping mapping = serviceFactory.servicePipelineMapping().as();

                    mapping.setSourceStep(sourceStepName);
                    mapping.setTargetStep(targetStepName);
                    mapping.setMap(map);

                    return mapping;
                }
            }
        }

        // No mappings were found in the given sourceStepMapping.
        return null;
    }
}
