package org.iplantc.core.client.pipelines.gxt3.dnd;

import java.util.List;

import org.iplant.pipeline.client.builder.PipelineCreator;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.gxt3.util.PipelineAutoBeanUtil;
import org.iplantc.core.uiapplications.client.Services;
import org.iplantc.core.uiapplications.client.models.autobeans.App;
import org.iplantc.core.uicommons.client.ErrorHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;
import com.sencha.gxt.dnd.core.client.DndDragEnterEvent;
import com.sencha.gxt.dnd.core.client.DndDragEnterEvent.DndDragEnterHandler;
import com.sencha.gxt.dnd.core.client.DndDragLeaveEvent;
import com.sencha.gxt.dnd.core.client.DndDragLeaveEvent.DndDragLeaveHandler;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
import com.sencha.gxt.dnd.core.client.StatusProxy;
import com.sencha.gxt.widget.core.client.container.Container;

/**
 * A PipelineBuilderDNDHandler for dropping Apps onto the PipelineCreator panel.
 * 
 * @author psarando
 * 
 */
public class PipelineBuilderDropHandler extends PipelineBuilderDNDHandler implements
        DndDragEnterHandler, DndDragLeaveHandler, DndDropHandler {
    private final PipelineCreator creator;

    public PipelineBuilderDropHandler(Container builderPanel, PipelineCreator creator) {
        super(builderPanel);

        this.creator = creator;
    }

    @Override
    public void onDragEnter(DndDragEnterEvent event) {
        StatusProxy eventStatus = event.getStatusProxy();
        @SuppressWarnings("unchecked")
        List<App> selected = (List<App>)event.getDragSource().getData();
        validateDNDEvent(eventStatus, selected);

        if (eventStatus.getStatus()) {
            // Event Status is true, so we have a valid App
            String appName = selected.get(0).getName();
            maskPipelineBuilder(I18N.DISPLAY.appendAppToWorkflow(appName));
        }
    }

    @Override
    public void onDragLeave(DndDragLeaveEvent event) {
        unmaskPipelineBuilder();
    }

    @Override
    public void onDrop(DndDropEvent event) {
        @SuppressWarnings("unchecked")
        List<App> data = (List<App>)event.getData();

        if (data == null || data.isEmpty()) {
            return;
        }

        for (final App app : data) {
            if (app.getPipelineEligibility().isValid()) {
                Services.USER_APP_SERVICE.getDataObjectsForApp(app.getId(), new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        AutoBean<App> appBean = AutoBeanUtils.getAutoBean(app);

                        // Clone the App AutoBean so we don't modify the original.
                        Splittable appJson = AutoBeanCodex.encode(appBean);
                        appBean = AutoBeanCodex.decode(appBean.getFactory(), App.class,
                                appJson.getPayload());

                        Splittable json = StringQuoter.split(result);
                        AutoBeanCodex.decodeInto(json, appBean);

                        creator.appendApp(PipelineAutoBeanUtil.appToPipelineApp(appBean));
                        unmaskPipelineBuilder();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        ErrorHandler.post(I18N.ERROR.dataObjectsRetrieveError(), caught);
                        unmaskPipelineBuilder();
                    }
                });
            }
        }
    }
}
