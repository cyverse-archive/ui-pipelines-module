package org.iplantc.de.pipelines.client.models;

import java.util.List;

import org.iplantc.de.apps.client.models.autobeans.DataObject;
import org.iplantc.de.commons.client.models.HasDescription;
import org.iplantc.de.commons.client.models.HasId;

import com.google.gwt.user.client.ui.HasName;

/**
 * An AutoBean interface for a service Template provided in service Pipeline JSON.
 * 
 * @author psarando
 * 
 */
public interface ServicePipelineTemplate extends HasId, HasName, HasDescription {

    public List<DataObject> getInputs();

    public void setInputs(List<DataObject> inputs);

    public List<DataObject> getOutputs();

    public void setOutputs(List<DataObject> outputs);
}
