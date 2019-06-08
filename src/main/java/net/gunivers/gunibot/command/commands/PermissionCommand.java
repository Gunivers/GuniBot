package net.gunivers.gunibot.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.az.lib.EmbedBuilder;
import net.gunivers.gunibot.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.datas.DataGuild;
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
		EmbedBuilder builder = new EmbedBuilder(event, "Permission List", null, null, null, event.getMember().get().getColor().block(), null);
		
		Field discord = new Field("Discord Built-ins");
		discord.setValue(Permission.discord.keySet().stream().map(Permission::getName).reduce("", (r,s) -> r += s + '\n'));
		builder.addField(discord);
		
		Field bot = new Field("Bot Customized");
		bot.setValue(Permission.bot.values().stream().sorted((a,b) -> a.higherThan(b) ? 1 : 0).map(Permission::getName).reduce("", (r,s) -> r += s + '\n'));
		builder.addField(bot);
		
		DataGuild guild = Main.getDataCenter().getDataGuild(event.getGuild().block());
		for (Role role : event.getGuild().block().getRoles().toIterable())
		{
			Field f = new Field(role.getName());
			f.setValue(guild.getDataRole(role).getPermissions().stream().map(Permission::getName).reduce("", (r,s) -> r += s + '\n'));
			builder.addField(f);
		}
		
		builder.setFooter("Generated with EmbedBuilder");
		builder.buildAndSend();
	}
	
	public void getUser(MessageCreateEvent event, List<String> args)
	{
		Member member = event.getGuild().block().getMemberById(Snowflake.of(args.get(0).replaceAll("<@|!|>", ""))).block();
		DataGuild guild = Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		Set<Permission> bots = guild.getDataMember(member).getPermissions();
		ArrayList<Permission> discord = new ArrayList<>();
		
		Permission.discord.entrySet().stream().filter(e -> member.getBasePermissions().block().contains(e.getValue()))
			.forEach(e -> discord.add(e.getKey()));
		
		sendGetMessage(event, discord, new ArrayList<>(bots), member.getMention());
	}
	
	public void getRole(MessageCreateEvent event, List<String> args)
	{
		Role role = event.getGuild().block().getRoleById(Snowflake.of(args.get(0).replaceAll("<@&|>", ""))).block();
		DataGuild guild = Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		Set<Permission> bots = guild.getDataRole(role).getPermissions();
		ArrayList<Permission> discord = new ArrayList<>();
		Permission.discord.entrySet().stream().filter(e -> role.getPermissions().contains(e.getValue())).forEach(e -> discord.add(e.getKey()));
		
		sendGetMessage(event, discord, new ArrayList<>(bots), role.getMention());
	}
	
	public void setUsers(MessageCreateEvent event, List<String> args)
	{
		Tuple4<ArrayList<Permission>, Boolean, ArrayList<Member>, ArrayList<Role>> tuple = getSetArguments(event, args);
		ArrayList<Permission> perms = tuple.getT1();
		boolean add = tuple.getT2();
		ArrayList<Member> users = tuple.getT3();
		
		if (perms.isEmpty())
		{
			event.getMessage().getChannel().flatMap(chan -> chan.createMessage("There is no such permission as "+ args.get(0))).subscribe();
			return;
		}
		
		DataGuild guild = Main.getDataCenter().getDataGuild(event.getGuild().block());
		for (Member member : users)
		{
			if (add) guild.getDataMember(member).getPermissions().addAll(perms);
			else guild.getDataMember(member).getPermissions().removeAll(perms);
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to users")).subscribe();
	}

	public void setRoles(MessageCreateEvent event, List<String> args)
	{
		Tuple4<ArrayList<Permission>, Boolean, ArrayList<Member>, ArrayList<Role>> tuple = getSetArguments(event, args);
		ArrayList<Permission> perms = tuple.getT1();
		boolean add = tuple.getT2();
		ArrayList<Role> roles = tuple.getT4();
		
		if (perms.isEmpty())
		{
			event.getMessage().getChannel().flatMap(chan -> chan.createMessage("There is no such permission as "+ args.get(0))).subscribe();
			return;
		}
		
		DataGuild guild = Main.getDataCenter().getDataGuild(event.getGuild().block());
		for (Role role : roles)
		{
			if (add) guild.getDataRole(role).getPermissions().addAll(perms);
			else guild.getDataRole(role).getPermissions().removeAll(perms);
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage("Successfully assigned permissions to roles")).subscribe();
	}
	
	
	private static void sendGetMessage(MessageCreateEvent event, ArrayList<Permission> discord, ArrayList<Permission> bots, String mention)
	{
		StringBuilder sb = new StringBuilder("\nDiscord Permissions:                   Bot Permissions:");
		for (int i = 0; i < discord.size() || i < bots.size(); i++)
		{
			String line = "\n - " + (i < discord.size() ? discord.get(i).getName() : "");
			while (line.length() < 40) line += ' ';
			if (i < bots.size()) line += " - " + bots.get(i).getName();
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
