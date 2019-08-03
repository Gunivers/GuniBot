package net.gunivers.gunibot.core.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import discord4j.core.object.util.Image;

public class ImageUtils {

	/**
	 * Centre le string passé en paramètre au centre de l'image
	 * @param string un string
	 * @param font le font à appliquer au string
	 * @param graphics un graphics
	 * @param height la hauteur de l'image
	 * @param width la largeur de l'image
	 */
	public static void drawCenterString(String string, Font font, Graphics graphics, int height, int width) {
		 FontMetrics metrics = graphics.getFontMetrics(font);
	     int x = 0 + (width - metrics.stringWidth(string)) / 2;
	     int y = 0 + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
	     graphics.setFont(font);
	     graphics.drawString(string, x, y);
	}
	
	/**
	 * Converti un BufferedImage en Image de Discord4j
	 * @param bi un BufferedImage
	 * @return une Image Discord4j crée à partir de bi, si bi vaut null, retourne null
	 */
	public static Image bufferedImageToByteArray(BufferedImage bi) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "png", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			return Image.ofRaw(imageInByte, Image.Format.PNG);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
