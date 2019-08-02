package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Ban;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Guild.NotificationLevel;
import discord4j.core.object.entity.Guild.VerificationLevel;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Image.Format;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.http.client.ClientException;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.datas.DataGuild;

public class BackupCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "administrator/backup.json";
	}

	public void defaultBackup(MessageCreateEvent event) {
		namedBackup(event, Arrays.asList(LocalDateTime.now().format(Parser.DateTimePattern())));
	}

	public void namedBackup(MessageCreateEvent event, List<String> args) {
		Message message = event.getMessage();
		TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
		User user_bot = event.getClient().getSelf().block();
		User author = event.getMember().get();

		String backup_name = args.get(0);

		if((backup_name == null) || backup_name.isEmpty()) {
			channel.createEmbed(spec -> {
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
			}).subscribe();
		} else {
			Guild guild = event.getGuild().block();

			DataGuild data_guild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

			if(!data_guild.inBigTask) {
				data_guild.inBigTask = true;
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.setDescription("Backup en cours...");
				}).subscribe();

				JSONObject json_datas = new JSONObject();

				//Begin backup

				// Guild
				JSONObject json_guild = new JSONObject();

				json_guild.put("name", guild.getName());
				if(guild.getAfkChannelId().isPresent()) json_guild.put("afk_id", guild.getAfkChannelId().get().asString());
				json_guild.put("afk_timeout", guild.getAfkTimeout());

				if(guild.getBannerUrl(Format.GIF).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getBannerUrl(Format.GIF).get()) == 200)) json_guild.put("banner", guild.getBannerUrl(Format.GIF).get());
				else if(guild.getBannerUrl(Format.PNG).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getBannerUrl(Format.PNG).get()) == 200)) json_guild.put("banner", guild.getBannerUrl(Format.PNG).get());

				JSONArray json_bans = new JSONArray();
				for(Ban ban:guild.getBans().toIterable()) {
					JSONObject json_ban = new JSONObject();

					json_ban.put("user", ban.getUser().getId().asString());
					if(ban.getReason().isPresent()) json_ban.put("reason", ban.getReason().get());

					json_bans.put(json_ban);
				}
				json_guild.put("bans", json_bans);

				JSONArray json_emojis = new JSONArray();
				for(GuildEmoji emoji:guild.getEmojis().toIterable()) {
					JSONObject json_emoji = new JSONObject();

					json_emoji.put("name", emoji.getName());
					json_emoji.put("emoji", emoji.getImageUrl());
					json_emoji.put("roles", emoji.getRoleIds().stream().map(Snowflake::asString).collect(Collectors.toList()));

					json_emojis.put(json_emoji);
				}
				json_guild.put("emojis", json_emojis);

				if(guild.getIconUrl(Format.GIF).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getIconUrl(Format.GIF).get()) == 200)) json_guild.put("icon", guild.getIconUrl(Format.GIF).get());
				else if(guild.getIconUrl(Format.PNG).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getIconUrl(Format.PNG).get()) == 200)) json_guild.put("icon", guild.getIconUrl(Format.PNG).get());

				json_guild.put("notification", guild.getNotificationLevel().getValue());
				json_guild.put("owner", guild.getOwnerId().asString());
				json_guild.put("region", guild.getRegionId());

				if(guild.getSplashUrl(Format.GIF).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getSplashUrl(Format.GIF).get()) == 200)) json_guild.put("splash", guild.getSplashUrl(Format.GIF).get());
				else if(guild.getSplashUrl(Format.PNG).isPresent() && (BotUtils.testHTTPCodeResponse(guild.getSplashUrl(Format.PNG).get()) == 200)) json_guild.put("splash", guild.getSplashUrl(Format.PNG).get());

				json_guild.put("verification", guild.getVerificationLevel().getValue());

				json_datas.put("guild", json_guild);
				// Roles
				JSONObject json_roles = new JSONObject();

				for(Role role:guild.getRoles().toIterable()) {
					JSONObject json_role = new JSONObject();

					Snowflake role_id = role.getId();
					json_role.put("name", role.getName());
					json_role.put("color", role.getColor().getRGB());
					json_role.put("position",role.getRawPosition());
					json_role.put("hoisted", role.isHoisted());
					json_role.put("mentionable", role.isMentionable());
					json_role.put("permissions", role.getPermissions().getRawValue());

					json_roles.put(role_id.asString(), json_role);
				}

				json_datas.put("roles", json_roles);
				// channels
				JSONObject json_channels = new JSONObject();

				for(GuildChannel guild_channel:guild.getChannels().toIterable()) {
					JSONObject json_channel = new JSONObject();

					Snowflake channel_id = guild_channel.getId();
					json_channel.put("name", guild_channel.getName());
					json_channel.put("position", guild_channel.getRawPosition());

					JSONArray json_permissions = new JSONArray();
					for(ExtendedPermissionOverwrite permission:guild_channel.getPermissionOverwrites()) {
						JSONObject json_permission = new JSONObject();

						json_permission.put("id", permission.getTargetId().asString());
						json_permission.put("type", permission.getType().getValue());
						json_permission.put("allowed", permission.getAllowed().getRawValue());
						json_permission.put("denied", permission.getDenied().getRawValue());

						json_permissions.put(json_permission);
					}
					json_channel.put("permissions", json_permissions);

					Channel.Type channel_type = guild_channel.getType();
					json_channel.put("type", channel_type.getValue());

					if(channel_type.equals(Channel.Type.GUILD_CATEGORY)) {
						//Category category = (Category) guild_channel;

					} else if(channel_type.equals(Channel.Type.GUILD_TEXT)) {
						TextChannel text_channel = (TextChannel) guild_channel;

						if(text_channel.getCategoryId().isPresent()) json_channel.put("parent_id", text_channel.getCategoryId().get().asString());
						json_channel.put("slow" ,text_channel.getRateLimitPerUser());
						json_channel.put("nsfw",text_channel.isNsfw());
						if(text_channel.getTopic().isPresent()) json_channel.put("topic", text_channel.getTopic().get());
					} else if(channel_type.equals(Channel.Type.GUILD_VOICE)) {
						VoiceChannel voice_channel = (VoiceChannel) guild_channel;

						if(voice_channel.getCategoryId().isPresent()) json_channel.put("parent_id", voice_channel.getCategoryId().get().asString());
						json_channel.put("bitrate", voice_channel.getBitrate());
						json_channel.put("user_limit", voice_channel.getUserLimit());
					} else {
						System.err.println("[Backup] A channel type is unsupported and cannot be completely saved : "+channel_type);
					}
					json_channels.put(channel_id.asString(), json_channel);
				}

				json_datas.put("channels", json_channels);
				// Webhook
				JSONObject json_webhooks = new JSONObject();

				for(Webhook webhook:guild.getWebhooks().toIterable()) {
					JSONObject json_webhook = new JSONObject();

					Snowflake webhook_id = webhook.getId();
					if(webhook.getName().isPresent()) json_webhook.put("name", webhook.getName().get());
					if(webhook.getAvatar().isPresent()) json_webhook.put("avatar", webhook.getAvatar().get());
					json_webhook.put("channel", webhook.getChannelId().asString());

					json_webhooks.put(webhook_id.asString(), json_webhook);
				}

				json_datas.put("webhooks", json_webhooks);
				//End backup

				data_guild.addBackup(backup_name, json_datas);
				data_guild.inBigTask = false;
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.addField("Backup terminé !","Nom de la backup : **"+backup_name+"**", false);
				}).subscribe();

			} else {
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.addField("Commande refusé !", "Une autre tâche est en cours !", false);
				}).subscribe();
			}
		}
	}

	public void restoreBackup(MessageCreateEvent event, List<String> args) {
		Message message = event.getMessage();
		TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
		User user_bot = event.getClient().getSelf().block();
		User author = event.getMember().get();

		String backup_name = args.get(0);

		if((backup_name == null) || backup_name.isEmpty()) {
			channel.createEmbed(spec -> {
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
			}).subscribe();
		} else {
			Guild guild = event.getGuild().block();

			DataGuild data_guild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

			if(!data_guild.inBigTask) {
				data_guild.inBigTask = true;
				JSONObject json_backup = data_guild.getBackup(backup_name);

				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.setDescription("Restauration en cours...");
				}).subscribe();

				//Begin backup

				// Guild
				JSONObject json_guild = json_backup.getJSONObject("guild");

				guild.edit(spec -> {
					spec.setName(json_guild.getString("name"));
					if(json_guild.has("afk_id")) spec.setAfkChannelId(Snowflake.of(json_guild.getString("afk_id")));
					spec.setAfkTimeout(json_guild.getInt("afk_timeout"));
					if(json_guild.has("banner")) spec.setBanner(Image.ofUrl(json_guild.getString("banner")).block());
					if(json_guild.has("icon")) spec.setIcon(Image.ofUrl(json_guild.getString("icon")).block());
					spec.setDefaultMessageNotificationsLevel(NotificationLevel.of(json_guild.getInt("notification")));
					spec.setOwnerId(Snowflake.of(json_guild.getString("owner")));
					spec.setRegion(guild.getClient().getRegions().filter(region -> region.getId().equals(json_guild.getString("region"))).blockFirst());
					if(json_guild.has("splash")) spec.setSplash(Image.ofUrl(json_guild.getString("splash")).block());
					spec.setVerificationLevel(VerificationLevel.of(json_guild.getInt("verification")));

					spec.setReason("backup Restoring !");
				}).block();

				JSONArray json_bans = json_guild.getJSONArray("bans");
				for(int i=0; i<json_bans.length(); i++) {
					JSONObject json_ban = json_bans.getJSONObject(i);

					if(json_ban.has("reason")) guild.ban(Snowflake.of(json_ban.getString("user")), spec -> spec.setReason(json_ban.getString("reason"))).block();
					else guild.ban(Snowflake.of(json_ban.getString("user")), spec -> {}).block();
				}

				JSONArray json_emojis = json_guild.getJSONArray("emojis");
				for(int i=0; i<json_emojis.length(); i++) {
					JSONObject json_emoji = json_emojis.getJSONObject(i);

					guild.createEmoji(spec -> {
						spec.setName(json_emoji.getString("name"));
						spec.setImage(Image.ofUrl(json_emoji.getString("emoji")).block());

						JSONArray role_id_list = json_emoji.getJSONArray("roles");
						for(int n=0; n<role_id_list.length(); n++) {
							spec.addRole(Snowflake.of(role_id_list.getString(n)));
						}

						spec.setReason("Restored Emoji");
					}).block();
				}

				// Roles
				JSONObject json_roles = json_backup.getJSONObject("roles");

				for(String s_role_id:json_roles.keySet()) {
					JSONObject json_role = json_roles.getJSONObject(s_role_id);

					Snowflake role_id = Snowflake.of(s_role_id);
					Optional<Role> opt_role = BotUtils.returnOptional(guild.getRoleById(role_id));

					if(opt_role.isPresent()) {
						Role role = opt_role.get();

						role.edit(spec -> {
							spec.setName(json_role.getString("name"));
							spec.setColor(new Color(json_role.getInt("color")));
							spec.setHoist(json_role.getBoolean("hoisted"));
							spec.setMentionable(json_role.getBoolean("mentionable"));
							spec.setPermissions(PermissionSet.of(json_role.getLong("permissions")));

							spec.setReason("restore role");
						}).doOnError(ClientException.class, e -> {
							if (e.getStatus().code() == 403) {
								System.err.println("No permission for edit and restore the role '"+role.getName()+"' ("+role.getId().asString()+")");
							}
						}).onErrorReturn(role).block();
						role.changePosition(json_role.getInt("position")).subscribe();
					} else {
						Role role = guild.createRole(spec -> {
							spec.setName(json_role.getString("name"));
							spec.setColor(new Color(json_role.getInt("color")));
							spec.setHoist(json_role.getBoolean("hoisted"));
							spec.setMentionable(json_role.getBoolean("mentionable"));
							spec.setPermissions(PermissionSet.of(json_role.getLong("permissions")));

							spec.setReason("restore role");
						}).block();
						role.changePosition(json_role.getInt("position")).subscribe();
					}
				}

				// channels
				JSONObject json_channels = json_backup.getJSONObject("channels");

				for(String s_channel_id:json_channels.keySet()) {
					JSONObject json_channel = json_channels.getJSONObject(s_channel_id);

					Snowflake channel_id = Snowflake.of(s_channel_id);
					Optional<GuildChannel> channel_opt = BotUtils.returnOptional(guild.getChannelById(channel_id));

					if(channel_opt.isPresent()) {
						GuildChannel guild_channel = channel_opt.get();
						Channel.Type channel_type = Channel.Type.of(json_channel.getInt("type"));

						if(channel_type.equals(Channel.Type.GUILD_CATEGORY)) {
							Category category = (Category) guild_channel;

							category.edit(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								spec.setReason("restore category");
							}).block();

						} else if(channel_type.equals(Channel.Type.GUILD_TEXT)) {
							TextChannel text_channel = (TextChannel) guild_channel;

							text_channel.edit(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								if(json_channel.has("parent_id")) spec.setParentId(Snowflake.of(json_channel.getString("parent_id")));
								spec.setNsfw(json_channel.getBoolean("nsfw"));
								spec.setRateLimitPerUser(json_channel.getInt("slow"));
								if(json_channel.has("topic")) spec.setTopic(json_channel.getString("topic"));

								spec.setReason("restore text channel");
							}).block();

						} else if(channel_type.equals(Channel.Type.GUILD_VOICE)) {
							VoiceChannel voice_channel = (VoiceChannel) guild_channel;

							voice_channel.edit(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								if(json_channel.has("parent_id")) spec.setParentId(Snowflake.of(json_channel.getString("parent_id")));
								spec.setBitrate(json_channel.getInt("bitrate"));
								spec.setUserLimit(json_channel.getInt("user_limit"));

								spec.setReason("restore voice channel");
							}).block();

						} else {
							System.err.println("[Backup] A channel type is unsupported and cannot be restored : "+channel_type);
						}
					} else {
						Channel.Type channel_type = Channel.Type.of(json_channel.getInt("type"));

						if(channel_type.equals(Channel.Type.GUILD_CATEGORY)) {
							guild.createCategory(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								spec.setReason("restore category");
							}).block();

						} else if(channel_type.equals(Channel.Type.GUILD_TEXT)) {
							guild.createTextChannel(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								if(json_channel.has("parent_id")) spec.setParentId(Snowflake.of(json_channel.getString("parent_id")));
								spec.setNsfw(json_channel.getBoolean("nsfw"));
								spec.setRateLimitPerUser(json_channel.getInt("slow"));
								if(json_channel.has("topic")) spec.setTopic(json_channel.getString("topic"));

								spec.setReason("restore text channel");
							}).block();

						} else if(channel_type.equals(Channel.Type.GUILD_VOICE)) {
							guild.createVoiceChannel(spec -> {
								spec.setName(json_channel.getString("name"));
								spec.setPosition(json_channel.getInt("position"));

								JSONArray json_permissions = json_channel.getJSONArray("permissions");
								HashSet<PermissionOverwrite> permissions = new HashSet<>(json_permissions.length());

								for(int i=0; i<json_permissions.length(); i++) {
									JSONObject json_permission = json_permissions.getJSONObject(i);

									Snowflake id = Snowflake.of(json_permission.getString("id"));
									PermissionOverwrite.Type target_type = PermissionOverwrite.Type.of(json_permission.getString("type"));
									PermissionSet allowed_permissions = PermissionSet.of(json_permission.getLong("allowed"));
									PermissionSet denied_permissions = PermissionSet.of(json_permission.getLong("denied"));

									if(target_type.equals(PermissionOverwrite.Type.MEMBER)) {

										permissions.add(ExtendedPermissionOverwrite.forMember(id, allowed_permissions, denied_permissions));
									} else if(target_type.equals(PermissionOverwrite.Type.ROLE)) {

										permissions.add(ExtendedPermissionOverwrite.forRole(id, allowed_permissions, denied_permissions));
									} else {
										System.err.println("[Backup] A permission type is unsupported and cannot be restored : "+target_type);
									}
								}
								spec.setPermissionOverwrites(permissions);

								if(json_channel.has("parent_id")) spec.setParentId(Snowflake.of(json_channel.getString("parent_id")));
								spec.setBitrate(json_channel.getInt("bitrate"));
								spec.setUserLimit(json_channel.getInt("user_limit"));

								spec.setReason("restore voice channel");
							}).block();

						} else {
							System.err.println("[Backup] A channel type is unsupported and cannot be restored : "+channel_type);
						}
					}
				}

				// Webhook
				JSONObject json_webhooks = json_backup.getJSONObject("webhooks");

				for(String s_webhook_id:json_webhooks.keySet()) {
					JSONObject json_webhook = json_webhooks.getJSONObject(s_webhook_id);

					Snowflake webhook_id = Snowflake.of(s_webhook_id);
					Optional<Webhook> opt_webhook = BotUtils.returnOptional(guild.getWebhooks().filter(w -> w.getId().equals(webhook_id)).next());
					if(opt_webhook.isPresent()) {
						Webhook webhook = opt_webhook.get();

						webhook.edit(spec -> {
							if(json_webhook.has("name")) spec.setName(json_webhook.getString("name"));
							if(json_webhook.has("avatar")) spec.setAvatar(Image.ofUrl(json_webhook.getString("avatar")).block());

							spec.setReason("Restore Webhook");
						}).block();
					} else {
						TextChannel text_channel = guild.getChannelById(Snowflake.of(json_webhook.getString("channel"))).ofType(TextChannel.class).block();

						text_channel.createWebhook(spec -> {
							if(json_webhook.has("name")) spec.setName(json_webhook.getString("name"));
							if(json_webhook.has("avatar")) spec.setAvatar(Image.ofUrl(json_webhook.getString("avatar")).block());

							spec.setReason("Restore Webhook");
						}).block();
					}
				}
				//End backup

				data_guild.inBigTask = false;
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.addField("Restauration terminé !","Nom de la backup restauré : **"+backup_name+"**", false);
				}).subscribe();

			} else {
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.addField("Commande refusé !", "Une autre tâche est en cours !", false);
				}).subscribe();
			}
		}
	}

	public void listBackup(MessageCreateEvent event) {
		Message message = event.getMessage();
		TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
		User user_bot = event.getClient().getSelf().block();
		User author = event.getMember().get();
		Guild guild = event.getGuild().block();

		Set<String> backups = Main.getBotInstance().getDataCenter().getDataGuild(guild).listBackup();

		if(backups.size()>0) {
			channel.createEmbed(spec -> {
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.addField("Liste des backups", String.join("\n", backups), false);
			}).subscribe();
		} else {
			channel.createEmbed(spec -> {
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.setDescription("Aucune backup enregistré !");
			}).subscribe();
		}
	}

	public void removeBackup(MessageCreateEvent event, List<String> args) {
		Message message = event.getMessage();
		TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
		User user_bot = event.getClient().getSelf().block();
		User author = event.getMember().get();
		Guild guild = event.getGuild().block();

		String backup_name = args.get(0);

		if((backup_name == null) || backup_name.isEmpty()) {
			channel.createEmbed(spec -> {
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
			}).subscribe();
		} else {

			DataGuild data_guild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

			if(data_guild.hasBackup(backup_name)) {
				data_guild.removeBackup(backup_name);
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.addField("Backup supprimé !", "Nom de la backup supprimé : **"+backup_name+"**", false);
				}).subscribe();
			} else {
				channel.createEmbed(spec -> {
					spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					spec.setColor(Color.ORANGE);
					spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					spec.setTimestamp(message.getTimestamp());

					spec.setDescription("Aucune backup enregistré avec ce nom : **"+backup_name+"**");
				}).subscribe();
			}
		}
	}

}
