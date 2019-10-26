package net.gunivers.gunibot.core.datas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.json.JSONObject;

import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationHolder;
import net.gunivers.gunibot.core.datas.config.ConfigurationTree;
import net.gunivers.gunibot.core.datas.config.WrappedConfiguration;
import net.gunivers.gunibot.core.datas.guild.WelcomeChannelSystem;
import net.gunivers.gunibot.core.lib.parsing.commons.StringParser;
import net.gunivers.gunibot.core.utils.BotUtils;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Classe de donnée pour les objets Guild. S'occupe également du chargement et
 * de la sauvegarde de la plupart des objets discord contenu dans ce guild.
 *
 * @author Syl2010
 *
 */
public class DataGuild extends DataObject<Guild> implements ConfigurationHolder
{
	private ConcurrentHashMap<Snowflake, DataMember> dataMembers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataTextChannel> dataTextChannels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataRole> dataRoles = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataVoiceChannel> dataVoiceChannels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataCategory> dataCategories = new ConcurrentHashMap<>();

	private HashMap<String, ConfigurationTree> configuration = new HashMap<>();

	private Configuration<String> prefix;
	private WelcomeChannelSystem welcome;

	private boolean ccEnabled = false;
	private long ccActive = -1L;
	private long ccArchive = -1L;

	public boolean inBigTask = false;
	private HashMap<String, JSONObject> backups = new HashMap<>();

	/**
	 * Créer cet objet lié à ce guild.
	 *
	 * @param guild le guild lié à cet objet.
	 */
	public DataGuild(Guild guild) { super(guild); }

	{
		ConfigurationTree global = ConfigurationTree.getOrNew(this, "global");
		this.welcome = new WelcomeChannelSystem(global.createPath("welcome"));

		ConfigurationTree cmd = ConfigurationTree.getOrNew(this, "cmd");
		this.prefix = new Configuration<>(cmd.getRoot(), "prefix", new StringParser("[^ ]+"), Configuration.STRING, "/");
	}

	/**
	 * Récupère et/ou enregistre un DataObject à partir de la map de donnée, du
	 * constructeur, et de l'entité en paramètre
	 *
	 * @param <D>			 le type de DataObject
	 * @param <E>			 le type de l'entité (équivalent au type du DataObject)
	 * @param dataMap		 la map de donnée contenant ou enregistrant le
	 *						DataObject
	 * @param dataConstructor le constructeur permettant de généré ce DataObject
	 * @param entity		  l'entité lié au DataObject à récupéré
	 * @return le DataObject récupéré à partir de l'entité
	 */
	private static <D extends DataObject<E>, E extends Entity> D createDataObject(Map<Snowflake, D> dataMap, Function<E, D> dataConstructor, E entity) {
		D dataObject = dataMap.get(entity.getId());
		if (dataObject != null)
			return dataObject;
		else {
			dataObject = dataConstructor.apply(entity);
			dataMap.put(entity.getId(), dataObject);
			return dataObject;
		}
	}

	/**
	 * Créer et/ou récupère les données du membre indiqué en cache dans cet objet.
	 *
	 * @param member le membre.
	 * @return l'objet de donnée du membre.
	 */
	public DataMember getDataMember(Member member) {
		return DataGuild.createDataObject(this.dataMembers, DataMember::new, member);
	}

	/**
	 * Créer et/ou récupère les données du channel textuel indiqué en cache dans cet
	 * objet.
	 *
	 * @param textChannel le channel textuel.
	 * @return l'objet de donnée du channel textuel.
	 */
	public DataTextChannel getDataTextChannel(TextChannel textChannel) {
		return DataGuild.createDataObject(this.dataTextChannels, DataTextChannel::new, textChannel);
	}

	/**
	 * Créer et/ou récupère les données du role indiqué en cache dans cet objet.
	 *
	 * @param role le role.
	 * @return l'objet de donnée du role.
	 */
	public DataRole getDataRole(Role role) {
		return DataGuild.createDataObject(this.dataRoles, DataRole::new, role);
	}

