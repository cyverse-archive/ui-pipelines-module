package org.iplantc.core.client.pipelines.views.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.events.PipelineChangeEvent;
import org.iplantc.core.client.pipelines.images.Resources;
import org.iplantc.core.client.pipelines.models.PipelineAppModel;
import org.iplantc.core.client.pipelines.views.dialogs.AppSelectionDialog;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.uiapplications.client.models.Analysis;
import org.iplantc.core.uiapplications.client.services.AppTemplateUserServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * 
 * @author sriram
 * 
 */
public class SelectAndOrderPanel extends PipelineStep {

    private final AbstractCatalogCategoryPanel categoryPanel;
    private final AppTemplateUserServiceFacade service;
    private Grid<PipelineAppModel> grid;
    private ToolBar toolbar;
    private final String ID_ADD = "idbtnAdd"; //$NON-NLS-1$
    private final String ID_REMOVE = "idbtnRemove"; //$NON-NLS-1$
    private final String ID_UP = "idbtnUp"; //$NON-NLS-1$
    private final String ID_DOWN = "idbtndown"; //$NON-NLS-1$
    private final FastMap<Button> buttons;

    public SelectAndOrderPanel(String title, AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateUserServiceFacade service) {
        super(title);
        this.categoryPanel = categoryPanel;
        this.service = service;
        buttons = new FastMap<Button>();
        setLayout(new FitLayout());
        setSize(500, 300);
        initGrid();
        initToolBar();
        compose();
    }

    private void initToolBar() {
        toolbar = new ToolBar();
        setTopComponent(toolbar);
        buildAndAddButtons();
    }

    private void buildAndAddButtons() {
        Button addBtn = buildButton(ID_ADD, I18N.DISPLAY.add(),
                AbstractImagePrototype.create(Resources.ICONS.add()),
                new AddButtonSelectionListener());
        addBtn.setEnabled(true);
        Button removeBtn = buildButton(ID_REMOVE, I18N.DISPLAY.remove(),
                AbstractImagePrototype.create(Resources.ICONS.remove()),
                new RemoveButtonSelectionListener());
        Button moveupBtn = buildButton(ID_UP, I18N.DISPLAY.moveUp(),
                AbstractImagePrototype.create(Resources.ICONS.up()), new MoveUpButtonSelectionListener());
        Button movedownBtn = buildButton(ID_DOWN, I18N.DISPLAY.moveDown(),
                AbstractImagePrototype.create(Resources.ICONS.down()),
                new MoveDownButtonSelectionListener());

        buttons.put(ID_ADD, addBtn);
        buttons.put(ID_REMOVE, removeBtn);
        buttons.put(ID_UP, moveupBtn);
        buttons.put(ID_DOWN, movedownBtn);

        toolbar.add(addBtn);
        toolbar.add(removeBtn);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(moveupBtn);
        toolbar.add(movedownBtn);
    }

    private class AddButtonSelectionListener extends SelectionListener<ButtonEvent> {
        AppSelectionDialog dialog;

        @Override
        public void componentSelected(ButtonEvent ce) {
            showAppSelectionDialog();
        }

        void showAppSelectionDialog() {
            // this command is executed when an app needs to be added to the grid
            Command addCmd = new Command() {
                @Override
                public void execute() {
                    Analysis app = dialog.getSelectedApp();
                    addAppModel(app, dialog);
                }

            };

            dialog = new AppSelectionDialog(categoryPanel, service, addCmd);
            dialog.updateStatusBar(getAppCount(), getLastAppName());
            dialog.show();
        }
    }

    private void firePipelineChangeEvent() {
        PipelineChangeEvent event = new PipelineChangeEvent(grid.getStore().getModels());
        EventBus.getInstance().fireEvent(event);
    }

