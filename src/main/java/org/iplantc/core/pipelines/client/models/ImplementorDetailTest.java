package org.iplantc.core.pipelines.client.models;

import java.util.List;

/**
 * An AutoBean interface for service Pipeline implementor test parameters.
 * 
 * @author psarando
 * 
 */
public interface ImplementorDetailTest {

    public List<String> getParams();

    public void setParams(List<String> params);
}
