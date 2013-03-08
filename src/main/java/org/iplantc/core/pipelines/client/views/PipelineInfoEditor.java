package org.iplantc.core.pipelines.client.views;

import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 * An Editor for the Pipeline name and description fields.
 * 
 * @author psarando
 * 
 */
public class PipelineInfoEditor implements IsWidget, Editor<Pipeline> {

    private static PipelineInfoEditorUiBinder uiBinder = GWT.create(PipelineInfoEditorUiBinder.class);
    private final Widget widget;

    interface PipelineInfoEditorUiBinder extends UiBinder<Widget, PipelineInfoEditor> {
    }

    @UiField
    TextField name;

    @UiField
    TextArea description;

    public PipelineInfoEditor() {
        widget = uiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public void clearInvalid() {
        name.clearInvalid();
        description.clearInvalid();
    }
}
