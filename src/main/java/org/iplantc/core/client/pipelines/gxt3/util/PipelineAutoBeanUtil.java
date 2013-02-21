package org.iplantc.core.client.pipelines.gxt3.util;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.json.autobeans.PipelineApp;
import org.iplant.pipeline.client.json.autobeans.PipelineAppData;
import org.iplant.pipeline.client.json.autobeans.PipelineAutoBeanFactory;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.uiapplications.client.Services;
import org.iplantc.core.uiapplications.client.models.autobeans.App;
import org.iplantc.core.uiapplications.client.models.autobeans.AppDataObject;
import org.iplantc.core.uiapplications.client.models.autobeans.DataObject;
import org.iplantc.core.uicommons.client.ErrorHandler;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

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
     * Clones the data contained in the given App into a PipelineApp, calling the App service to fetch
     * the input and output data objects for inclusion in the PipelineApp result.
     * 
     * @param app Must be eligible for pipelines, otherwise the callback's onFailure method is called.
     * @param callback Receives the PipelineApp result on success, cloned from the data contained in the
     *            given app plus the data objects returned from the App service.
     */
    public static void appToPipelineApp(final App app, final AsyncCallback<PipelineApp> callback) {
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
    public static PipelineApp appBeanToPipelineApp(AutoBean<App> appBean) {
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