    private void addAppModel(final Analysis app, final AppSelectionDialog dialog) {
        service.getDataObjectsForAnalysis(app.getId(), new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JSONObject obj = JSONParser.parseStrict(result).isObject();
                obj.put(PipelineAppModel.TEMPLATE_ID, obj.get(JSONMetaDataObject.ID));
                obj.put(JSONMetaDataObject.DESCRIPTION, obj.get(JSONMetaDataObject.NAME));
                obj.put(JSONMetaDataObject.NAME, new JSONString(""));

                PipelineAppModel appModel = new PipelineAppModel(obj, app);
                grid.getStore().add(appModel);
                if (dialog != null) {
                    dialog.updateStatusBar(getAppCount(), getLastAppName());
                }
                firePipelineChangeEvent();
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.dataObjectsRetrieveError(), caught);
            }

        });
    }

    private void addAppModel(final Analysis app, JSONObject stepObj) {
        PipelineAppModel appModel = new PipelineAppModel(stepObj, app);
        grid.getStore().add(appModel);
    }

    private int getAppCount() {
        return grid.getStore().getCount();
    }

    private String getLastAppName() {
        ListStore<PipelineAppModel> store = grid.getStore();
        return store.getCount() == 0 ? I18N.DISPLAY.lastAppNotDefined() : store.getAt(
                store.getCount() - 1).getName();
    }

    @SuppressWarnings("rawtypes")
    private void addStoreEventListeners() {
        final ListStore<PipelineAppModel> store = grid.getStore();
        store.addListener(Store.Add, new Listener<StoreEvent>() {
            @Override
            public void handleEvent(StoreEvent be) {
                firePipelineStepValidationEvent(store.getCount() > 1);
            }

        });

        store.addListener(Store.Remove, new Listener<StoreEvent>() {

            @Override
            public void handleEvent(StoreEvent be) {
                firePipelineStepValidationEvent(store.getCount() > 1);
            }
        });
    }

    private class RemoveButtonSelectionListener extends SelectionListener<ButtonEvent> {
        @Override
        public void componentSelected(ButtonEvent ce) {
            PipelineAppModel appModel = grid.getSelectionModel().getSelectedItem();
            if (appModel != null) {
                grid.getStore().remove(appModel);
                firePipelineChangeEvent();
            }

        }

    }

    private void refresh(List<PipelineAppModel> items) {
        ListStore<PipelineAppModel> store = grid.getStore();
        store.removeAll();
        store.add(items);
    }

    private void selectAnalaysis(PipelineAppModel a) {
        grid.getSelectionModel().select(a, false);
    }

    private class MoveUpButtonSelectionListener extends SelectionListener<ButtonEvent> {
        @Override
        public void componentSelected(ButtonEvent ce) {
            PipelineAppModel a = grid.getSelectionModel().getSelectedItem();
            ListStore<PipelineAppModel> store = grid.getStore();
            if (a != null) {
                int index = store.indexOf(a);
                List<PipelineAppModel> items = move(a, index - 1);
                refresh(items);
                firePipelineChangeEvent();
                selectAnalaysis(a);
            }
        }

    }

    private class MoveDownButtonSelectionListener extends SelectionListener<ButtonEvent> {
        @Override
        public void componentSelected(ButtonEvent ce) {
            ListStore<PipelineAppModel> store = grid.getStore();
            PipelineAppModel a = grid.getSelectionModel().getSelectedItem();
            if (a != null) {
                int index = store.indexOf(a);
                List<PipelineAppModel> items = move(a, index + 1);
                refresh(items);
                firePipelineChangeEvent();
                selectAnalaysis(a);
            }

        }

    }

    private Button buildButton(String id, String text, AbstractImagePrototype imageResource,
            SelectionListener<ButtonEvent> listener) {
        Button b = new Button(text, imageResource, listener);
        b.setEnabled(false);
        b.setId(id);
        return b;
    }

    private void initGrid() {
        grid = new Grid<PipelineAppModel>(new ListStore<PipelineAppModel>(), buildColumnModel());
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getView().setEmptyText(I18N.DISPLAY.noApps());
        grid.getView().setForceFit(true);
        grid.getSelectionModel().addListener(Events.SelectionChange, new GridSelectionChangeListener());
        addStoreEventListeners();
        add(grid);
    }

    private final class GridSelectionChangeListener implements Listener<BaseEvent> {
        @Override
        public void handleEvent(BaseEvent be) {
            Collection<Button> items = buttons.values();
            if (grid.getSelectionModel().getSelectedItems().size() > 0) {
                for (Button b : items) {
                    if (b.getId().equals(ID_UP)) {
                        b.setEnabled(!isFirst());
                    } else if (b.getId().equals(ID_DOWN)) {
                        b.setEnabled(!isLast());
                    } else {
                        b.enable();
                    }
                }

            } else {
                // never disable add button
                for (Button b : items) {
                    if (!b.getId().equals(ID_ADD)) {
                        b.disable();
                    }
                }
            }

        }
    }

    private boolean isLast() {
        PipelineAppModel appModel = grid.getSelectionModel().getSelectedItem();
        if (appModel != null) {
            ListStore<PipelineAppModel> store = grid.getStore();
            return store.indexOf(appModel) == store.getCount() - 1;
        } else {
            return false;
        }
    }

    private boolean isFirst() {
        PipelineAppModel appModel = grid.getSelectionModel().getSelectedItem();
        if (appModel != null) {
            return grid.getStore().indexOf(appModel) == 0;
        } else {
            return false;
        }
    }

    private List<PipelineAppModel> move(PipelineAppModel selectedAppModel, int new_index) {
        PipelineAppModel[] items = new PipelineAppModel[grid.getStore().getCount()];
        int i = 0;
        grid.getStore().remove(selectedAppModel);
        for (PipelineAppModel appModel : grid.getStore().getModels()) {
            if (i == new_index) {
                i = i + 1;
            }
            items[i++] = appModel;
        }
        items[new_index] = selectedAppModel;
        return Arrays.asList(items);
    }

    private ColumnModel buildColumnModel() {
        // initExpander();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig name = new ColumnConfig();
        name.setRenderer(new RenderCellWithToolTip());
        name.setId(Analysis.NAME);
        name.setHeader(I18N.DISPLAY.appName());
        name.setWidth(180);

        ColumnConfig integrator = new ColumnConfig();
        integrator.setRenderer(new IntegratorNameCellRenderer());
        integrator.setId(Analysis.INTEGRATOR_NAME);
        integrator.setHeader(I18N.DISPLAY.integratedby());
        integrator.setWidth(130);

        ColumnConfig date = new ColumnConfig();
        date.setId(Analysis.INTEGRATION_DATE);
        date.setHeader(I18N.DISPLAY.publishedOn());
        date.setWidth(130);
        date.setDateTimeFormat(DateTimeFormat
                .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM));

        configs.add(name);
        configs.add(integrator);
        configs.add(date);

        return new ColumnModel(configs);
    }

    private void compose() {
        add(grid);
    }

    public List<PipelineAppModel> getApps() {
        return grid.getStore().getModels();
    }

    @Override
    public boolean isValid() {
        return grid.getStore().getCount() > 1;
    }

    @Override
    public JSONValue toJson() {
        JSONArray arr = new JSONArray();
        int i = 0;
        for (PipelineAppModel model : grid.getStore().getModels()) {
            arr.set(i++, model.stepToJson());
        }
        return arr;
    }

    private class IntegratorNameCellRenderer implements GridCellRenderer<PipelineAppModel> {

        @Override
        public Object render(PipelineAppModel model, String property, ColumnData config, int rowIndex,
                int colIndex, ListStore<PipelineAppModel> store, Grid<PipelineAppModel> grid) {
            return model.getApp().getIntegratorsName();
        }

    }

    private class RenderCellWithToolTip implements GridCellRenderer<PipelineAppModel> {
        @Override
        public Object render(PipelineAppModel model, String property, ColumnData config, int rowIndex,
                int colIndex, ListStore<PipelineAppModel> store, Grid<PipelineAppModel> grid) {
            String html = ""; //$NON-NLS-1$
            Analysis app = model.getApp();
            if (app.isUser_favourite()) {
                html = "<img src='./images/fav.png'></img> &nbsp;"; //$NON-NLS-1$
            }

            if (app.getDescription() != null && !app.getDescription().isEmpty()
                    && !app.getDescription().equalsIgnoreCase("none")) { //$NON-NLS-1$
                return html + "<span qtip='" + app.getDescription() + "'>" + app.getName() + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return html + "<span qtip='" + app.getName() + "'>" + app.getName() + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

        }

    }

    @Override
    protected void setData(JSONObject obj) {
        if (obj != null) {
            JSONArray arr = obj.get("steps").isArray();
            for (int i = 0;i < arr.size(); i ++) {
                JSONObject stepObj = arr.get(i).isObject();
                JSONObject appObj = JsonUtil.getObject(stepObj, "Analysis");
                Analysis app = new Analysis(appObj);
                addAppModel(app, stepObj);
            }
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    firePipelineChangeEvent();
                    
                }
            });
        }

    }

}
