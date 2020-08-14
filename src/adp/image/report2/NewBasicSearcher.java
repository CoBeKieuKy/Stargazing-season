package adp.image.report2;

import adp.image.report2.NewAbstractSearcher;

import java.awt.image.BufferedImage;

/**
 * A simple concrete implementation of AbstractSearcher that does not support cancellation.
 */
public class NewBasicSearcher extends NewAbstractSearcher {

    public NewBasicSearcher(BufferedImage image1, BufferedImage image2, SearchListener listener) {
        super( image1, image2, listener);
    }

    /**
     * This implementation just throws an UnsupportedOperationException.
     */
    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

}
