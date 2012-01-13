package org.iplantc.core.client.pipelines;

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
}
