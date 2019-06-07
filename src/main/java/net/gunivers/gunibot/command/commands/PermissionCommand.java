package net.gunivers.gunibot.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.utils.Util;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public class PermissionCommand extends Command
{
	static
	{
		new Permission("perm.test");
	}
	
	@Override
	public String getSyntaxFile() { return "permission.json"; }
	
	public void help(MessageCreateEvent event)
	{
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed -> 
		{
			Util.formatEmbed(event, "Help: permission", embed);
			
			embed.addField("Command:", "permission", true);
			embed.addField("Aliases:", this.getAliases().toString(), true);
			embed.addField("Description:", this.getDescription(), true);
			embed.addField("Syntax:", "/perm list"
					+ "\n/perm get <permission> (true|false) @<user|role>"
					+ "\n/perm set <permissions> (true|false) @<users|roles> ", true);
			embed.addField("Required Permissions:", this.getPermissions().toString(), true);
			
		})).subscribe();
	}

	public void list(MessageCreateEvent event)
	{
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed ->
		{
			Util.formatEmbed(event, "Permissions List", embed);
			
			StringBuilder sb = new StringBuilder();
			Permission.discord.keySet().forEach(p -> sb.append(p.getName() + '\n'));
			embed.addField("*Discord linked permissions*", sb.toString(), false);
			
			sb.setLength(0);
			Permission.bot.keySet().forEach(p -> sb.append(p.getName() + '\n'));
			embed.addField("*Custom permissions*", sb.toString(), false);
			
			for (Entry<Role, ArrayList<Permission>> role : Permission.roles.entrySet())
			{
				sb.setLength(0);
				role.getValue().forEach(p -> sb.append(p.getName() + "; "));
				embed.addField(role.getKey().getMention(), sb.toString(), true);
			}
		})).subscribe();
	}
	
	public void getUser(MessageCreateEvent event, List<String> args)
	{
		Member member = event.getGuild().block().getMemberById(Snowflake.of(args.get(0).replaceAll("<@|!|>", ""))).block();
		
		ArrayList<Permission> customs = new ArrayList<>();
		ArrayList<Permission> discord = new ArrayList<>();
		
		Permission.bot.entrySet().stream().filter(e -> e.getValue().contains(member)).forEach(e -> customs.add(e.getKey()));
		Permission.discord.entrySet().stream().filter(e -> member.getBasePermissions().block().contains(e.getValue()))
			.forEach(e -> discord.add(e.getKey()));
		
		sendGetMessage(event, discord, customs, member.getMention());
	}
	
	public void getRole(MessageCreateEvent event, List<String> args)
	{
		Role role = event.getGuild().block().getRoleById(Snowflake.of(args.get(0).replaceAll("<@&|>", ""))).block();
		
		ArrayList<Permission> customs = Permission.roles.getOrDefault(role, new ArrayList<>());
		ArrayList<Permission> discord = new ArrayList<>();
		Permission.discord.entrySet().stream().filter(e -> role.getPermissions().contains(e.getValue())).forEach(e -> discord.add(e.getKey()));
		
		sendGetMessage(event, discord, customs, role.getMention());
	}
	
	public void setUsers(MessageCreateEvent event, List<String> args)
	{
		Tuple4<ArrayList<Permission>, Boolean, ArrayList<Member>, ArrayList<Role>> tuple = getSetArguments(event, args);
		ArrayList<Permission> perms = tuple.getT1();
		boolean add = tuple.getT2();
		ArrayList<Member> users = tuple.getT3();
		
		if (perms.isEmpty()) {
			event.getMessage().getChannel().flatMap(chan -> chan.createMessage("There is no such permission as "+ args.get(0))).subscribe();
			return;
		}
		
		for (Permission perm : perms)
		{
			users.stream().filter(user -> !Permission.bot.get(perm).contains(user)).forEach(user ->
			{ 
				if(add) Permission.bot.get(perm).add(user);
				else Permission.bot.get(perm).remove(user);
			});
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to users")).subscribe();
	}

	public void setRoles(MessageCreateEvent event, List<String> args)
	{
		Tuple4<ArrayList<Permission>, Boolean, ArrayList<Member>, ArrayList<Role>> tuple = getSetArguments(event, args);
		ArrayList<Permission> perms = tuple.getT1();
		boolean add = tuple.getT2();
		ArrayList<Role> roles = tuple.getT4();
		
		if (perms.isEmpty()) {
			event.getMessage().getChannel().flatMap(chan -> chan.createMessage("There is no such permission as "+ args.get(0))).subscribe();
			return;
		}
		
		for (Role role : roles)
		{
			if (!Permission.roles.keySet().contains(role)) Permission.roles.put(role, new ArrayList<>());
			perms.stream().filter(perm -> !Permission.roles.get(role).contains(perm)).forEach(perm ->
			{
				if (add) Permission.roles.get(role).add(perm);
				else Permission.roles.get(role).remove(perm);
			});
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to roles")).subscribe();
	}
	
	
	private static void sendGetMessage(MessageCreateEvent event, ArrayList<Permission> discord, ArrayList<Permission> customs, String mention)
	{
		StringBuilder sb = new StringBuilder("\nDiscord Permissions:                   Bot Permissions:");
		for (int i = 0; i < discord.size() || i < customs.size(); i++)
		{
			String line = "\n - " + (i < discord.size() ? discord.get(i).getName() : "");
			while (line.length() < 40) line += ' ';
			if (i < customs.size()) line += " - " + customs.get(i).getName();
			sb.append(line);
		}
		
		event.getMessage().getChannel().flatMap(channel -> channel.createMessage("**All permissions of " + mention + "**\n"
				+ "```yaml" + sb.toString() + "```")).subscribe();
	}
	
	private static Tuple4<ArrayList<Permission>, Boolean, ArrayList<Member>, ArrayList<Role>>
		getSetArguments(MessageCreateEvent event, List<String> args)
	{
		ArrayList<Permission> perms = Permission.getByName(args.get(0));
		
		boolean add = Boolean.parseBoolean(args.get(1));

		ArrayList<Member> users = Arrays.asList(args.get(2).split(";")).stream().collect(ArrayList::new, (l,s) ->
		l.add(event.getGuild().block().getMemberById(Snowflake.of(s.replaceAll("<@|!|>", ""))).block()), List::addAll);
		
		ArrayList<Role> roles = Arrays.asList(args.get(2).split(";")).stream().collect(ArrayList::new, (l,s) ->
		l.add(event.getGuild().block().getRoleById(Snowflake.of(s.replaceAll("<@|!|>", ""))).block()), List::addAll);
		
		return Tuples.of(perms, add, users, roles);
	}
}
