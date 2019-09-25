package net.gunivers.gunibot.datas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import discord4j.core.object.entity.Role;
import net.gunivers.gunibot.command.permissions.Permissible;
import net.gunivers.gunibot.command.permissions.Permission;

public class DataRole extends DataObject<Role> implements Permissible {
    private Set<Permission> perms = new HashSet<>();

    public DataRole(Role role) {
	super(role);
    }

    @Override
    public Set<Permission> getPermissions() {
	this.recalculatePermissions();
	return perms;
    }

    @Override
    public void setPermissions(Collection<Permission> perms) {
	this.perms = new HashSet<>(perms);
    }

    @Override
    public void recalculatePermissions() {
	perms.remove(Permission.SERVER_OWNER);
	perms.remove(Permission.BOT_DEV);
	perms.removeAll(perms.stream().filter(Permission::isFromDiscord).collect(Collectors.toSet()));
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
