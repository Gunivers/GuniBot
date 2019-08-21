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
import net.gunivers.gunibot.datas.DataRole;

public class Permission
{
	public static final Set<Permission> all = new HashSet<>();
	public static final HashMap<String, Permission> bot = new HashMap<>();
	public static final HashMap<discord4j.core.object.util.Permission, Permission> discord = new HashMap<>();

	public static final Permission EVERYONE			= new Permission("perm.everyone", 1);
	public static final Permission SERVER_MODERATOR	= new Permission("server.moderator", 3);
	public static final Permission SERVER_TRUSTED	= new Permission("server.trusted", 5);
	public static final Permission SERVER_OWNER		= new Permission("server.owner", 6);
	public static final Permission BOT_TRUSTED		= new Permission("bot.trusted", 8);
	public static final Permission BOT_DEV			= new Permission("bot.dev", 10);
	
	static
	{
		Arrays.asList(discord4j.core.object.util.Permission.values()).forEach(perm ->
			new Permission(perm.name().toLowerCase(), perm));

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
		Permission p = new Permission(0, "you.should.not.see.this");
		
		DataMember dm = guild.getDataMember(member);
		dm.recalculatePermissions();
		
		for (Permission perm : dm.getPermissions()) if (perm.higherThan(p)) p = perm;

		for (Role role : member.getRoles().toIterable())
		{
			DataRole dr = guild.getDataRole(role);
			dr.recalculatePermissions();
			
			for (Permission perm : dr.getPermissions())
				if (perm.higherThan(p)) p = perm;
		}

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
		this(level, name);
		bot.putIfAbsent(name, this);
	}

	private Permission(String name, discord4j.core.object.util.Permission perm)
	{
		this(9, "discord." + name);
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