	/**
	 * Créer et/ou récupère les données du channel vocal indiqué en cache dans cet
	 * objet.
	 *
	 * @param voiceChannel le channel vocal.
	 * @return l'objet de donnée du channel vocal.
	 */
	public DataVoiceChannel getDataVoiceChannel(VoiceChannel voiceChannel) {
		return DataGuild.createDataObject(this.dataVoiceChannels, DataVoiceChannel::new, voiceChannel);
	}

	/**
	 * Créer et/ou récupère les données de la catégorie indiqué en cache dans cet
	 * objet.
	 *
	 * @param category la catégorie.
	 * @return l'objet de donnée de la catégorie.
	 */
	public DataCategory getDataCategory(Category category) {
		return DataGuild.createDataObject(this.dataCategories, DataCategory::new, category);
	}

	// //En réflexion
	// public void clearDataMembersCache() {
	// dataMembers.clear();
	// }
	//
	// public void clearDataTextChannelsCache() {
	// dataTextChannels.clear();
	// }
	//
	// public void clearDataRolesCache() {
	// dataRoles.clear();
	// }
	//
	// public void clearDataVoiceChannelsCache() {
	// dataVoiceChannels.clear();
	// }
	//
	// public void clearDataCategoriesCache() {
	// dataCategories.clear();
	// }
	//
	// public void clearAllCache() {
	// clearDataMembersCache();
	// clearDataTextChannelsCache();
	// clearDataRolesCache();
	// clearDataVoiceChannelsCache();
	// clearDataCategoriesCache();
	// }

	/**
	 * Extrait les données json de tout les DataObject de la Map et les ajoute dans
	 * le JsonObject renvoyé par cette fonction, en assignant chaque noeud json
	 * récupéré avec l'id du DataObject
	 *
	 * @param <D>	 Type du DataObject
	 * @param <E>	 Type de l'entité lié au DataObject
	 * @param dataMap la map de données contenant tout les DataObject d'où extraire
	 *				les données Json
	 * @return Un JSONObject contenant un noeud por chaque DataObject de la map
	 */
	private static <D extends DataObject<E>, E extends Entity> JSONObject extractJsonDatas(Map<Snowflake, D> dataMap) {
	JSONObject json = new JSONObject();
	dataMap.entrySet().forEach(entry -> json.put(entry.getKey().asString(), entry.getValue().save()));
	return json;
	}

	/**
	 * Fonction de sauvegarde de donnée appelé par DataCenter. Les données json sont
	 * récupérés pour être enregistré dans la base de donnée. gère également la
	 * sauvegarde des données des objets discord contenue dans cet objet.
	 */
	@Override
	public JSONObject save()
	{
		JSONObject json = super.save();

		json.putOpt("members", DataGuild.extractJsonDatas(this.dataMembers));
		json.putOpt("text_channels", DataGuild.extractJsonDatas(this.dataTextChannels));
		json.putOpt("roles", DataGuild.extractJsonDatas(this.dataRoles));
		json.putOpt("voice_channels", DataGuild.extractJsonDatas(this.dataVoiceChannels));
		json.putOpt("categories", DataGuild.extractJsonDatas(this.dataCategories));

		json.putOpt("prefix", this.prefix);
		json.putOpt("welcome", this.welcome.save());

		JSONObject cc = new JSONObject();
		cc.putOpt("enabled", this.ccEnabled);
		cc.putOpt("active", this.ccActive);
		cc.putOpt("archive", this.ccArchive);
		json.put("cchannel", cc);

		json.putOpt("backups", this.backups);

		return json;
	}

