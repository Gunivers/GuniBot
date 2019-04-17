package net.gunivers.gunibot.syl2010.lib.parser;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.NoSuchElementException;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;

public class Parser {

	/**
	 * Permet d'obtenir un flux de serveur à partir d'une chaine de caractère pouvant être un
	 * ID, ou un nom.
	 *
	 * @param s_guild le string du serveur à analysé.
	 * @param bot le bot en cours d'éxécution (pour la recherche du serveur visé).
	 * @return Renvoit le flux de serveur.
	 */
	public static Flux<Guild> parseGuild(String s_guild, DiscordClient bot) {
		Flux<Guild> guilds;

		try {
			guilds = bot.getGuildById(Snowflake.of(s_guild)).flux();
		} catch (NumberFormatException e) {
			guilds = bot.getGuilds().filter(int_guild -> int_guild.getName().equalsIgnoreCase(s_guild)).single().flux();
		}

		return guilds;
	}

	/**
	 * Permet d'obtenir un flux d'utilisateur à partir d'une chaine de caractère pouvant être un
	 * ID, une mention, ou un nom.
	 *
	 * @param s_user le string de l'utilisateur à analysé.
	 * @param bot le bot en cours d'éxécution (pour la recherche de l'utilisateur visé).
	 * @return Renvoit le flux des utilisateurs.
	 */
	public static Flux<User> parseUser(String s_user, DiscordClient bot) {
		Flux<User> users;

		try {
			users = bot.getUserById(Snowflake.of(s_user.replace("<@", "").replace(">", "").replace("!", ""))).flux();
		} catch (NumberFormatException e) {
			users = bot.getUsers().filter(int_user -> int_user.getUsername().equalsIgnoreCase(s_user));
		}

		return users;
	}

	/**
	 * Permet d'obtenir un flux de membre à partir d'une chaine de caractère pouvant être un
	 * ID, une mention, ou un nom.
	 *
	 * @param s_member le string du membre à analysé.
	 * @param server le serveur où se trouve le membre.
	 * @return Renvoit le flux de membre.
	 */
	public static Flux<Member> parseMember(String s_member, Guild server) {
		Flux<Member> members;

		try {
			members = server.getMemberById(Snowflake.of(s_member.replace("<@", "").replace(">", "").replace("!", ""))).flux();
		} catch (NumberFormatException e) {
			members = server.getMembers().filter(int_member -> (int_member.getDisplayName().equalsIgnoreCase(s_member) || int_member.getUsername().equalsIgnoreCase(s_member)));
		}

		return members;
	}

	/**
	 * Permet d'obtenir un flux de channel à partir d'une chaine de caractère pouvant être
	 * un ID, une mention, ou un nom.
	 *
	 * @param s_channel le string du channel à analysé.
	 * @param server    le serveur de la commande (pour la recherche du channel
	 *                  visé).
	 * @return Renvoit un flux de channel.
	 */
	public static Flux<GuildChannel> parseChannel(String s_channel, Guild server) {
		Flux<GuildChannel> channels;

		try {
			channels = server.getChannelById(Snowflake.of(s_channel.replace("<#", "").replace(">", ""))).flux();
		} catch (NumberFormatException e) {
			channels = server.getChannels().filter(int_text_channel -> int_text_channel.getName().equalsIgnoreCase(s_channel));
		}

		return channels;
	}

	/**
	 * Permet d'obtenir un flux de catégorie à partir d'une chaine de caractère pouvant être
	 * un ID, ou un nom.
	 *
	 * @param s_category le string de la catégorie à analysé.
	 * @param server    le serveur de la commande (pour la recherche de la catégorie
	 *                  visé).
	 * @return Renvoit un flux de catégorie.
	 */
	public static Flux<Category> parseCategory(String s_category, Guild server) {
		return parseChannel(s_category, server).ofType(Category.class);
	}

	/**
	 * Permet d'obtenir un flux de channel textuel à partir d'une chaine de caractère pouvant être
	 * un ID, une mention, ou un nom.
	 *
	 * @param s_text_channel le string du channel textuel à analysé.
	 * @param server    le serveur de la commande (pour la recherche du channel
	 *                  visé).
	 * @return Renvoit un flux de channel textuel.
	 */
	public static Flux<TextChannel> parseTextChannel(String s_text_channel, Guild server) {
		return parseChannel(s_text_channel, server).ofType(TextChannel.class);
	}

