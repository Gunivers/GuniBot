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
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;
import net.gunivers.gunibot.utils.tuple.Tuple3;
import net.gunivers.gunibot.utils.tuple.Tuple5;
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
	    OldSerializer s = new OldSerializer();
	    s.put("wormhole", linkedChannels);
	    return s;
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
	List<Long> id = memory.linkedChannels.keySet().stream().map(t -> t._1).collect(Collectors.toList());
	discordClient.getEventDispatcher().on(MessageCreateEvent.class)
		.filter(mce -> id.contains(mce.getMessage().getChannelId().asLong())).subscribe(e2 -> copyMessage(e2));
    }

    /**
     * Liste les différents Sender Channels
     * 
     * @param e
     */
    public void list(MessageCreateEvent e) {
    final String messageIfEmpty = "Aucun wormhole existant.";
	final Field list = new Field("‌‌Sender Channels");
	// On trie et ajoute les index en préfixe des éléments de la liste
	String text = sortSetAndZipWithIndex(memory.linkedChannels.keySet()).map(t -> {
	    Guild g = e.getClient().getGuildById(Snowflake.of(t._3)).block();
	    return t._1 + ". #" + g.getChannelById(Snowflake.of(t._2)).block().getName() + " in **" + g.getName()
		    + "**";
	}).collect(Collectors.joining("\n"));
	// On envoie l'embed
	list.setValue(text.isEmpty() ? messageIfEmpty : text);
	constructAndSendEmbed(list, e);
    }

    /**
     * Liste les différents Receiver Channels
     * 
     * @param e
     */
    public void subList(MessageCreateEvent e, List<String> args) {
	// On récupère le salon correspondant à l'index donné
	Optional<Tuple3<Integer, Long, Long>> foundValue = sortSetAndZipWithIndex(memory.linkedChannels.keySet())
		.filter(t -> t._1 == Integer.parseInt(args.get(0))).findFirst();
	if (!foundValue.isPresent()) {
	    e.getMessage().getChannel().block().createMessage("Number " + args.get(0) + " not valid!").subscribe();
	} else {

	    Tuple2<Long, Long> foundChannel = Tuple.newTuple(foundValue.get()._2, foundValue.get()._3);
	    Guild guild = e.getClient().getGuildById(Snowflake.of(foundChannel._2)).block();
	    GuildChannel channel = guild.getChannelById(Snowflake.of(foundChannel._1)).block();
	    final Field list = new Field("‌‌Receiver Channels of #" + channel.getName() + " in " + guild.getName());

	    // On récupère les salons liés à celui trouvé
	    Set<Tuple2<Long, Long>> children = getChildren(channel.getId().asLong(), guild.getId().asLong());

	    // On trie et ajoute les index en préfixe des éléments de la liste
	    String text = sortSetAndZipWithIndex(children).map(t -> {
		Guild g = e.getClient().getGuildById(Snowflake.of(t._3)).block();
		return t._1 + ". #" + g.getChannelById(Snowflake.of(t._2)).block().getName() + " in **" + g.getName()
			+ "**";
	    }).collect(Collectors.joining("\n"));
	    // On envoie l'embed
	    list.setValue(text);
	    constructAndSendEmbed(list, e);
	}
    }

    /**
     * Construit l'embed utilisé dans la classe
     * 
     * @param field
     * @param e
     */
    private void constructAndSendEmbed(Field field, MessageCreateEvent e) {
	EmbedBuilder eb = new EmbedBuilder(e.getMessage().getChannel().block(), null, null);
	eb.addField(field);
	eb.setColor(new Color(255, 87, 34));
	eb.setRequestedBy(e.getMember().get());
	eb.buildAndSend();
    }

    /**
     * @param set
     * @return le set trié en fonction des Longs et zip avec l'index
     */
    private Stream<Tuple3<Integer, Long, Long>> sortSetAndZipWithIndex(Set<Tuple2<Long, Long>> set) {
	List<Tuple2<Long, Long>> temp = set.stream().sorted((t1, t2) -> (Long.toString(t1._1) + Long.toString(t1._2))
		.compareTo(Long.toString(t2._1) + Long.toString(t2._2))).collect(Collectors.toList());
	return ListUtils.mapWithIndex(temp, (tuple, i) -> Tuple.newTuple(i, tuple._1, tuple._2)).stream();
    }

    public void link(MessageCreateEvent e, List<String> args) {
	// Récupère les salons concernés par les arguments spécifiés
	Flux<GuildChannel> channels = Main.getBotInstance().getBotClient().getGuilds()
		.flatMap(g -> g.getChannels().filter(c -> c instanceof MessageChannel
			&& (c.getId().asString().equals(args.get(0)) || c.getId().asString().equals(args.get(1)))));

	// Si il y a bien deux salons
	if (channels.count().block() == 2) {

	    // Séparation des deux salons
	    GuildChannel channel1 = channels.blockFirst().getId().asString().equals(args.get(0)) ? channels.blockFirst()
		    : channels.blockLast();
	    GuildChannel channel2 = channels.blockLast().getId().asString().equals(args.get(1)) ? channels.blockLast()
		    : channels.blockFirst();

	    // Si le lien existe déjà
	    if (getChildren(channel1.getId().asLong(), channel1.getGuildId().asLong()).stream()
		    .filter(t -> t._1 == channel2.getId().asLong() && t._2 == channel2.getGuildId().asLong())
		    .count() == 0) {

		// Envoie d'un message de confirmation de lien
		String message = "Confirm link between " + channel1.getMention() + " in "
			+ channel1.getGuild().block().getName() + " and " + channel2.getMention() + " in "
			+ channel2.getGuild().block().getName() + "?";
		sendTemporaryValidationMessage(e.getMessage().getChannel().block(), message,
			Tuple.newTuple(channel1, channel2, e.getMember().get().getId()),
			(t, r) -> onThumbEmojiAdded(t, r));
	    } else {
		e.getMessage().getChannel().block().createMessage("The specified link already exists!").subscribe();
	    }

	} else {
	    e.getMessage().getChannel().block().createMessage("Specified channels not valid!").subscribe();
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
    private void sendTemporaryValidationMessage(MessageChannel channel, String message,
	    Tuple3<GuildChannel, GuildChannel, Snowflake> linkInfos,
	    BiConsumer<Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable>, ReactionAddEvent> onValid) {
	Mono<Message> msg = channel.createMessage(message);
	Message m = msg.block();
	m.addReaction(ReactionEmoji.unicode("👍")).subscribe();
	m.addReaction(ReactionEmoji.unicode("👎")).subscribe();

	// Auto-suppression au bout de TIME millisecondes
	Disposable disp = Mono.just(m).delayElement(Duration.ofMillis(TIME)).subscribe(ms -> ms.delete().subscribe());

	// Ajout des boutons et liaison des events
	Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos = Tuple5.newTuple(linkInfos._1,
		linkInfos._2, linkInfos._3, m, disp);
	Events.REACTION_ADDED.on(m, ReactionEmoji.unicode("👍"), e -> onValid.accept(infos, e));
	Events.REACTION_ADDED.on(m, ReactionEmoji.unicode("👎"), t -> onThumbDownEmojiAdded(m, disp));
    }

    /**
     * Propose la suppression du lien entre 2 salons
     * 
     * @param e
     * @param args
     */
    public void remove(MessageCreateEvent e, List<String> args) {
	// On récupère le salon correspondant au premier index donné
	Optional<Tuple3<Integer, Long, Long>> foundValue = sortSetAndZipWithIndex(memory.linkedChannels.keySet())
		.filter(t -> t._1 == Integer.parseInt(args.get(0))).findFirst();
	if (!foundValue.isPresent()) {
	    e.getMessage().getChannel().block().createMessage("Number " + args.get(0) + " not valid!").subscribe();
	} else {

	    Tuple2<Long, Long> foundChannel = Tuple.newTuple(foundValue.get()._2, foundValue.get()._3);
	    Guild guild = e.getClient().getGuildById(Snowflake.of(foundChannel._2)).block();
	    GuildChannel channel = guild.getChannelById(Snowflake.of(foundChannel._1)).block();

	    // On récupère les salons liés à celui trouvé
	    Set<Tuple2<Long, Long>> children = getChildren(channel.getId().asLong(), guild.getId().asLong());

	    // On récupère le salon ayant pour index la deuxième valeur spécifiée
	    Optional<Tuple3<Integer, Long, Long>> foundValue2 = sortSetAndZipWithIndex(children)
		    .filter(t -> t._1 == Integer.parseInt(args.get(1))).findFirst();
	    if (!foundValue2.isPresent()) {
		e.getMessage().getChannel().block().createMessage("Number " + args.get(1) + " not valid!").subscribe();
	    } else {

		Tuple2<Long, Long> foundChannel2 = Tuple.newTuple(foundValue2.get()._2, foundValue2.get()._3);
		Guild guild2 = e.getClient().getGuildById(Snowflake.of(foundChannel2._2)).block();
		GuildChannel channel2 = guild.getChannelById(Snowflake.of(foundChannel2._1)).block();

		String message = "Remove link between " + channel.getMention() + " in " + guild.getName() + " and "
			+ channel2.getMention() + " in " + guild2.getName() + "?";
		sendTemporaryValidationMessage(e.getMessage().getChannel().block(), message,
			Tuple.newTuple(channel, channel2, e.getMember().get().getId()),
			(t, r) -> onThumbEmojiAddedForRemove(t, r));
	    }
	}
    }

    private void onThumbEmojiAddedForRemove(
	    final Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos, ReactionAddEvent e) {
	if (infos != null)
	    // Si l'utilisateur corresponds à celui ayant fait la requète de liaison
	    if (e.getUserId().equals(infos._3)) {
		// On annue les events et on supprime le message
		Events.REACTION_ADDED.cancel(e.getMessage().block());
		infos._5.dispose();
		infos._4.delete().subscribe();

		e.getMessage().block().getChannel().block().createMessage("Specified link removed!").subscribe();
		// On crée la nouvelle liste des enfants sans celui supprimé
		Set<Tuple2<Long, Long>> children = getChildren(infos._1.getId().asLong(),
			infos._1.getGuildId().asLong()).stream()
				.filter(t -> !(t._1.equals(infos._2.getId().asLong())
					&& t._2.equals(infos._2.getGuildId().asLong())))
				.collect(Collectors.toSet());
		// On l'associe au parent
		Tuple2<Long, Long> parent = memory.linkedChannels.keySet().stream().filter(
			t -> t._1.equals(infos._1.getId().asLong()) && t._2.equals(infos._1.getGuildId().asLong()))
			.findFirst().get();
		if (children.isEmpty()) {
		    memory.linkedChannels.remove(parent);
		} else {
		    memory.linkedChannels.put(parent, children);
		}
	    }
    }

    /**
     * @param m Le Message à supprimé
     * @param d L'action à annuler
     */
    private void onThumbDownEmojiAdded(Message m, Disposable d) {
	m.delete().subscribe();
	d.dispose();
    }

    /**
     * @param infos Les informations nécessaires à la liaison des salons
     * @param e     L'event d'ajout de l'emote
     */
    private void onThumbEmojiAdded(final Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> infos,
	    ReactionAddEvent e) {
	if (infos != null)
	    // Si l'utilisateur corresponds à celui ayant fait la requète de liaison
	    if (e.getUserId().equals(infos._3)) {

		// On annue les events et on supprime le message
		Events.REACTION_ADDED.cancel(e.getMessage().block());
		infos._5.dispose();
		infos._4.delete().subscribe();

		// Si les guildes existent toujours au moment de la validation
		if (BotUtils.returnOptional(e.getClient().getGuildById(infos._1.getGuildId())).isPresent()
			&& BotUtils.returnOptional(e.getClient().getGuildById(infos._2.getGuildId())).isPresent()) {

		    // Si les salons existent toujours au moment de la validation
		    if (channelExist(infos._1.getGuild().block(), infos._1.getId().asLong())
			    && channelExist(infos._2.getGuild().block(), infos._2.getId().asLong())) {

			e.getMessage().block().getChannel().block().createMessage("Specified channels linked!")
				.subscribe();

			// On ajoute le channel recepteur à la liste du channel émetteur
			Set<Tuple2<Long, Long>> values = getChildren(infos._1.getId().asLong(),
				infos._1.getGuildId().asLong());
			values.add(Tuple.newTuple(infos._2.getId().asLong(), infos._2.getGuildId().asLong()));
			Optional<Tuple2<Long, Long>> optional = getKey(infos._1.getId().asLong(),
				infos._1.getGuildId().asLong());
			memory.linkedChannels.put(
				optional.isPresent() ? optional.get()
					: Tuple.newTuple(infos._1.getId().asLong(), infos._1.getGuildId().asLong()),
				values);
			// Si le channel émetteur reçoit un message, on appelle la fonction copyMessage
			discordClient.getEventDispatcher().on(MessageCreateEvent.class)
				.filter(mce -> infos._1.getId().asLong() == mce.getMessage().getChannelId().asLong())
				.subscribe(e2 -> copyMessage(e2));
		    } else {
			e.getChannel().block().createMessage("One of the two channels not exist!").subscribe();
		    }
		} else {
		    e.getChannel().block().createMessage("One of the two guilds not exist!").subscribe();
		}
	    }
    }

    /**
     * Copie le message de l'événément dans tous les channels liés au salon du
     * message
     * 
     * @param e
     */
    private void copyMessage(MessageCreateEvent e) {
	if (e.getMember().isPresent()) {

	    String message = Arrays.asList(e.getMessage().getContent().get().split(" ")).stream()
		    .map(s -> s.matches("<@\\d*>")
			    ? e.getGuild().block().getMemberById(Snowflake.of(s.substring(2, s.length() - 1))).block()
				    .getDisplayName()
			    : s)
		    .collect(Collectors.joining(" "));

	    // On récupère tous les salons liés au channel courant
	    Set<Tuple2<Long, Long>> values = getChildren(e.getMessage().getChannelId().asLong(),
		    e.getGuildId().get().asLong());

	    // On écrit le message dans les salons en question en prenant l'apparence de
	    // l'utilisateur à l'origine du message
	    values.forEach(channel -> {
		if (BotUtils
			.returnOptional(e.getClient().getGuildById(Snowflake.of(channel._2)).block()
				.getChannelById(Snowflake.of(channel._1)))
			.isPresent() && e.getMessage().getContent().isPresent()) {
		    BotUtils.sendMessageWithIdentity(e.getMember().get(),
			    (MessageChannel) e.getClient().getGuildById(Snowflake.of(channel._2)).block()
				    .getChannelById(Snowflake.of(channel._1)).block(),
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
		.filter(t -> t.equals(Tuple.newTuple(channelId, guildId))).findFirst().orElse(Tuple.newTuple(0L, 0L)),
		new HashSet<>());
	Set<Tuple2<Long, Long>> childrenFiltred = children.stream()
		.filter(t -> BotUtils.returnOptional(discordClient.getGuildById(Snowflake.of(t._2))).isPresent()
			&& channelExist(discordClient.getGuildById(Snowflake.of(t._2)).block(), t._1))
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
		.filter(t -> t.equals(Tuple.newTuple(channelId, guildId))).findFirst();
	isValid = value.isPresent() ? 0 : 1;
	if (isValid == 0) {
	    isValid = BotUtils.returnOptional(discordClient.getGuildById(Snowflake.of(value.get()._2))).isPresent()
		    && channelExist(discordClient.getGuildById(Snowflake.of(value.get()._2)).block(), value.get()._1)
			    ? 0
			    : 2;
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
