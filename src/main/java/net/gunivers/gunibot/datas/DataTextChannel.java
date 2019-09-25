package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;

public class DataTextChannel extends DataObject<TextChannel> {
    private long owner = -1L;
    private boolean privacy = true;

    public DataTextChannel(TextChannel textChannel) {
	super(textChannel);
    }

    @Override
    public JSONObject save() {
	JSONObject obj = super.save();
	obj.put("owner", owner);
	obj.put("private", privacy);
	return obj;
    }

    @Override
    public void load(JSONObject obj) {
	super.load(obj);
	this.owner = obj.getLong("owner");
	this.privacy = obj.getBoolean("private");
    }

    /**
     * If this channel has no owner, this will be -1 by default
     * 
     * @return the owner id, as a long
     */
    public long getOwner() {
	return owner;
    }

    public boolean isPrivate() {
	return privacy;
    }

    public void setOwner(Member owner) {
	this.owner = owner == null ? -1L : owner.getId().asLong();
    }

    public void setPrivate(boolean privacy) {
	this.privacy = privacy;
    }

}
