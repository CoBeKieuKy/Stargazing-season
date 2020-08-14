package adp.image.report3;

import java.awt.image.BufferedImage;

public class AdvancedSearcher extends AdvancedAbstractSearcher {

    public AdvancedSearcher(BufferedImage image1, BufferedImage image2, int firstPosition, int endPosition, SearchListener listener) {
        super(image1, image2, firstPosition, endPosition, listener);
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }
}
