package org.iplantc.core.client.pipelines.gxt3.util;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.json.autobeans.PipelineApp;
import org.iplant.pipeline.client.json.autobeans.PipelineAppData;
import org.iplant.pipeline.client.json.autobeans.PipelineAutoBeanFactory;
import org.iplantc.core.uiapplications.client.models.autobeans.App;
import org.iplantc.core.uiapplications.client.models.autobeans.AppDataObject;
import org.iplantc.core.uiapplications.client.models.autobeans.DataObject;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * A Utility class for the Pipeline AutoBeans.
 * 
 * @author psarando
 * 
 */
public class PipelineAutoBeanUtil {

    private static PipelineAutoBeanFactory factory = GWT.create(PipelineAutoBeanFactory.class);

    /**
     * @return A singleton instance of the PipelineAutoBeanFactory.
     */
    public static PipelineAutoBeanFactory getPipelineAutoBeanFactory() {
        return factory;
    }

    /**
     * Converts an App AutoBean into a PipelineApp.
     * 
     * @param appBean
     * @return A PipelineApp cloned from the data contained in the given appBean.
     */
    public static PipelineApp appToPipelineApp(AutoBean<App> appBean) {
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
}
