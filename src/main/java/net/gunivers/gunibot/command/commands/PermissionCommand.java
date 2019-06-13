package net.gunivers.gunibot.command.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.az.lib.EmbedBuilder;
import net.gunivers.gunibot.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.az.lib.SimpleParser;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;
import net.gunivers.gunibot.datas.DataRole;
import net.gunivers.gunibot.syl2010.lib.parser.Parser;
import reactor.core.publisher.Flux;

public class PermissionCommand extends Command
{
	@Override
	public String getSyntaxFile() { return "permission.json"; }

	public void list(MessageCreateEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder(event, "Permission List", null, null, null, event.getMember().get().getColor().block(), null);
		builder.setRequestedBy(event.getMember().orElse(null));
		
		Field discord = new Field("Discord Built-ins");
		discord.setValue(Permission.discord.keySet().stream().map(Permission.discord::get).map(Permission::getName)
				.reduce("", (r,s) -> r += s + '\n'));
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
		
		builder.buildAndSend();
	}
	
	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		Flux<DataMember> members = Parser.parseMember(args.get(0), g.getEntity()).map(g::getDataMember);
		Flux<DataRole> roles = Parser.parseRole(args.get(0), g.getEntity()).map(g::getDataRole);
		
		if (members.blockFirst() == null && roles.blockFirst() == null) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage(args.get(0) + " did not match for any user nor role.")).subscribe();
			return;
		}
		
		EmbedBuilder builder = new EmbedBuilder(event, "Permissions of: ", null); builder.setRequestedBy(event.getMember().get());
		for (DataMember m : members.toIterable())
		{
			Field f = new Field("User: " + m.getEntity().getDisplayName());
			f.setValue(m.getPermissions().stream().map(Permission::getName).reduce("", (r,s) -> r += " - "+ s +'\n'));
			m.getEntity().getBasePermissions().block().forEach(p -> f.getValue().append(" - "+ Permission.discord.get(p).getName() +'\n'));
			builder.addField(f);
		}
		
		for (DataRole r : roles.toIterable())
		{
			Field f = new Field("Role: " + r.getEntity().getName());
			f.setValue(r.getPermissions().stream().map(Permission::getName).reduce("", (a,b) -> a += " - "+ b +'\n'));
			r.getEntity().getPermissions().forEach(p -> f.getValue().append(" - "+ Permission.discord.get(p).getName() +'\n'));
			builder.addField(f);
		}
		
		builder.buildAndSend();
	}

	public void set(MessageCreateEvent event, List<String> args)
	{
		boolean add = Boolean.parseBoolean(args.get(1));
		DataGuild g =  Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		Set<Permission> perms = SimpleParser.parseList(args.get(0)).stream().map(Permission::getByName).reduce(new HashSet<>(), (l,s) -> {l.addAll(s); return l;});
		
		//If the level of any permission is higher than the highest level of the user
		int level = Permission.getHighestPermission(event.getMember().get()).getLevel();
		for (Permission p : perms) if (p.getLevel() > level) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("The permission '"+ p.getName() +"' is of level "+ p.getLevel()
					+ "\nYou may not access permissions of higher level than "+ level)).subscribe();
			return;
		}
		
		EmbedBuilder builder = new EmbedBuilder(event, "Success!", null);
		builder.setRequestedBy(event.getMember().get());
		builder.addField("Permissions changed:", perms.stream().map(Permission::getName).reduce("", (a,b) -> a + " - " + b + '\n'), true);
		
		//Managing users permissions
		List<DataMember> users = SimpleParser.parseList(args.get(2)).stream().flatMap(s -> Parser.parseMember(s, g.getEntity()).toStream()).map(g::getDataMember).collect(Collectors.toList());
		Field u = new Field("Users:");
		for (DataMember user : users) {
			if (add) user.getPermissions().addAll(perms);
			else user.getPermissions().removeAll(perms);
			u.getValue().append(" - " + user.getEntity().getDisplayName() + '\n');
		}
		
		//Managing roles permissions
		List<DataRole> roles = SimpleParser.parseList(args.get(2)).stream().flatMap(s -> Parser.parseRole(s, g.getEntity()).toStream()).map(g::getDataRole).collect(Collectors.toList());
		Field r = new Field("Roles:");
		for (DataRole role : roles) {
			if (add) role.getPermissions().addAll(perms);
			else role.getPermissions().removeAll(perms);
			r.getValue().append(" - " + role.getEntity().getName() + '\n');
		}
		
		builder.addField(u);
		builder.addField(r);
		builder.buildAndSend();
	}
}
