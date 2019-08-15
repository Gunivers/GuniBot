package net.gunivers.gunibot.command.permissions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;

public class Permission
{
	public static final Set<Permission> all = new HashSet<>();
	public static final HashMap<String, Permission> bot = new HashMap<>();
	public static final HashMap<discord4j.core.object.util.Permission, Permission> discord = new HashMap<>();

	static
	{
		Arrays.asList(discord4j.core.object.util.Permission.values()).forEach(perm ->
		new Permission(perm.name().toLowerCase(), perm));

		new Permission("other.everyone", 1);
		new Permission("other.average", 2);
		new Permission("server.moderator", 3);
		new Permission("server.trusted", 5);
		new Permission("server.owner", 6);
		new Permission("bot.trusted", 7);
		new Permission("bot.trainee", 8);
		new Permission("bot.dev", 9);

		discord.get(discord4j.core.object.util.Permission.ADMINISTRATOR).level = 4;
	}

	{
		all.add(this);
	}

	private final String name;
	private int level;

	public static Permission getHighestPermission(Member member)
	{
		DataGuild guild = Main.getBotInstance().getDataCenter().getDataGuild(member.getGuild().block());
		Permission p = Permission.bot.get("other.everyone");

		for (Permission perm : guild.getDataMember(member).getPermissions()) if (perm.higherThan(p)) p = perm;

		for (Role role : member.getRoles().toIterable())
			for (Permission perm : guild.getDataRole(role).getPermissions()) if (perm.higherThan(p)) p = perm;

		return p;
	}

	public static Set<Permission> getByName(String name)
	{
		boolean multiple = '*' == name.charAt(name.length() -1);
		if (!name.matches("([a-z_]+\\.)+" + (multiple ? "\\*" : "[a-z_]+"))) return new HashSet<>();

		return all.stream().filter(p -> (multiple && p.getName().matches(name.substring(0, name.length() -2) + "(\\.[a-z_]+)+"))
				|| p.getName().equals(name)).collect(Collectors.toSet());
	}

	public static discord4j.core.object.util.Permission get(Permission perm)
	{
		Optional<Entry<discord4j.core.object.util.Permission,Permission>> p = discord.entrySet().stream().filter(e -> e.getValue() == perm)
				.findFirst();

		if (p.isPresent()) return p.get().getKey();
		return null;
	}

	public static boolean hasPermissions(Member member, Set<Permission> perms)
	{
		for (Permission perm : perms) if (!perm.hasPermission(member)) return false;
		return true;
	}

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
		discord.putIfAbsent(perm, this);
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
		DataGuild data = Main.getBotInstance().getDataCenter().getDataGuild(user.getGuild().block());
		if (user.getRoles().toStream().anyMatch(role -> data.getDataRole(role).getPermissions().contains(this))) return true;

		DataMember member = data.getDataMember(user);
		return member.getPermissions().contains(this) || user.getBasePermissions().block().contains(Permission.get(this));
	}

	public boolean higherThan(Permission perm)
	{
		if (this.isFromDiscord()) return false;
		return this.level > perm.level;
	}

	public boolean isFromBot() { return bot.values().contains(this); }
	public boolean isFromDiscord() { return discord.values().contains(this); }

	public String getName() { return this.name; }
	public int getLevel() { return level; }

	@Override
	public boolean equals(Object perm)
	{
		if (perm instanceof Permission)
			return ((Permission) perm).name.equals(this.name) && ((Permission) perm).level == this.level;

		return false;
	}
}
