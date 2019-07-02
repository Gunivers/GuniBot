package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;
import java.util.HashSet;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;

public class Serializer {

	public final String systemId;
	private HashSet<AttachedDatas<? extends Entity>> datas;

	public Serializer(String system_id) {
		systemId = system_id;
		datas = new HashSet<>();
	}

	public void putGuildData(Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedGuild(id, datas));
	}

	public void putGuildData(Guild guild, HashMap<String,Object> datas) {
		this.datas.add(new AttachedGuild(guild, datas));
	}

	public void putUserData(Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedUser(id, datas));
	}

	public void putUserData(User user, HashMap<String,Object> datas) {
		this.datas.add(new AttachedUser(user, datas));
	}

	public void putTextChannelData(Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedTextChannel(id, datas));
	}

	public void putTextChannelData(TextChannel text_channel, HashMap<String,Object> datas) {
		this.datas.add(new AttachedTextChannel(text_channel, datas));
	}

	public void putVoiceChannelData(Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedVoiceChannel(id, datas));
	}

	public void putVoiceChannelData(VoiceChannel voice_channel, HashMap<String,Object> datas) {
		this.datas.add(new AttachedVoiceChannel(voice_channel, datas));
	}

	public void putCategoryData(Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedCategory(id, datas));
	}

	public void putCategoryData(Category category, HashMap<String,Object> datas) {
		this.datas.add(new AttachedCategory(category, datas));
	}

	public void putMemberData(Snowflake guild_id, Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedMember(guild_id, id, datas));
	}

	public void putMemberData(Member member, HashMap<String,Object> datas) {
		this.datas.add(new AttachedMember(member, datas));
	}

	public void putRoleData(Snowflake guild_id, Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedRole(guild_id, id, datas));
	}

	public void putRoleData(Role role, HashMap<String,Object> datas) {
		this.datas.add(new AttachedRole(role, datas));
	}

	//	public HashMap<String,Object> get(Class<Entity> type, Snowflake id) {
	//		return this.datas.stream().filter(e -> e.type.equals(type) && e.id.equals(id)).findFirst().get().datas;
	//	}

	public HashMap<String,Object> getGuildData(Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(Guild.class) && e.id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getUserData(Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(User.class) && e.id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getTextChannelData(Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(TextChannel.class) && e.id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getVoiceChannelData(Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(VoiceChannel.class) && e.id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getCategoryData(Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(Category.class) && e.id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getMemberData(Snowflake guild_id, Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(Guild.class) && e.id.equals(guild_id) && (e instanceof DoubleAttachedDatas<?,?>) && ((DoubleAttachedDatas<?,?>) e).subType.equals(Member.class) && ((DoubleAttachedDatas<?,?>) e).id.equals(id)).findFirst().get().datas;
	}

	public HashMap<String,Object> getRoleData(Snowflake guild_id, Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(Guild.class) && e.id.equals(guild_id) && (e instanceof DoubleAttachedDatas<?,?>) && ((DoubleAttachedDatas<?,?>) e).subType.equals(Role.class) && ((DoubleAttachedDatas<?,?>) e).id.equals(id)).findFirst().get().datas;
	}

}
