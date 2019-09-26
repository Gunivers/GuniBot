package net.gunivers.gunibot.core.datas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import discord4j.core.object.entity.Member;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.command.permissions.Permissible;
import net.gunivers.gunibot.command.permissions.Permission;

public class DataMember extends DataObject<Member> implements Permissible {
    private Set<Permission> perms = new HashSet<>();

    public DataMember(Member member) {
	super(member);
    }

    @Override
    public Set<Permission> getPermissions() {
	this.recalculatePermissions();
	return perms;
    }

    @Override
    public void setPermissions(Collection<Permission> permissions) {
	this.perms = new HashSet<>(permissions);
    }

    @Override
    public void recalculatePermissions() {
	perms.remove(Permission.SERVER_OWNER);
	perms.remove(Permission.BOT_DEV);
	perms.removeAll(perms.stream().filter(Permission::isFromDiscord).collect(Collectors.toSet()));

	perms.add(Permission.EVERYONE);
	if (this.getEntity().getGuild().block().getOwnerId().equals(this.getEntity().getId())) {
	    perms.add(Permission.SERVER_OWNER);
	}
	if (Main.getBotInstance().getConfig().developperIds.contains(this.getEntity().getId())) {
	    perms.add(Permission.BOT_DEV);
	}
    }

    @Override
    public JSONObject save() {
	JSONObject json = super.save();
	json.put("permissions", perms.stream().map(Permission::getName).collect(Collectors.toSet()));
	return json;
    }

    @Override
    public void load(JSONObject json) {
	super.load(json);
	JSONArray permsJson = json.optJSONArray("permissions");
	if (permsJson != null) {
	    perms = permsJson.toList().stream().map(Object::toString).map(Permission::getByName).collect(HashSet::new,
		    Set::addAll, Set::addAll);
	}
    }
}
