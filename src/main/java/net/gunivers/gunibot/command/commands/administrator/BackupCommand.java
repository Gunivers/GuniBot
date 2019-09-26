package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
import discord4j.core.object.entity.Member;
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
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.utils.BotUtils;

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
	User userBot = event.getClient().getSelf().block();
	User author = event.getMember().get();

	String backupName = args.get(0);

	if ((backupName == null) || backupName.isEmpty()) {
	    channel.createEmbed(spec -> {
		spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		spec.setColor(Color.ORANGE);
		spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		spec.setTimestamp(message.getTimestamp());

		spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
	    }).subscribe();
	} else {
	    Guild guild = event.getGuild().block();

	    DataGuild dataGuild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

	    if (!dataGuild.inBigTask) {
		dataGuild.inBigTask = true;
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.setDescription("Backup en cours...");
		}).subscribe();

		JSONObject jsonDatas = new JSONObject();

		// Begin backup

		// Guild
		JSONObject jsonGuild = new JSONObject();

		jsonGuild.put("name", guild.getName());
		if (guild.getAfkChannelId().isPresent()) {
		    jsonGuild.put("afk_id", guild.getAfkChannelId().get().asString());
		}
		jsonGuild.put("afk_timeout", guild.getAfkTimeout());

		if (guild.getBannerUrl(Format.GIF).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getBannerUrl(Format.GIF).get()) == 200)) {
		    jsonGuild.put("banner", guild.getBannerUrl(Format.GIF).get());
		} else if (guild.getBannerUrl(Format.PNG).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getBannerUrl(Format.PNG).get()) == 200)) {
		    jsonGuild.put("banner", guild.getBannerUrl(Format.PNG).get());
		}

		JSONArray jsonBans = new JSONArray();
		for (Ban ban : guild.getBans().toIterable()) {
		    JSONObject jsonBan = new JSONObject();

		    jsonBan.put("user", ban.getUser().getId().asString());
		    if (ban.getReason().isPresent()) {
			jsonBan.put("reason", ban.getReason().get());
		    }

		    jsonBans.put(jsonBan);
		}
		jsonGuild.put("bans", jsonBans);

		JSONArray jsonEmojis = new JSONArray();
		for (GuildEmoji emoji : guild.getEmojis().toIterable()) {
		    JSONObject jsonEmoji = new JSONObject();

		    jsonEmoji.put("name", emoji.getName());
		    jsonEmoji.put("emoji", emoji.getImageUrl());
		    jsonEmoji.put("roles",
			    emoji.getRoleIds().stream().map(Snowflake::asString).collect(Collectors.toList()));

		    jsonEmojis.put(jsonEmoji);
		}
		jsonGuild.put("emojis", jsonEmojis);

		if (guild.getIconUrl(Format.GIF).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getIconUrl(Format.GIF).get()) == 200)) {
		    jsonGuild.put("icon", guild.getIconUrl(Format.GIF).get());
		} else if (guild.getIconUrl(Format.PNG).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getIconUrl(Format.PNG).get()) == 200)) {
		    jsonGuild.put("icon", guild.getIconUrl(Format.PNG).get());
		}

		jsonGuild.put("notification", guild.getNotificationLevel().getValue());
		jsonGuild.put("owner", guild.getOwnerId().asString());
		jsonGuild.put("region", guild.getRegionId());

		if (guild.getSplashUrl(Format.GIF).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getSplashUrl(Format.GIF).get()) == 200)) {
		    jsonGuild.put("splash", guild.getSplashUrl(Format.GIF).get());
		} else if (guild.getSplashUrl(Format.PNG).isPresent()
			&& (BotUtils.testHTTPCodeResponse(guild.getSplashUrl(Format.PNG).get()) == 200)) {
		    jsonGuild.put("splash", guild.getSplashUrl(Format.PNG).get());
		}

		jsonGuild.put("verification", guild.getVerificationLevel().getValue());

		jsonDatas.put("guild", jsonGuild);
		// Roles
		JSONObject jsonRoles = new JSONObject();

		for (Role role : guild.getRoles().toIterable()) {
		    JSONObject jsonRole = new JSONObject();

		    Snowflake roleId = role.getId();
		    jsonRole.put("name", role.getName());
		    jsonRole.put("color", role.getColor().getRGB());
		    jsonRole.put("position", role.getRawPosition());
		    jsonRole.put("hoisted", role.isHoisted());
		    jsonRole.put("mentionable", role.isMentionable());
		    jsonRole.put("permissions", role.getPermissions().getRawValue());

		    jsonRoles.put(roleId.asString(), jsonRole);
		}

		jsonDatas.put("roles", jsonRoles);
		// channels
		JSONObject jsonChannels = new JSONObject();

		for (GuildChannel guildChannel : guild.getChannels().toIterable()) {
		    JSONObject jsonChannel = new JSONObject();

		    Snowflake channelId = guildChannel.getId();
		    jsonChannel.put("name", guildChannel.getName());
		    jsonChannel.put("position", guildChannel.getRawPosition());

		    JSONArray jsonPermissions = new JSONArray();
		    for (ExtendedPermissionOverwrite permission : guildChannel.getPermissionOverwrites()) {
			JSONObject jsonPermission = new JSONObject();

			jsonPermission.put("id", permission.getTargetId().asString());
			jsonPermission.put("type", permission.getType().getValue());
			jsonPermission.put("allowed", permission.getAllowed().getRawValue());
			jsonPermission.put("denied", permission.getDenied().getRawValue());

			jsonPermissions.put(jsonPermission);
		    }
		    jsonChannel.put("permissions", jsonPermissions);

		    Channel.Type channelType = guildChannel.getType();
		    jsonChannel.put("type", channelType.getValue());

		    if (channelType.equals(Channel.Type.GUILD_CATEGORY)) {
			// Category category = (Category) guildChannel;

		    } else if (channelType.equals(Channel.Type.GUILD_TEXT)) {
			TextChannel textChannel = (TextChannel) guildChannel;

			if (textChannel.getCategoryId().isPresent()) {
			    jsonChannel.put("parent_id", textChannel.getCategoryId().get().asString());
			}
			jsonChannel.put("slow", textChannel.getRateLimitPerUser());
			jsonChannel.put("nsfw", textChannel.isNsfw());
			if (textChannel.getTopic().isPresent()) {
			    jsonChannel.put("topic", textChannel.getTopic().get());
			}
		    } else if (channelType.equals(Channel.Type.GUILD_VOICE)) {
			VoiceChannel voiceChannel = (VoiceChannel) guildChannel;

			if (voiceChannel.getCategoryId().isPresent()) {
			    jsonChannel.put("parent_id", voiceChannel.getCategoryId().get().asString());
			}
			jsonChannel.put("bitrate", voiceChannel.getBitrate());
			jsonChannel.put("user_limit", voiceChannel.getUserLimit());
		    } else {
			System.err.println("[Backup] A channel type is unsupported and cannot be completely saved : "
				+ channelType);
		    }
		    jsonChannels.put(channelId.asString(), jsonChannel);
		}

		jsonDatas.put("channels", jsonChannels);
		// Members
		JSONObject jsonMembers = new JSONObject();
		for (Member member : guild.getMembers().toIterable()) {
		    JSONObject jsonMember = new JSONObject();

		    jsonMember.put("roles",
			    member.getRoleIds().stream().map(s -> s.asString()).collect(Collectors.toList()));
		    if (member.getNickname().isPresent()) {
			jsonMember.put("nickname", member.getNickname().get());
		    }
		    jsonMembers.put(member.getId().asString(), jsonMember);
		}

		jsonDatas.put("members", jsonMembers);
		// Webhook
		JSONObject jsonWebhooks = new JSONObject();

		for (Webhook webhook : guild.getWebhooks().toIterable()) {
		    JSONObject jsonWebhook = new JSONObject();

		    Snowflake webhookId = webhook.getId();
		    if (webhook.getName().isPresent()) {
			jsonWebhook.put("name", webhook.getName().get());
		    }
		    if (webhook.getAvatar().isPresent()) {
			jsonWebhook.put("avatar", webhook.getAvatar().get());
		    }
		    jsonWebhook.put("channel", webhook.getChannelId().asString());

		    jsonWebhooks.put(webhookId.asString(), jsonWebhook);
		}

		jsonDatas.put("webhooks", jsonWebhooks);
		// End backup

		dataGuild.addBackup(backupName, jsonDatas);
		dataGuild.inBigTask = false;
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.addField("Backup terminé !", "Nom de la backup : **" + backupName + "**", false);
		}).subscribe();

	    } else {
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.addField("Commande refusé !", "Une autre tâche est en cours !", false);
		}).subscribe();
	    }
	}
    }

    public void restoreBackup(MessageCreateEvent event, List<String> args) {
	Message message = event.getMessage();
	TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
	User userBot = event.getClient().getSelf().block();
	User author = event.getMember().get();

	String backupName = args.get(0);

	if ((backupName == null) || backupName.isEmpty()) {
	    channel.createEmbed(spec -> {
		spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		spec.setColor(Color.ORANGE);
		spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		spec.setTimestamp(message.getTimestamp());

		spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
	    }).subscribe();
	} else {
	    Guild guild = event.getGuild().block();

	    DataGuild dataGuild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

	    if (!dataGuild.inBigTask) {
		dataGuild.inBigTask = true;
		JSONObject jsonBackup = dataGuild.getBackup(backupName);

		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.setDescription("Restauration en cours...");
		}).subscribe();

		// Begin restore

		// Guild
		JSONObject jsonGuild = jsonBackup.getJSONObject("guild");

		guild.edit(spec -> {
		    spec.setName(jsonGuild.getString("name"));
		    if (jsonGuild.has("afk_id")) {
			spec.setAfkChannelId(Snowflake.of(jsonGuild.getString("afk_id")));
		    }
		    spec.setAfkTimeout(jsonGuild.getInt("afk_timeout"));
		    if (jsonGuild.has("banner")) {
			spec.setBanner(Image.ofUrl(jsonGuild.getString("banner")).block());
		    }
		    if (jsonGuild.has("icon")) {
			spec.setIcon(Image.ofUrl(jsonGuild.getString("icon")).block());
		    }
		    spec.setDefaultMessageNotificationsLevel(NotificationLevel.of(jsonGuild.getInt("notification")));
		    spec.setOwnerId(Snowflake.of(jsonGuild.getString("owner")));
		    spec.setRegion(guild.getClient().getRegions()
			    .filter(region -> region.getId().equals(jsonGuild.getString("region"))).blockFirst());
		    if (jsonGuild.has("splash")) {
			spec.setSplash(Image.ofUrl(jsonGuild.getString("splash")).block());
		    }
		    spec.setVerificationLevel(VerificationLevel.of(jsonGuild.getInt("verification")));

		    spec.setReason("backup Restoring !");
		}).block();

		JSONArray jsonBans = jsonGuild.getJSONArray("bans");
		for (int i = 0; i < jsonBans.length(); i++) {
		    JSONObject jsonBan = jsonBans.getJSONObject(i);

		    if (jsonBan.has("reason")) {
			guild.ban(Snowflake.of(jsonBan.getString("user")),
				spec -> spec.setReason(jsonBan.getString("reason"))).block();
		    } else {
			guild.ban(Snowflake.of(jsonBan.getString("user")), spec -> {
			}).block();
		    }
		}

		JSONArray jsonEmojis = jsonGuild.getJSONArray("emojis");
		for (int i = 0; i < jsonEmojis.length(); i++) {
		    JSONObject jsonEmoji = jsonEmojis.getJSONObject(i);

		    guild.createEmoji(spec -> {
			spec.setName(jsonEmoji.getString("name"));
			spec.setImage(Image.ofUrl(jsonEmoji.getString("emoji")).block());

			JSONArray roleIdList = jsonEmoji.getJSONArray("roles");
			for (int n = 0; n < roleIdList.length(); n++) {
			    spec.addRole(Snowflake.of(roleIdList.getString(n)));
			}

			spec.setReason("Restored Emoji");
		    }).block();
		}

		// Roles
		JSONObject jsonRoles = jsonBackup.getJSONObject("roles");

		for (String strRoleId : jsonRoles.keySet()) {
		    JSONObject jsonRole = jsonRoles.getJSONObject(strRoleId);

		    Snowflake roleId = Snowflake.of(strRoleId);
		    Optional<Role> optRole = BotUtils.returnOptional(guild.getRoleById(roleId));

		    if (optRole.isPresent()) {
			Role role = optRole.get();

			role.edit(spec -> {
			    spec.setName(jsonRole.getString("name"));
			    spec.setColor(new Color(jsonRole.getInt("color")));
			    spec.setHoist(jsonRole.getBoolean("hoisted"));
			    spec.setMentionable(jsonRole.getBoolean("mentionable"));
			    spec.setPermissions(PermissionSet.of(jsonRole.getLong("permissions")));

			    spec.setReason("restore role");
			}).onErrorReturn((Predicate<? super Throwable>) e -> {
			    if (e.getClass().getName().equals(ClientException.class.getName())
				    && ((ClientException) e).getStatus().code() == 403) {
				System.err.println("No permission for edit and restore the role '" + role.getName()
					+ "' (" + role.getId().asString() + ")");
				return true;
			    } else
				return false;
			}, role).block();
			role.changePosition(jsonRole.getInt("position")).subscribe();
		    } else {
			Role role = guild.createRole(spec -> {
			    spec.setName(jsonRole.getString("name"));
			    spec.setColor(new Color(jsonRole.getInt("color")));
			    spec.setHoist(jsonRole.getBoolean("hoisted"));
			    spec.setMentionable(jsonRole.getBoolean("mentionable"));
			    spec.setPermissions(PermissionSet.of(jsonRole.getLong("permissions")));

			    spec.setReason("restore role");
			}).block();
			role.changePosition(jsonRole.getInt("position")).subscribe();
		    }
		}

		// channels
		JSONObject jsonChannels = jsonBackup.getJSONObject("channels");

		for (String strChannelId : jsonChannels.keySet()) {
		    JSONObject jsonChannel = jsonChannels.getJSONObject(strChannelId);

		    Snowflake channelId = Snowflake.of(strChannelId);
		    Optional<GuildChannel> channelOpt = BotUtils.returnOptional(guild.getChannelById(channelId));

		    if (channelOpt.isPresent()) {
			GuildChannel guildChannel = channelOpt.get();
			Channel.Type channelType = Channel.Type.of(jsonChannel.getInt("type"));

			if (channelType.equals(Channel.Type.GUILD_CATEGORY)) {
			    Category category = (Category) guildChannel;

			    category.edit(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				spec.setReason("restore category");
			    }).block();

			} else if (channelType.equals(Channel.Type.GUILD_TEXT)) {
			    TextChannel textChannel = (TextChannel) guildChannel;

			    textChannel.edit(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				if (jsonChannel.has("parent_id")) {
				    spec.setParentId(Snowflake.of(jsonChannel.getString("parent_id")));
				}
				spec.setNsfw(jsonChannel.getBoolean("nsfw"));
				spec.setRateLimitPerUser(jsonChannel.getInt("slow"));
				if (jsonChannel.has("topic")) {
				    spec.setTopic(jsonChannel.getString("topic"));
				}

				spec.setReason("restore text channel");
			    }).block();

			} else if (channelType.equals(Channel.Type.GUILD_VOICE)) {
			    VoiceChannel voiceChannel = (VoiceChannel) guildChannel;

			    voiceChannel.edit(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				if (jsonChannel.has("parent_id")) {
				    spec.setParentId(Snowflake.of(jsonChannel.getString("parent_id")));
				}
				spec.setBitrate(jsonChannel.getInt("bitrate"));
				spec.setUserLimit(jsonChannel.getInt("user_limit"));

				spec.setReason("restore voice channel");
			    }).block();

			} else {
			    System.err.println(
				    "[Backup] A channel type is unsupported and cannot be restored : " + channelType);
			}
		    } else {
			Channel.Type channelType = Channel.Type.of(jsonChannel.getInt("type"));

			if (channelType.equals(Channel.Type.GUILD_CATEGORY)) {
			    guild.createCategory(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				spec.setReason("restore category");
			    }).block();

			} else if (channelType.equals(Channel.Type.GUILD_TEXT)) {
			    guild.createTextChannel(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				if (jsonChannel.has("parent_id")) {
				    spec.setParentId(Snowflake.of(jsonChannel.getString("parent_id")));
				}
				spec.setNsfw(jsonChannel.getBoolean("nsfw"));
				spec.setRateLimitPerUser(jsonChannel.getInt("slow"));
				if (jsonChannel.has("topic")) {
				    spec.setTopic(jsonChannel.getString("topic"));
				}

				spec.setReason("restore text channel");
			    }).block();

			} else if (channelType.equals(Channel.Type.GUILD_VOICE)) {
			    guild.createVoiceChannel(spec -> {
				spec.setName(jsonChannel.getString("name"));
				spec.setPosition(jsonChannel.getInt("position"));

				JSONArray jsonPermissions = jsonChannel.getJSONArray("permissions");
				HashSet<PermissionOverwrite> permissions = new HashSet<>(jsonPermissions.length());

				for (int i = 0; i < jsonPermissions.length(); i++) {
				    JSONObject jsonPermission = jsonPermissions.getJSONObject(i);

				    Snowflake id = Snowflake.of(jsonPermission.getString("id"));
				    PermissionOverwrite.Type targetType = PermissionOverwrite.Type
					    .of(jsonPermission.getString("type"));
				    PermissionSet allowedPermissions = PermissionSet
					    .of(jsonPermission.getLong("allowed"));
				    PermissionSet deniedPermissions = PermissionSet
					    .of(jsonPermission.getLong("denied"));

				    if (targetType.equals(PermissionOverwrite.Type.MEMBER)) {

					permissions.add(ExtendedPermissionOverwrite.forMember(id, allowedPermissions,
						deniedPermissions));
				    } else if (targetType.equals(PermissionOverwrite.Type.ROLE)) {

					permissions.add(ExtendedPermissionOverwrite.forRole(id, allowedPermissions,
						deniedPermissions));
				    } else {
					System.err.println(
						"[Backup] A permission type is unsupported and cannot be restored : "
							+ targetType);
				    }
				}
				spec.setPermissionOverwrites(permissions);

				if (jsonChannel.has("parent_id")) {
				    spec.setParentId(Snowflake.of(jsonChannel.getString("parent_id")));
				}
				spec.setBitrate(jsonChannel.getInt("bitrate"));
				spec.setUserLimit(jsonChannel.getInt("user_limit"));

				spec.setReason("restore voice channel");
			    }).block();

			} else {
			    System.err.println(
				    "[Backup] A channel type is unsupported and cannot be restored : " + channelType);
			}
		    }
		}

		// Members
		JSONObject jsonMembers = jsonBackup.getJSONObject("members");
		for (String strMemberId : jsonMembers.keySet()) {
		    JSONObject jsonMember = jsonMembers.getJSONObject(strMemberId);

		    Snowflake memberId = Snowflake.of(strMemberId);
		    Optional<Member> optMember = BotUtils.returnOptional(guild.getMemberById(memberId));

		    if (optMember.isPresent()) {

			try {
			    optMember.get().edit(spec -> {
				JSONArray rolesArray = jsonMember.getJSONArray("roles");
				HashSet<Snowflake> rolesId = new HashSet<>(rolesArray.length());
				for (int i = 0; i < rolesArray.length(); i++) {
				    rolesId.add(Snowflake.of(rolesArray.getString(i)));
				}
				spec.setRoles(rolesId);
				if (jsonMember.has("nickname")) {
				    spec.setNickname(jsonMember.getString("nickname"));
				}

				spec.setReason("restore member");
			    }).block();
			} catch (ClientException e) {
			    if ((e != null) && e.getClass().getName().equals(ClientException.class.getName())
				    && e.getStatus().code() == 403) {
				System.err.println("No permission for edit and restore the member '"
					+ optMember.get().getUsername() + "' (" + optMember.get().getId().asString()
					+ ")");
			    } else
				throw e;
			}

		    } else {
			System.err.println(
				String.format("[Backup] The backup member id '%s' is not in the guild!", strMemberId));
		    }
		}

		// Webhook
		JSONObject jsonWebhooks = jsonBackup.getJSONObject("webhooks");

		for (String strWebhookId : jsonWebhooks.keySet()) {
		    JSONObject jsonWebhook = jsonWebhooks.getJSONObject(strWebhookId);

		    Snowflake webhookId = Snowflake.of(strWebhookId);
		    Optional<Webhook> optWebhook = BotUtils
			    .returnOptional(guild.getWebhooks().filter(w -> w.getId().equals(webhookId)).next());
		    if (optWebhook.isPresent()) {
			Webhook webhook = optWebhook.get();

			webhook.edit(spec -> {
			    if (jsonWebhook.has("name")) {
				spec.setName(jsonWebhook.getString("name"));
				// if(jsonWebhook.has("avatar"))
				// spec.setAvatar(Image.ofRaw(jsonWebhook.getString("avatar").getBytes(),
				// Format.PNG));
			    }

			    spec.setReason("Restore Webhook");
			}).block();
		    } else {
			TextChannel textChannel = guild.getChannelById(Snowflake.of(jsonWebhook.getString("channel")))
				.ofType(TextChannel.class).block();

			textChannel.createWebhook(spec -> {
			    if (jsonWebhook.has("name")) {
				spec.setName(jsonWebhook.getString("name"));
				// if(jsonWebhook.has("avatar"))
				// spec.setAvatar(Image.ofRaw(jsonWebhook.getString("avatar").getBytes(),
				// Format.PNG));
			    }

			    spec.setReason("Restore Webhook");
			}).block();
		    }
		}
		// End backup

		dataGuild.inBigTask = false;
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.addField("Restauration terminé !", "Nom de la backup restauré : **" + backupName + "**",
			    false);
		}).subscribe();

	    } else {
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.addField("Commande refusé !", "Une autre tâche est en cours !", false);
		}).subscribe();
	    }
	}
    }

    public void listBackup(MessageCreateEvent event) {
	Message message = event.getMessage();
	TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
	User userBot = event.getClient().getSelf().block();
	User author = event.getMember().get();
	Guild guild = event.getGuild().block();

	Set<String> backups = Main.getBotInstance().getDataCenter().getDataGuild(guild).listBackup();

	if (backups.size() > 0) {
	    channel.createEmbed(spec -> {
		spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		spec.setColor(Color.ORANGE);
		spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		spec.setTimestamp(message.getTimestamp());

		spec.addField("Liste des backups", String.join("\n", backups), false);
	    }).subscribe();
	} else {
	    channel.createEmbed(spec -> {
		spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		spec.setColor(Color.ORANGE);
		spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		spec.setTimestamp(message.getTimestamp());

		spec.setDescription("Aucune backup enregistré !");
	    }).subscribe();
	}
    }

    public void removeBackup(MessageCreateEvent event, List<String> args) {
	Message message = event.getMessage();
	TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
	User userBot = event.getClient().getSelf().block();
	User author = event.getMember().get();
	Guild guild = event.getGuild().block();

	String backupName = args.get(0);

	if ((backupName == null) || backupName.isEmpty()) {
	    channel.createEmbed(spec -> {
		spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		spec.setColor(Color.ORANGE);
		spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		spec.setTimestamp(message.getTimestamp());

		spec.addField("Syntaxe incorrect !", "le nom doit être valide !", false);
	    }).subscribe();
	} else {

	    DataGuild dataGuild = Main.getBotInstance().getDataCenter().getDataGuild(guild);

	    if (dataGuild.hasBackup(backupName)) {
		dataGuild.removeBackup(backupName);
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.addField("Backup supprimé !", "Nom de la backup supprimé : **" + backupName + "**", false);
		}).subscribe();
	    } else {
		channel.createEmbed(spec -> {
		    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    spec.setColor(Color.ORANGE);
		    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    spec.setTimestamp(message.getTimestamp());

		    spec.setDescription("Aucune backup enregistré avec ce nom : **" + backupName + "**");
		}).subscribe();
	    }
	}
    }

}
