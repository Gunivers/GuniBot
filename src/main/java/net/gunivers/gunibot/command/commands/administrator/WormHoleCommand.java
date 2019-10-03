package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.Ignore;
import net.gunivers.gunibot.core.datas.serialize.OldRestorable;
import net.gunivers.gunibot.core.datas.serialize.OldSerializer;
import net.gunivers.gunibot.core.event.Events;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.utils.BotUtils;
import net.gunivers.gunibot.core.utils.ListUtils;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;
import net.gunivers.gunibot.core.utils.tuple.Tuple3;
import net.gunivers.gunibot.core.utils.tuple.Tuple5;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Ignore
public class WormHoleCommand extends Command {

    private static class Memory implements OldRestorable {
	private HashMap<Tuple2<Long, Long>, Set<Tuple2<Long, Long>>> linkedChannels = new HashMap<>();
	private WormHoleCommand instance;

	@Override
	public OldSerializer save() {
	    OldSerializer serializer = new OldSerializer();
	    serializer.put("wormhole", linkedChannels);
	    return serializer;
	}

	@SuppressWarnings("unchecked")
	@Override

	public void load(OldSerializer serializer) {
	    linkedChannels = (HashMap<Tuple2<Long, Long>, Set<Tuple2<Long, Long>>>) serializer.get("wormhole");
	    if (linkedChannels == null) {
		linkedChannels = new HashMap<>();
	    }
	    instance.linkAll();
	}
    }

    // Delay d'attente avant auto-suppression de la demande de confirmation
    private final static int TIME = 60000;

    // Stock les les channels associés et leur Guild respectif
    private Memory memory;

    {
	memory = new Memory();
	memory.instance = this;
	System.out.println(Main.getBotInstance());
	dataCenter.registerOldSerializer("wormhole", memory);
    }

    /**
     * Recréer les liens entre tous les salons sauvegardés
     */
    private void linkAll() {
	List<Long> id = memory.linkedChannels.keySet().stream().map(t -> t.value1).collect(Collectors.toList());
	discordClient.getEventDispatcher().on(MessageCreateEvent.class)
		.filter(messageCreateEvent -> id.contains(messageCreateEvent.getMessage().getChannelId().asLong()))
		.subscribe(event2 -> copyMessage(event2));
    }

    /**
     * Liste les différents Sender Channels
     * 
     * @param event
     */
    public void list(MessageCreateEvent event) {
	final String messageIfEmpty = "Aucun wormhole existant.";
	final Field list = new Field("‌‌Sender Channels");
	// On trie et ajoute les index en préfixe des éléments de la liste
	String text = sortSetAndZipWithIndex(memory.linkedChannels.keySet()).map(tuple -> {
	    Guild guild = event.getClient().getGuildById(Snowflake.of(tuple.value3)).block();
	    return tuple.value1 + ". #" + guild.getChannelById(Snowflake.of(tuple.value2)).block().getName() + " in **"
		    + guild.getName() + "**";
	}).collect(Collectors.joining("\n"));
	// On envoie l'embed
	list.setValue(text.isEmpty() ? messageIfEmpty : text);
	constructAndSendEmbed(list, event);
    }

    /**
     * Liste les différents Receiver Channels
     * 
     * @param event
     */
    public void subList(MessageCreateEvent event, List<String> args) {
	// On récupère le salon correspondant à l'index donné
	Optional<Tuple3<Integer, Long, Long>> foundValue = sortSetAndZipWithIndex(memory.linkedChannels.keySet())
		.filter(tuple -> tuple.value1 == Integer.parseInt(args.get(0))).findFirst();
	if (!foundValue.isPresent()) {
	    event.getMessage().getChannel().block().createMessage("Number " + args.get(0) + " not valid!").subscribe();
	} else {

	    Tuple2<Long, Long> foundChannel = Tuple.newTuple(foundValue.get().value2, foundValue.get().value3);
	    Guild guild = event.getClient().getGuildById(Snowflake.of(foundChannel.value2)).block();
	    GuildChannel channel = guild.getChannelById(Snowflake.of(foundChannel.value1)).block();
	    final Field list = new Field("‌‌Receiver Channels of #" + channel.getName() + " in " + guild.getName());

	    // On récupère les salons liés à celui trouvé
	    Set<Tuple2<Long, Long>> children = getChildren(channel.getId().asLong(), guild.getId().asLong());

	    // On trie et ajoute les index en préfixe des éléments de la liste
	    String text = sortSetAndZipWithIndex(children).map(tuple -> {
		Guild internalGuild = event.getClient().getGuildById(Snowflake.of(tuple.value3)).block();
		return tuple.value1 + ". #" + internalGuild.getChannelById(Snowflake.of(tuple.value2)).block().getName()
			+ " in **" + internalGuild.getName() + "**";
	    }).collect(Collectors.joining("\n"));
	    // On envoie l'embed
	    list.setValue(text);
	    constructAndSendEmbed(list, event);
	}
    }

