package net.gunivers.gunibot.core.feature;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Image;
import net.gunivers.gunibot.core.utils.ImageUtils;

public class NumberToReaction {

	private final static int SIZE = 1024;
	private final static Color COLOR = new Color(255, 87, 34);
	private final static Font FONT = new Font("Comic Sans MS", Font.BOLD, 600);
	
	public static void convertNumberToReaction(int i, Message m) {
	     
	     BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
	     Graphics2D graphics = img.createGraphics();
	     graphics.setColor(COLOR);
	     graphics.fillRoundRect(0, 0, SIZE - 1, SIZE - 1, SIZE / 2, SIZE / 2);
	     graphics.setColor(new Color(255, 255, 255));
	     ImageUtils.drawCenterString(Integer.toString(i), FONT, graphics, SIZE, SIZE);
	   
	     Image image = ImageUtils.bufferedImageToByteArray(img);
	     m.getGuild().block().createEmoji(gecs -> {
	    	 gecs.setImage(image);
	    	 gecs.setName("levelNumber");
	     }).subscribe(e -> {
	    	 m.addReaction(ReactionEmoji.custom(e)).subscribe();
	    	 e.delete().subscribe();
	     });
	}
}