package net.gunivers.gunibot.datas;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;

import discord4j.core.object.entity.Member;
import net.gunivers.gunibot.command.permissions.Permission;

public class DataMember extends DataObject<Member>
{
	private Set<Permission> perms = new HashSet<>();

	public DataMember(Member member)
	{
		super(member);
	}

	public Set<Permission> getPermissions() { return perms; }

	@Override
	public JSONObject save()
	{
		JSONObject json = super.save();
		json.put("permissions", perms.stream().map(Permission::getName).collect(Collectors.toSet()));
		return json;
	}

	@Override
	public void load(JSONObject json)
	{
		super.load(json);
		perms = json.getJSONArray("permissions").toList().stream().map(Object::toString).map(Permission::getByName)
				.collect(HashSet::new, Set::addAll, Set::addAll);
	}
}
