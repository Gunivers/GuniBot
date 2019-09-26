package net.gunivers.gunibot.core.feature.auto_vocal_channel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class VoiceChannelCreator {

	private static Set<Snowflake> voiceChannels = new HashSet<>();
	private static Map<Snowflake, Snowflake> createdChannels = new HashMap<>();
	private static Map<Snowflake, Disposable> removingChannels = new HashMap<>();
	private static List<String> names = new ArrayList<>();
	
	private final static int DELETE_DELAY = 300000; 
	
	public static void addVoiceChannelCreator(VoiceChannel... voiceChannel) {
		voiceChannels.addAll(Arrays.asList(voiceChannel).stream().map(vc -> vc.getId()).collect(Collectors.toList()));
	}
	
	public static boolean isVoiceChannelCreator(VoiceChannel voiceChannel) {
		return voiceChannels.contains(voiceChannel.getId());
	}
	
	public static void removeVoiceChannelCreator(VoiceChannel... voiceChannel) {
		voiceChannels.removeAll(Arrays.asList(voiceChannel).stream().map(vc -> vc.getId()).collect(Collectors.toList()));
	}
	
	public static Set<Snowflake> getVoiceChannelCreator() {
		return new HashSet<>(voiceChannels);
	}
	
	public static void init(DiscordClient dc) {
		try {
			names = Files.lines(Paths.get(VoiceChannelCreator.class.getResource("/other/bdd name").toURI())).collect(Collectors.toList());
			Collections.shuffle(names);
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		//On vérifie s'il s'agit d'une connexion, si oui, on ne garde que les salons définis comme étant Creator
		dc.getEventDispatcher().on(VoiceStateUpdateEvent.class)
			.filter(e -> e.getCurrent().getChannelId().isPresent() && voiceChannels.contains(e.getCurrent().getChannelId().get()))
			.subscribe(e -> joinEvent(e.getCurrent().getUser().block().asMember(e.getCurrent().getGuildId()).block(), e.getCurrent().getChannel().block()));
		
		//On vérifie s'il s'agit d'une connexion, si oui, on ne garde que les salons définis comme étant créés et étant en cours de suppression
		dc.getEventDispatcher().on(VoiceStateUpdateEvent.class)
				.filter(e -> e.getCurrent().getChannelId().isPresent() && createdChannels.containsValue(e.getCurrent().getChannelId().get()) && removingChannels.containsKey(e.getCurrent().getChannelId().get()))
				.subscribe(e -> joinDeletingEvent(e.getCurrent().getChannelId().get()));
		
		//On vérifie s'il s'agit d'une déconnexion, si oui, on ne garde que les salons définis comme étant créés par un salon
		dc.getEventDispatcher().on(VoiceStateUpdateEvent.class)
			.filter(e -> e.getOld().isPresent() && e.getOld().get().getChannelId().isPresent() && createdChannels.containsValue(e.getOld().get().getChannelId().get()))
			.subscribe(e -> leaveEvent(e.getOld().get().getChannel().block()));
	}

	
	private static void joinEvent(Member e, VoiceChannel vc) {
		if(createdChannels.containsKey(e.getId()))
			e.edit(gmes -> gmes.setNewVoiceChannel(createdChannels.get(e.getId()))).subscribe();
		else {
			VoiceChannel vc2 = vc.getGuild().block().createVoiceChannel(vccs -> 
			{ 
				Random r = new Random();
				vccs.setName(names.get(r.nextInt(names.size()))); 
				vccs.setParentId(vc.getCategoryId().get()); 
				vccs.setPosition(vc.getRawPosition() + 1); 
				Set<PermissionOverwrite> set = new HashSet<>();
				set.add(PermissionOverwrite.forMember(e.getId(), PermissionSet.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES), PermissionSet.none()));
				vccs.setPermissionOverwrites(set);
			}
			).block();
			e.edit(gmes -> gmes.setNewVoiceChannel(vc2.getId())).subscribe();
			createdChannels.put(e.getId(), vc2.getId());
		}
	}
	
	private static void joinDeletingEvent(Snowflake sf) {
		removingChannels.get(sf).dispose();
		createdChannels.remove(createdChannels.keySet().stream().filter(s -> createdChannels.get(s).equals(sf)).findFirst().get());
		removingChannels.remove(sf);
	}
	
	private static void leaveEvent(VoiceChannel vc) {
		if(vc.getVoiceStates().count().block().intValue() == 0) {
			try {
				Mono<VoiceChannel> mono = Mono.just(vc).delayElement(Duration.ofMillis(DELETE_DELAY));
				Disposable disposal = mono.subscribe(s -> s.delete().subscribe());
				removingChannels.put(vc.getId(), disposal);
				Thread.sleep(DELETE_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