    /**
     * Construit l'embed utilisé dans la classe
     * 
     * @param field
     * @param event
     */
    private void constructAndSendEmbed(Field field, MessageCreateEvent event) {
	EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getChannel().block(), null, null);
	embedBuilder.addField(field);
	embedBuilder.setColor(new Color(255, 87, 34));
	embedBuilder.setRequestedBy(event.getMember().get());
	embedBuilder.buildAndSend();
    }

    /**
     * @param tupleSet
     * @return le set trié en fonction des Longs et zip avec l'index
     */
    private Stream<Tuple3<Integer, Long, Long>> sortSetAndZipWithIndex(Set<Tuple2<Long, Long>> tupleSet) {
	List<Tuple2<Long, Long>> temp = tupleSet.stream()
		.sorted((tuple1, tuple2) -> (Long.toString(tuple1.value1) + Long.toString(tuple1.value2))
			.compareTo(Long.toString(tuple2.value1) + Long.toString(tuple2.value2)))
		.collect(Collectors.toList());
	return ListUtils.mapWithIndex(temp, (tuple, i) -> Tuple.newTuple(i, tuple.value1, tuple.value2)).stream();
    }

    public void link(MessageCreateEvent event, List<String> args) {
	// Récupère les salons concernés par les arguments spécifiés
	Flux<GuildChannel> channels = Main.getBotInstance().getBotClient().getGuilds()
		.flatMap(guild -> guild.getChannels().filter(
			channel -> channel instanceof MessageChannel && (channel.getId().asString().equals(args.get(0))
				|| channel.getId().asString().equals(args.get(1)))));

	// Si il y a bien deux salons
	if (channels.count().block() == 2) {

	    // Séparation des deux salons
	    GuildChannel channel1 = channels.blockFirst().getId().asString().equals(args.get(0)) ? channels.blockFirst()
		    : channels.blockLast();
	    GuildChannel channel2 = channels.blockLast().getId().asString().equals(args.get(1)) ? channels.blockLast()
		    : channels.blockFirst();

	    // Si le lien existe déjà
	    if (getChildren(channel1.getId().asLong(), channel1.getGuildId().asLong()).stream()
		    .filter(t -> t.value1 == channel2.getId().asLong() && t.value2 == channel2.getGuildId().asLong())
		    .count() == 0) {

		// Envoie d'un message de confirmation de lien
		String message = "Confirm link between " + channel1.getMention() + " in "
			+ channel1.getGuild().block().getName() + " and " + channel2.getMention() + " in "
			+ channel2.getGuild().block().getName() + "?";
		sendTemporaryValidationMessage(event.getMessage().getChannel().block(), message,
			Tuple.newTuple(channel1, channel2, event.getMember().get().getId()),
			(tuple, reactionAddEvent) -> onThumbEmojiAdded(tuple, reactionAddEvent));
	    } else {
		event.getMessage().getChannel().block().createMessage("The specified link already exists!").subscribe();
	    }

	} else {
	    event.getMessage().getChannel().block().createMessage("Specified channels not valid!").subscribe();
	}
    }

    /**
     * Envoie un message à durée de TIME millisecondes avec deux reactions. Si la
     * reaction "Pouce en l'air" est cliqué par l'utilisateur ayant pour Snowflake
     * celui spécifié dans linkInfo, la méthode onValid avec pour attribut linkInfo,
     * le message affiché et le disposable de ce dernier sera appelé. Si l'autre
     * réaction est cliqué, supprime le message et annule le disposable.
     * 
     * @param channel
     * @param message
     * @param linkInfos
     * @param onValid
     */
    private void sendTemporaryValidationMessage(MessageChannel channel, String strMessage,
	    Tuple3<GuildChannel, GuildChannel, Snowflake> linkInfos,
	    BiConsumer<Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable>, ReactionAddEvent> onValid) {
	Mono<Message> messageMono = channel.createMessage(strMessage);
	Message message = messageMono.block();
	message.addReaction(ReactionEmoji.unicode("👍")).subscribe();
	message.addReaction(ReactionEmoji.unicode("👎")).subscribe();

	// Auto-suppression au bout de TIME millisecondes
	Disposable disposable = Mono.just(message).delayElement(Duration.ofMillis(TIME))
		.subscribe(internalMessage -> internalMessage.delete().subscribe());

	// Ajout des boutons et liaison des events
	Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos = Tuple5.newTuple(linkInfos.value1,
		linkInfos.value2, linkInfos.value3, message, disposable);
	Events.REACTION_ADDED.on(message, ReactionEmoji.unicode("👍"),
		reactionAddEvent -> onValid.accept(infos, reactionAddEvent));
	Events.REACTION_ADDED.on(message, ReactionEmoji.unicode("👎"),
		reactionAddEvent -> onThumbDownEmojiAdded(message, disposable));
    }

    /**
     * Propose la suppression du lien entre 2 salons
     * 
     * @param event
     * @param args
     */
    public void remove(MessageCreateEvent event, List<String> args) {
	// On récupère le salon correspondant au premier index donné
	Optional<Tuple3<Integer, Long, Long>> foundValue = sortSetAndZipWithIndex(memory.linkedChannels.keySet())
		.filter(tuple -> tuple.value1 == Integer.parseInt(args.get(0))).findFirst();
	if (!foundValue.isPresent()) {
	    event.getMessage().getChannel().block().createMessage("Number " + args.get(0) + " not valid!").subscribe();
	} else {

	    Tuple2<Long, Long> foundChannel = Tuple.newTuple(foundValue.get().value2, foundValue.get().value3);
	    Guild guild = event.getClient().getGuildById(Snowflake.of(foundChannel.value2)).block();
	    GuildChannel channel = guild.getChannelById(Snowflake.of(foundChannel.value1)).block();

	    // On récupère les salons liés à celui trouvé
	    Set<Tuple2<Long, Long>> children = getChildren(channel.getId().asLong(), guild.getId().asLong());

	    // On récupère le salon ayant pour index la deuxième valeur spécifiée
	    Optional<Tuple3<Integer, Long, Long>> foundValue2 = sortSetAndZipWithIndex(children)
		    .filter(tuple -> tuple.value1 == Integer.parseInt(args.get(1))).findFirst();
	    if (!foundValue2.isPresent()) {
		event.getMessage().getChannel().block().createMessage("Number " + args.get(1) + " not valid!")
			.subscribe();
	    } else {

		Tuple2<Long, Long> foundChannel2 = Tuple.newTuple(foundValue2.get().value2, foundValue2.get().value3);
		Guild guild2 = event.getClient().getGuildById(Snowflake.of(foundChannel2.value2)).block();
		GuildChannel channel2 = guild.getChannelById(Snowflake.of(foundChannel2.value1)).block();

		String message = "Remove link between " + channel.getMention() + " in " + guild.getName() + " and "
			+ channel2.getMention() + " in " + guild2.getName() + "?";
		sendTemporaryValidationMessage(event.getMessage().getChannel().block(), message,
			Tuple.newTuple(channel, channel2, event.getMember().get().getId()),
			(tuple, reactionAddEvent) -> onThumbEmojiAddedForRemove(tuple, reactionAddEvent));
	    }
	}
    }

    private void onThumbEmojiAddedForRemove(
	    final Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos, ReactionAddEvent event) {
	if (infos != null)
	    // Si l'utilisateur corresponds à celui ayant fait la requète de liaison
	    if (event.getUserId().equals(infos.value3)) {
		// On annue les events et on supprime le message
		Events.REACTION_ADDED.cancel(event.getMessage().block());
		infos.value5.dispose();
		infos.value4.delete().subscribe();

		event.getMessage().block().getChannel().block().createMessage("Specified link removed!").subscribe();
		// On crée la nouvelle liste des enfants sans celui supprimé
		Set<Tuple2<Long, Long>> children = getChildren(infos.value1.getId().asLong(),
			infos.value1.getGuildId().asLong())
				.stream()
				.filter(tuple -> !(tuple.value1.equals(infos.value2.getId().asLong())
					&& tuple.value2.equals(infos.value2.getGuildId().asLong())))
				.collect(Collectors.toSet());
		// On l'associe au parent
		Tuple2<Long, Long> parent = memory.linkedChannels.keySet().stream()
			.filter(t -> t.value1.equals(infos.value1.getId().asLong())
				&& t.value2.equals(infos.value1.getGuildId().asLong()))
			.findFirst().get();
		if (children.isEmpty()) {
		    memory.linkedChannels.remove(parent);
		} else {
		    memory.linkedChannels.put(parent, children);
		}
	    }
    }

    /**
     * @param message    Le Message à supprimé
     * @param disposable L'action à annuler
     */
    private void onThumbDownEmojiAdded(Message message, Disposable disposable) {
	message.delete().subscribe();
	disposable.dispose();
    }

    /**
     * @param infos Les informations nécessaires à la liaison des salons
     * @param event L'event d'ajout de l'emote
     */
    private void onThumbEmojiAdded(final Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos,
	    ReactionAddEvent event) {
	if (infos != null)
	    // Si l'utilisateur corresponds à celui ayant fait la requète de liaison
	    if (event.getUserId().equals(infos.value3)) {

		// On annue les events et on supprime le message
		Events.REACTION_ADDED.cancel(event.getMessage().block());
		infos.value5.dispose();
		infos.value4.delete().subscribe();

		// Si les guildes existent toujours au moment de la validation
		if (BotUtils.returnOptional(event.getClient().getGuildById(infos.value1.getGuildId())).isPresent()
			&& BotUtils.returnOptional(event.getClient().getGuildById(infos.value2.getGuildId()))
				.isPresent()) {

		    // Si les salons existent toujours au moment de la validation
		    if (channelExist(infos.value1.getGuild().block(), infos.value1.getId().asLong())
			    && channelExist(infos.value2.getGuild().block(), infos.value2.getId().asLong())) {

			event.getMessage().block().getChannel().block().createMessage("Specified channels linked!")
				.subscribe();

			// On ajoute le channel recepteur à la liste du channel émetteur
			Set<Tuple2<Long, Long>> values = getChildren(infos.value1.getId().asLong(),
				infos.value1.getGuildId().asLong());
			values.add(Tuple.newTuple(infos.value2.getId().asLong(), infos.value2.getGuildId().asLong()));
			Optional<Tuple2<Long, Long>> optional = getKey(infos.value1.getId().asLong(),
				infos.value1.getGuildId().asLong());
			memory.linkedChannels.put(optional.isPresent() ? optional.get()
				: Tuple.newTuple(infos.value1.getId().asLong(), infos.value1.getGuildId().asLong()),
				values);
			// Si le channel émetteur reçoit un message, on appelle la fonction copyMessage
			discordClient.getEventDispatcher().on(MessageCreateEvent.class)
				.filter(messageCreateEvent -> infos.value1.getId().asLong() == messageCreateEvent
					.getMessage().getChannelId().asLong()
					&& messageCreateEvent.getMessage().getContent().isPresent())
				.subscribe(event2 -> copyMessage(event2));
		    } else {
			event.getChannel().block().createMessage("One of the two channels not exist!").subscribe();
		    }
		} else {
		    event.getChannel().block().createMessage("One of the two guilds not exist!").subscribe();
		}
	    }
    }

    /**
     * Copie le message de l'événément dans tous les channels liés au salon du
     * message
     * 
     * @param event
     */
    private void copyMessage(MessageCreateEvent event) {
	if (event.getMember().isPresent()) {

	    String message = Arrays
		    .asList(event.getMessage().getContent().get().split(" ")).stream().map(
			    newStrMessage -> newStrMessage.matches("<@\\d*>")
				    ? event.getGuild().block()
					    .getMemberById(Snowflake
						    .of(newStrMessage.substring(2, newStrMessage.length() - 1)))
					    .block().getDisplayName()
				    : newStrMessage)
		    .collect(Collectors.joining(" "));

	    // On récupère tous les salons liés au channel courant
	    Set<Tuple2<Long, Long>> values = getChildren(event.getMessage().getChannelId().asLong(),
		    event.getGuildId().get().asLong());

	    // On écrit le message dans les salons en question en prenant l'apparence de
	    // l'utilisateur à l'origine du message
	    values.forEach(channel -> {
		if (BotUtils
			.returnOptional(event.getClient().getGuildById(Snowflake.of(channel.value2)).block()
				.getChannelById(Snowflake.of(channel.value1)))
			.isPresent() && event.getMessage().getContent().isPresent()) {
		    BotUtils.sendMessageWithIdentity(event.getMember().get(),
			    (MessageChannel) event.getClient().getGuildById(Snowflake.of(channel.value2)).block()
				    .getChannelById(Snowflake.of(channel.value1)).block(),
			    message);
		}
	    });
	}
    }

    /**
     * @param channelId
     * @param guildId
     * @return Les salons liés au salon spécifié en paramètre
     */
    private Set<Tuple2<Long, Long>> getChildren(Long channelId, Long guildId) {
	Set<Tuple2<Long, Long>> children = memory.linkedChannels.getOrDefault(memory.linkedChannels.keySet().stream()
		.filter(tuple -> tuple.equals(Tuple.newTuple(channelId, guildId))).findFirst()
		.orElse(Tuple.newTuple(0L, 0L)), new HashSet<>());
	Set<Tuple2<Long, Long>> childrenFiltred = children.stream().filter(
		tuple -> BotUtils.returnOptional(discordClient.getGuildById(Snowflake.of(tuple.value2))).isPresent()
			&& channelExist(discordClient.getGuildById(Snowflake.of(tuple.value2)).block(), tuple.value1))
		.collect(Collectors.toSet());
	if (children.size() != childrenFiltred.size()) {
	    children = childrenFiltred;
	    Optional<Tuple2<Long, Long>> keyOptional = getKey(channelId, guildId);
	    if (keyOptional.isPresent()) {
		memory.linkedChannels.put(keyOptional.get(), children);
	    }
	    System.out.println("Removed channels");
	}
	return children;
    }

    /**
     * @param channelId
     * @param guildId
     * @return la clé de la map associée au channel et à la guild donnée, null si
     *         celle-ci n'existe pas
     */
    private Optional<Tuple2<Long, Long>> getKey(Long channelId, Long guildId) {
	int isValid = 0;
	Optional<Tuple2<Long, Long>> value = memory.linkedChannels.keySet().stream()
		.filter(tuple -> tuple.equals(Tuple.newTuple(channelId, guildId))).findFirst();
	isValid = value.isPresent() ? 0 : 1;
	if (isValid == 0) {
	    isValid = BotUtils.returnOptional(discordClient.getGuildById(Snowflake.of(value.get().value2))).isPresent()
		    && channelExist(discordClient.getGuildById(Snowflake.of(value.get().value2)).block(),
			    value.get().value1) ? 0 : 2;
	}
	if (isValid > 0) {
	    if (isValid == 2) {
		memory.linkedChannels.remove(value.get());
	    }
	    return Optional.empty();
	}
	return value;
    }

    /**
     * @param guild
     * @param channelId
     * @return true si un channel ayant channelId pour ID existe dans guild. False
     *         sinon
     */
    private boolean channelExist(Guild guild, Long channelId) {
	return BotUtils.returnOptional(guild.getChannelById(Snowflake.of(channelId))).isPresent();
    }

    @Override
    public String getSyntaxFile() {
	return "administrator/wormhole.json";
    }

}
