package org.iplantc.core.client.pipelines.events;

import com.google.gwt.event.shared.EventHandler;

public interface PipelineStepValidationEventHandler extends EventHandler {

    void onValidate(PipelineStepValidationEvent event);

}
