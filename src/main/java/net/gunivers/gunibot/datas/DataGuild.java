package net.gunivers.gunibot.datas;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;

import net.gunivers.gunibot.command.permissions.Permission;

/**
 * Classe de donnée pour les objets Guild.
 * S'occupe également du chargement et de la sauvegarde de la plupart des objets discord contenu dans ce guild.
 * 
 * @author Syl2010
 *
 */
public class DataGuild extends DataObject<Guild>
{
	private ConcurrentHashMap<Snowflake, DataMember> dataMembers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataTextChannel> dataTextChannels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataRole> dataRoles = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataVoiceChannel> dataVoiceChannels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Snowflake, DataCategory> dataCategories = new ConcurrentHashMap<>();

	private String welcomeMessage = "Welcome to {server}";
	private DataTextChannel welcomeChannel = null;

	{
		this.getDataMember(this.getEntity().getOwner().block()).getPermissions().add(Permission.bot.get("server.owner"));
	}
	
	/**
	 * Créer cet objet lié à ce guild.
	 * @param guild le guild lié à cet objet.
	 */
	public DataGuild(Guild guild) {
		super(guild);
	}

	/**
	 * Créer cet objet lié à ce guild et charge les données json dans la fonction {@link #load(JSONObject)}.
	 * @param guild le guild lié à cet objet.
	 * @param json les données json à chargés dans {@link #load(JSONObject)}.
	 */
	public DataGuild(Guild guild, JSONObject json) {
		super(guild, json);
	}

	/**
	 * Créer et/ou récupère les données du membre indiqué en cache dans cet objet.
	 * @param member le membre.
	 * @return l'objet de donnée du membre.
	 */
	public DataMember getDataMember(Member member) {
		DataMember data_member = dataMembers.get(member.getId());
		if(data_member != null) {
			return data_member;
		} else {
			data_member = new DataMember(member);
			dataMembers.put(member.getId(), data_member);
			return data_member;
		}
	}

	/**
	 * Créer et/ou récupère les données du channel textuel indiqué en cache dans cet objet.
	 * @param text_channel le channel textuel.
	 * @return l'objet de donnée du channel textuel.
	 */
	public DataTextChannel getDataTextChannel(TextChannel text_channel) {
		DataTextChannel data_text_channel = dataTextChannels.get(text_channel.getId());
		if(data_text_channel != null) {
			return data_text_channel;
		} else {
			data_text_channel = new DataTextChannel(text_channel);
			dataTextChannels.put(text_channel.getId(), data_text_channel);
			return data_text_channel;
		}
	}

	/**
	 * Créer et/ou récupère les données du role indiqué en cache dans cet objet.
	 * @param role le role.
	 * @return l'objet de donnée du role.
	 */
	public DataRole getDataRole(Role role) {
		DataRole data_role = dataRoles.get(role.getId());
		if(data_role != null) {
			return data_role;
		} else {
			data_role = new DataRole(role);
			dataRoles.put(role.getId(), data_role);
			return data_role;
		}
	}

	/**
	 * Créer et/ou récupère les données du channel vocal indiqué en cache dans cet objet.
	 * @param voice_channel le channel vocal.
	 * @return l'objet de donnée du channel vocal.
	 */
	public DataVoiceChannel getDataVoiceChannel(VoiceChannel voice_channel) {
		DataVoiceChannel data_voice_channel = dataVoiceChannels.get(voice_channel.getId());
		if(data_voice_channel != null) {
			return data_voice_channel;
		} else {
			data_voice_channel = new DataVoiceChannel(voice_channel);
			dataVoiceChannels.put(voice_channel.getId(), data_voice_channel);
			return data_voice_channel;
		}
	}

	/**
	 * Créer et/ou récupère les données de la catégorie indiqué en cache dans cet objet.
	 * @param category la catégorie.
	 * @return l'objet de donnée de la catégorie.
	 */
	public DataCategory getDataCategory(Category category) {
		DataCategory data_category = dataCategories.get(category.getId());
		if(data_category != null) {
			return data_category;
		} else {
			data_category = new DataCategory(category);
			dataCategories.put(category.getId(), data_category);
			return data_category;
		}
	}

