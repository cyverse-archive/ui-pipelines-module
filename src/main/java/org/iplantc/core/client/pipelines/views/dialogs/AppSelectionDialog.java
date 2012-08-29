package org.iplantc.core.client.pipelines.views.dialogs;

import java.util.ArrayList;

import org.iplantc.core.client.pipelines.Constants;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.uiapplications.client.events.AnalysisCategorySelectedEvent;
import org.iplantc.core.uiapplications.client.events.AnalysisCategorySelectedEventHandler;
import org.iplantc.core.uiapplications.client.models.Analysis;
import org.iplantc.core.uiapplications.client.models.AnalysisGroup;
import org.iplantc.core.uiapplications.client.services.AppTemplateServiceFacade;
import org.iplantc.core.uiapplications.client.views.panels.AbstractCatalogCategoryPanel;
import org.iplantc.core.uiapplications.client.views.panels.BaseCatalogMainPanel;
import org.iplantc.core.uiapplications.client.events.AppSearchResultSelectedEvent;
import org.iplantc.core.uiapplications.client.events.AppSearchResultSelectedEventHandler;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.de.client.DeCommonI18N;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Lets the user choose an app and click "add" to add it to some other UI component. The "add" button
 * leaves the dialog open, so multiple apps can be added in one session by using the "add" button
 * repeatedly.
 * 
 * @author hariolf
 * 
 */
public class AppSelectionDialog extends Dialog {
    private static final String ADD = YES; // button id for the add button

    private final AbstractCatalogCategoryPanel categoryPanel;
    private BaseCatalogMainPanel appsListPanel;
    private ArrayList<HandlerRegistration> handlers;
    private final AppTemplateServiceFacade service;
    private ContentPanel mainPanel;
    private BorderLayoutData dataWest;
    private BorderLayoutData dataCenter;
    private Status countLabel;
    private Status lastAppLabel;
    private final Command cmdAdd;

    /**
     * 
     * @param categoryPanel
     * @param service
     * @param cmdAdd called when the add button is clicked
     */
    public AppSelectionDialog(AbstractCatalogCategoryPanel categoryPanel,
            AppTemplateServiceFacade service, Command cmdAdd) {
        this.categoryPanel = categoryPanel;
        this.service = service;
        this.cmdAdd = cmdAdd;
        init();
    }

    private void init() {
        addBeforeShowListener();

        setHeading(I18N.DISPLAY.selectWindowTitle());
        setSize(800, 400);
        setModal(true);

        setButtons(ADD + CLOSE);
        // make the yes button into an add button
        getButtonById(ADD).setText(DeCommonI18N.DISPLAY.add());

        initHandlers();

        initMainPanel();
        initLayout();
        initAppListPanel();

        countLabel = new Status();
        countLabel.setBox(true);
        lastAppLabel = new Status();
        lastAppLabel.setBox(true);

        updateAddButton();

        compose();

        addAfterHideListener();

    }

    private void initAppListPanel() {
        appsListPanel = new BaseCatalogMainPanel(Constants.CLIENT.tagAppSelectDialog(), service);
        appsListPanel.setSize(400, 400);
        appsListPanel.addGridSelectionChangeListener(buildGridChangeListener());
    }

