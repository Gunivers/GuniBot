package net.gunivers.gunibot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackScheduler implements AudioLoadResultHandler {

    private final AudioPlayer player;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
    }
    
    /**
     * Play audio source.
     */
    @Override
    public void trackLoaded(final AudioTrack track) {
    	System.out.println("[Audio] Play audio source.");
        player.playTrack(track);
    }

    /**
     * Multiple audio track.
     */
    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
    	System.out.println("[Audio] Multiple audio track.");
    }
    
    /**
     * No audio to extract.
     */
    @Override
    public void noMatches() {
    	System.out.println("[Audio] No audio to extract!");
    }

    /**
     * Parse fail.
     */
    @Override
    public void loadFailed(final FriendlyException exception) {
    	System.out.println("[Audio] Parse Fail!");
    }
}