package net.gunivers.gunibot.syl2010.lib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

public class FiledMessageCreateSpec extends MessageCreateSpec {

	@Override
	public FiledMessageCreateSpec setContent(String content) {
		if (content.length() > 2000) {
			return (FiledMessageCreateSpec) super.addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		} else {
			return (FiledMessageCreateSpec) super.setContent(content);
		}
	}

	@Override
	public FiledMessageCreateSpec setTts(boolean tts) {
		return (FiledMessageCreateSpec) super.setTts(tts);
	}

	@Override
	public FiledMessageCreateSpec setEmbed(Consumer<? super EmbedCreateSpec> spec) {
		return (FiledMessageCreateSpec) super.setEmbed(spec);
	}

	@Override
	public FiledMessageCreateSpec addFile(String fileName, InputStream stream) {
		return (FiledMessageCreateSpec) super.addFile(fileName, stream);
	}

	@Override
	public FiledMessageCreateSpec addFileSpoiler(String fileName, InputStream stream) {
		return (FiledMessageCreateSpec) super.addFileSpoiler(fileName, stream);
	}

	@Override
	public FiledMessageCreateSpec setNonce(Snowflake nonce) {
		return (FiledMessageCreateSpec) super.setNonce(nonce);
	}
}
