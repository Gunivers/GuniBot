package net.gunivers.gunibot.core.audio;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;

public final class LavaPlayerAudioProvider extends AudioProvider {

    private final AudioPlayer player;
    private final MutableAudioFrame frame = new MutableAudioFrame();
    
    /**
     * Allocate a ByteBuffer for LavaPlayer to use. Hold audio data. 
     *     
     * @param player
     */
    public LavaPlayerAudioProvider(final AudioPlayer player) {
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        frame.setBuffer(getBuffer());
        this.player = player;
    }

    /**
     * Write audio data. if successful flip to read.
     */
    @Override
    public boolean provide() {
        final boolean didProvide = player.provide(frame);
        
        if (didProvide) {
            getBuffer().flip();
        }
        return didProvide;
    }
}