	//	//En réflexion
	//	public void clearDataMembersCache() {
	//		dataMembers.clear();
	//	}
	//
	//	public void clearDataTextChannelsCache() {
	//		dataTextChannels.clear();
	//	}
	//
	//	public void clearDataRolesCache() {
	//		dataRoles.clear();
	//	}
	//
	//	public void clearDataVoiceChannelsCache() {
	//		dataVoiceChannels.clear();
	//	}
	//
	//	public void clearDataCategoriesCache() {
	//		dataCategories.clear();
	//	}
	//
	//	public void clearAllCache() {
	//		clearDataMembersCache();
	//		clearDataTextChannelsCache();
	//		clearDataRolesCache();
	//		clearDataVoiceChannelsCache();
	//		clearDataCategoriesCache();
	//	}

	/**
	 * Fonction de sauvegarde de donnée appelé par DataCenter.
	 * Les données json sont récupérés pour être enregistré dans la base de donnée.
	 * gère également la sauvegarde des données des objets discord contenue dans cet objet.
	 */
	@Override
	public JSONObject save() {
		JSONObject json = super.save();

		JSONObject json_members = new JSONObject();
		for(Entry<Snowflake, DataMember> member_entry:dataMembers.entrySet()) {
			json_members.putOpt(member_entry.getKey().asString(), member_entry.getValue().save());
		}
		json.putOpt("members", json_members);

		JSONObject json_text_channels = new JSONObject();
		for(Entry<Snowflake, DataTextChannel> text_channel_entry:dataTextChannels.entrySet()) {
			json_text_channels.putOpt(text_channel_entry.getKey().asString(), text_channel_entry.getValue().save());
		}
		json.putOpt("text_channels", json_text_channels);

		JSONObject json_roles = new JSONObject();
		for(Entry<Snowflake, DataRole> roles_entry:dataRoles.entrySet()) {
			json_roles.putOpt(roles_entry.getKey().asString(), roles_entry.getValue().save());
		}
		json.putOpt("roles", json_roles);

		JSONObject json_voice_channels = new JSONObject();
		for(Entry<Snowflake, DataVoiceChannel> voice_channel_entry:dataVoiceChannels.entrySet()) {
			json_voice_channels.putOpt(voice_channel_entry.getKey().asString(), voice_channel_entry.getValue().save());
		}
		json.putOpt("voice_channels", json_voice_channels);

		JSONObject json_categories = new JSONObject();
		for(Entry<Snowflake, DataCategory> category_entry:dataCategories.entrySet()) {
			json_categories.putOpt(category_entry.getKey().asString(), category_entry.getValue().save());
		}
		json.putOpt("categories", json_categories);

		json.putOpt("welcome_message", welcomeMessage);
		if (welcomeChannel != null)
		{
			json.putOpt("welcome_channel", welcomeChannel.getEntity().getId().asLong());
		}
		
		return json;
	}

