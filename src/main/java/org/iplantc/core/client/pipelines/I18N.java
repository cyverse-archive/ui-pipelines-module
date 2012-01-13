package org.iplantc.core.client.pipelines;

import com.google.gwt.core.client.GWT;

public class I18N {
    /** Strings displayed in the UI */
    public static final PipelinesDisplayStrings DISPLAY = (PipelinesDisplayStrings)GWT
            .create(PipelinesDisplayStrings.class);
    
    /** Strings displayed in the UI */
    public static final PipelinesErrorStrings ERROR = (PipelinesErrorStrings)GWT
            .create(PipelinesErrorStrings.class);
}
