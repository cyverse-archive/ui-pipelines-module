package org.iplantc.core.client.pipelines.events;

import java.util.List;

import org.iplantc.core.client.pipelines.models.PipelineAppModel;

import com.google.gwt.event.shared.GwtEvent;

/**
 * An event that is fired when pipeline apps change. Change can be just an order change or new apps added
 * or existing apps being removed
 * 
 * @author sriram
 * 
 */

public class PipelineChangeEvent extends GwtEvent<PipelineChangeEventHandler> {

    /**
     * Defines the GWT Event Type.
     * 
     * @see org.iplantc.core.uiapplications.client.events.handlers.AppGroupSelectedEventHandler.AnalysisCategorySelectedEventHandler
     */
    public static final GwtEvent.Type<PipelineChangeEventHandler> TYPE = new GwtEvent.Type<PipelineChangeEventHandler>();

    private List<PipelineAppModel> appModels;

    public PipelineChangeEvent(List<PipelineAppModel> appModels) {
        this.appModels = appModels;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<PipelineChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PipelineChangeEventHandler handler) {
        handler.onChange(this);

    }

    /**
     * @return the appModels
     */
    public List<PipelineAppModel> getAppModels() {
        return appModels;
    }

}
