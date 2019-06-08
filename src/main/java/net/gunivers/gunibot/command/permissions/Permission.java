package net.gunivers.gunibot.command.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import javax.naming.InvalidNameException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;

public class Permission
{
	public static final HashMap<String, Permission> bot = new HashMap<>();
	public static final HashMap<Permission, discord4j.core.object.util.Permission> discord = new HashMap<>();
	
	static
	{
		Arrays.asList(discord4j.core.object.util.Permission.values()).forEach(perm ->
			new Permission(perm.name().toLowerCase(), perm));
		
		new Permission("other.everyone", 1);
		new Permission("other.average", 2);
		new Permission("server.moderator", 3);
		new Permission("server.administrator", 4);
		new Permission("server.trusted", 5);
		new Permission("server.owner", 6);
		new Permission("bot.trusted", 7);
		new Permission("bot.trainee", 8);
		new Permission("bot.dev", 9);
		
		Permission.getPermission(discord4j.core.object.util.Permission.ADMINISTRATOR).level = 5;
	}

	private final String name;
	private int level;

	/**
	 * @param level of permission, where:
	 * <ol>
	 * <li> Everyone
	 * <li> Average
	 * <li> Moderator
	 * <li> Administrator [linked with discord.administrator]
	 * <li> Server Trusted
	 * <li> Server Owner
	 * <li> Trusted
	 * <li> Assistant Developper
	 * <li> Bot Developper
	 * </ol>
	 */
	public Permission(String name, int level)
	{
		this(-1, name);
		bot.put(name, this);
	}

	private Permission(String name, discord4j.core.object.util.Permission perm)
	{
		this(10, "discord." + name);
		discord.putIfAbsent(this, perm);
	}
	
	private Permission(int level, String name)
	{
		if (!name.matches("([a-z_]+\\.)+[a-z_]+"))
			throw new RuntimeException(new InvalidNameException("Permission name should matche '([a-z_]+\\.)+[a-z_]+'"));
		
		this.name = name;
		this.level = level;
	}


	public boolean hasPermission(Member user)
	{
		DataGuild data = Main.getDataCenter().getDataGuild(user.getGuild().block());
		if (user.getRoles().toStream().anyMatch(role -> data.getDataRole(role).getPermissions().contains(this))) return true;

		DataMember member = data.getDataMember(user);
		return member.getPermissions().contains(this) || user.getBasePermissions().block().contains(discord.get(this));
	}
	
	public boolean higherThan(Permission perm)
	{
		if (this.isFromDiscord() && this != Permission.getPermission(discord4j.core.object.util.Permission.ADMINISTRATOR)) return false;
		return this.level > perm.level;
	}
	
	
	public boolean isFromBot() { return bot.values().contains(this); }
	public boolean isFromDiscord() { return discord.keySet().contains(this); }
	
	public String getName() { return this.name; }
	public int getLevel() { return level; }

	
	public static Permission getHighestPermission(Member member)
	{
		DataGuild guild = Main.getDataCenter().getDataGuild(member.getGuild().block());
		Permission p = Permission.bot.get("other.everyone");
		
		for (Permission perm : bot.values()) if (guild.getDataMember(member).getPermissions().contains(p) && perm.higherThan(p)) p = perm;
		
		for (Role role : member.getRoles().toIterable())
			for (Permission perm : guild.getDataRole(role).getPermissions()) if (perm.higherThan(p)) p = perm;

		return p;
	}
	
	public static ArrayList<Permission> getByName(String name)
	{
		ArrayList<Permission> perms = new ArrayList<>();
		
		boolean multiple = '*' == name.charAt(name.length() -1);
		if (!name.matches("([a-z_]+\\.)+" + (multiple ? "\\*" : "[a-z_]+"))) return perms;
		
		ArrayList<Permission> permissions = new ArrayList<>(discord.keySet());
		permissions.addAll(bot.values());
		
		permissions.stream().filter(p -> (multiple && p.getName().matches(name.substring(0, name.length() -2) + "(\\.[a-z_]+)+"))
				|| p.getName().equals(name)).forEach(perms::add);
		
		return perms;
	}
	
	public static Permission getPermission(discord4j.core.object.util.Permission perm)
	{
		return discord.entrySet().stream().filter(e -> e.getValue() == perm).findFirst().get().getKey();
	}

	public static boolean hasPermissions(Member member, Set<Permission> perms)
	{
		for (Permission perm : perms)
			if (!perm.hasPermission(member)) return false;
			
		return true;
	}
	
	@Override
	public boolean equals(Object perm)
	{
		if (perm instanceof Permission)
			return ((Permission) perm).getName().equals(this.name);
		
		return false;
	}
}