    private void initMainPanel() {
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);
        mainPanel.setSize(700, 400);
    }

    private void addAfterHideListener() {
        addListener(Events.Hide, new Listener<WindowEvent>() {
            @Override
            public void handleEvent(WindowEvent be) {
                categoryPanel.cleanup();
                appsListPanel.cleanup();

                for (HandlerRegistration handler : handlers) {
                    handler.removeHandler();
                }
            }
        });
    }

    private void addBeforeShowListener() {
        addListener(Events.BeforeShow, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                categoryPanel.selectDefault();
            }
        });
    }

    private Listener<BaseEvent> buildGridChangeListener() {
        return new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                updateAddButton();
            }
        };
    }

    /**
     * Enables or disables the add button depending on the current selection
     */
    private void updateAddButton() {
        getButtonById(ADD).setEnabled(appsListPanel.getSelectedApp() != null);
    }

    private BorderLayoutData initLayoutRegion(LayoutRegion region, float size, boolean collapsible) {
        BorderLayoutData ret = new BorderLayoutData(region);

        if (size > 0) {
            ret.setSize(size);
        }

        ret.setCollapsible(collapsible);
        ret.setSplit(true);

        return ret;
    }

    private void initLayout() {
        BorderLayout layout = new BorderLayout();

        // make sure we re-draw when a panel expands
        layout.addListener(Events.Expand, new Listener<BorderLayoutEvent>() {
            @Override
            public void handleEvent(BorderLayoutEvent be) {
                layout();
            }
        });

        mainPanel.setLayout(layout);

        dataWest = initLayoutRegion(LayoutRegion.WEST, getWestWidth(), true);
        dataCenter = initLayoutRegion(LayoutRegion.CENTER, 400, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        add(mainPanel);
        compose();
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        resizeContents(getInnerWidth(), getInnerHeight());
    }

    @Override
    protected void onAfterLayout() {
        super.onAfterLayout();

        resizeContents(getInnerWidth(), getInnerHeight());
    }

    /**
     * Resizes this panel's inner tree panel.
     * 
     * @param width
     * @param height
     */
    private void resizeContents(int width, int height) {
        if (mainPanel != null) {
            mainPanel.setWidth(width);
            mainPanel.setHeight(height);
        }
    }

    private void compose() {
        mainPanel.add(categoryPanel, dataWest);
        mainPanel.add(appsListPanel, dataCenter);
        add(mainPanel);

        ToolBar statusBar = new ToolBar();
        statusBar.add(countLabel);
        statusBar.add(lastAppLabel);
        ButtonBar buttonBar = getButtonBar();
        buttonBar.insert(countLabel, 0);
        buttonBar.insert(lastAppLabel, 1);
        buttonBar.insert(new FillToolItem(), 2);
    }

    protected int getWestWidth() {
        return 220;
    }

    private void initHandlers() {
        EventBus eventbus = EventBus.getInstance();
        handlers = new ArrayList<HandlerRegistration>();

        handlers.add(eventbus.addHandler(AnalysisCategorySelectedEvent.TYPE,
                new AnalysisCategorySelectedEventHandlerImpl()));
        getButtonById(ADD).addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Analysis app = appsListPanel.getSelectedApp();
                if (app != null) {
                    cmdAdd.execute();
                }
            }
        });

        handlers.add(eventbus.addHandler(AppSearchResultSelectedEvent.TYPE,
                new AppSearchResultSelectedEventHandler() {
            @Override
                    public void onSelection(AppSearchResultSelectedEvent event) {
                if (Constants.CLIENT.tagAppSelectDialog().equals(event.getSourceTag())) {
                    categoryPanel.selectCategory(event.getCategoryId());
                    appsListPanel.selectTool(event.getAppId());
                }
            }
        }));
    }

    /**
     * Updates the app count and last app added.
     * 
     * @param appCount number of apps
     * @param lastAppName name of the app that was added last, or null
     */
    public void updateStatusBar(int appCount, String lastAppName) {
        countLabel.setText(appCount == 1 ? I18N.DISPLAY
                .appCountSingular() : I18N.DISPLAY
                .appCountPlural(appCount));
        lastAppLabel.setText(I18N.DISPLAY
                .lastApp(lastAppName == null ? I18N.DISPLAY
                        .lastAppNotDefined() : lastAppName));
    }

    /**
     * Returns the selected app or null if nothing selected
     * 
     * @return the selected app
     */
    public Analysis getSelectedApp() {
        return appsListPanel.getSelectedApp();
    }

    private class AnalysisCategorySelectedEventHandlerImpl implements
            AnalysisCategorySelectedEventHandler {
        @Override
        public void onSelection(AnalysisCategorySelectedEvent event) {
            if (event.getSourcePanel() == categoryPanel && appsListPanel != null) {
                appsListPanel.setHeading(event.getGroup().getName());
                updateAnalysesListing(event.getGroup());
            }
        }
    }

    private void updateAnalysesListing(final AnalysisGroup group) {
        appsListPanel.mask(DeCommonI18N.DISPLAY.loadingMask());
        service.getAnalysis(group.getId(), new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ArrayList<Analysis> analyses = new ArrayList<Analysis>();

                JSONArray templates = JsonUtil.getArray(JsonUtil.getObject(result), "templates"); //$NON-NLS-1$
                if (templates != null) {
                    for (int i = 0; i < templates.size(); i++) {
                        Analysis analysis = new Analysis(JsonUtil.getObjectAt(templates, i));
                        analyses.add(analysis);
                    }
                }

                appsListPanel.seed(analyses, group);
                appsListPanel.unmask();
                // updateAddButton();
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
                appsListPanel.unmask();
            }
        });

    }
}
