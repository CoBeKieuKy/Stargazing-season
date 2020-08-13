package adp.image.searcher;

import adp.image.jar.Searcher;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

public class AdvancedAbstractSearcher extends RecursiveAction implements Searcher {

    private final BufferedImage image1;
    private final BufferedImage image2;
    private final int firstPosition;
    private final int endPosition;
    private final SearchListener listener;

    private volatile int counter = 0;
    private volatile int currentPosition;

    protected AdvancedAbstractSearcher(final BufferedImage image1, final BufferedImage image2, final int firstPosition, final int endPosition, SearchListener listener) {
        this.image1 = image1;
        this.image2 = image2;
        this.firstPosition = firstPosition;
        this.endPosition = endPosition;
        this.currentPosition = firstPosition;
        this.listener = listener;
    }

    @Override
    public int numberOfPositionsToTry() {
        return this.endPosition - this.firstPosition;
    }

    @Override
    public int numberOfPositionsTriedSoFar() {
        return this.counter;
    }

    @Override
    public void reset() {
        this.counter = 0;
        this.currentPosition = this.firstPosition;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void runSearch( final SearchListener listener) {
        this.reset();
        listener.information( "SEARCHING...");
        final long startTime = System.currentTimeMillis();
        while( true) {
            final int foundMatch = this.findMatch( listener, startTime);
            if ( foundMatch >= 0) {
                listener.possibleMatch( foundMatch, System.currentTimeMillis() - startTime, numberOfPositionsTriedSoFar());
            } else {
                break;
            }
        }
        listener.information("Finished at " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s\n");
    }

    private int findMatch( final SearchListener listener, final long startTime) {
        while( this.counter < numberOfPositionsToTry()) {
            final boolean hit = tryPosition();
            this.currentPosition++;
            this.counter++;
            if ( hit) {
                return this.currentPosition - 1;
            } else if ( this.counter % 1000 == 0) {
                listener.update( this.currentPosition - 1, System.currentTimeMillis() - startTime, numberOfPositionsTriedSoFar());
            }
        }
        return -1;
    }

    protected boolean tryPosition() {
        final int x1 = this.currentPosition % this.image1.getWidth();
        final int y1 = this.currentPosition / this.image1.getWidth();
        double difference = 0;
        int count = 0;

        for( int x2 = 0; x2 < this.image2.getWidth(); x2++) {
            if ( x1 + x2 >= this.image1.getWidth()) {
                break;
            }
            for( int y2 = 0; y2 < this.image2.getHeight(); y2++) {
                if ( y1 + y2 >= this.image1.getHeight()) {
                    break;
                }
                final int rgb1 = this.image1.getRGB(x1 + x2, y1 + y2);
                final int rgb2 = this.image2.getRGB(x2, y2);
                final double delta = compare(rgb1, rgb2);
                difference += delta;
                count++;
            }
        }
        return difference / count < 10; // was 5
    }

    protected double compare( final int rgb1, final int rgb2) {
        final Color c1 = new Color( rgb1);
        final Color c2 = new Color( rgb2);
        final int dRed = Math.abs( c1.getRed() - c2.getRed());
        final int dGreen = Math.abs( c1.getGreen() - c2.getGreen());
        final int dBlue = Math.abs( c1.getBlue() - c2.getBlue());
        final double distance = Math.sqrt((dRed * dRed)+(dGreen * dGreen)+(dBlue * dBlue));

        return distance;
    }


    @Override
    protected void compute() {
        System.out.println("run now");

        if ( endPosition - firstPosition < image1.getHeight() * image1.getWidth()) {
            this.runSearch(this.listener);

        } else {
            int middle = endPosition / 2;

            AdvancedAbstractSearcher subTask1 = new AdvancedAbstractSearcher(image1, image2, firstPosition, middle, this.listener);
            AdvancedAbstractSearcher subTask2 = new AdvancedAbstractSearcher(image1, image2, middle, endPosition, this.listener);

            invokeAll(subTask1, subTask2);
        }
    }
}