	/**
	 * Créer et/ou hydrate les DataObject de la Map à partir des données json
	 * indiqués
	 *
	 * @param <D>			 Type des DataObject
	 * @param <E>			 Type de l'entité lié au DataObject
	 * @param dataMap		 la datMap à hydraté
	 * @param dataConstructor le contructeur de DataObject
	 * @param entityMono	  la fonction de récupération de l'entité
	 * @param json			les données à utiliser pour hydratés les DataObject
	 * @param type			le type/classe de l'entité
	 */
	private <D extends DataObject<E>, E extends Entity> void hydratateDataObject(Map<Snowflake, D> dataMap, Function<E, D> dataConstructor, Function<Snowflake, Mono<? super E>> entityMono, JSONObject json, Class<E> type)
	{
		if (json != null)
			for (String strId : json.keySet())
			{
				Snowflake id = Snowflake.of(strId);
				Optional<E> opt = BotUtils.returnOptional(entityMono.apply(id).ofType(type));

				if (opt.isPresent())
				{
					if (dataMap.containsKey(id))
						dataMap.get(id).load(json.getJSONObject(strId));
					else
					{
						D dataMember = dataConstructor.apply(opt.get());
						dataMember.load(json.getJSONObject(strId));
						dataMap.put(id, dataMember);
					}
				} else
					System.err.println(String.format(
							"No reachable entity '%s' with id '%s' in the guild '%s' (%s)! Skipping loading of this entity!",
							type.getSimpleName(), id.asString(), this.getEntity().getName(), this.getEntity().getId().asString()));
			}
		else
			System.out.println(String.format("No entity '%s' datas in the guild '%s' (%s)! Skipping loading of their entities!",
					type.getSimpleName(), this.getEntity().getName(), this.getEntity().getId().asString()));
	}

	/**
	 * Fonction de chargement des données appelé par DataCenter. Les données json de
	 * la base de donnée sont transmises à cette fonction. Gère également le
	 * chargement des données des objets discords contenu dans cet objet.
	 */
	@Override
	public void load(JSONObject json) {
		super.load(json);

	// debug
	// System.out.println(json.toString());

		this.hydratateDataObject(this.dataMembers, DataMember::new, this.getEntity()::getMemberById, json.optJSONObject("members"), Member.class);
		this.hydratateDataObject(this.dataTextChannels, DataTextChannel::new, this.getEntity()::getChannelById, json.optJSONObject("text_channels"), TextChannel.class);
		this.hydratateDataObject(this.dataRoles, DataRole::new, this.getEntity()::getRoleById, json.optJSONObject("roles"), Role.class);
		this.hydratateDataObject(this.dataVoiceChannels, DataVoiceChannel::new, this.getEntity()::getChannelById, json.optJSONObject("voice_channels"), VoiceChannel.class);
		this.hydratateDataObject(this.dataCategories, DataCategory::new, this.getEntity()::getChannelById, json.optJSONObject("categories"), Category.class);

		this.prefix.setValue(json.optString("prefix", this.prefix.getValue()));
		this.welcome.load(json.optJSONObject("welcome"));

		JSONObject customChannel = json.optJSONObject("cchannel");
		if (customChannel == null)
			customChannel = new JSONObject();

		this.ccEnabled = customChannel.optBoolean("enabled", false);
		this.ccActive = customChannel.optLong("active", -1L);
		this.ccArchive = customChannel.optLong("archive", -1L);

		JSONObject jsonBackups = json.optJSONObject("backups");

		if (jsonBackups != null)
			for (String backupName : jsonBackups.keySet())
				this.backups.put(backupName, jsonBackups.getJSONObject(backupName));
	}

	public Configuration<String> prefix() { return this.prefix; }
	public WelcomeChannelSystem welcomeSystem() { return this.welcome; }

	public String getPrefix() { return this.prefix.getValue(); }
	public boolean isCCEnabled() { return this.ccEnabled; }
	public long getCCActive() { return this.ccActive; }
	public long getCCArchive() { return this.ccArchive; }

	public void setPrefix(String prefix) { this.prefix.setValue(prefix); }
	public void setCCEnable(boolean enable) { this.ccEnabled = enable; }
	public void setCCActive(long c) { this.ccActive = c; }
	public void setCCArchive(long c) { this.ccArchive = c; }

	public void addBackup(String backupName, JSONObject jsonDatas) { this.backups.put(backupName, jsonDatas); }
	public boolean hasBackup(String backupName) { return this.backups.containsKey(backupName); }
	public JSONObject getBackup(String backupName) { return this.backups.get(backupName); }
	public Set<String> listBackup() { return new HashSet<>(this.backups.keySet()); }
	public void removeBackup(String backupName) { this.backups.remove(backupName); }

	@Override
	public WrappedConfiguration getConfiguration() { return new WrappedConfiguration(this.configuration); }
}
