/******************************************************************************
  *  Name:    Greg Umali
  * 
  *  Description:  Structure that locates minimum-energy seam (horizontal or
  *                vertical) and can remove it from the picture for resizing
  *                with minimum loss of important features in the picture.
  * 
  *****************************************************************************/
import java.awt.Color;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    // defensive copy of the Picture argument
    private Picture picture;
    
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new NullPointerException();
        
        int width = picture.width();
        int height = picture.height();
        
        this.picture = new Picture(width, height);

        // make a defensive copy of picture by setting each pixel
        // of the copy to the color from the original picture
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                this.picture.set(w, h, picture.get(w, h));
            }
        }
    }
    
    // current picture
    public Picture picture() {
        // make a defensive copy of picture by setting each pixel
        // of the copy to the color from the original picture
        Picture pictureCopy = new Picture(width(), height());
        
        for (int w = 0; w < width(); w++) {
            for (int h = 0; h < height(); h++) {
                pictureCopy.set(w, h, picture.get(w, h));
            }
        }
        
        return pictureCopy;
    }
    
    // width of current picture
    public int width() {
        return picture.width();
    }
    
    // height of current picture
    public int height() {
        return picture.height();
    }
    
    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) {
            throw new IndexOutOfBoundsException();
        }
        
        return Math.sqrt(xGradSq(x, y) + yGradSq(x, y));
    }
    
    private int xGradSq(int x, int y) {
        if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) {
            throw new IndexOutOfBoundsException();
        }
        
        // stores the color of adjacent pixels
        Color lPxColor;
        Color rPxColor;
        
        // set values for left adj pixel, wrapping around if x = 0
        if (x == 0) lPxColor = picture.get(width() - 1, y);
        else lPxColor = picture.get(x - 1, y);
        int lR = lPxColor.getRed();
        int lG = lPxColor.getGreen();
        int lB = lPxColor.getBlue();
        
        // set values for right adj pixel, wrapping around if x = width()-1
        if (x == width() - 1) rPxColor = picture.get(0, y);
        else rPxColor = picture.get(x + 1, y);
        int rR = rPxColor.getRed();
        int rG = rPxColor.getGreen();
        int rB = rPxColor.getBlue();
        
        // compute x squared gradients
        return (lR - rR) * (lR - rR)
            + (lG - rG) * (lG - rG)
            + (lB - rB) * (lB - rB);
    }
    
    private int yGradSq(int x, int y) {
        if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) {
            throw new IndexOutOfBoundsException();
        }
        
        // stores the colors of above and below adjacent pixels
        Color uPxColor;
        Color dPxColor;
        
        // set values for upper adj pixel, wrapping around if y = 0
        if (y == 0) uPxColor = picture.get(x, height() - 1);
        else uPxColor = picture.get(x, y-1);
        int uR = uPxColor.getRed();
        int uG = uPxColor.getGreen();
        int uB = uPxColor.getBlue();
        
        // set values for downwards adj pixel, wrapping around if y = height()-1
        if (y == height() - 1) dPxColor = picture.get(x, 0);
        else dPxColor = picture.get(x, y+1);
        int dR = dPxColor.getRed();
        int dG = dPxColor.getGreen();
        int dB = dPxColor.getBlue();
        
        return (uR - dR) * (uR - dR) + 
            (uG - dG) * (uG - dG) + 
            (uB - dB) * (uB - dB);
    }
    
    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        // transpose horizontal-seamed energy matrix to vertical-seamed matrix
        transpose();
        
        // find vertical seam
        int[] hSeam = findVerticalSeam();
        
        // return picture to normal
        transpose();
        
        return hSeam;
    }
         
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int width = width();
        int height = height();
        
        // filled with energy values of the current picture
        double[][] energyMatrix = new double[height][width];
        
        // fill in the values of the energy matrix
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                energyMatrix[row][col] = energy(col, row);
            }
        }
        
        // 2D array denoting the current shortest distance to each pixel
        // (starting from pixels from the top row) as we relax edges
        // values are initialized to positive infinity
        double[][] distTo = new double[height][width];
        
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                distTo[h][w] = Double.POSITIVE_INFINITY;
            }
        }
        
        // will track the edge that leads to a vertex in the minimum energy path
        int[][] edgeTo = new int[height][width];
        
        // initialize the distTo values for the top row vertices
        for (int i = 0; i < width; i++) {
            distTo[0][i] = energyMatrix[0][i];
            edgeTo[0][i] = i;
        }
        
        double neighbor;
        
        
        // goes through all rows up to the second to last one
        for (int r = 0; r < height - 1; r++) {
            // goes through all vertices in a given row
            for (int c = 0; c < width; c++) {
                
                // CHECK LEFT LINK
                if (c != 0) {
                    // check diagonal left neighbor
                    neighbor = energyMatrix[r + 1][c - 1];
                    if (distTo[r][c] + neighbor < distTo[r + 1][c - 1]) {
                        distTo[r + 1][c - 1] = distTo[r][c] + neighbor;
                        edgeTo[r + 1][c - 1] = c;
                    }
                }
                
                // CHECK RIGHT LINK
                if (c != width - 1) {
                    // check diagonal right neighbor
                    neighbor = energyMatrix[r + 1][c + 1];
                    if (distTo[r][c] + neighbor < distTo[r + 1][c + 1]) {
                        distTo[r + 1][c + 1] = distTo[r][c] + neighbor;
                        edgeTo[r + 1][c + 1] = c;
                    }
                }
                
                // CHECK BOTTOM LINK
                neighbor = energyMatrix[r + 1][c];
                if (distTo[r][c] + neighbor < distTo[r + 1][c]) {
                    distTo[r + 1][c] = distTo[r][c] + neighbor;
                    edgeTo[r + 1][c] = c;
                }
            }
        } 
        
        // At this point, distTo now has minimum distances to all vertices, 
        // which resembles an edge-weighted digraph. 
        
        // Find vertex on bottom row with lowest sum energy path
        double minEnergy = Double.POSITIVE_INFINITY;
        int minVertex = -1;
        for (int i = 0; i < width(); i++) {
            if (distTo[height() - 1][i] < minEnergy) {
                minEnergy = distTo[height() - 1][i];
                minVertex = i;
            }
        }
        
        int[] vSeam = new int[height()];
        int row = height() - 1; // current row we are considering
        int col = minVertex; // working column that contains each part of the seam
        
        // while vSeam isn't empty
        while (row >= 0) {
            // traces back along edgeTo array to construct path to min dist val
            vSeam[row] = col;
            col = edgeTo[row][col];
            row--;
        }
        return vSeam;
    }
    
    private void transpose() {
        // swapped dimensions
        Picture transposed = new Picture(height(), width());
        
        for (int w = 0; w < width(); w++) {
            for (int h = 0; h < height(); h++) {
                // swapped dimensions in transposed, set to color of
                // corresponding pixel in picture
                transposed.set(h, w, picture.get(w, h));
            }
        }
        
        picture = transposed;
    }
    
    
    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (picture.height() == 1) throw new IllegalArgumentException();
        
        // check for invalid seams
        if (seam == null) throw new NullPointerException();
        // iterate through all items of the seam
        for (int i = 0; i < seam.length; i++) {
            // check that each element is valid
            if (seam[i] < 0 || seam[i] > height() - 1) {
                throw new IllegalArgumentException();
            }
            
            // check that each element is within 1 of adjacent elements of seam
            int diff;
            if (i != 0) {
                diff = seam[i] - seam[i - 1];
                if (diff < -1 || diff > 1) {
                    throw new IllegalArgumentException();
                }
            }
        }
        if (seam.length != width()) throw new IllegalArgumentException();
        
        // turn picture so the horizontal seam is changed into a vertical seam
        transpose();
        
        // remove seam
        removeVerticalSeam(seam);
        
        // restore the picture
        transpose();
    }
    
    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (picture.width() == 1) throw new IllegalArgumentException();
        
        // check for invalid seams
        if (seam == null) throw new NullPointerException();
        // iterate through all items of the seam
        for (int i = 0; i < seam.length; i++) {
            // check that each element is valid
            if (seam[i] < 0 || seam[i] > width() - 1) {
                throw new IllegalArgumentException();
            }
            
            // check that each element is within 1 of adjacent elements of seam
            int diff;
            if (i != 0) {
                diff = seam[i] - seam[i - 1];
                if (diff < -1 || diff > 1) {
                    throw new IllegalArgumentException();
                }
            }
        }
        if (seam.length != height()) throw new IllegalArgumentException();
        
        // create new picture object with space minus one column
        Picture trimmed = new Picture(width() - 1, height());
        
        // for each row
        for (int r = 0; r < height(); r++) {
            // add all pixels as before from everything before the deletion
            for (int c = 0; c < seam[r]; c++) {
                trimmed.set(c, r, picture.get(c, r));
            }
            
            // starting at the removed index, set the colors of all pixels that
            // follow the removed pixel in the row
            for (int c = seam[r]; c < width() - 1; c++) {
                trimmed.set(c, r, picture.get(c + 1, r));
            }
        }
        
        picture = trimmed;
    }
    
    // do unit testing of this class
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        SeamCarver carver = new SeamCarver(picture);
        
        // 6x5.png
        System.out.println("Creating a new seamcarver with picture " + args[0]);
        System.out.println();
        
        carver.picture().show();
        
        System.out.println("Width: " + carver.width());
        System.out.println("Height: " + carver.height());
        System.out.println();
        
        // should be {2 2 1 2 1 2}
        System.out.println("Horizontal Seam:");
        for (int i : carver.findHorizontalSeam()) {
            System.out.print(i + " ");
        }
        System.out.println();
        
        // should be {3 4 3 2 2}
        System.out.println("Vertical Seam:");
        for (int i : carver.findVerticalSeam()) {
            System.out.print(i + " ");
        }
        System.out.println();
        
        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        
        System.out.println("Removing 2 horizontal, 2 vertical seams:");
        System.out.println("New Width: " + carver.width());
        System.out.println("New Height: " + carver.height());
        System.out.println();
        
        carver.picture().show();
    }
}