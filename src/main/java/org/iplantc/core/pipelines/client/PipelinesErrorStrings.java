package org.iplantc.core.pipelines.client;

import com.google.gwt.i18n.client.Messages;

public interface PipelinesErrorStrings extends Messages {

    /**
     * Error msg to display when service call to retrieve data objects fails
     * 
     * @return a string representing error msg
     */
    String dataObjectsRetrieveError();

    /**
     * Error msg to display when service call to publish workflow fails
     * 
     * @return a string representing error msg
     */
    String workflowPublishError();

    /**
     * Error msg to display when attempting to publish an invalid workflow.
     * 
     * @return a string representing error msg
     */
    String workflowValidationError();

    /**
     * Error msg to display when a mapping step is invalid.
     * 
     * @return a string representing error msg
     */
    String mappingStepError();
}
