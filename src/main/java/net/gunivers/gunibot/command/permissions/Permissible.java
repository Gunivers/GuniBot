package net.gunivers.gunibot.command.permissions;

import java.util.Collection;

public interface Permissible
{
	public void setPermissions(Collection<Permission> perms);
	public Collection<Permission> getPermissions();
	public void recalculatePermissions();
}
