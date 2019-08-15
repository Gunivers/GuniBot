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
		this.displayImage = image != null;
	}
	
	/**
	 * Normalize and send this embed through the channel specified in constructor.
	 */
	public void buildAndSend()
	{
		this.normalize();
		this.embeds = Flux.empty();
		this.messages = Flux.empty();
		
		Mono<Message> msg = channel.createEmbed(embed ->
		{
			embed.setTimestamp(Instant.now());
			
			if (title != null) embed.setTitle(title);
			if (url != null) embed.setUrl(url);
			
			if (author != null) embed.setAuthor(author.getDisplayName(), authorURL, author.getAvatarUrl());
			if (author != null || color != null) embed.setColor(color == null ? author.getColor().block() : color);
			if (displayImage && image != null) embed.setImage(image);
			
			if (description != null) embed.setDescription(description);
			if (displayFooter && footer != null) embed.setFooter(footer, footerURL);
			if (thumbnail != null) embed.setThumbnail(thumbnail);
			
			fields.stream().forEachOrdered(field -> embed.addField(field.name, field.value.toString(), field.inline));
		});
		
		this.embeds = this.embeds.concatWithValues(msg.map(Message::getEmbeds).block().toArray(new Embed[0]));
		this.messages = this.messages.concatWith(msg);
		
		if (child != null)
		{
			child.buildAndSend();
			this.embeds = this.embeds.concatWith(child.embeds);
			this.messages = this.messages.concatWith(child.messages);
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
		if (title != null && title.trim().equals("")) title = null;
		if (url != null && url.trim().equals("")) url = null;
		
		if (authorURL != null && authorURL.trim().equals("")) authorURL = null;
		if (image != null && image.trim().equals("")) image = null;
		
		if (description != null && description.trim().equals("")) description = null;
		if (footer != null && footer.trim().equals("")) footer = null;
		if (footerURL != null && footerURL.trim().equals("")) footerURL = null;
		if (thumbnail != null && thumbnail.trim().equals("")) thumbnail = null;
		
		
		if (title != null && title.length() > TITLE_LIMIT) title = title.substring(0, TITLE_LIMIT);
		if (description != null && description.length() > DESCRIPTION_LIMIT) description = description.substring(0, DESCRIPTION_LIMIT);
		if (footer != null && footer.length() > FOOTER_LIMIT) footer = footer.substring(0, FOOTER_LIMIT);
		
		fields.forEach(Field::normalize);
		if (fields.size() > FIELDS_LIMIT)
		{
			child = new EmbedBuilder(channel, null, null, color == null ? author == null ? null : author.getColor().block() : color, image, null,
					footer, footerURL, null);
			
			child.fields = fields.subList(FIELDS_LIMIT, fields.size());
			child.normalize();

			this.displayImage = false;
			this.displayFooter = false;
			this.fields = fields.subList(0, FIELDS_LIMIT);
		}
	}
	
	/**
	 * Add a field of specified name, value, and inline.
	 * @see #addField(Field)
	 */
	public void addField(String name, String value, boolean inline) { this.addField(new Field(name, value)); }

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
	
	public boolean removeField(Field field) { return fields.remove(field); }
	public void clear() { fields.clear(); }
	
	public void setRequestedBy(Member member)
	{
		if (color == null) color = member.getColor().block();
		if (footer == null)
		{
			footer = "Request by " + member.getDisplayName();
			displayFooter = true;
			
			if (footerURL == null) footerURL = member.getAvatarUrl();
		}
	}
	
	/**
	 * If this builder was not yet built, return an empty Flux. Else, return the embeds that were sended.
	 * @return
	 */
	public Flux<Embed> getEmbeds() { return embeds == null ? Flux.empty() : embeds; }
	/**
	 * If this builder was not yet built, return an empty Flux. Else, return the messages that were sended.
	 * @return
	 */
	public Flux<Message> getMessages() { return messages == null ? Flux.empty() : messages; }
	
	public List<Field> getFields() { return Collections.unmodifiableList(fields); }
	public MessageChannel getChannel() { return channel; }
	
	public String getTitle() { return title; }
	public String getTitleURL() { return url; }
	
	public Member getAuthor() { return author; }
	public String getAuthorURL() { return authorURL; }
	public Color getColor() { return color; }
	public String getImageURL() { return image; }
	
	public String getDescription() { return description; }
	public String getFooter() { return footer; }
	public String getFooterImageURL() { return footerURL; }
	public String getThumbnail() { return thumbnail; }
	
	public EmbedBuilder setChannel(MessageChannel channel) { this.channel = channel; return this; }
	
	public EmbedBuilder setTitle(String title) { this.title = title; return this; }
	public EmbedBuilder setTitleURL(String url) { this.url = url; return this; }
	
	public EmbedBuilder setAuthor(Member author) { this.author = author; return this; }
	public EmbedBuilder setAuthorURL(String url) { this.authorURL = url; return this; }
	public EmbedBuilder setColor(Color color) { this.color = color; return this; }
	public EmbedBuilder setImage(String url) { this.image = url; this.displayImage = image != null; return this; }
	
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
		
		public EmbedBuilder getEmbed() { return father; }
		
		public String getName() { return name; }
		public StringBuilder getValue() { return value; }
		public boolean isInline() { return inline; }
		
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
			value = new StringBuilder(value.toString().trim());
			
			if (name == null || name.trim().equals("")) name = NO_NAME;
			if (value.toString().equals("")) value = new StringBuilder(NO_VALUE);
			
			if (name.length() > NAME_LIMIT) name = name.substring(0, NAME_LIMIT);
			if (value.length() > VALUE_LIMIT)
			{
				int index = value.toString().contains("\n") ? value.substring(0, VALUE_LIMIT).lastIndexOf('\n') : VALUE_LIMIT;
				
				Field child = new Field(name + "-child", value.substring(index));
				father.fields.add(father.fields.indexOf(this), child);
			
				value = new StringBuilder(value.substring(0, index));
				child.father = father;
				child.normalize();
			}
		}

		/**
		 * @param field
		 * @return true if the name, value, and inline properties are similar themselves.
		 */
		public boolean isSimilar(Field field)
		{
			return field.name.equals(name) && field.value.toString().equals(value.toString()) && field.inline == inline;
		}
	}
}
