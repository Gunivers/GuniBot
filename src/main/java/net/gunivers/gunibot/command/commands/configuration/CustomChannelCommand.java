package net.gunivers.gunibot.command.commands.configuration;

import static net.gunivers.gunibot.core.custom_channel.CustomChannelCreator.create;
import static net.gunivers.gunibot.core.custom_channel.CustomChannelCreator.getByGuild;
import static net.gunivers.gunibot.core.custom_channel.CustomChannelCreator.getByOwner;
import static net.gunivers.gunibot.core.custom_channel.CustomChannelCreator.removeChannel;
import static net.gunivers.gunibot.core.custom_channel.CustomChannelCreator.renewChannel;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Category;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.Ignore;
import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.core.custom_channel.CustomChannelCreator;
import net.gunivers.gunibot.core.custom_channel.Invitation;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.DataTextChannel;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.EmbedBuilder.Field;

/**
 * @author A~Z<br>
 * @see CustomChannelCreator
 */
@Ignore
public class CustomChannelCommand extends Command {
    public final static String SYSTEM_DISABLED = "Sorry, custom channels are disabled on your server";
    public final static String CONFIG_MISSING = "Your server miss either cchanel.active either cchanel.archive categories configuration";
    public final static String NO_CHANNEL = "You do not own any custom channel!";

    @Override
    public String getSyntaxFile() {
	return "configuration/customchannel.json";
    }

    public void list(MessageCreateEvent event, List<String> args) {
	EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Custom Channel's List", null);
	builder.setRequestedBy(event.getMember().get());

	Field names = new Field("Name");
	Field owners = new Field("Owner");
	Field privacies = new Field("Privacy");
	builder.addField(names);
	builder.addField(owners);
	builder.addField(privacies);

	for (DataTextChannel channel : getByGuild(
		Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block()))) {
	    names.getValue().append(channel.getEntity().getName() + '\n');
	    owners.getValue().append(
		    event.getGuild().block().getMemberById(Snowflake.of(channel.getOwner())).block().getDisplayName()
			    + '\n');
	    privacies.getValue().append(channel.isPrivate() + "\n");
	}

	builder.buildAndSend();
    }

    public void add(MessageCreateEvent event, List<String> args) {
	DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
	if (!g.isCCEnabled()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(SYSTEM_DISABLED)).subscribe();
	    return;
	}

	if (g.getCCActive() == -1L || g.getCCArchive() == -1L) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(CONFIG_MISSING)).subscribe();
	    return;
	}

	if (create(event.getMember().get(),
		Parser.parseCategory(String.valueOf(g.getCCActive()), g.getEntity()).blockFirst(), args.get(0),
		g.getEntity().getChannelById(Snowflake.of(g.getCCArchive())).ofType(Category.class).block(),
		Boolean.valueOf(args.get(1)))) {
	    DataTextChannel channel = getByOwner(event.getMember().get());
	    event.getMessage().getChannel()
		    .flatMap(c -> c.createMessage(channel.getEntity().getMention() + " successfully created!"))
		    .subscribe();
	} else {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Could not create #" + args.get(0)))
		    .subscribe();
	}
    }

    public void del(MessageCreateEvent event) {
	DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
	if (!g.isCCEnabled()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(SYSTEM_DISABLED)).subscribe();
	    return;
	}

	DataTextChannel data = getByOwner(event.getMember().get());
	if (data == null) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(NO_CHANNEL)).subscribe();
	    return;
	}

	if (removeChannel(data)) {
	    event.getMessage().getChannel()
		    .flatMap(c -> c.createMessage("The channel " + data.getEntity().getName() + "was deleted"))
		    .subscribe();
	} else {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Could not delete your custom channel"))
		    .subscribe();
	}
    }

    public void renew(MessageCreateEvent event) {
	DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
	if (!g.isCCEnabled()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(SYSTEM_DISABLED)).subscribe();
	    return;
	}

	if (g.getCCActive() == -1L || g.getCCArchive() == -1L) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(CONFIG_MISSING)).subscribe();
	    return;
	}

	DataTextChannel data = getByOwner(event.getMember().get());
	if (data == null) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(NO_CHANNEL)).subscribe();
	    return;
	}

	if (renewChannel(data, Parser.parseCategory("" + g.getCCActive(), g.getEntity()).blockFirst(),
		Parser.parseCategory("" + g.getCCArchive(), g.getEntity()).blockFirst())) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Renewed " + data.getEntity().getName()))
		    .subscribe();
	} else {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Could not renew your channel!")).subscribe();
	}
    }

    public void privacy(MessageCreateEvent event, List<String> args) {
	DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
	if (!g.isCCEnabled()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(SYSTEM_DISABLED)).subscribe();
	    return;
	}

	DataTextChannel data = getByOwner(event.getMember().get());
	if (data == null) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(NO_CHANNEL)).subscribe();
	    return;
	}

	data.setPrivate(Boolean.valueOf(args.get(0)));
	data.getEntity()
		.addRoleOverwrite(g.getEntity().getId(),
			data.isPrivate()
				? PermissionOverwrite.forRole(g.getEntity().getId(), PermissionSet.none(),
					PermissionSet.of(Permission.VIEW_CHANNEL))
				: PermissionOverwrite.forRole(g.getEntity().getId(),
					PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()))
		.subscribe();

	event.getMessage().getChannel()
		.flatMap(c -> c.createMessage("Your channel is now " + (data.isPrivate() ? "private" : "public")))
		.subscribe();
    }

    public void invite(MessageCreateEvent event, List<String> args) {
	DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
	if (!g.isCCEnabled()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(SYSTEM_DISABLED)).subscribe();
	    return;
	}

	DataTextChannel data = getByOwner(event.getMember().get());
	if (data == null) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage(NO_CHANNEL)).subscribe();
	    return;
	}
	if (!data.isPrivate()) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Your channel is public!")).subscribe();
	    return;
	}

	Parser.parseMember(args.get(0), g.getEntity()).toStream()
		.forEach(m -> Invitation.getInstance(event.getMember().get(), m));
    }
}
