package net.gunivers.gunibot.core;

import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.reaction.ReactionEmoji.Custom;
import discord4j.core.object.util.Image;
import discord4j.rest.http.client.ClientException;
import net.gunivers.gunibot.Main;
import reactor.core.publisher.Mono;

public class BotUtils {

	/**
	 * Fait que le bot prend le pseudo et l'image de profil d'un membre de façon à exécuter une action avant de reprendre ses paramètres par défault.
	 * @param m Un Member
	 * @param r Une Runnable à exécuter
	 */
	public static void takeMemberIdentity(Member m, Runnable r) {
		DiscordClient dc = Main.getBotInstance().getBotClient();
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

	/**
	 * Get an {@link Optional} safely, as it will prevent 404 errors from being thrown and instead return an empty Optional
	 * @param mono a {@link Mono}
	 * @return An Optional containing {@link Mono#block()} if everything goes well, or {@link Optional#empty()} in the case the mono
	 * 		emits a ClientException of code 404
	 */
	public static <T> Optional<T> returnOptional(Mono<T> mono) {
		try {
			return mono.blockOptional();
		} catch(ClientException e) {
			if(e.getStatus().code() == 404) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}

	/**
	 * @param emoji
	 * @return an unique id for any kind of discord emoji
	 */
	public static long emojiToId(ReactionEmoji emoji)
	{
		if (emoji.getClass() == Custom.class) return emoji.asCustomEmoji().get().getId().asLong();
		else return emoji.asUnicodeEmoji().get().getRaw().hashCode();
	}
}
