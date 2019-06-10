package net.gunivers.gunibot.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.az.lib.EmbedBuilder;
import net.gunivers.gunibot.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.az.lib.SimpleParser;
import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;
import net.gunivers.gunibot.datas.DataRole;
import net.gunivers.gunibot.syl2010.lib.parser.Parser;

public class PermissionCommand extends Command
{
	@Override
	public String getSyntaxFile() { return "permission.json"; }

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
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		DataMember m = g.getDataMember(Parser.parseMember(args.get(0), g.getEntity()).blockFirst());
		
		EmbedBuilder builder = new EmbedBuilder(event, "Permissions of " + m.getEntity().getDisplayName(), null);
		builder.setAuthor(m.getEntity()); builder.setAuthor(null);
		
		Field discord = new Field("Discord Permissions"); 
		discord.setValue(Permission.discord.keySet().stream().filter(p -> p.hasPermission(m.getEntity())).map(Permission::getName)
				.reduce(" - ", (r,s) -> r += "\n - " + s));
		
		builder.addField(discord);
		builder.addField("Bot Permissions", m.getPermissions().stream().map(Permission::getName).reduce("", (r,s) -> r += " - "+ s +'\n'), true);
		
		builder.buildAndSend();
	}
	
	public void getRole(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		DataRole r = g.getDataRole(Parser.parseRole(args.get(0), g.getEntity()).blockFirst());
		
		EmbedBuilder builder = new EmbedBuilder(event, "Permissions of " + r.getEntity().getName(), null);
		builder.setAuthor(event.getMember().get()); builder.setAuthor(null);
		
		Field discord = new Field("Discord Permissions"); 
		discord.setValue(Permission.discord.keySet().stream().filter(p -> r.getEntity().getPermissions().contains(Permission.discord.get(p)))
				.map(Permission::getName).reduce(" - ", (a,b) -> a += "\n - " + b));
		
		builder.addField(discord);
		builder.addField("Bot Permissions", r.getPermissions().stream().map(Permission::getName).reduce(" - ", (a,b) -> a += "\n - " + b), true);
	
		builder.buildAndSend();
	}
	
	public void setUsers(MessageCreateEvent event, List<String> args)
	{
		DataGuild g =  Main.getDataCenter().getDataGuild(event.getGuild().block());
		boolean add = Boolean.parseBoolean(args.get(1));
		
		List<Permission> perms = SimpleParser.parseList(args.get(0)).stream().map(Permission::getByName)
				.reduce(new ArrayList<Permission>(), (l,s) -> {l.addAll(s); return l;});
		
		int level = Permission.getHighestPermission(event.getMember().get()).getLevel();
		for (Permission p : perms) if (p.getLevel() > level)
		{
			event.getMessage().getChannel().flatMap(c -> c.createMessage("The permission '"+ p.getName() +"' is of level "+ p.getLevel()
					+ "\nYou may not access permissions of higher level than "+ level)).subscribe();
			return;
		}
		
		List<Member> users = SimpleParser.parseList(args.get(2)).stream().map(s -> Parser.parseMember(s, g.getEntity()).toStream()
				.collect(Collectors.toList())).reduce(new ArrayList<>(), (l,s) -> { l.addAll(s); return l; });
		
		for (Member user : users)
		{
			if (add) g.getDataMember(user).getPermissions().addAll(perms);
			else g.getDataMember(user).getPermissions().removeAll(perms);
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage(
				"Successfully assigned permissions "+ args.get(0) +" to members " + args.get(2))).subscribe();
	}

	public void setRoles(MessageCreateEvent event, List<String> args)
	{
		DataGuild g =  Main.getDataCenter().getDataGuild(event.getGuild().block());
		boolean add = Boolean.parseBoolean(args.get(1));
		
		List<Permission> perms = SimpleParser.parseList(args.get(0)).stream().map(Permission::getByName)
				.reduce(new ArrayList<Permission>(), (l,s) -> {l.addAll(s); return l;});
		
		int level = Permission.getHighestPermission(event.getMember().get()).getLevel();
		for (Permission p : perms) if (p.getLevel() > level)
		{
			event.getMessage().getChannel().flatMap(c -> c.createMessage("The permission '"+ p.getName() +"' is of level "+ p.getLevel()
					+ "\nYou may not access permissions of higher level than "+ level)).subscribe();
			return;
		}
		
		List<Role> roles = SimpleParser.parseList(args.get(2)).stream().map(s -> Parser.parseRole(s, g.getEntity()).blockFirst())
				.collect(Collectors.toList());
		
		for (Role role : roles)
		{
			if (add) g.getDataRole(role).getPermissions().addAll(perms);
			else g.getDataRole(role).getPermissions().removeAll(perms);
		}
		
		event.getMessage().getChannel().flatMap(chan -> chan.createMessage(
				"Successfully assigned permissions "+ args.get(0) +" to roles " + args.get(2))).subscribe();
	}
}
