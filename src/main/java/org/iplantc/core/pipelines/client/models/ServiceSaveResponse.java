package org.iplantc.core.pipelines.client.models;

import java.util.List;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

/**
 * An AutoBean interface for a Pipeline save service response.
 * 
 * @author psarando
 * 
 */
public interface ServiceSaveResponse {

    /**
     * @return The list of IDs of Pipelines that were created or updated in the service call.
     */
    @PropertyName("analyses")
    public List<String> getWorkflowIds();
}
