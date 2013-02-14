package org.iplantc.core.client.pipelines.gxt3.dnd;

import java.util.List;

import org.iplantc.core.uiapplications.client.models.autobeans.App;

import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent.DndDragStartHandler;
import com.sencha.gxt.dnd.core.client.StatusProxy;
import com.sencha.gxt.widget.core.client.container.Container;

/**
 * A PipelineBuilderDNDHandler for dragging Apps from the Apps Grid.
 * 
 * @author psarando
 * 
 */
public class AppsGridDragHandler extends PipelineBuilderDNDHandler implements DndDragStartHandler {

    public AppsGridDragHandler(Container builderPanel) {
        super(builderPanel);
    }

    @Override
    public void onDragStart(DndDragStartEvent event) {
        @SuppressWarnings("unchecked")
        List<App> selected = (List<App>)event.getData();
        StatusProxy eventStatus = event.getStatusProxy();

        validateDNDEvent(eventStatus, selected);
    }
}