	/**
	 * Permet d'obtenir un flux de channel vocal à partir d'une chaine de caractère pouvant être
	 * un ID ou un nom.
	 *
	 * @param s_voice_channel le string du channel vocal à analysé.
	 * @param server    le serveur de la commande (pour la recherche du channel vocal
	 *                  visé).
	 * @return Renvoit un flux de channels vocal.
	 */
	public static Flux<VoiceChannel> parseVoiceChannel(String s_voice_channel, Guild server) {
		return parseChannel(s_voice_channel, server).ofType(VoiceChannel.class);
	}

	/**
	 * Permet d'obtenir un flux de role à partir d'une chaine de caractère pouvant être un
	 * ID, une mention, ou un nom.
	 *
	 * @param s_role le string du role à analysé.
	 * @param server le serveur de la commande (pour la recherche du role visé).
	 * @return Renvoit le flux de role.
	 */
	public static Flux<Role> parseRole(String s_role, Guild server) {
		Flux<Role> roles;

		try {
			roles = server.getRoleById(Snowflake.of(s_role.replace("<@&", "").replace(">", ""))).flux();
		} catch (NumberFormatException e) {
			roles = server.getRoles().filter(int_role -> int_role.getName().equalsIgnoreCase(s_role));
		}

		return roles;
	}

	/**
	 * Permet d'obtenir une durée à partir d'une chaine de caractère composé de nombres et des lettres D, H, M et S.<br>
	 * D = day (jour)<br>
	 * H = hour (heure)<br>
	 * M = minute (minute)<br>
	 * S = second (seconde)<br>
	 * Chacun de ces modifeurs ci-dessus permet de précisez la durée du nombre précédent.<br>
	 * Exemple : 7D = 7 jours, 5H32M = 5 Heures et 32 minutes, 75S = 75 secondes (1 minutes et 15 secondes)<br>
	 * Les lettres peuvent être minuscules
	 *
	 * @param s_duration le string de la durée à analysé.
	 * @return Renvoit une durée (Duration).
	 * @throws DurationParsingException Si le paramètre ne peut être parsé (caractère invalide).
	 */
	public static Duration parseDuration(String s_duration) throws DurationParsingException {
		String temp_num = "";
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;

		for(char car:s_duration.toCharArray()) {
			if (Character.isDigit(car)) {
				temp_num += car;

			} else {
				if ((car == 'D') || (car == 'd')) {
					day += Integer.parseInt(temp_num);
				} else if ((car == 'H') || (car == 'h')) {
					hour += Integer.parseInt(temp_num);
				} else if ((car == 'M') || (car == 'm')) {
					minute += Integer.parseInt(temp_num);
				} else if ((car == 'S') || (car == 's')) {
					second += Integer.parseInt(temp_num);
				} else {
					throw new DurationParsingException("le caractère '"+car+"' ne fait pas partie des caractères autorisés par le parseur de temps\n"+helpDurationFormat());
				}

				temp_num = "";
			}
		}
		return Duration.ofSeconds(second + 60*minute + 60*60*hour + 24*60*60*day);
	}

	/**
	 * Aide sur le parseur de durée indiqué dans DurationParsingException
	 * @return aide sur le parseur
	 */
	public static String helpDurationFormat() {
		String output = "Format du parseur : `xxDxxHxxMxxS`\n";
		output += "xx = un nombre\n";
		output += "D = le nombre précédent sont des jours\n";
		output += "H = le nombre précédent sont des heures\n";
		output += "M = le nombre précédent sont des minutes\n";
		output += "S = le nombre précédent sont des secondes\n";
		output += "Exemple : `2D20M120S` => 2 jours, 20 minutes et 120 secondes (2 jours et 22 minutes)";

		return output;
	}

	/**
	 * Aide sur le parseur de date indiqué dans DateParsingException
	 * @return aide sur le parseur
	 */
	public static String helpDateFormat() {
		String output = "Format du parseur : `dd/MM/uuuu`\n";
		output += "dd = le jour du mois\n";
		output += "MM = le mois de l'année\n";
		output += "uuuu = l'année\n";
		output += "Exemple : `05/06/2018` => le 5 juin de l'année 2018";

		return output;
	}

	/**
	 * Aide sur le parseur d'heure indiqué dans TimeParsingException
	 * @return aide sur le parseur
	 */
	public static String helpTimeFormat() {
		String output = "Format du parseur : `HH:mm:ss ou HH:mm`\n";
		output += "HH = l'heure du jour\n";
		output += "mm = les minute de l'heure\n";
		output += "ss = les secondes de la minute\n";
		output += "Exemple : `17:43` => 17H43";

		return output;
	}

