package org.iplantc.core.client.pipelines.models;

import java.util.List;

import org.iplantc.core.metadata.client.property.PropertyData;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * 
 * A map of property data and the app to which the property data belongs to
 * 
 * @author sriram
 * 
 */

public class PipelineMapping extends BaseModelData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static String PIPELINE_APP = "app";
    public static String PIPELINE_PROPERTY_DATA = "propertydata";

    public PipelineMapping(PipelineAppModel app, List<PropertyData> data) {
        set(PIPELINE_APP, app);
        set(PIPELINE_PROPERTY_DATA, data);
    }

    @SuppressWarnings("unchecked")
    public List<PropertyData> getPropertyDataList() {
        return (List<PropertyData>)get(PIPELINE_PROPERTY_DATA);
    }

    public PipelineAppModel getAppModel() {
        return ((PipelineAppModel)(get(PIPELINE_APP)));
    }

}
