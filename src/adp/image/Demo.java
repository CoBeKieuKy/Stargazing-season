package adp.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import adp.image.jar.BasicSearcher;
import adp.image.jar.Searcher;
import adp.image.jar.Searcher.SearchListener;

/**
 * This class implements {@link Search.SearchListener} and emits all messages
 * on the command line output. 
 */
public class Demo implements SearchListener {

	public Demo() throws IOException {

		BufferedImage[] images = testImages();
		Searcher searcher = new BasicSearcher( images[0], images[1]);
		searcher.runSearch( this);

	}


	public Demo( File file1, File file2) throws IOException {

		BufferedImage image1 = ImageIO.read(file1); 
		BufferedImage image2 = ImageIO.read(file2); 

		Searcher searcher = new BasicSearcher( image1, image2);
		searcher.runSearch( this);

	}

	@Override
	public void information( String message) {
		System.out.println( message);    
	}

	@Override
	public void possibleMatch( int position, long elapsedTime, long numberOfPositionsTriedSoFar) {
		System.out.println( "Possible match at: " + position + " at " + (elapsedTime / 1000.0) + "s (" + numberOfPositionsTriedSoFar + " positions attempted)");
	}

	@Override
	public void update( int position, long elapsedTime, long numberOfPositionsTriedSoFar) {
		System.out.println( "Searching at: " + position + " at " + (elapsedTime / 1000.0) + "s (" + numberOfPositionsTriedSoFar + " positions attempted)");
	}

	private static BufferedImage[] testImages() {
		BufferedImage i1 = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		BufferedImage i2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

		Graphics g = i2.getGraphics();
		g.setColor(Color.RED);
		g.fillRect( 0, 0, 10, 10);
		g = i1.getGraphics();
		g.setColor(Color.RED);
		g.fillRect( 10, 0, 10, 10);

		return new BufferedImage[] { i1, i2 };
	}

	/**
	 * Set the file names to search images of different sizes.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args) throws IOException {
		File file1 = new File( "bigImage1r.jpg");    
//		File file2 = new File( "small1r.jpg");    
//		File file2 = new File( "small2r.jpg");    
		File file2 = new File( "tiny3r.jpg");    
		new Demo( file1, file2);  
	}



}
