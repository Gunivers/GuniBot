package net.gunivers.gunibot.core.datas.guild;

import org.json.JSONObject;

import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.lib.parsing.commons.NumberParser.LongParser;

public class CustomChannelSystem extends System
{
	public static final String NAME = "cchannel";

	private Configuration<Long> activeCategory;
	private Configuration<Long> archiveCategory;

	public CustomChannelSystem(DataGuild guild, ConfigurationNode parent)
	{
		super(NAME, guild, parent);
		this.activeCategory = parent.createConfiguration("active", new LongParser(0), "Category ID", -1L);
		this.archiveCategory = parent.createConfiguration("archive", new LongParser(0), "Category ID", -1L);
	}

	{
		this.enabled.setDefaultValue(false);
		this.enabled.reset();
	}

	@Override
	public void load(JSONObject source)
	{
		if (source == null) source = new JSONObject();
		super.load(source);

		this.activeCategory.setValue(source.optLong("active", this.activeCategory.getDefaultValue()));
		this.archiveCategory.setValue(source.optLong("archive", this.activeCategory.getDefaultValue()));
	}

	@Override
	public JSONObject save()
	{
		JSONObject obj = super.save();
		obj.put("active", this.activeCategory.getValue());
		obj.put("archive", this.archiveCategory.getValue());
		return obj;
	}

	public Configuration<Long> activeCategory() { return this.activeCategory; }
	public Configuration<Long> archiveCategory() { return this.archiveCategory; }

	public Long getActiveCategory() { return this.activeCategory.getValue(); }
	public Long getArchiveCategory() { return this.archiveCategory.getValue(); }

	public void setActiveCategory(long id) { this.activeCategory.setValue(id); }
	public void setArchiveCategory(long id) { this.archiveCategory.setValue(id); }
}