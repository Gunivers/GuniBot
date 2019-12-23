package net.gunivers.gunibot.core.lib;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class allows you to build and generate embeds easily
 * @author A~Z
 *
 */
public class EmbedBuilder
{
	public static final short TITLE_LIMIT = discord4j.core.object.Embed.MAX_TITLE_LENGTH;
	public static final short DESCRIPTION_LIMIT = discord4j.core.object.Embed.MAX_DESCRIPTION_LENGTH;
	public static final short FIELDS_LIMIT = discord4j.core.object.Embed.MAX_FIELDS;
	public static final short FOOTER_LIMIT = discord4j.core.object.Embed.Footer.MAX_TEXT_LENGTH;
	public static final short AUTHOR_NAME_LIMIT = discord4j.core.object.Embed.Author.MAX_NAME_LENGTH;

	private MessageChannel channel;

	private String title = null;
	private String url = null;

	private Member author = null;
	private String authorURL = null;
	private Color color = null;
	private String image = null;

	private String description = null;
	private String footer = null;
	private String footerURL = null;
	private String thumbnail = null;

	private boolean displayImage = true;
	private boolean displayFooter = true;
	private List<Field> fields = new ArrayList<>();

	private EmbedBuilder child = null;
	private Flux<Embed> embeds = null;
	private Flux<Message> messages = null;

	public EmbedBuilder(MessageChannel channel) {
		this.channel = channel; }

	public EmbedBuilder(MessageChannel channel, String title, String titleURL) {
		this(channel, title, titleURL, null, null, null, null, null, null, null, null); }

	public EmbedBuilder(MessageChannel channel, Member author, String authorURL, Color color, String imageURL) {
		this(channel, null, null, author, authorURL, color, imageURL); }

	public EmbedBuilder(MessageChannel channel, String description, String footer, String footerURL, String thumbnail) {
		this(channel, null, null, description, footer, footerURL, thumbnail); }

	public EmbedBuilder(MessageChannel channel, String title, String titleURL, Member author, String authorURL, Color color, String imageURL) {
		this(channel, title, titleURL, author, authorURL, color, imageURL, null, null, null, null); }

	public EmbedBuilder(MessageChannel channel, String title, String titleURL, String description, String footer, String footerURL,
			String thumbnail) {
		this(channel, title, titleURL, null, null, null, null, description, footer, footerURL, thumbnail); }

	public EmbedBuilder(MessageChannel channel, Member author, String authorURL, Color color, String imageURL, String desc, String footer,
			String footerURL, String thumbnail) {
		this(channel, null, null, author, authorURL, color, imageURL, desc, footer, footerURL, thumbnail); }

	public EmbedBuilder(MessageChannel channel, String title, String titleURL, Member author, String authorURL, Color color, String imageURL,
			String description, String footer, String footerURL, String thumbnail)
	{
		this.channel = channel;
		this.title = title;
		this.url = titleURL;

		this.description = description;
		this.footer = footer;
		this.footerURL = footerURL;
		this.thumbnail = thumbnail;

		this.author = author;
		this.authorURL = authorURL;
		this.color = color;
		this.image = imageURL;

		this.displayFooter = footer != null;
		this.displayImage = this.image != null;
	}

	/**
	 * Normalize and send this embed through the channel specified in constructor.
	 */
	public void buildAndSend()
	{
		this.normalize();
		this.embeds = Flux.empty();
		this.messages = Flux.empty();

		Mono<Message> msg = this.channel.createEmbed(embed ->
		{
			embed.setTimestamp(Instant.now());

			if (this.title != null) embed.setTitle(this.title);
			if (this.url != null) embed.setUrl(this.url);

			if (this.author != null) embed.setAuthor(this.author.getDisplayName(), this.authorURL, this.author.getAvatarUrl());
			if (this.author != null || this.color != null) embed.setColor(this.color == null ? this.author.getColor().block() : this.color);
			if (this.displayImage && this.image != null) embed.setImage(this.image);

			if (this.description != null) embed.setDescription(this.description);
			if (this.displayFooter && this.footer != null) embed.setFooter(this.footer, this.footerURL);
			if (this.thumbnail != null) embed.setThumbnail(this.thumbnail);

			this.fields.stream().forEachOrdered(field -> embed.addField(field.name, field.value.toString(), field.inline));
		});

		this.embeds = this.embeds.concatWithValues(msg.map(Message::getEmbeds).block().toArray(new Embed[0]));
		this.messages = this.messages.concatWith(msg);

		if (this.child != null)
		{
			this.child.buildAndSend();
			this.embeds = this.embeds.concatWith(this.child.embeds);
			this.messages = this.messages.concatWith(this.child.messages);
		}
	}

