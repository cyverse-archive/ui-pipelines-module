package org.iplantc.core.client.pipelines.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler interface for PipelineChangeEvent
 * 
 * @author sriram
 *
 */
public interface PipelineChangeEventHandler extends EventHandler {

    void onChange(PipelineChangeEvent event);
    
}
