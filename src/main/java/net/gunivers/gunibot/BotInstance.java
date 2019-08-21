package net.gunivers.gunibot;

import java.time.Duration;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.gateway.retry.RetryOptions;
import net.gunivers.gunibot.audio.Audio;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.datas.DataCenter;
import net.gunivers.gunibot.event.Events;
import net.gunivers.gunibot.feature.auto_vocal_channel.VoiceChannelCreator;
import reactor.core.scheduler.Schedulers;

public class BotInstance {

	private DataCenter dataCenter; // Système controlant toute les données du bot
	private DiscordClient botClient;
	private BotConfig config;

	/**
	 * créé le bot à partir du token donné et l'initialise.
	 *
	 * @param BotConfig la configuration du bot.
	 */
	public BotInstance(BotConfig config) {
		this.config = config;

		if (config.token == null) {
			throw new IllegalArgumentException("Vous devez indiquez votre token en argument !");
		} else {

			Audio.init();

			System.out.println("Build Discord Client...");
			DiscordClientBuilder builder = new DiscordClientBuilder(config.token);
			builder.setRetryOptions(new RetryOptions(Duration.ofSeconds(30), Duration.ofMinutes(1), 1000, Schedulers.single())); // En cas de déconnection imprévue, tente de se reconnecter à l'infini
			builder.setInitialPresence(Presence.doNotDisturb(Activity.watching("Démarrage...")));
			botClient = builder.build();

			System.out.println("Initialize Data Center...");
			dataCenter = new DataCenter(this);

			System.out.println("Loading commands...");
			System.out.println("Liste des commandes chargées :");
			Command.loadCommands(dataCenter, botClient);
			dataCenter.loadSystems();
			dataCenter.loadOldSerializer();
			System.out.println("Nombre de commandes chargées : " + Command.commands.size());

			EventDispatcher dispatcher = botClient.getEventDispatcher();

			// Initializing Events (nécessaire pour l'initialisation du bots et de ses données)
			dispatcher.on(ReadyEvent.class).take(1).subscribe(event -> { //code éxécuté qu'une seule fois lorsque le bot est connecté à discord
				System.out.println("Initialize Events...");
				botClient.updatePresence(Presence.idle(Activity.listening("Initializing events..."))).subscribe();
				Events.initialize(event);
				botClient.updatePresence(Presence.online(Activity.listening("/help"))).subscribe();
			});

			dispatcher.on(GuildCreateEvent.class).subscribe(event -> dataCenter.addGuild(event.getGuild()));

			//debug
			//dispatcher.on(MessageCreateEvent.class).map(MessageCreateEvent::getMessage).subscribe(msg -> {System.out.println("Message Received : "+msg.getContent().orElse("NO_CONTENT"));});

			VoiceChannelCreator.init(botClient);
		}
	}

	/**
	 * Connecte le bot en bloquant le thread
	 */
	public void loginBlock() {
		if(!botClient.isConnected()) botClient.login().block();
		else throw new IllegalStateException("The client is already connected!");
	}

	/**
	 * Connecte le bot en libérant le thread.<br>
	 * ATTENTION : Tout les threads créé par le bot sont des daemons. Si le thread principal meurt, la jvm s'arrète !
	 */
	public void loginSubscribe() {
		if(!botClient.isConnected()) botClient.login().subscribe();
		else throw new IllegalStateException("The client is already connected!");
	}

	public void shutdown() {
		if(botClient.isConnected()) dataCenter.shutdown();
		else throw new IllegalStateException("The client is not connected!");
	}

	public DataCenter getDataCenter() { return dataCenter; }

	public DiscordClient getBotClient() { return botClient; }

	public BotConfig getConfig() { return config; }

}
