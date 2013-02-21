package org.iplantc.core.client.pipelines.gxt3.views;

import java.util.ArrayList;
import java.util.List;

import org.iplant.pipeline.client.json.autobeans.PipelineApp;
import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.client.pipelines.gxt3.models.PipelineAppProperties;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

/**
 * A ColumnModel for the "Select & Order Apps" grid.
 * 
 * @author psarando
 * 
 */
public class AppColumnModel extends ColumnModel<PipelineApp> {

    public AppColumnModel(PipelineAppProperties props) {
        super(createColumnConfigList(props));
    }

    public static List<ColumnConfig<PipelineApp, ?>> createColumnConfigList(PipelineAppProperties props) {
        ColumnConfig<PipelineApp, Integer> step = new ColumnConfig<PipelineApp, Integer>(props.step(),
                50, I18N.DISPLAY.step());
        step.setFixed(true);
        step.setHideable(false);
        step.setSortable(false);
        step.setMenuDisabled(true);

        // steps start at 0.
        step.setCell(new AbstractCell<Integer>() {

            @Override
            public void render(Context context, Integer value, SafeHtmlBuilder sb) {
                sb.append(value + 1);
            }

        });

        ColumnConfig<PipelineApp, String> name = new ColumnConfig<PipelineApp, String>(props.name(),
                180, I18N.DISPLAY.name());
        name.setSortable(false);

        ColumnConfig<PipelineApp, String> description = new ColumnConfig<PipelineApp, String>(
                props.description(), 180, I18N.DISPLAY.description());
        description.setSortable(false);

        List<ColumnConfig<PipelineApp, ?>> list = new ArrayList<ColumnConfig<PipelineApp, ?>>();
        list.add(step);
        list.add(name);
        list.add(description);

        return list;
    }
}
