package net.gunivers.gunibot.core.datas.guild;

import org.json.JSONObject;

import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.parsing.commons.NumberParser.LongParser;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

public class WelcomeSystem extends System
{
	private Configuration<String> message;
	private Configuration<Long> channel;

	public WelcomeSystem(DataGuild guild, ConfigurationNode parent)
	{
		super(guild, parent);
		this.message = new Configuration<>(parent, "message", String::trim, Configuration.STRING, "Server: {server} ; User: {user} ; Mention: {user.mention}");
		this.channel = new Configuration<>(parent, "channel", new LongParser(0), "Text Channel ID", 0L);
	}

	public void welcome(Member member)
	{
		if (this.isEnabled() && this.channel.getValue() != null)
		{
			TextChannel tc = super.guild.getEntity().getChannelById(Snowflake.of(this.channel.getValue())).ofType(TextChannel.class).block();
			EmbedBuilder builder = new EmbedBuilder(tc, "Welcome to "+ super.guild.getEntity().getName() +'!', null);

			builder.setDescription(this.message.getDefaultValue()
					.replace("{server}", super.guild.getEntity().getName())
					.replace("{user}", member.getDisplayName())
					.replace("{user.mention}", member.getMention()));

			builder.setColor(member.getClient().getSelf().block().asMember(member.getGuildId()).block().getColor().block());
			builder.buildAndSend();
		}
	}

	@Override
	public void load(JSONObject source)
	{
		if (source == null) source = new JSONObject();
		super.load(source);

		this.message.setValue(source.optString("message", this.message.getDefaultValue()));
		this.channel.setValue(source.optLong("channel", this.channel.getDefaultValue()));
	}

	@Override
	public JSONObject save()
	{
		JSONObject obj = super.save();
		obj.putOpt("message", this.message.getValue());
		obj.putOpt("channel", this.channel.getValue());
		return obj;
	}

	public String getMessage() { return this.message.getValue(); }
	public long getChannel() { return this.channel.getValue(); }

	public void setMessage(String msg) { this.message.setValue(msg); }
	public void setChannel(long channel) { this.channel.setValue(channel); }
}
