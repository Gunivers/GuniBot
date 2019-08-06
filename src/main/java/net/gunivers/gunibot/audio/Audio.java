package net.gunivers.gunibot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.voice.AudioProvider;

public class Audio {
	
	public static AudioPlayerManager playerManager;
	static AudioPlayer player;
	public static AudioProvider provider;
	
	public static TrackScheduler scheduler;
	
	/**
	 * Initialize audio providers and url parsers.
	 */
	public static void init() {
		playerManager = new DefaultAudioPlayerManager();
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		
		AudioSourceManagers.registerRemoteSources(playerManager);
		
		player = playerManager.createPlayer();
		
		provider = new LavaPlayerAudioProvider(player);
		scheduler = new TrackScheduler(player);
	}
}
