/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core.utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.RenderingHints;



import org.fluidproject.vulab.rascal.ui.ScreencastApplet;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;



public class Frame {
	private BufferedImage image;
	private long timestamp;
        public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());
	
	public Frame(BufferedImage image, long timestamp){
		this.image = image;
		this.timestamp = timestamp;
	}

	public BufferedImage getImage() {
	logger.info("getting image with timestamp" + timestamp);
            return image;
	}

	public void setImage(BufferedImage image) {


            this.image = image;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public String toString(){
		return "Frame at timestap: "+timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void toFile(File dir){


            if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
                logger.info("Dir exists" + dir.toString() );
                String filename = dir.toString() + File.separator  + timestamp + ".jpg";

                try {
                   

                        logger.info("Before ImageIO " + filename);
			ImageIO.write(image, "jpg", new File(filename));
                        logger.info("image written to file " + filename);
                  
                } catch (IOException e) {
		
                    logger.info("IO Exception for file " + filename);

		} catch (NullPointerException e)
                {
                    logger.info("Null pointer exception for file " + filename);
                }
            } else{
                logger.info("Write file : Problem with Dir " + dir.toString() );
            }
	}
	
	public void adjustImage(){

            acceleratedImage();
		
            image = scale(image, 0.45f);
            //Scaling value : .10 gives a very small image, while .90 produces a bigger image
            //with a bigger file size
            //image = scale(image, 0.50f);
                //For compression, the more the value, the smoother is the resulting image and bigger the size of the image.
                //This value is inversely proportional to the file compression.
		image = compress(image, 0.45f);
                //image = compress(image, 0.09f);
                logger.info("adjusted image with timestamp" + timestamp);
	}
	
	private void acceleratedImage(){
		BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics g = bi.getGraphics();
		g.drawImage(image, 0, 0, null);
		image = bi;
	}
	
	private static BufferedImage scale(BufferedImage image, float scale) {
		//Date now = new Date();
		int width = (int)(image.getWidth()*scale);
		int height = (int)(image.getHeight()*scale);
		/*

                Image img = image.getScaledInstance(width,height, Image.SCALE_SMOOTH);
		image.getGraphics().dispose();
		BufferedImage result = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		result.createGraphics().drawImage(img, 0, 0, null);
		img = null;
		image = null;
		//Date now2 = new Date();
		*/
                
                BufferedImage result = getScaledInstance(image,width,height, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                 return result;
	
	}
	
	private static BufferedImage compress(BufferedImage image, float quality) {
		try {
			
			Date now = new Date();
			Iterator<ImageWriter> writers = ImageIO
					.getImageWritersBySuffix("jpeg");
			ImageWriter writer = writers.next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer.setOutput(ImageIO.createImageOutputStream(out));
			writer.write(null, new IIOImage(image, null, null), param);
			byte[] data = out.toByteArray();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			
			out.close();
			BufferedImage result = ImageIO.read(in);
			in.close();
			writer = null;
			writers = null;
			param = null;
			Date now2 = new Date();
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

        /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

	
}
