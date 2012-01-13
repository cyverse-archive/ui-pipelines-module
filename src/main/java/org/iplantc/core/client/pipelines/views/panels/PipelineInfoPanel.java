package org.iplantc.core.client.pipelines.views.panels;

import org.iplantc.core.client.pipelines.I18N;
import org.iplantc.core.jsonutil.JsonUtil;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * 
 * @author sriram
 * 
 */
public class PipelineInfoPanel extends PipelineStep {

    private static final String AUTO_GEN = "auto-gen"; //$NON-NLS-1$
    private FormPanel panel;
    private TextField<String> txtPipelineName;
    private TextArea txtPipelineDesc;

    private FormData formData;

    public PipelineInfoPanel(String title) {
        super(title);
        initForm();
        compose();
    }

    private void initForm() {
        panel = new FormPanel();
        panel.setHeaderVisible(false);
        formData = new FormData("-20"); //$NON-NLS-1$
        panel.setLabelAlign(LabelAlign.TOP);
        panel.setBodyBorder(false);
        initPipelineNameField();
        initPipelineDescField();
    }

    private void compose() {
        panel.add(txtPipelineName, formData);
        panel.add(txtPipelineDesc, formData);
        add(panel);
    }

    private void initPipelineNameField() {
        txtPipelineName = new TextField<String>();
        txtPipelineName.setAutoValidate(true);
        txtPipelineName.setFieldLabel(I18N.DISPLAY.pipelineName());
        txtPipelineName.setAllowBlank(false);
    }

    private void initPipelineDescField() {
        txtPipelineDesc = new TextArea();
        txtPipelineDesc.setAutoValidate(true);
        txtPipelineDesc.setFieldLabel(I18N.DISPLAY.pipelineDescription());
        txtPipelineDesc.setHeight(100);
        txtPipelineDesc.setAllowBlank(false);
    }

    @Override
    public boolean isValid() {
        return panel.isValid();
    }

    @Override
    public JSONValue toJson() {
        JSONObject obj = new JSONObject();
        obj.put("id", new JSONString(AUTO_GEN)); //$NON-NLS-1$
        obj.put("analysis_name", //$NON-NLS-1$
                new JSONString(
                        JsonUtil.formatString(txtPipelineName.getValue() != null ? txtPipelineName
                                .getValue() : ""))); //$NON-NLS-1$
        obj.put("description", //$NON-NLS-1$
                new JSONString(
                        JsonUtil.formatString(txtPipelineDesc.getValue() != null ? txtPipelineDesc
                                .getValue() : ""))); //$NON-NLS-1$
        return obj;
    }

}
