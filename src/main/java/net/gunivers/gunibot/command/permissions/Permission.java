package net.gunivers.gunibot.command.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InvalidNameException;

import discord4j.core.object.entity.Member;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;

public class Permission
{
	public static final Set<Permission> bot = new HashSet<>();
	public static final HashMap<Permission, discord4j.core.object.util.Permission> discord = new HashMap<>();
	
	static
	{
		Arrays.asList(discord4j.core.object.util.Permission.values()).forEach(perm ->
			new Permission(perm.name().toLowerCase(), perm));
		
		new Permission("bot.dev");
	}
	
	private final String name;
	
	public Permission(String name)
	{
		this(name, true);
		bot.add(this);
	}

	private Permission(String name, discord4j.core.object.util.Permission perm)
	{
		this("discord." + name, false);
		discord.putIfAbsent(this, perm);
	}
	
	private Permission(String name, boolean bot)
	{
		if (!name.matches("([a-z_]+\\.)+[a-z_]+"))
			throw new RuntimeException(new InvalidNameException("Permission name should matche '([a-z_]+\\.)+[a-z_]+'"));
		
		this.name = name;
	}
	
	
	public boolean hasPermission(Member user)
	{
		DataGuild data = Main.getDataCenter().getDataGuild(user.getGuild().block());
		if (user.getRoles().any(role -> data.getDataRole(role).getPermissions().contains(this)).block()) return true;

		DataMember member = data.getDataMember(user);
		return member.getPermissions().contains(this) || user.getBasePermissions().block().contains(discord.get(this));
	}
	
	
	public boolean isFromBot() { return bot.contains(this); }
	public boolean isFromDiscord() { return discord.keySet().contains(this); }
	
	public String getName() { return this.name; }

	
	public static ArrayList<Permission> getByName(String name)
	{
		ArrayList<Permission> perms = new ArrayList<>();
		
		boolean multiple = '*' == name.charAt(name.length() -1);
		if (!name.matches("([a-z_]+\\.)+" + (multiple ? "\\*" : "[a-z_]+"))) return perms;
		
		ArrayList<Permission> permissions = new ArrayList<>(discord.keySet());
		permissions.addAll(bot);
		
		permissions.stream().filter(p -> (multiple && p.getName().matches(name.substring(0, name.length() -2) + "(\\.[a-z_]+)+"))
				|| p.getName().equals(name)).forEach(perms::add);
		
		return perms;
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
