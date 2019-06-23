package net.gunivers.gunibot.datas;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import discord4j.core.object.entity.Role;
import net.gunivers.gunibot.command.permissions.Permission;

public class DataRole extends DataObject<Role>
{
	private Set<Permission> perms = new HashSet<>();

	public DataRole(Role role) {
		super(role);
	}


	public Set<Permission> getPermissions() { return perms; }

	@Override
	public JSONObject save()
	{
		JSONObject json = super.save();
		json.put("permissions", perms.stream().collect(HashSet::new, (set, perm) -> set.add(perm.getName()), Set::addAll));
		return json;
	}

	@Override
	public void load(JSONObject json)
	{
		super.load(json);
		perms = json.getJSONArray("permissions").toList().stream()
				.collect(HashSet::new, (s, p) -> s.addAll(Permission.getByName((String) p)), Set::addAll);
	}
}
