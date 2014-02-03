package org.iplantc.de.pipelines.client.models;

import java.util.Map;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

/**
 * An AutoBean interface for a service Pipeline step input to output mapping.
 * 
 * @author psarando
 * 
 */
public interface ServicePipelineMapping {

    @PropertyName("source_step")
    public String getSourceStep();

    @PropertyName("source_step")
    public void setSourceStep(String source_step);

    @PropertyName("target_step")
    public String getTargetStep();

    @PropertyName("target_step")
    public void setTargetStep(String target_step);

    public Map<String, String> getMap();

    public void setMap(Map<String, String> map);
}