	/**
	 * Fonction de chargement des données appelé par DataCenter.
	 * Les données json de la base de donnée sont transmises à cette fonction.
	 * Gère également le chargement des données des objets discords contenu dans cet objet.
	 */
	@Override
	public void load(JSONObject json) {
		JSONObject json_members = json.optJSONObject("members");
		JSONObject json_text_channels = json.optJSONObject("text_channels");
		JSONObject json_roles = json.optJSONObject("roles");
		JSONObject json_voice_channels = json.optJSONObject("voice_channels");
		JSONObject json_categories = json.optJSONObject("categories");
		
		if(json_members != null) {
			for(String s_member_id:json_members.keySet()) {
				Snowflake member_id = Snowflake.of(s_member_id);
				Optional<Member> opt_member = getEntity().getMemberById(member_id).blockOptional();
				if(opt_member.isPresent()) {
					if (dataMembers.containsKey(member_id)) {
						dataMembers.get(member_id).load(json_members.getJSONObject(s_member_id));
					} else {
						dataMembers.put(member_id, new DataMember(opt_member.get(), json_members.getJSONObject(s_member_id)));
					}
				} else {
					System.err.println(String.format("The member id '%s' in the guild '%s' (%s) is not reachable! Skipping loading of this member!", member_id.asString(), getEntity().getName(), getEntity().getId().asString()));
				}
			}
		} else {
			System.out.println(String.format("No members datas in the guild '%s' (%s)! Skipping loading of the members!", getEntity().getName(), getEntity().getId().asString()));
		}

		if(json_text_channels != null) {
			for(String s_text_channel_id : json_text_channels.keySet()) {
				Snowflake text_channel_id = Snowflake.of(s_text_channel_id);
				Optional<TextChannel> opt_text_channel = getEntity().getChannelById(text_channel_id).ofType(TextChannel.class).blockOptional();
				if(opt_text_channel.isPresent()) {
					if (dataTextChannels.containsKey(text_channel_id)) {
						dataTextChannels.get(text_channel_id).load(json_text_channels.getJSONObject(s_text_channel_id));
					} else {
						dataTextChannels.put(text_channel_id, new DataTextChannel(opt_text_channel.get(), json_text_channels.getJSONObject(s_text_channel_id)));
					}
				} else {
					System.err.println(String.format("The text channel id '%s' in the guild '%s' (%s) is not reachable! Skipping loading of this text channel!", text_channel_id.asString(), getEntity().getName(), getEntity().getId().asString()));
				}
			}
		} else {
			System.out.println(String.format("No text channels datas in the guild '%s' (%s)! Skipping loading of the text channels!", getEntity().getName(), getEntity().getId().asString()));
		}

		if(json_roles != null) {
			for(String s_role_id:json_roles.keySet()) {
				Snowflake role_id = Snowflake.of(s_role_id);
				Optional<Role> opt_role = getEntity().getRoleById(role_id).blockOptional();
				if(opt_role.isPresent()) {
					if (dataRoles.containsKey(role_id)) {
						dataRoles.get(role_id).load(json_roles.getJSONObject(s_role_id));
					} else {
						dataRoles.put(role_id, new DataRole(opt_role.get(), json_roles.getJSONObject(s_role_id)));
					}
				} else {
					System.err.println(String.format("The role id '%s' in the guild '%s' (%s) is not reachable! Skipping loading of this role!", role_id.asString(), getEntity().getName(), getEntity().getId().asString()));
				}
			}
		} else {
			System.out.println(String.format("No roles datas in the guild '%s' (%s)! Skipping loading of the roles!", getEntity().getName(), getEntity().getId().asString()));
		}

		if(json_voice_channels != null) {
			for(String s_voice_channel_id:json_voice_channels.keySet()) {
				Snowflake voice_channel_id = Snowflake.of(s_voice_channel_id);
				Optional<VoiceChannel> opt_voice_channel = getEntity().getChannelById(voice_channel_id).ofType(VoiceChannel.class).blockOptional();
				if(opt_voice_channel.isPresent()) {
					if (dataVoiceChannels.containsKey(voice_channel_id)) {
						dataVoiceChannels.get(voice_channel_id).load(json_voice_channels.getJSONObject(s_voice_channel_id));
					} else {
						dataVoiceChannels.put(voice_channel_id, new DataVoiceChannel(opt_voice_channel.get(), json_voice_channels.getJSONObject(s_voice_channel_id)));
					}
				} else {
					System.err.println(String.format("The voice channel id '%s' in the guild '%s' (%s) is not reachable! Skipping loading of this voice channel!", voice_channel_id.asString(), getEntity().getName(), getEntity().getId().asString()));
				}
			}
		} else {
			System.out.println(String.format("No voice channels datas in the guild '%s' (%s)! Skipping loading of the voice channels!", getEntity().getName(), getEntity().getId().asString()));
		}

		if(json_categories != null) {
			for(String s_category_id:json_categories.keySet()) {
				Snowflake category_id = Snowflake.of(s_category_id);
				Optional<Category> opt_category = getEntity().getChannelById(category_id).ofType(Category.class).blockOptional();
				if(opt_category.isPresent()) {
					if (dataCategories.containsKey(category_id)) {
						dataCategories.get(category_id).load(json_categories.getJSONObject(s_category_id));
					} else {
						dataCategories.put(category_id, new DataCategory(opt_category.get(), json_categories.getJSONObject(s_category_id)));
					}
				} else {
					System.err.println(String.format("The category id '%s' in the guild '%s' (%s) is not reachable! Skipping loading of this category!", category_id.asString(), getEntity().getName(), getEntity().getId().asString()));
				}
			}
		} else {
			System.out.println(String.format("No categories datas in the guild '%s' (%s)! Skipping loading of the categories!", getEntity().getName(), getEntity().getId().asString()));
		}

		welcomeMessage = json.getString("welcome");
//		welcomeChannel = new DataTextChannel(text_channel);
	}
	
	public String getWelcomeMessage() { return welcomeMessage; }
	public DataTextChannel getWelcomeChannel() { return welcomeChannel; }
	
	public void setWelcomeMessage(String msg) { this.welcomeMessage = msg; }
	public void setWelcomeChannel(DataTextChannel channel) { this.welcomeChannel = channel; }
}
