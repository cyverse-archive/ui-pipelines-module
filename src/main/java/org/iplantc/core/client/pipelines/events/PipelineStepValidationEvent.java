package org.iplantc.core.client.pipelines.events;

import com.google.gwt.event.shared.GwtEvent;

public class PipelineStepValidationEvent extends GwtEvent<PipelineStepValidationEventHandler> {

    /**
     * Defines the GWT Event Type.
     * 
     * @see org.iplantc.de.client.events.PipelineStepValidationEventHandler
     */
    public static final GwtEvent.Type<PipelineStepValidationEventHandler> TYPE = new GwtEvent.Type<PipelineStepValidationEventHandler>();

    private boolean valid;

    public PipelineStepValidationEvent(boolean valid) {
        this.valid = valid;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<PipelineStepValidationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PipelineStepValidationEventHandler handler) {
        handler.onValidate(this);

    }

    /**
     * @param valid the valid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

}
