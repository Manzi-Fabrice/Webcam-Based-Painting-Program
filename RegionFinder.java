import java.awt.*;
import java.awt.image.*;
import java.util.*;


/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 */
public class RegionFinder {
    private static final int minRegion = 50;            // how many points in a region to be worth considering
    private BufferedImage image;                            // the image in which to find regions
    private BufferedImage recoloredImage;                   // the image with identified regions recolored

    private ArrayList<ArrayList<Point>> regions;         // a region is a list of points

    private static final int maxColorDiff = 20;          // how similar a pixel color must be to the target color, to belong to a region

    public RegionFinder() {
        this.image = null;
    } //default setting of image

    //constructor with image to set
    public RegionFinder(BufferedImage image) {
        this.image = image;
    }

    //image setter
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    //image getter
    public BufferedImage getImage() {
        return image;
    }

    //recolored image getter
    public BufferedImage getRecoloredImage() {
        return recoloredImage;
    }
    //regions getter
    public ArrayList<ArrayList<Point>> getRegions() {
        return regions;
    }


    /**
     * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
     */
    public void findRegions(Color targetColor) {
        int width= image.getWidth();
        int height= image.getHeight();

        regions= new ArrayList<>(); // initialize an empty arraylist to hold visted regions

        //array to keep track of visited and unvisited points that is initially set to false
        boolean[][] visited = new boolean[width][height];

        for (int i=0; i<width; i++){ // this goes over every pixel
            for (int j=0; j<height;j++){
               // checks unvisted points
                if (visited[i][j]==false) {
                     // get the colour at that pixel
                    Color color1 = new Color(image.getRGB(i, j));

                    //calls method to check color proximity
                    if (colorMatch(targetColor, color1)) {
                        //updates to visited
                        visited[i][j] = true;
                        //creates new region
                        ArrayList<Point> newRegion = new ArrayList<>();
                        newRegion.add(new Point(i, j));
                        neighbouringRegion(i, j, targetColor, newRegion, visited);

                        // Only add the region if it's large enough
                        if (newRegion.size() >= minRegion) {
                            regions.add(newRegion);
                        }
                    }
                }
            }
        }
    }

    /**
     * method to determine the neighbor region of each point
     * **/
    private void neighbouringRegion(int x, int y, Color targetColor, ArrayList<Point> newRegion, boolean[][] visited) {
        int width = image.getWidth();
        int height = image.getHeight();

        ArrayList<Point> toVisit = new ArrayList<>(); // creating an array that will hold the point to visit
        toVisit.add(new Point(x, y));
        visited[x][y] = true;

        //loops through all points in the arraylist until it is empty
        while (!toVisit.isEmpty()) {
            Point current = toVisit.remove(0); // Remove the point at the beginning of the list
            newRegion.add(current);

            //checks 8-adjacency (NE, N, NW, W, E, SE, S, SW) neighbors
            for (int x1 = -1; x1 <= 1; x1++) {
                for (int y1 = -1; y1 <= 1; y1++) {
                    if (x1 == 0 && y1 == 0) continue; // Skip the center point itself

                    //create neighbor x and y
                    int nx = current.x + x1;
                    int ny = current.y + y1;

                    // Check bounds and whether the pixel has already been visited
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && !visited[nx][ny]) {
                        //checks if the color is close enough and add to list to visit
                        Color neighborColor = new Color(image.getRGB(nx, ny));
                        if (colorMatch(targetColor, neighborColor)) {
                            visited[nx][ny] = true;
                            toVisit.add(new Point(nx, ny)); // Add to the end of the list
                        }
                    }
                }
            }
        }
    }


    /**
     * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
     */
        protected static boolean colorMatch(Color c1, Color c2) {
        int redDiff = Math.abs(c1.getRed() - c2.getRed());
        int greenDiff = Math.abs(c1.getGreen() - c2.getGreen());
        int blueDiff = Math.abs(c1.getBlue() - c2.getBlue());

        return redDiff <= maxColorDiff && greenDiff <= maxColorDiff && blueDiff <= maxColorDiff;
    }




    /**
     * Returns the largest region detected (if any region has been detected)
     */
    public ArrayList<Point> largestRegion() {
        if (regions.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Point> currentMax = regions.get(0);
        for (int i = 1; i < regions.size(); i++) {
            if (regions.get(i).size() > currentMax.size()) {
                currentMax = regions.get(i);  /
            }
        }
        return currentMax;
    }


    /**
     * Sets recoloredImage to be a copy of image,
     * but with each region a uniform random color,
     * so we can see where they are
     */
    public void recolorImage() {
        recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
        for (ArrayList<Point> region : regions) {
            int red= (int) (Math.random()*256);
            int blue= (int) (Math.random()*256);
            int green= (int) (Math.random()*256);
            Color color= new Color(red,green,blue);

            for (Point p : region) {
                recoloredImage.setRGB(p.x, p.y, color.getRGB());
            }
        }

    }
}






