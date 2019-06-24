package net.gunivers.gunibot.core;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Système d'horloge synchronisé.
 * Permet de chronométré un grand nombre de valeurs tout en conservant une synchronisation parfaite par rapport à son temps de départ.<br>
 * Chaque valeur enregistré est incrémenté à chaque seconde une fois le système démarré.<br>
 * Selon le temps d'éxécution utilisé pour incrémenté toutes les valeurs, le temps morts ({@link Thread#sleep(long, int)}) est réduit afin qu'une boucle du système fasse exactement 1 seconde.
 * La précision de la synchronisation dépend de la précision de {@link Instant#now()} (qui dépend lui même de la précision de la JVM).<br>
 * @param I Type de l'ID à utilisé pour enregistré les valeurs à incrémenté sans les mélangés (Il est recommandé que le type utilisé implémente la fonction {@link Object#equals(Object)} ou qu'il soit primitif).
 * @author Syl2010
 * @see Instant#now()
 */
public class ClockSystem<I> {

	private Thread thread;
	private ConcurrentHashMap<I,Long> timers;
	private boolean stop;

	public boolean debug = false;

	/**
	 * Crée un sytème d'horloge synchronisé
	 */
	public ClockSystem() {
		this("ClockSystem Thread");
	}

	/**
	 * Crée un système d'horloge synchronisé et modifie le nom de son thread
	 * @param thread_name le nom du thread (utile pour le débugging dans la JVM)
	 */
	public ClockSystem(String thread_name) {
		thread = new Thread(() -> {
			try {
				Thread.sleep(1000);
				Instant time_sync = Instant.now();

				while(!stop) {
					for(I id : timers.keySet()) {
						timers.replace(id, timers.get(id) + 1);
					}
					Instant now = Instant.now();
					long millis_delay = now.toEpochMilli() - time_sync.toEpochMilli();
					int nano_delay = now.getNano() - time_sync.getNano();
					int trunc_nano_delay = Math.round(millis_delay % 1000 * 1_000_000) - nano_delay;

					if(debug) {
						System.out.println("millis delay : "+ millis_delay +" ms");
						System.out.println("nano delay : "+ nano_delay +" / 999999999 (<1s)");
						System.out.println("nano delay : "+ trunc_nano_delay +" / 999999 (<1ms)");
						System.out.println("-----------------------------------------------------------------");
					}

					if(millis_delay <= 1000) {
						Thread.sleep(1000 - millis_delay, trunc_nano_delay);
					}

					time_sync = time_sync.plusSeconds(1);
				}
			} catch (InterruptedException e) {
				stop = true;
				Thread.currentThread().interrupt();
			}
		}, thread_name);
		timers = new ConcurrentHashMap<>();
		stop = true;
	}

	/**
	 * Change le thread du système en démon (daemon) ou en thread utilisateur.
	 * Lorsque tout les threads utilisateurs sont morts, les démons sont automatiquements tué.
	 * @param daemon active ou non le mode démon du thread
	 * @see Thread#setDaemon(boolean)
	 */
	public void setDaemon(boolean daemon) {
		thread.setDaemon(daemon);
	}

	/**
	 * Indique si le thread du sytème est un démon (daemon) ou un thread utilisateur.
	 * @return {@code true} si le thread est un démon, {@code false} si c'est un thread utilisateur.
	 */
	public boolean isDaemon() {
		return thread.isDaemon();
	}

	/**
	 * Créez une entrée avec l'ID donnée et démarrant à 0
	 * @param id ID de l'entrée
	 * @return l'ancienne valeur de l'ID indiqué (peut être null si l'entrée n'existait pas)
	 */
	public Long putTimer(I id) {
		return putTimer(id, 0);
	}

	/**
	 * Créez une entrée avec l'ID donnée et démarrant à la valeur indiqué
	 * @param id ID de l'entrée
	 * @param value la valeur de départ de cet entrée
	 * @return l'ancienne valeur de l'ID indiqué (peut être null si l'entrée n'existait pas)
	 */
	public Long putTimer(I id, long value) {
		return timers.put(id, value);
	}

	/**
	 * Ajoute au système toute les entrées enregistrés dans la map donnée.
	 * @param map la map contenant tout les entrées à ajouté (les anciennes entrée avec un même ID qu'une des nouvelles sont écrasés)
	 */
	public void putAllTimer(Map<I,Long> map) {
		timers.putAll(map);
	}

	/**
	 * Retire l'entrée ayant l'ID indiqué du système.
	 * @param id ID de l'entrée à supprimé
	 * @return la valeur que possédé l'entrée ayant cet ID (peut être null si l'entrée n'existait pas)
	 */
	public Long removeTimer(I id) {
		return timers.remove(id);
	}

	/**
	 * Récupère la valeur de l'entrée possédant l'ID indiqué.
	 * @param id ID de l'entrée.
	 * @return la valeur enregistré sous l'ID indiqué (peut être null si l'entrée n'existe pas).
	 */
	public Long get(I id) {
		return timers.get(id);
	}

	/**
	 * Démarre l'horloge du système.
	 * Utilisez {@link #stop()} pour arrétez le système.
	 */
	public void start() {
		stop = false;
		thread.start();
	}

	/**
	 * Arrète l'horloge du système.
	 * L'horloge s'arrètera à la fin de sa boucle (maximum 1 seconde après).
	 */
	public void stop() {
		stop = true;
	}

	/**
	 * Arrète l'horloge du système.
	 * Si le thread de l'horloge est en temps mort ({@link Thread#sleep(long, int)}), il est directement interrompue.
	 */
	public void shutdown() {
		stop();
		thread.interrupt();
	}
}
