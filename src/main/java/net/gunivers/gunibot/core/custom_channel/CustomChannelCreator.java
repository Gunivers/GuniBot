package net.gunivers.gunibot.core.custom_channel;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.DataTextChannel;
import net.gunivers.gunibot.core.event.Events;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public final class CustomChannelCreator
{
	public static final int DEFAULT_RATE_LIMIT = 2;
	public static final long TIME_BEFORE_ARCHIVAGE = 3; //30 days before archiving
	public static final long TIME_BEFORE_DELETION = 3;  //90 days before deletion

	private static final HashMap<Long, DataTextChannel> channels = new HashMap<>();
	private static final HashMap<DataTextChannel, Disposable> tasks = new HashMap<>();

	/**
	 * Create a unique custom channel for a member.<br>
	 * If any of the specified values is null, return false<br>
	 * If the member already possess a channel, return false<br>
	 * <p>
	 * The channel will be created in the category 'active' and scheduled to be archived if there is no activity during
	 * {@link #TIME_BEFORE_ARCHIVAGE} seconds, and any archived channel is scheduled to be deleted unless renewal before
	 * {@link #TIME_BEFORE_DELETION} seconds.<br>
	 * Furthermore, the owner of this channel will get all permissions on this channel.
	 * 
	 * @param owner
	 * @param active
	 * @param name
	 * @param archive
	 * @return
	 */
	public static boolean create(final Member owner, final Category active, final String name, final Category archive, boolean privacy)
	{
		if (owner == null || active == null || name == null || owner.getGuildId().asLong() != active.getGuildId().asLong()) return false;
		if (channels.containsKey(owner.getId().asLong())) return false;

		owner.getGuild().flatMap(guild -> guild.createTextChannel(channel ->
		{
			channel.setParentId(active.getId());
			channel.setName(name);
			channel.setTopic(owner.getDisplayName() + "'s custom channel!");

			channel.setReason(owner.getMention() + " created a custom channel");
			channel.setRateLimitPerUser(DEFAULT_RATE_LIMIT);

			Set<PermissionOverwrite> perms = new HashSet<>();
			perms.add(PermissionOverwrite.forMember(owner.getId(), PermissionSet.all(), PermissionSet.none()));

			if (privacy)
				perms.add(PermissionOverwrite.forRole(guild.getId(), PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL)));

			channel.setPermissionOverwrites(perms);
		})).doOnNext(channel ->
		{
			DataGuild guild = Main.getBotInstance().getDataCenter().getDataGuild(owner.getGuild().block());
			DataTextChannel data = guild.getDataTextChannel(channel);

			if (!Status.ACTIVE.map.containsKey(guild)) Status.ACTIVE.map.put(guild, new HashSet<>());
			Status.ACTIVE.map.get(guild).add(data);

			data.setOwner(owner);
			data.setPrivate(privacy);

			channels.put(owner.getId().asLong(), data);
			tasks.put(data, CustomChannelCreator.archiveTask(data, archive));
		}).subscribe();

		return true;
	}

	private static Disposable archiveTask(final DataTextChannel channel, final Category archive)
	{
		final Disposable d = Mono.just(channel.getEntity()).delayElement(Duration.ofSeconds(TIME_BEFORE_ARCHIVAGE))
				.subscribe(c ->
				{
					c.edit(spec -> spec.setParentId(archive == null ? null : archive.getId())).subscribe();
					c.getGuild().map(Main.getBotInstance().getDataCenter()::getDataGuild).subscribe(guild ->
					{
						if (!Status.ARCHIVE.map.containsKey(guild)) Status.ARCHIVE.map.put(guild, new HashSet<>());
						Status.ACTIVE.map.get(guild).remove(channel);
						Status.ARCHIVE.map.get(guild).add(channel);
					});

					tasks.get(channel).dispose();
					tasks.replace(channel, CustomChannelCreator.deletionTask(channel));
				});

		Events.getDispatcher().on(MessageCreateEvent.class).filter(event -> event.getMessage().getChannelId().equals(channel.getEntity().getId()))
		.subscribe(event ->
		{
			d.dispose();
			CustomChannelCreator.archiveTask(channel, archive);
		});

		return d;
	}

	private static Disposable deletionTask(final DataTextChannel channel)
	{
		final Disposable d = Mono.just(channel.getEntity()).delayElement(Duration.ofSeconds(TIME_BEFORE_DELETION))
				.subscribe(c ->
				{
					c.delete("Custom channel deletion by inactivity");
					c.getGuild().map(Main.getBotInstance().getDataCenter()::getDataGuild).subscribe(g -> Status.ARCHIVE.map.get(g).remove(channel));
					channels.remove(channel.getOwner());
				});

		return d;
	}

	/**
	 * If 'channel' is archived, move it to the active category and returns true
	 * else return false
	 * @param channel to renew
	 * @param active the category where to move the channel
	 * @param archive the category where to move the channel after {@link #TIME_BEFORE_ARCHIVAGE} passed without activity on the channel
	 */
	public static boolean renewChannel(DataTextChannel channel, Category active, Category archive)
	{
		if (channel == null || active == null || archive == null) return false;

		switch (CustomChannelCreator.getStatus(channel))
		{
		case ARCHIVE:
			tasks.get(channel).dispose();
			tasks.replace(channel, archiveTask(channel, archive));
			channel.getEntity().edit(c -> c.setParentId(active.getId())).subscribe();

			DataGuild guild = Main.getBotInstance().getDataCenter().getDataGuild(channel.getEntity().getGuild().block());
			Status.ARCHIVE.map.get(guild).remove(channel);
			Status.ACTIVE.map.get(guild).add(channel);
			return true;

		default: return false;
		}
	}

	public static boolean removeChannel(DataTextChannel channel)
	{
		if (!tasks.containsKey(channel)) return false;
		tasks.get(channel).dispose();

		channels.remove(channel.getOwner());
		boolean ok = Status.ARCHIVE.map.get(Main.getBotInstance().getDataCenter().getDataGuild(channel.getEntity().getGuild().block())).remove(channel);
		ok = ok ? ok : Status.ACTIVE.map.get(Main.getBotInstance().getDataCenter().getDataGuild(channel.getEntity().getGuild().block())).remove(channel);

		channel.getEntity().delete("Custom channel forcefully deleted").subscribe();
		return ok;
	}

	public static DataTextChannel getByOwner(Member owner) { return owner == null ? null : channels.get(owner.getId().asLong()); }

	public static Status getStatus(DataTextChannel channel)
	{
		for (Status s : Status.values()) for (Set<DataTextChannel> set : s.map.values()) if (set.contains(channel)) return s;
		return Status.UNKNOWN;
	}

	public static Set<DataTextChannel> getByGuild(DataGuild guild)
	{
		Set<DataTextChannel> s = new HashSet<>();
		for (Status status : Status.values()) s.addAll(CustomChannelCreator.getByStatus(guild, status));
		return s;
	}

	public static Set<DataTextChannel> getByStatus(Status status)
	{
		return status.map.values().stream().reduce(new HashSet<>(), (r,s) -> { r.addAll(s); return r; });
	}

	public static Set<DataTextChannel> getByStatus(DataGuild guild, Status status)
	{
		if (!status.map.containsKey(guild)) status.map.put(guild, new HashSet<>());
		return Collections.unmodifiableSet(status.map.get(guild));
	}


	public static enum Status
	{
		ACTIVE("active"),
		ARCHIVE("archive"),
		UNKNOWN("unknown");

		private final String name;
		private final HashMap<DataGuild, Set<DataTextChannel>> map = new HashMap<>();

		private Status(String name) { this.name = name; }

		public String getName() { return name; }
		@Override
		public String toString() { return this.name().toLowerCase(); }
	}
}