	/**
	 * Normalize this builder to discord embeds's norms.
	 * Hence, empty String values will be replaced by {@code null} and thus not considered in the creating process of {@link #buildAndSend()}.
	 * If those values are above discord's limits, they will be cut down to those.
	 * Afterward, each Field will be normalized following {@link Field#normalize()}.
	 * <p>
	 * If there is more than the discord max amount of fields, this embed builder will collect the first 25 fields, then generate a second
	 * builder containing the remaining fields. The generated builder would then be recursiely normalized.
	 */
	public void normalize()
	{
		if (this.title != null && this.title.trim().equals("")) this.title = null;
		if (this.url != null && this.url.trim().equals("")) this.url = null;

		if (this.authorURL != null && this.authorURL.trim().equals("")) this.authorURL = null;
		if (this.image != null && this.image.trim().equals("")) this.image = null;

		if (this.description != null && this.description.trim().equals("")) this.description = null;
		if (this.footer != null && this.footer.trim().equals("")) this.footer = null;
		if (this.footerURL != null && this.footerURL.trim().equals("")) this.footerURL = null;
		if (this.thumbnail != null && this.thumbnail.trim().equals("")) this.thumbnail = null;


		if (this.title != null && this.title.length() > TITLE_LIMIT) this.title = this.title.substring(0, TITLE_LIMIT);
		if (this.description != null && this.description.length() > DESCRIPTION_LIMIT) this.description = this.description.substring(0, DESCRIPTION_LIMIT);
		if (this.footer != null && this.footer.length() > FOOTER_LIMIT) this.footer = this.footer.substring(0, FOOTER_LIMIT);

		this.fields.forEach(Field::normalize);
		if (this.fields.size() > FIELDS_LIMIT)
		{
			this.child = new EmbedBuilder(this.channel, null, null, this.color == null ? this.author == null ? null : this.author.getColor().block() : this.color, this.image, null,
					this.footer, this.footerURL, null);

			this.child.fields = this.fields.subList(FIELDS_LIMIT, this.fields.size());
			this.child.normalize();

			this.displayImage = false;
			this.displayFooter = false;
			this.fields = this.fields.subList(0, FIELDS_LIMIT);
		}
	}

	/**
	 * Add a field of specified name, value, and inline.
	 * @see #addField(Field)
	 */
	public void addField(String name, String value, boolean inline) { this.addField(new Field(name, value, inline)); }

	/**
	 * If the specified field already possess a father, then it will be replaced by a field of same values but different instance.<br>
	 * This builder then set itself as the father of the field, and add the field the field list.
	 * @param field
	 */
	public void addField(Field field)
	{
		if (field.father != null) field = new Field(field.name, field.value.toString(), field.inline);
		field.father = this;
		this.fields.add(field);
	}

	public boolean removeField(Field field) { return this.fields.remove(field); }
	public void clear() { this.fields.clear(); }

	public void setRequestedBy(Member member)
	{
		if (this.color == null) this.color = member.getColor().block();
		if (this.footer == null)
		{
			this.footer = "Request by " + member.getDisplayName();
			this.displayFooter = true;

			if (this.footerURL == null) this.footerURL = member.getAvatarUrl();
		}
	}

	/**
	 * If this builder was not yet built, return an empty Flux. Else, return the embeds that were sended.
	 * @return
	 */
	public Flux<Embed> getEmbeds() { return this.embeds == null ? Flux.empty() : this.embeds; }
	/**
	 * If this builder was not yet built, return an empty Flux. Else, return the messages that were sended.
	 * @return
	 */
	public Flux<Message> getMessages() { return this.messages == null ? Flux.empty() : this.messages; }

