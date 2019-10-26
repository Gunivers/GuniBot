package net.gunivers.gunibot.core.datas.guild;

import org.json.JSONObject;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.parsing.commons.BooleanParser;
import net.gunivers.gunibot.core.lib.parsing.commons.NumberParser.LongParser;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

public class WelcomeChannelSystem
{
	private ConfigurationNode parent;
	private Configuration<Boolean> enabled;
	private Configuration<String> message;
	private Configuration<Long> channel;

	public WelcomeChannelSystem(ConfigurationNode parent)
	{
		this.parent = parent;

		this.enabled = new Configuration<>(parent, "enabled", new BooleanParser(), Configuration.BOOLEAN, true);
		this.channel = new Configuration<>(parent, "channel", new LongParser(), "Text Channel", null);
		this.message = new Configuration<>(parent, "message", String::trim, Configuration.STRING, "Server: {server} ; User: {user} ; Mention: {user.mention}");
	}

	public void welcome(Member member)
	{
		DataGuild guild = Main.getBotInstance().getDataCenter().getDataGuild(member.getGuild().block());

		if (this.enabled.getValue() && this.channel.getValue() != null && this.channel.getValue() >= 0)
		{
			TextChannel tc = guild.getEntity().getChannelById(Snowflake.of(this.channel.getValue())).ofType(TextChannel.class).block();

			EmbedBuilder builder = new EmbedBuilder(tc, "Welcome to "+ guild.getEntity().getName() +'!', null);
			builder.setColor(member.getClient().getSelf().block().asMember(member.getGuildId()).block().getColor().block());
			builder.setDescription(this.message.getDefaultValue().replace("{server}", guild.getEntity().getName()).replace("{user}", member.getDisplayName()).replace("{user.mention}", member.getMention()));
			builder.buildAndSend();
		}
	}

	public void load(JSONObject source)
	{
		if (source == null) source = new JSONObject();
		this.enabled.setValue(source.optBoolean("enabled", this.enabled.getDefaultValue()));
		this.message.setValue(source.optString("message", this.message.getDefaultValue()));
		this.channel.setValue(source.optLong("channel", this.channel.getDefaultValue()));
	}

	public JSONObject save()
	{
		JSONObject welcome = new JSONObject();
		welcome.putOpt("enabled", this.enabled.getValue());
		welcome.putOpt("message", this.message.getValue());
		welcome.putOpt("channel", this.channel.getValue());
		return welcome;
	}

	public ConfigurationNode getParent() { return this.parent; }

	public boolean isEnabled() { return this.enabled.getValue(); }
	public String getMessage() { return this.message.getValue(); }
	public long getChannel() { return this.channel.getValue(); }

	public void setEnable(boolean enable) { this.enabled.setValue(enable); }
	public void setMessage(String msg) { this.message.setValue(msg); }
	public void setChannel(long channel) { this.channel.setValue(channel); }

	public String toJSONString() { return this.save().toString(); }
}