	/**
	 * Aide sur le parseur de date et d'heure indiqué dans DateTimeParsingException
	 * @return aide sur le parseur
	 */
	public static String helpDateTimeFormat() {
		String output = "Format du parseur : `dd/MM/uuuuTHH:mm:ss ou dd/MM/uuuuTHH:mm`\n";
		output += "dd = le jour du mois\n";
		output += "MM = le mois de l'année\n";
		output += "uuuu = l'année\n";
		output += "HH = l'heure du jour\n";
		output += "mm = les minute de l'heure\n";
		output += "ss = les secondes de la minute\n";
		output += "Exemple : `25/12/2018T23:55:00` => le 25 décembre 2018 à 23H55\n";
		output += "Exemple : `24/12/2018T12H:00` => le 24 décembre 2018 à midi (12:00:00)";

		return output;
	}

	/**
	 * Transforme la durée indiqué en une chaine de caractère complète (en français)
	 * @param durée la durée à transformé
	 * @return la durée lisible complète, en français
	 */
	public static String toString(Duration durée) {
		String output = "";
		long time = durée.abs().getSeconds();

		long second = time % 60;
		long minute = time % 3600 / 60;
		long hour = time % 86400 / 3600;
		long day = time / 86400;

		if(day == 1) {
			output += "1 jour ";
		} else if (day > 1) {
			output += day + " jours ";
		}

		if ((day > 0) && (((hour > 0) && ((minute <= 0) && (second <= 0))) ^ ((minute > 0) && ((hour <= 0) && (second <= 0))) ^ ((second > 0) && ((minute <= 0) && (hour <= 0))))) {
			output += "et ";
		}

		if(hour == 1) {
			output += "1 heure ";
		} else if (hour > 1) {
			output += hour + " heures ";
		}

		if ((hour > 0) && ((minute > 0) ^ (second > 0))) {
			output += "et ";
		}

		if(minute == 1) {
			output += "1 minute ";
		} else if (minute > 1) {
			output += minute + " minutes ";
		}

		if ((minute > 0) && (second > 0)) {
			output += "et ";
		}

		if(second == 1) {
			output += "1 seconde";
		} else if (second > 1) {
			output += second + " secondes";
		}

		return output.trim();
	}

	/**
	 * Transforme la date indiqué en une chaine de caractère complète (en français)
	 * @param date la date à transformé
	 * @return la date lisible complète, en français
	 */
	public static String toString(LocalDate date) {
		String jour = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + date.getDayOfMonth();
		String mois = date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
		int annee = date.getYear();

		return jour + " " + mois + " " + annee;
	}

	/**
	 * Transforme l'heure indiqué en une chaine de caractère complète (en français)
	 * @param time l'heure à transformé
	 * @return l'heure lisible complète, en français
	 */
	public static String toString(LocalTime time) {
		String output = "";

		int heure = time.getHour();
		int minute = time.getMinute();
		int seconde = time.getSecond();

		if(heure < 10) {
			output += "0"+heure + "H";
		} else {
			output += heure + "H";
		}

		if (minute < 10) {
			output += "0"+minute;
		} else {
			output += minute;
		}

		if (seconde == 1) {
			output += " et 1 seconde";
		} else if (seconde > 1) {
			output += " et " + seconde + " secondes";
		}
		return output;
	}

	/**
	 * Transforme la date et l'heure indiqué en une chaine de caractère complète (en français)
	 * @param date_time la date et l'heure à transformé
	 * @return la date et l'heure lisible complète, en français
	 */
	public static String toString(LocalDateTime date_time) {
		return toString(date_time.toLocalDate()) + " à "+toString(date_time.toLocalTime());
	}

	/**
	 * Renvoit le format de la date (dd/MM/uuuu ou dd/MM/uu)
	 * @return le format de la date
	 */
	public static DateTimeFormatter DatePattern() {
		return new DateTimeFormatterBuilder()
				.appendValue(DAY_OF_MONTH, 2)
				.appendLiteral('/')
				.appendValue(MONTH_OF_YEAR, 2)
				.appendLiteral('/')
				.appendValue(YEAR, 2, 4, SignStyle.NEVER)
				.toFormatter();
	}

	/**
	 * Renvoit le format de l'heure (HH:mm:ss.SS ou HH:mm:ss ou HH:mm)
	 * @return le format de l'heure
	 */
	public static DateTimeFormatter TimePattern() {
		return DateTimeFormatter.ISO_LOCAL_TIME;
	}

	/**
	 * Renvoit le format de la date et de l'heure (dd/MM/uuuuTHH:mm:ss.SS)
	 * @return le format de la date et de l'heure
	 */
	public static DateTimeFormatter DateTimePattern() {
		return new DateTimeFormatterBuilder().append(DatePattern()).appendLiteral('T').append(TimePattern()).toFormatter();
	}

