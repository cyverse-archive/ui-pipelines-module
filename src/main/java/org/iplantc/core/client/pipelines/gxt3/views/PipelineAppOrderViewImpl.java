package org.iplantc.core.client.pipelines.gxt3.views;

import java.util.List;

import org.iplantc.core.client.pipelines.gxt3.models.PipelineAppProperties;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * An implementation of the PipelineAppOrderView.
 * 
 * @author psarando
 * 
 */
public class PipelineAppOrderViewImpl implements PipelineAppOrderView {

    @UiTemplate("PipelineAppOrderView.ui.xml")
    interface PipelineAppOrderUiBinder extends UiBinder<Widget, PipelineAppOrderViewImpl> {
    }

    private static PipelineAppOrderUiBinder uiBinder = GWT.create(PipelineAppOrderUiBinder.class);
    private static PipelineAppProperties pipelineAppProps = GWT.create(PipelineAppProperties.class);
    private final Widget widget;
    private Presenter presenter;

    public PipelineAppOrderViewImpl() {
        widget = uiBinder.createAndBindUi(this);

        appOrderGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @UiField
    Grid<PipelineApp> appOrderGrid;

    @UiField
    ListStore<PipelineApp> pipelineAppStore;

    @UiFactory
    ListStore<PipelineApp> createListStore() {
        ListStore<PipelineApp> store = new ListStore<PipelineApp>(new ModelKeyProvider<PipelineApp>() {

            @Override
            public String getKey(PipelineApp item) {
                return presenter.getStepName(item);
            }
        });

        store.addSortInfo(new StoreSortInfo<PipelineApp>(pipelineAppProps.step(), SortDir.ASC));

        return store;
    }

    @UiFactory
    ColumnModel<PipelineApp> createColumnModel() {
        return new AppColumnModel(pipelineAppProps);
    }

    @UiHandler("addAppsBtn")
    public void onAddAppsClick(SelectEvent e) {
        presenter.onAddAppsClicked();
    }

    @UiHandler("removeAppBtn")
    public void onRemoveAppClick(SelectEvent e) {
        presenter.onRemoveAppClicked();
    }

    @UiHandler("moveUpBtn")
    public void onMoveUpClick(SelectEvent e) {
        presenter.onMoveUpClicked();
    }

    @UiHandler("moveDownBtn")
    public void onMoveDownClick(SelectEvent e) {
        presenter.onMoveDownClicked();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPipeline(Pipeline pipeline) {
        pipelineAppStore.clear();

        List<PipelineApp> apps = pipeline.getApps();
        if (apps != null) {
            pipelineAppStore.addAll(apps);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        // A valid Pipeline has at least 2 Apps.
        return pipelineAppStore.size() > 1;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ListStore<PipelineApp> getPipelineAppStore() {
        return pipelineAppStore;
    }

    @Override
    public PipelineApp getOrderGridSelectedApp() {
        return appOrderGrid.getSelectionModel().getSelectedItem();
    }
}
