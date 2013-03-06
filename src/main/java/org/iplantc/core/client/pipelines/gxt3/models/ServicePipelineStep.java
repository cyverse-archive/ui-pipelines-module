package org.iplantc.core.client.pipelines.gxt3.models;

import org.iplantc.core.uicommons.client.models.HasDescription;
import org.iplantc.core.uicommons.client.models.HasId;

import com.google.gwt.user.client.ui.HasName;
import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

/**
 * An AutoBean interface for a service Pipeline step.
 * 
 * @author psarando
 * 
 */
public interface ServicePipelineStep extends HasId, HasName, HasDescription {

    public void setId(String id);

    @PropertyName("template_id")
    public String getTemplateId();

    @PropertyName("template_id")
    public void setTemplateId(String template_id);

    public ServicePipelineStepConfig getConfig();

    public void setConfig(ServicePipelineStepConfig config);
}