	/**
	 * Permet de récupéré la date à partir d'une chaine de caractère.
	 * Le format est le suivant : dd/MM/uuuu ou dd/MM/uu (05/06/1998)
	 * @param s_date la chaine de caractère à analysé
	 * @return la date correspondant
	 * @throws DateParsingException si la syntaxe de s_date est invalide
	 */
	public static LocalDate parseDate(String s_date) throws DateParsingException {
		try {
			return LocalDate.parse(s_date, DatePattern());
		} catch(DateTimeParseException e) {
			throw new DateParsingException("Syntaxe invalide, impossible de parsé '"+s_date+"', vérifiez votre syntaxe !\n"+helpDateFormat(),e);
		}

	}

	/**
	 * Permet de récupéré l'heure à partir d'une chaine de caractère
	 * Le format est le suivant : HH:mm:ss.SS ou HH:mm:ss ou HH:mm (06:32:42.65)
	 * @param s_time la chaine de caractère à analysé
	 * @return l'heure correspondant
	 * @throws TimeParsingException si la syntaxe de s_time est invalide
	 */
	public static LocalTime parseTime(String s_time) throws TimeParsingException {
		try {
			return LocalTime.parse(s_time, TimePattern());
		} catch(DateTimeParseException e) {
			throw new TimeParsingException("Syntaxe invalide, impossible de parsé '"+s_time+"', vérifiez votre syntaxe !\n"+helpTimeFormat(),e);
		}
	}

	/**
	 * Permet de récupéré la date et l'heure à partir d'une chaine de caractère.
	 * Le format rassemble le format de {@link #parseDate(String)} et de {@link #parseTime(String)} séparé par le caractère 'T' (05/06/1998T06:32:42.65).
	 * Si l'heure n'est pas indiqué, il sera considée comme étant nul (00:00:00.00).
	 * @param s_datetime la chaine de caractère à analysé
	 * @return la date et l'heure correspondant
	 * @throws DateTimeParsingException si la syntaxe de la date ou de l'heure est invalide
	 */
	public static LocalDateTime parseDateTime(String s_datetime) throws DateTimeParsingException {
		try {
			return LocalDateTime.parse(s_datetime, DateTimePattern());
		} catch(DateTimeParseException e) {
			throw new DateTimeParsingException("Syntaxe invalide, impossible de parsé '"+s_datetime+"', vérifiez votre syntaxe !\n"+helpDateTimeFormat());
		}
	}

	/**
	 * Test un flux d'entité (pour le système de parsing). Si un flux est vide ou contient plus d'une entité, un {@link ObjectParsingException} est levé.
	 * @param entity_flux le flux à testé.
	 * @return l'entité du flux.
	 * @throws ObjectParsingException si le flux est vide ou contient plus d'une entité.
	 */
	public static <T extends Entity> T singleEntity(Flux<T> entity_flux) throws ObjectParsingException {
		String type_unknown;
		String type_multiple;

		if (entity_flux.any(entity -> entity instanceof Guild).block()) {
			type_unknown = "Serveur inconnu !";
			type_multiple = "Plusieurs serveurs ont le même nom, veuillez utilisez l'ID du serveur à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof Member).block()) {
			type_unknown = "Membre inconnu !";
			type_multiple = "Plusieurs membres ont le même nom, veuillez utilisez l'ID du membre ou une mention à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof User).block()) {
			type_unknown = "Utilisateur inconnu !";
			type_multiple = "Plusieurs serveurs ont le même nom, veuillez utilisez l'ID de l'utilisateur ou une mention à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof Category).block()) {
			type_unknown = "Catégorie inconnue !";
			type_multiple = "Plusieurs catégories ont le même nom, veuillez utilisez l'ID de la catégorie à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof TextChannel).block()) {
			type_unknown = "Channel textuel inconnu !";
			type_multiple = "Plusieurs channels textuels ont le même nom, veuillez utilisez l'ID du channel ou une mention à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof VoiceChannel).block()) {
			type_unknown = "Channel vocal inconnu !";
			type_multiple = "Plusieurs channels vocaux ont le même nom, veuillez utilisez l'ID du channel à la place de son nom !";

		} else if (entity_flux.any(entity -> entity instanceof Role).block()) {
			type_unknown = "Role inconnue !";
			type_multiple = "Plusieurs roles ont le même nom, veuillez utilisez l'ID du role ou une mention à la place de son nom !";

		} else {
			type_unknown = "Entité inconnue !";
			type_multiple = "Plusieurs entités ont le même nom, veuillez utilisez l'ID de l'entité ou une mention à la place de son nom !";
		}

		try {
			return entity_flux.single().block();
		} catch (NoSuchElementException e) {
			throw new ObjectParsingException(type_unknown, e);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjectParsingException(type_multiple, e);
		}
	}
}
