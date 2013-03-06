package org.iplantc.core.client.pipelines.gxt3.models;

import java.util.List;

import org.iplantc.core.uicommons.client.models.HasDescription;
import org.iplantc.core.uicommons.client.models.HasId;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

/**
 * An AutoBean interface for a service Pipeline analysis.
 * 
 * @author psarando
 * 
 */
public interface ServicePipelineAnalysis extends HasId, HasDescription {

    public void setId(String id);

    @PropertyName("analysis_name")
    public String getAnalysisName();

    @PropertyName("analysis_name")
    public void setAnalysisName(String name);

    public ImplementorDetails getImplementation();

    public void setImplementation(ImplementorDetails implementation);

    @PropertyName("full_username")
    public String getFullUsername();

    @PropertyName("full_username")
    public void setFullUsername(String full_username);

    public List<ServicePipelineStep> getSteps();

    public void setSteps(List<ServicePipelineStep> publishSteps);

    public List<ServicePipelineMapping> getMappings();

    public void setMappings(List<ServicePipelineMapping> publishMappings);
}
