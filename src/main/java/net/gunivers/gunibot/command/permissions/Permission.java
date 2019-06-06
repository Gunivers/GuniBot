package net.gunivers.gunibot.command.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.InvalidNameException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;

public class Permission
{
	public static final HashMap<Permission, ArrayList<Member>> custom = new HashMap<>();
	public static final HashMap<Permission, discord4j.core.object.util.Permission> discord = new HashMap<>();
	public static final HashMap<Role, ArrayList<Permission>> roles = new HashMap<>();
	
	static
	{
		Arrays.asList(discord4j.core.object.util.Permission.values()).forEach(perm ->
			new Permission(perm.name().toLowerCase(), perm));
	}
	
	private final String name;
	
	public Permission(String name)
	{
		this("custom." + name, true);
		custom.putIfAbsent(this, new ArrayList<>());
	}

	public Permission(String name, discord4j.core.object.util.Permission perm)
	{
		this("discord." + name, false);
		discord.putIfAbsent(this, perm);
	}
	
	private Permission(String name, boolean custom)
	{
		if (!name.matches("([a-z_]+\\.)+[a-z_]+"))
			throw new RuntimeException(new InvalidNameException("Permission name should matche '([a-z_]+\\.)+[a-z_]+'"));
		
		this.name = name;
	}
	
	
	public boolean hasPermission(Member user)
	{	
		for (Entry<Role, ArrayList<Permission>> entry : roles.entrySet())
			if (entry.getValue().contains(this) && user.getRoles().collectList().block().contains(entry.getKey()))
				return true;
		
		return (this.isCustom() && custom.get(this).contains(user))
			|| user.getBasePermissions().block().contains(discord.get(this));
	}
	
	
	public boolean isCustom() { return custom.keySet().contains(this); }
	public boolean isDiscord() { return discord.keySet().contains(this); }
	
	public String getName() { return this.name; }

	
	public static ArrayList<Permission> getByName(String name)
	{
		ArrayList<Permission> perms = new ArrayList<>();
		boolean multiple = '*' == name.charAt(name.length() -1);

		if (!name.matches("([a-z_]+\\.)+" + (multiple ? "\\*" : "[a-z_]+"))) return perms;
		
		ArrayList<Permission> permissions = new ArrayList<>(discord.keySet());
		permissions.addAll(custom.keySet());
		
		for (Permission p : permissions)
		{
			if (multiple)
			{
				if (p.getName().matches(name.substring(0, name.length() -2) + "(\\.[a-z_]+)+")) perms.add(p);
			}
			else if (p.getName().equals(name)) perms.add(p);
		}
		
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
