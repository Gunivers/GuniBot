package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Ban;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.entity.Channel.Type;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.util.Image.Format;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
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

			if(!data_guild.inBackup) {
				data_guild.inBackup = true;
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
				if(guild.getBannerUrl(Format.GIF).isPresent()) json_guild.put("banner", guild.getBannerUrl(Format.GIF).get());

				JSONArray json_bans = new JSONArray();
				for(Ban ban:guild.getBans().toIterable()) {
					JSONObject json_ban = new JSONObject();

					json_ban.put("user", ban.getUser().getId().asString());
					if(ban.getReason().isPresent()) json_ban.put("reason", ban.getReason().get());

					json_bans.put(json_ban);
				}
				json_guild.put("bans", json_bans);

				json_guild.put("content_filter", guild.getContentFilterLevel().getValue());
				if(guild.getEmbedChannelId().isPresent()) json_guild.put("embed_channel", guild.getEmbedChannelId().get().asString());

				JSONArray json_emojis = new JSONArray();
				for(GuildEmoji emoji:guild.getEmojis().toIterable()) {
					JSONObject json_emoji = new JSONObject();

					json_emoji.put("name", emoji.getName());
					json_emoji.put("emoji", emoji.getImageUrl());
					json_emoji.put("roles", emoji.getRoleIds().stream().map(Snowflake::asString).collect(Collectors.toList()));

					json_emojis.put(json_emoji);
				}
				json_guild.put("emojis", json_emojis);

				if(guild.getIconUrl(Format.GIF).isPresent()) json_guild.put("icon", guild.getIconUrl(Format.GIF).get());
				json_guild.put("mfa", guild.getMfaLevel().getValue());
				json_guild.put("notification", guild.getNotificationLevel().getValue());
				json_guild.put("owner", guild.getOwnerId().asString());
				json_guild.put("region", guild.getRegionId());
				if(guild.getSplashUrl(Format.GIF).isPresent()) json_guild.put("splash", guild.getSplashUrl(Format.GIF).get());
				if(guild.getSystemChannelId().isPresent()) json_guild.put("system_channel", guild.getSystemChannelId().get());
				json_guild.put("verification", guild.getVerificationLevel().getValue());
				if(guild.getWidgetChannelId().isPresent()) json_guild.put("widget_channel", guild.getWidgetChannelId().get());

				json_datas.put("guild", json_guild);
				// Roles
				JSONObject json_roles = new JSONObject();

				for(Role role:guild.getRoles().toIterable()) {
					JSONObject json_role = new JSONObject();

					Snowflake role_id = role.getId();
					json_role.put("name", role.getName());
					json_role.put("color", role.getColor().getRGB());
					json_role.put("position",role.getRawPosition());
					json_role.put("everyone", role.isEveryone());
					json_role.put("hoisted", role.isHoisted());
					json_role.put("managed", role.isManaged());
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

					Type channel_type = guild_channel.getType();
					json_channel.put("type", channel_type.getValue());

					if(channel_type.equals(Type.GUILD_CATEGORY)) {
						//Category category = (Category) guild_channel;

					} else if(channel_type.equals(Type.GUILD_TEXT)) {
						TextChannel text_channel = (TextChannel) guild_channel;

						if(text_channel.getCategoryId().isPresent()) json_channel.put("parent_id", text_channel.getCategoryId().get().asString());
						json_channel.put("slow" ,text_channel.getRateLimitPerUser());
						json_channel.put("nsfw",text_channel.isNsfw());
						if(text_channel.getTopic().isPresent()) json_channel.put("topic", text_channel.getTopic().get());
					} else if(channel_type.equals(Type.GUILD_VOICE)) {
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
					json_webhook.put("creator", webhook.getCreatorId().asString());
					json_webhook.put("token", webhook.getToken());

					json_webhooks.put(webhook_id.asString(), json_webhook);
				}

				json_datas.put("webhooks", json_webhooks);
				//End backup

				data_guild.addBackup(backup_name, json_datas);
				data_guild.inBackup = false;
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

					spec.addField("Commande refusé !", "Backup déjà en cours !", false);
				}).subscribe();
			}
		}
	}

	public void restoreBackup(MessageCreateEvent event, List<String> args) {

	}

	public void listBackup(MessageCreateEvent event) {

	}

	public void removeBackup(MessageCreateEvent event, List<String> args) {

	}

}
