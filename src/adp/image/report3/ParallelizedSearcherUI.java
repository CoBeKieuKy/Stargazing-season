package adp.image.report3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import adp.image.jar.Searcher;
import adp.image.jar.Searcher.SearchListener;

/**
 * This class implements a basic GUI interface for Searcher
 * implementations. This class implements SearchListener to
 * receive the Searcher's output information.
 */
public class ParallelizedSearcherUI extends JFrame implements SearchListener {
    private static final long serialVersionUID = 1L;

    private final JButton openBigButton = new JButton( "Open main image");
    private final JLabel mainFilenameLabel = new JLabel();

    private final JButton openSmallButton = new JButton( "Open small image");
    private final JLabel smallFilenameLabel = new JLabel();

    private final ImagePanel mainImagePanel = new ImagePanel();
    private final ImagePanel smallImagePanel = new ImagePanel();

    private final JFileChooser chooser = new JFileChooser();

    private final JLabel outputLabel = new JLabel( "information");
    private final JLabel outputLabel2 = new JLabel("points");
    private final JButton startButton = new JButton( "Start");

    private Searcher searcher;

    private BufferedImage mainImage;
    private BufferedImage smallImage;

    ForkJoinPool pool = new ForkJoinPool();

    /**
     * Construct an SearchUI and set it visible.
     */
    public ParallelizedSearcherUI() {
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE); // kill the application on closing the window


        final JPanel mainFilePanel = new JPanel( new BorderLayout());
        mainFilePanel.setSize(900,600);
        mainFilePanel.add( this.openBigButton, BorderLayout.WEST);
        mainFilePanel.add( this.mainFilenameLabel, BorderLayout.CENTER);

        final JPanel smallFilePanel = new JPanel( new BorderLayout());
        smallFilePanel.add( this.openSmallButton, BorderLayout.WEST);
        smallFilePanel.add( this.smallFilenameLabel, BorderLayout.CENTER);

        final JPanel topPanel = new JPanel( new GridLayout(0,1));
        topPanel.add( mainFilePanel);
        topPanel.add( smallFilePanel);

        final JPanel imagePanel = new JPanel( new BorderLayout());
        imagePanel.add( mainImagePanel, BorderLayout.CENTER);
        imagePanel.add( smallImagePanel, BorderLayout.EAST);

        final JPanel bottomPanel = new JPanel( new BorderLayout());
        bottomPanel.add( this.outputLabel, BorderLayout.CENTER);
        bottomPanel.add(this.outputLabel2, BorderLayout.EAST);
        bottomPanel.add( this.startButton, BorderLayout.SOUTH);

        final JPanel mainPanel = new JPanel( new BorderLayout());
        mainPanel.add( topPanel, BorderLayout.NORTH);
        mainPanel.add( imagePanel, BorderLayout.CENTER);
        mainPanel.add( bottomPanel, BorderLayout.SOUTH);

