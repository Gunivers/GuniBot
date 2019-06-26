package net.gunivers.gunibot.core;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.reaction.ReactionEmoji.Custom;
import discord4j.rest.http.client.ClientException;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple3;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import reactor.core.publisher.Mono;

public class BotUtils {

	/**
	 * Fait que le bot prend le pseudo et l'image de profil d'un membre de façon à
	 * exécuter une action avant de reprendre ses paramètres par défault.
	 * 
	 * @param m
	 *            Un Member
	 * @param r
	 *            Une Runnable à exécuter
	 */
	public static void sendMessageWithIdentity(Member m, MessageChannel tc, String message) {

		String content = "{\"content\": \"" + message + "\"}";

		Mono<Webhook> hook = ((TextChannel) tc).createWebhook((wh) -> {
			wh.setAvatar(m.getAvatar().block());
			wh.setName(m.getNickname().orElseGet(() -> m.getUsername()));
		});

		Tuple3<Long, String, Mono<Void>> webh = hook.map(w -> Tuple.newTuple(w.getId().asLong(), w.getToken(), w.delete())).block();

		RequestBody rb = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
		URL url;
		try {
			url = new URL("https://discordapp.com/api/webhooks/" + webh._1 + '/' + webh._2);
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).method("POST", rb).header("accept-encoding", "gzip").build();
			try (Response response = client.newCall(request).execute()) {
				webh._3.subscribe();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	
	}

	/**
	 * Get an {@link Optional} safely, as it will prevent 404 errors from being
	 * thrown and instead return an empty Optional
	 * 
	 * @param mono
	 *            a {@link Mono}
	 * @return An Optional containing {@link Mono#block()} if everything goes well,
	 *         or {@link Optional#empty()} in the case the mono emits a
	 *         ClientException of code 404
	 */
	public static <T> Optional<T> returnOptional(Mono<T> mono) {
		try {
			return mono.blockOptional();
		} catch (ClientException e) {
			if (e.getStatus().code() == 404) {
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
	public static long emojiToId(ReactionEmoji emoji) {
		if (emoji.getClass() == Custom.class)
			return emoji.asCustomEmoji().get().getId().asLong();
		else
			return emoji.asUnicodeEmoji().get().getRaw().hashCode();
	}
}
