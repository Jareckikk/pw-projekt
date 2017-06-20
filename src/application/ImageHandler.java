package application;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageHandler {
	
	public static BufferedImage[] split(BufferedImage image, int rows, int cols){
	     int chunks = rows * cols;

	     int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
	     int chunkHeight = image.getHeight() / rows;
	     int count = 0;
	     BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
	     for (int x = 0; x < rows; x++) {
	         for (int y = 0; y < cols; y++) {
	             //Initialize the image array with image chunks
	             imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

	             // draws the image chunk
	             Graphics2D gr = imgs[count++].createGraphics();
	             gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
	             gr.dispose();
	         }
	     }
	     System.out.println("Splitting done");

	     //writing mini images into image files
	     //for (int i = 0; i < imgs.length; i++) {
	     //    ImageIO.write(imgs[i], "jpg", new File("img" + i + ".jpg"));
	     //}
	     //System.out.println("Mini images created");
	     return imgs;
	}

	public static BufferedImage merge(BufferedImage[] imgs, int rows, int cols){
        int chunks = rows * cols;
        int chunkWidth, chunkHeight;
        int type;
        //fetching image files
       /* File[] imgFiles = new File[chunks];
        for (int i = 0; i < chunks; i++) {
            imgFiles[i] = new File("archi" + i + ".jpg");
        }*/

       //creating a bufferd image array from image files
       /* BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
            buffImages[i] = ImageIO.read(imgFiles[i]);
        }*/
        type = imgs[0].getType();
        chunkWidth = imgs[0].getWidth();
        chunkHeight = imgs[0].getHeight();

        //Initializing the final image
        BufferedImage finalImg = new BufferedImage(chunkWidth*cols, chunkHeight*rows, type);

        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(imgs[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }
        System.out.println("Image concatenated.....");
        
        return finalImg;
        //ImageIO.write(finalImg, "jpeg", new File("finalImg.jpg"));
	}
	
}
