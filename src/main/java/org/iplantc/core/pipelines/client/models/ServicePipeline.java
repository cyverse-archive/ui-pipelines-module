package org.iplantc.core.pipelines.client.models;

import java.util.List;

/**
 * An AutoBean interface for a service Pipeline.
 * 
 * @author psarando
 * 
 */
public interface ServicePipeline {

    public List<ServicePipelineAnalysis> getAnalyses();

    public void setAnalyses(List<ServicePipelineAnalysis> analyses);
}
