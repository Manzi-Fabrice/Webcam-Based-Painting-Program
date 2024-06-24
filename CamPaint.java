import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.swing.*;

/**
 * Webcam-based drawing
 */
public class CamPaint extends VideoGUI {
    private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
    private RegionFinder finder;			// handles the finding
    private Color targetColor;          	// color of regions of interest (set by mouse press)
    private Color paintColor = Color.red;	// the color to put into the painting from the "brush"
    private BufferedImage painting;			// the resulting masterpiece

    /**
     * Initializes the region finder and the drawing
     */
    public CamPaint() {
        finder = new RegionFinder();
        clearPainting();
    }

    /**
     * Resets the painting to a blank image
     */
    protected void clearPainting() {
        painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * VideoGUI method, here drawing one of live webcam, recolored image, or painting,
     * depending on display variable ('w', 'r', or 'p')
     */

    @Override
    public void handleImage() {
        if (displayMode == 'w') {
            // Live webcam feed, just show the current image
            setImage1(image);
        }
        else {
            // Process the image to find regions
            finder.setImage(image);
            finder.findRegions(targetColor);

            if (displayMode == 'r') {
                // Recolored image, show only the largest region found
                try {
//                    BufferedImage recolored = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
//
                    BufferedImage recolored = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);


                    ArrayList<Point> largestRegion = finder.largestRegion();

                    // Paint only the largest region on the recolored image
                    for (Point p : largestRegion) {
                        recolored.setRGB(p.x, p.y, paintColor.getRGB());
                    }

                    setImage1(recolored);
                } catch (NoSuchElementException e) {
                    System.out.println("No large enough region found");
                    setImage1(image);  // Show the original image if no region is large enough
                }
            }
            else if (displayMode == 'p') {
                // Painting mode, continuously update the painting with the largest region
                finder.setImage(image);
                finder.findRegions(targetColor);

                try {
                    ArrayList<Point> largestRegion = finder.largestRegion();

                    // Update the painting by adding the largest region from the current frame
                    for (Point p : largestRegion) {
                        // Check to avoid painting outside the bounds
                        if (p.x >= 0 && p.x < painting.getWidth() && p.y >= 0 && p.y < painting.getHeight()) {
                            painting.setRGB(p.x, p.y, paintColor.getRGB());
                        }
                    }

                    setImage1(painting);
                } catch (NoSuchElementException e) {
                    System.out.println("No large enough region found");
                }
            }

        }
    }



    /**
     * Overrides the Webcam method to set the track color.
     */
    @Override

    public void handleMousePress(int x, int y) {
        // Set the target color based on where the user clicked
        targetColor = new Color(image.getRGB(x, y));
        finder.setImage(image);
        finder.findRegions(targetColor);
    }


    /**
     * Webcam method, here doing various drawing commands
     */
    @Override
    public void handleKeyPress(char k) {
        if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
            displayMode = k;
        }
        else if (k == 'c') { // clear
            clearPainting();
        }
        else if (k == 'o') { // save the recolored image
            ImageIOLibrary.saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
        }
        else if (k == 's') { // save the painting
            ImageIOLibrary.saveImage(painting, "pictures/painting.png", "png");
        }
        else {
            System.out.println("unexpected key "+k);
        }
    }


    public static void main(String[] args) {
        new CamPaint();
    }
}
