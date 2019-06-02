package net.gunivers.gunibot.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.utils.Util;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public class PermissionCommand extends Command
{
	@Override
	public String getSyntaxFile() { return "permission.json"; }
	
	public void help(MessageCreateEvent event)
	{
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed -> 
		{
			Util.formatEmbed(event, "**Help: permission**", embed);
			
			embed.addField("Command:", "permission", true);
			embed.addField("Aliases:", this.getAliases().toString(), true);
			embed.addField("Description:", this.getDescription(), true);
			embed.addField("Syntax:", "/perm (list|get|set) ...", true);
			embed.addField("Required Permissions:", this.getPersmissions().toString(), true);
			
		})).subscribe();
	}

	public void list(MessageCreateEvent event)
	{
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed ->
		{
			Util.formatEmbed(event, "**Permissions List**", embed);
			
			StringBuilder sb = new StringBuilder();
			Permission.discord.keySet().forEach(p -> sb.append(p.getName() + '\n'));
			embed.addField("*Discord linked permissions*", sb.toString(), false);
			
			sb.setLength(0);
			Permission.custom.keySet().forEach(p -> sb.append(p.getName() + '\n'));
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
		Permission.custom.entrySet().stream().filter(e -> e.getValue().contains(member)).forEach(e -> customs.add(e.getKey()));
		Permission.discord.entrySet().stream().filter(e -> member.getBasePermissions().block().contains(e.getValue()))
			.forEach(e -> discord.add(e.getKey()));
		
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed ->
			formatGetEmbed(event, args.get(0), discord, customs, embed)
			.setAuthor(member.getDisplayName(), null, member.getAvatarUrl())))
		.subscribe();
	}
	
	public void getRole(MessageCreateEvent event, List<String> args)
	{
		Role role = event.getGuild().block().getRoleById(Snowflake.of(args.get(0).replaceAll("<@&|>", ""))).block();
		
		ArrayList<Permission> customs = Permission.roles.get(role);
		ArrayList<Permission> discord = new ArrayList<>();
		Permission.discord.entrySet().stream().filter(e -> role.getPermissions().contains(e.getValue())).forEach(e -> discord.add(e.getKey()));
		
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed ->
			formatGetEmbed(event, args.get(0), discord, customs, embed).setColor(role.getColor()))).subscribe();
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
			users.stream().filter(user -> !Permission.custom.get(perm).contains(user)).forEach(user ->
			{ 
				if(add) Permission.custom.get(perm).add(user);
				else Permission.custom.get(perm).remove(user);
			});
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to users"));
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
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to roles"));
	}
	
	
	private static EmbedCreateSpec formatGetEmbed(MessageCreateEvent event, String owner, ArrayList<Permission> discord,
			ArrayList<Permission> customs, EmbedCreateSpec embed)
	{	
		Util.formatEmbed(event, owner + "'s permissions", embed);
		StringBuilder sb = new StringBuilder();
		
		discord.forEach(perm -> sb.append(perm.getName() + '\n'));
		embed.addField("Discord Permissions", sb.toString(), false);
			
		customs.forEach(perm -> sb.append(perm.getName() + '\n'));
		embed.addField("Bot Permissions", sb.toString(), false);
		
		return embed;
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
