package net.gunivers.gunibot;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.gateway.retry.RetryOptions;
import net.gunivers.gunibot.auto_vocal_channel.VoiceChannelCreator;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.datas.DataCenter;
import net.gunivers.gunibot.event.Events;
import reactor.core.scheduler.Schedulers;

public class BotInstance {

	private DataCenter dataCenter; // Système controlant toute les données du bot
	private LinkedBlockingQueue<GuildCreateEvent> guildQueue; // File d'attente des serveurs à charger
	private DiscordClient botClient;

	/**
	 * créé le bot à partir du token donné et l'initialise.
	 *
	 * @param token le token nécessaire à la connection sur discord du bot.
	 */
	public BotInstance(String token) {
		guildQueue = new LinkedBlockingQueue<>();

		if (token == null) {
			throw new IllegalArgumentException("Vous devez indiquez votre token en argument !");
		} else {
			System.out.println("Liste des commandes chargées :");
			Command.loadCommands();
			System.out.println("Nombre de commandes chargées : " + Command.commands.size());
			DiscordClientBuilder builder = new DiscordClientBuilder(token);
			builder.setRetryOptions(new RetryOptions(Duration.ofSeconds(30), Duration.ofMinutes(1), 1000, Schedulers.single())); // En cas de déconnection imprévue, tente de se reconnecter à l'infini
			builder.setInitialPresence(Presence.doNotDisturb(Activity.watching("Démarrage...")));
			botClient = builder.build();

			EventDispatcher dispatcher = botClient.getEventDispatcher();

			// Initializing Events (nécessaire pour l'initialisation du bots et de ses données)
			dispatcher.on(ReadyEvent.class).take(1).subscribe(event -> { //code éxécuté qu'une seule fois lorsque le bot est connecté à discord
				dataCenter = new DataCenter(event);
				while (guildQueue.size() > 0) {
					try {
						dataCenter.addGuild(guildQueue.take().getGuild());
					} catch (InterruptedException e) {
						System.err.println("Guild queue is empty and interrupted !");
					}
				}
				botClient.updatePresence(Presence.online(Activity.listening("/help"))).subscribe();
				Events.initialize(event);
			});

			dispatcher.on(GuildCreateEvent.class).subscribe(event ->
			{
				if (dataCenter == null)
				{
					try { guildQueue.put(event); } catch (InterruptedException e)
					{
						System.err.println("Guild queue is full and interrupted ! Skipping guild event !");
					}
				}
				else
					dataCenter.addGuild(event.getGuild());
			});

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

}
