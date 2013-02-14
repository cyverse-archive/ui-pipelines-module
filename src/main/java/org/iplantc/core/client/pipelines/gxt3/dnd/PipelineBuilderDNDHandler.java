package org.iplantc.core.client.pipelines.gxt3.dnd;

import java.util.List;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.uiapplications.client.models.autobeans.App;

import com.sencha.gxt.dnd.core.client.DndDragCancelEvent;
import com.sencha.gxt.dnd.core.client.DndDragCancelEvent.DndDragCancelHandler;
import com.sencha.gxt.dnd.core.client.StatusProxy;
import com.sencha.gxt.widget.core.client.container.Container;

/**
 * A common Handler for drag'n'drop events from the Apps grid to the PipelineBuilder.
 * 
 * @author psarando
 * 
 */
public abstract class PipelineBuilderDNDHandler implements DndDragCancelHandler {
    private final Container builderPanel;

    public PipelineBuilderDNDHandler(Container builderPanel) {
        this.builderPanel = builderPanel;
    }

    protected void maskPipelineBuilder(String message) {
        if (builderPanel != null) {
            builderPanel.mask(message);
        }
    }

    protected void unmaskPipelineBuilder() {
        if (builderPanel != null) {
            builderPanel.unmask();
        }
    }

    protected void validateDNDEvent(StatusProxy eventStatus, List<App> selected) {
        if (eventStatus == null) {
            return;
        }

        if (selected == null || selected.isEmpty()) {
            eventStatus.setStatus(false);
            return;
        }

        for (App app : selected) {
            if (!app.getPipelineEligibility().isValid()) {
                eventStatus.setStatus(false);
                eventStatus.update(app.getPipelineEligibility().getReason());
                return;
            }

            eventStatus.update(I18N.DISPLAY.appendAppToWorkflow(app.getName()));
        }
    }

    @Override
    public void onDragCancel(DndDragCancelEvent event) {
        unmaskPipelineBuilder();
    }
}