	public List<Field> getFields() { return Collections.unmodifiableList(this.fields); }
	public MessageChannel getChannel() { return this.channel; }

	public String getTitle() { return this.title; }
	public String getTitleURL() { return this.url; }

	public Member getAuthor() { return this.author; }
	public String getAuthorURL() { return this.authorURL; }
	public Color getColor() { return this.color; }
	public String getImageURL() { return this.image; }

	public String getDescription() { return this.description; }
	public String getFooter() { return this.footer; }
	public String getFooterImageURL() { return this.footerURL; }
	public String getThumbnail() { return this.thumbnail; }

	public EmbedBuilder setChannel(MessageChannel channel) { this.channel = channel; return this; }

	public EmbedBuilder setTitle(String title) { this.title = title; return this; }
	public EmbedBuilder setTitleURL(String url) { this.url = url; return this; }

	public EmbedBuilder setAuthor(Member author) { this.author = author; return this; }
	public EmbedBuilder setAuthorURL(String url) { this.authorURL = url; return this; }
	public EmbedBuilder setColor(Color color) { this.color = color; return this; }
	public EmbedBuilder setImage(String url) { this.image = url; this.displayImage = this.image != null; return this; }

	public EmbedBuilder setDescription(String desc) { this.description = desc; return this; }
	public EmbedBuilder setFooter(String footer) { this.footer = footer; this.displayFooter = footer != null; return this; }
	public EmbedBuilder setFooterImageURL(String url) { this.footerURL = url; return this; }
	public EmbedBuilder setThumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }


	public static class Field
	{
		public static final short NAME_LIMIT = discord4j.core.object.Embed.Field.MAX_NAME_LENGTH;
		public static final short VALUE_LIMIT = discord4j.core.object.Embed.Field.MAX_VALUE_LENGTH;

		public static final String NO_NAME = "NO NAME";
		public static final String NO_VALUE = "NO_VALUE";

		private EmbedBuilder father = null;

		private String name;
		private StringBuilder value;
		private boolean inline;

		public Field(String name) { this(name, "", true); }
		public Field(String name, boolean inline) { this(name, "", inline); }
		public Field(String name, String value) { this(name, value, true); }

		public Field(String name, String value, boolean inline)
		{
			this.name = name;
			this.value = new StringBuilder(value);
			this.inline = inline;
		}

		public EmbedBuilder getEmbed() { return this.father; }

		public String getName() { return this.name; }
		public StringBuilder getValue() { return this.value; }
		public boolean isInline() { return this.inline; }

		public void setName(String name) { this.name = name; }
		public void setValue(String value) { this.value = new StringBuilder(value); }
		public void setInline(boolean inline) { this.inline = inline; }

		/**
		 * Normalize this Field to discord embeds's norms.<br>
		 * If the name or value is blank or empty, it will be replaced by the relevant constant: {@link #NO_NAME} or {@link NO_VALUE}<br>
		 * If the value's length is greater than discord's limit, it is cut down to create another child field at the index of
		 * either {@link #VALUE_LIMIT} if this field's value doesn't contains '\n', either at its last occurence within
		 */
		public void normalize()
		{
			this.value = new StringBuilder(this.value.toString().trim());

			if (this.name == null || this.name.trim().equals("")) this.name = NO_NAME;
			if (this.value.toString().equals("")) this.value = new StringBuilder(NO_VALUE);

			if (this.name.length() > NAME_LIMIT) this.name = this.name.substring(0, NAME_LIMIT);
			if (this.value.length() > VALUE_LIMIT)
			{
				int index = this.value.toString().contains("\n") ? this.value.substring(0, VALUE_LIMIT).lastIndexOf('\n') : VALUE_LIMIT;

				Field child = new Field(this.name + "-child", this.value.substring(index));
				this.father.fields.add(this.father.fields.indexOf(this), child);

				this.value = new StringBuilder(this.value.substring(0, index));
				child.father = this.father;
				child.normalize();
			}
		}

		/**
		 * @param field
		 * @return true if the name, value, and inline properties are similar themselves.
		 */
		public boolean isSimilar(Field field)
		{
			return field.name.equals(this.name) && field.value.toString().equals(this.value.toString()) && field.inline == this.inline;
		}
	}
}
