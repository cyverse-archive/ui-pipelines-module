package org.iplantc.core.client.pipelines.gxt3.views;

import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
    private final Driver driver = GWT.create(Driver.class);
    private final Widget widget;

    interface PipelineInfoEditorUiBinder extends UiBinder<Widget, PipelineInfoEditor> {
    }

    interface Driver extends SimpleBeanEditorDriver<Pipeline, PipelineInfoEditor> {
    }

    @UiField
    TextField name;

    @UiField
    TextArea description;

    public PipelineInfoEditor() {
        widget = uiBinder.createAndBindUi(this);
        driver.initialize(this);
    }

    @UiHandler("name")
    public void nameChanged(ValueChangeEvent<String> event) {
        driver.flush();
    }

    @UiHandler("description")
    public void descriptionChanged(ValueChangeEvent<String> event) {
        driver.flush();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public Pipeline getPipeline() {
        return driver.flush();
    }

    public void setPipeline(Pipeline pipeline) {
        driver.edit(pipeline);
    }

    public boolean isValid() {
        driver.flush();

        return !driver.hasErrors();
    }

}
