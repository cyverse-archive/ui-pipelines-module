package org.iplantc.core.client.pipelines.images;

import com.google.gwt.resources.client.ImageResource;

/**
 * Provides access to bundled image resources.
 */
public interface Icons extends org.iplantc.core.uicommons.client.images.Icons {
    @Source("tick.png")
    ImageResource stepComplete();

    @Source("exclamation.png")
    ImageResource stepError();

    @Source("arrow_down.png")
    ImageResource down();

    @Source("arrow_up.png")
    ImageResource up();

    @Source("add.png")
    ImageResource add();

    @Source("delete.gif")
    ImageResource remove();

    /**
     * Image resource.
     * 
     * @return image.
     */
    @Source("publish.png")
    ImageResource publish();

}
