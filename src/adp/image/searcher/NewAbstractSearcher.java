package adp.image.searcher;

import adp.image.jar.Searcher;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Class providing the basic infrastructure for concrete Searchers.
 */
public abstract class NewAbstractSearcher extends Thread implements Searcher{

    private final BufferedImage image1;
    private final BufferedImage image2;
    private final int firstPosition;
    private final int endPosition;
    private final SearchListener listener;

    private volatile int counter = 0;
    private volatile int currentPosition;

    /**
     * Constructs an AbstractSearcher that will attempt to find image2 somewhere in image1.
     */
    protected NewAbstractSearcher( final BufferedImage image1, final BufferedImage image2, SearchListener listener) {
        this.image1 = image1;
        this.image2 = image2;
        this.firstPosition = 0;
        this.endPosition = (image1.getWidth() * image1.getHeight()) - 1;
        this.currentPosition = 0;
        this.listener = listener;
    }

    /**
     * Constructs an AbstractSearcher that will attempt to find image2 in image1 at
     * every position between {@code firstPosition} (inclusive) and {@code endPosition} (exclusive).
     */
    protected NewAbstractSearcher(final BufferedImage image1, final BufferedImage image2, final int firstPosition, final int endPosition, SearchListener listener) {
        this.image1 = image1;
        this.image2 = image2;
        this.firstPosition = firstPosition;
        this.endPosition = endPosition;
        this.currentPosition = firstPosition;
        this.listener = listener;
    }

    /** {@inheritDoc} */
    @Override
    public final int numberOfPositionsToTry() {
        return this.endPosition - this.firstPosition;
    }

    /** {@inheritDoc} */
    @Override
    public final int numberOfPositionsTriedSoFar() {
        return this.counter;
    }

    /**
     * Resets the searcher to re-run its search. If any state is added in a subclass, this method
     * probably needs to be overridden to reset that state, as well as calling this implmentation.
     */
    @Override
    public void reset() {
        this.counter = 0;
        this.currentPosition = this.firstPosition;
    }

//    /**
//     * Attempts all positions specified for this Searcher in turn, emitting information about
//     * any position that appears to produce a match to the provided {@link Searcher.SearchListener} object.
//     * The SearchListener methods are invoked on the thread that calls this method.
//     */
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
        //listener.information(this.counter + " positions attempted.");
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

    /**
     * Tries the next possible position and returns true if the subimage at the current position matches
     * the one being searched for.
     * @return true if image is matched
     */
    protected boolean tryPosition() {
        final int x1 = this.currentPosition % this.image1.getWidth();
        final int y1 = this.currentPosition / this.image1.getWidth();
        double difference = 0;
        int count = 0;

//		System.out.println( "Position: " + this.currentPosition);
//		max = 0;
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


//				if ( this.currentPosition == 10) {
//					System.out.print( (x1 + x2) + "," + (y1 + y2) + " == " + x2 + "," + y2 + " ----> ");
//				}

                final double delta = compare(rgb1, rgb2);
                difference += delta;
                count++;
            }
        }
        //System.out.println( "Max: " + max);
        //System.out.println( difference + " ,,, " + difference / count);
        //image1.setRGB(x1, y1, 255); //(int)(difference / count));
        return difference / count < 10; // was 5
    }

//private double max = 0;

    protected double compare( final int rgb1, final int rgb2) {
        final Color c1 = new Color( rgb1);
        final Color c2 = new Color( rgb2);

//		if ( this.currentPosition == 10) {
//			System.out.println( c1 + " == " + c2);
//			throw new RuntimeException();
//
//		}


        final int dRed = Math.abs( c1.getRed() - c2.getRed());
        final int dGreen = Math.abs( c1.getGreen() - c2.getGreen());
        final int dBlue = Math.abs( c1.getBlue() - c2.getBlue());

        final double distance = Math.sqrt((dRed * dRed)+(dGreen * dGreen)+(dBlue * dBlue));
//if ( distance > max) { max = distance; }

        return distance;

    }

    public void run(){
        this.runSearch(this.listener);
    }
}
