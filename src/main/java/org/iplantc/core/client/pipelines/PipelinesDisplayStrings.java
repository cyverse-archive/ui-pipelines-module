package org.iplantc.core.client.pipelines;

import org.iplantc.core.uicommons.client.CommonUIDisplayStrings;

/** Display strings for the Pipelines module */
public interface PipelinesDisplayStrings extends CommonUIDisplayStrings {

    /**
     * pipeline name field caption
     * 
     * @return a String representing pipeline name caption
     */
    String pipelineName();

    /**
     * pipeline desc field caption
     * 
     * @return a String representing pipeline desc field caption
     */
    String pipelineDescription();

    /**
     * 
     * @return
     */
    String appName();

    /**
     * 
     * @return
     */
    String noApps();

    /**
     * Window title of the "select apps" window
     * 
     * @return a string representing the add button label
     */
    String selectWindowTitle();

    /**
     * Remove button label
     * 
     * @return a string representing the remove button label
     */
    String remove();

    /**
     * Up button label
     * 
     * @return a string representing the up button label
     */
    String up();

    /**
     * Dowm button label
     * 
     * @return a string representing the down button label
     */
    String down();

    /**
     * Text for the app count label when count!=1
     * 
     * @param count the number of apps
     * @return a string representing the label
     */
    String appCountPlural(int count);

    /**
     * Text for the app count label when count==1
     * 
     * @return a string representing the label
     */
    String appCountSingular();

    /**
     * Text for a label showing the last app that was added to a pipeline
     * 
     * @param appName name of the app
     * @return a string representing the label
     */
    String lastApp(String appName);

    /**
     * Text for the status label to show when no app has been added yet
     * 
     * @return a string representing the label
     */
    String lastAppNotDefined();

    /**
     * Text for a "Map Outputs to Inputs" label.
     * 
     * @return a string representing the label
     */
    String mapOutputsToInputs();

    /**
     * Text for "Input Label" label.
     * 
     * @return a string representing the label
     */
    String inputLabel();

    /**
     * Text for "Select Input(s)" label.
     * 
     * @return a string representing the label
     */
    String selectInputs();

    /**
     * Text for "Output(s)" label.
     * 
     * @return a string representing the label
     */
    String outputs();

    /**
     * Text for a "User Provided" label.
     * 
     * @return a string representing the label
     */
    String userProvided();

    /**
     * Message for successful workflow publish
     * 
     * @return a string representing the message
     */
    String publishWorkflowSuccess();

    String workflowInfo();

    String selectAndOrderApps();

    String publishToWorkspace();

    /**
     * Tool tip for the info panel
     * 
     * @return a string representing the text
     */
    String infoPnlTip();

    /**
     * Tool tip for the select & order panel
     * 
     * @return a string representing the text
     */
    String selectOrderPnlTip();

    /**
     * Tool tip for the inputs & outputs panel
     * 
     * @return a string representing the text
     */
    String inputsOutputsPnlTip();

    /**
     * Heading of the quick tips panel
     * 
     * @return a string representing the text
     */
    String quickTipsHeading();

    /**
     * Label for the "move up" button
     * 
     * @return a string representing the text
     */
    String moveUp();

    /**
     * Label for the "move down" button
     * 
     * @return a string representing the text
     */
    String moveDown();

    /**
     * Label for the PipelineCreator panel.
     * 
     * @return a string representing the text
     */
    String dragDropAppsToCreator();

    /**
     * Label for the PipelineCreator panel drag-n-drop feedback.
     * 
     * @return a string representing the text
     */
    String appendAppToWorkflow(String appName);
}