        this.openBigButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( final ActionEvent ev) {
                if ( chooser.showOpenDialog( ParallelizedSearcherUI.this) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    mainFilenameLabel.setText( file.getName());
                    try {
                        mainImage = ImageIO.read(file);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mainImagePanel.resetHighlights();
                    mainImagePanel.setImage(mainImage);
                    pack();
                    mainImagePanel.repaint();
                }
            }
        });

        this.openSmallButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( final ActionEvent ev) {
                if ( chooser.showOpenDialog( ParallelizedSearcherUI.this) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    smallFilenameLabel.setText( file.getName());
                    try {
                        smallImage = ImageIO.read(file);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    smallImagePanel.setImage(smallImage);
                    mainImagePanel.resetHighlights();
                    pack();
                }
            }
        });


        this.startButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( final ActionEvent ev) {
                runSearch();
            }
        });

        this.chooser.setMultiSelectionEnabled( false);
        this.chooser.setFileSelectionMode( JFileChooser.FILES_ONLY);
        this.chooser.setCurrentDirectory( new File( "."));


        add( mainPanel);
        pack();
        setVisible( true);
    }

    private void addActionListenerStartButton(){
        startButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( final ActionEvent ev) {
                runSearch();
            }
        });
    }
    /**
     * Clears output label and runs the search by calling {@link Searcher#runSearch(SearchListener)}.
     */
    private void runSearch() {
        // stop start button from running another search
        for (ActionListener al : startButton.getActionListeners()) {
            startButton.removeActionListener(al);
        }
        //Advanced Search, using ForkJoinPool to manage mainTask ( an object of class AdvancedSearcher
        // which extends RecursiveTask
        AdvancedSearcher mainTask = new AdvancedSearcher(mainImage, smallImage,
                0, (mainImage.getWidth() * mainImage.getHeight()),this);

        //return all searched points
        Integer all_searched_points = pool.invoke(mainTask);
        pool.shutdown();
        this.outputLabel2.setText("Have checked through: "+all_searched_points+" points\n");
    }

    /**
     * Implements {@link SearchListener#information(String)} by displaying the information
     * in the UI output label.
     */
    @Override
    public void information( final String message) {
        this.outputLabel.setText( message + "\n");
    }

    /**
     * Implements {@link SearchListener#possibleMatch(int, long, long) by displaying the information
     * in the UI output label.
     */
    @Override
    public void possibleMatch( final int position, final long elapsedTime, final long positionsTriedSoFar) {
        int x = position % mainImage.getWidth();
        int y = position / mainImage.getWidth();
        this.outputLabel.setText( "Possible match at: [" + x + "," + y  + "] at " + (elapsedTime / 1000.0) + "s (" + positionsTriedSoFar + " positions attempted)\n");
        int w = smallImage.getWidth();
        int h = smallImage.getHeight();
        Rectangle r = new Rectangle( x, y, w, h);
        mainImagePanel.addHighlight(r);

    }

    @Override
    public void update( final int position, final long elapsedTime, final long positionsTriedSoFar) {
        int x = position % mainImage.getWidth();
        int y = position / mainImage.getWidth();
        int estimate_times = mainImage.getWidth()*mainImage.getHeight();
        this.outputLabel.setText( "Update at: [" + x + "," + y  + "] at " + (elapsedTime / 1000.0) + "s (" + positionsTriedSoFar + " positions attempted)\n");
        float percent = (float)  positionsTriedSoFar*100/estimate_times;
        if (percent>99.5){
            percent =100;
        }

        if (percent==100){
            addActionListenerStartButton();
        }
    }


    private static void launch() {
        new ParallelizedSearcherUI();

    }

    private static class ImagePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private BufferedImage image;

        private List<Rectangle> highlights = new ArrayList<Rectangle>();

        public void setImage( BufferedImage image) {
            this.image = image;

            double scale = 1;

            if ( image.getWidth() >= image.getHeight()) {
                if ( image.getWidth() > 800) {
                    scale = 800.0 / image.getWidth();
                }
            } else {
                if ( image.getHeight() > 800) {
                    scale = 800.0 / image.getHeight();
                }
            }
            Dimension d = new Dimension(
                    (int)Math.ceil(image.getWidth() * scale),
                    (int)Math.ceil(image.getHeight() * scale));
            setPreferredSize( d);

            invalidate();
            repaint();
        }

        public void addHighlight( Rectangle r) {
            synchronized( highlights) {
                this.highlights.add( r);
            }
            repaint();
        }

        public void resetHighlights() {
            synchronized( highlights) {
                this.highlights.clear();
            }
            repaint();
        }

        @Override
        public void paintComponent( Graphics g) {
            if ( this.image != null) {
                g = g.create();
                double scale = getWidth() / (double)this.image.getWidth();
                //System.out.println( scale + "!");
                g.drawImage( this.image, 0, 0, getWidth(), (int)(this.image.getHeight() * scale), this);
                //System.out.println( ">>>" + completed);
                g.setColor( Color.YELLOW);
                synchronized( highlights) {
                    for( Rectangle r : highlights) {
                        Rectangle s = new Rectangle(
                                (int)(r.x * scale),
                                (int)(r.y * scale),
                                (int)(r.width * scale),
                                (int)(r.height * scale));
                        ((Graphics2D) g).draw( s);
                    }
                }
            }
        }

    }

    public static void main( final String[] args) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {  launch(); }
                }
        );
    }
}
