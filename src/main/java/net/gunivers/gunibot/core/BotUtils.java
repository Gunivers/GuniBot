package net.gunivers.gunibot.core;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Image;
import net.gunivers.gunibot.Main;

public class BotUtils {

	/**
	 * Fait que le bot prend le pseudo et l'image de profil d'un membre de façon à exécuter une action avant de reprendre ses paramètres par défault.
	 * @param m Un Member
	 * @param r Une Runnable à exécuter
	 */
	public static void takeMemberIdentity(Member m, Runnable r) {
		DiscordClient dc = Main.getBotClient();
		Image defaultImage = dc.getSelf().block().getAvatar().block();
		String defaultName = dc.getSelf().block().getUsername();
		dc.edit(ues ->
		{
			ues.setAvatar(m.getAvatar().block());
			ues.setUsername(m.getDisplayName());
		});
		r.run();
		dc.edit(ues -> 
		{
			ues.setAvatar(defaultImage);
			ues.setUsername(defaultName);
		}
		);
		
	}

}
