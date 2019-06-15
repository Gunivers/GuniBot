package net.gunivers.gunibot.core.az.lib;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Mono;

public class EmbedBuilder
{
	public static final short TITLE_LIMIT = discord4j.core.object.Embed.MAX_TITLE_LENGTH;
	public static final short DESCRIPTION_LIMIT = discord4j.core.object.Embed.MAX_DESCRIPTION_LENGTH;
	public static final short FIELDS_LIMIT = discord4j.core.object.Embed.MAX_FIELDS;
	public static final short FOOTER_LIMIT = discord4j.core.object.Embed.Footer.MAX_TEXT_LENGTH;
	public static final short AUTHOR_NAME_LIMIT = discord4j.core.object.Embed.Author.MAX_NAME_LENGTH;
	
	private final Mono<? extends MessageChannel> channel;
	
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
	
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel) {
		this.channel = channel;
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, String title, String titleURL) {
		this(channel, title, titleURL, null, null, null, null, null, null, null, null);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, Member author, String authorURL, Color color, String imageURL) {
		this(channel, null, null, author, authorURL, color, imageURL);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, String description, String footer, String footerURL, String thumbnail) {
		this(channel, null, null, description, footer, footerURL, thumbnail);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, String title, String titleURL, Member author, String authorURL, Color color, String imageURL)
	{
		this(channel, title, titleURL, author, authorURL, color, imageURL, null, null, null, null);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, String title, String titleURL, String description, String footer, String footerURL,
			String thumbnail) {
		this(channel, title, titleURL, null, null, null, null, description, footer, footerURL, thumbnail);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, Member author, String authorURL, Color color, String imageURL, String desc, String footer,
			String footerURL, String thumbnail) {
		this(channel, null, null, author, authorURL, color, imageURL, desc, footer, footerURL, thumbnail);
	}
	
	public EmbedBuilder(Mono<? extends MessageChannel> channel, String title, String titleURL, Member author, String authorURL, Color color, String imageURL,
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
	
	
	public void buildAndSend()
	{
		this.normalize();
		
		channel.flatMap(c -> c.createEmbed(embed ->
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
		})).subscribe();
		
		if (child != null) child.buildAndSend();
	}
	
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
	
	public void addField(String name, String value, boolean inline) { this.addField(new Field(name, value)); }
	public void addField(Field field)
	{
		if (field.father != null) field = new Field(field.name, field.value.toString());
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
	
	public List<Field> getFields() { return Collections.unmodifiableList(fields); }
	public Mono<? extends MessageChannel> getChannel() { return channel; }
	
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
	
	public void setTitle(String title) { this.title = title; }
	public void setTitleURL(String url) { this.url = url; }
	
	public void setAuthor(Member author) { this.author = author; }
	public void setAuthorURL(String url) { this.authorURL = url; }
	public void setColor(Color color) { this.color = color; }
	public void setImage(String url) { this.image = url; this.displayImage = image != null;}
	
	public void setDescription(String desc) { this.description = desc; }
	public void setFooter(String footer) { this.footer = footer; this.displayFooter = footer != null; }
	public void setFooterImageURL(String url) { this.footerURL = url; }
	public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
	

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
		
		public void normalize()
		{
			value = new StringBuilder(value.toString().trim());
			
			if (name == null || name.trim().equals("")) name = NO_NAME;
			if (value.toString().equals("")) value = new StringBuilder(NO_VALUE);
			
			if (name.length() > NAME_LIMIT) name = name.substring(0, NAME_LIMIT);
			if (value.length() > VALUE_LIMIT)
			{
				int index = value.toString().contains("\n") ? value.substring(0, VALUE_LIMIT).lastIndexOf('\n') : VALUE_LIMIT;
				
				Field child = new Field(name, value.substring(index));
				father.fields.add(father.fields.indexOf(this), child);
			
				value = new StringBuilder(value.substring(0, index));
				child.father = father;
				child.normalize();
			}
		}
	}
}
