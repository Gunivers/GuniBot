package net.gunivers.gunibot.core.utils;

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
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple3;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class BotUtils {

    /**
     * Fait que le bot prend le pseudo et l'image de profil d'un membre de façon à
     * exécuter une action avant de reprendre ses paramètres par défault.
     * 
     * @param member Un Member
     * @param r      Une Runnable à exécuter
     */
    public static void sendMessageWithIdentity(Member member, MessageChannel messageChannel, String strMessage) {

	String content = "{\"content\": \"" + strMessage + "\"}";

	Mono<Webhook> webhookMono = ((TextChannel) messageChannel).createWebhook((webhookSpec) -> {
	    webhookSpec.setAvatar(member.getAvatar().block());
	    webhookSpec.setName(member.getNickname().orElseGet(() -> member.getUsername()));
	});

	Tuple3<Long, String, Mono<Void>> webhookTuple = webhookMono
		.map(webhook -> Tuple.newTuple(webhook.getId().asLong(), webhook.getToken(), webhook.delete())).block();

	RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
	URL url;
	try {
	    url = new URL("https://discordapp.com/api/webhooks/" + webhookTuple.value1 + '/' + webhookTuple.value2);
	    OkHttpClient client = new OkHttpClient();
	    Request request = new Request.Builder().url(url).method("POST", requestBody)
		    .header("accept-encoding", "gzip").build();
	    try (Response response = client.newCall(request).execute()) {
		webhookTuple.value3.subscribe();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Get an {@link Optional} safely, as it will prevent 404 errors from being
     * thrown and instead return an empty Optional
     * 
     * @param mono a {@link Mono}
     * @return An Optional containing {@link Mono#block()} if everything goes well,
     *         or {@link Optional#empty()} in the case the mono emits a
     *         ClientException of code 404
     */
    public static <T> Optional<T> returnOptional(Mono<T> mono) {
	try {
	    return mono.blockOptional();
	} catch (ClientException e) {
	    if (e.getStatus().code() == 404)
		return Optional.empty();
	    else
		throw e;
	}
    }

    /**
     * test and return the HTTP code for a default request to the provided URI
     * 
     * @param uri the URI to test
     * @return the response code
     */
    public static int testHTTPCodeResponse(String uri) {
	return HttpClient.create().post().uri(uri).response().block().status().code();
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
