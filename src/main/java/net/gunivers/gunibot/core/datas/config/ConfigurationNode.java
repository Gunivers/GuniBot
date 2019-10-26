package net.gunivers.gunibot.core.datas.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InvalidNameException;

@SuppressWarnings("unchecked")
public class ConfigurationNode
{
	protected final ConfigurationNode parent;
	protected final String name;
	protected final HashMap<String, ConfigurationNode> children = new HashMap<>();

	protected boolean deleted = false;
	protected boolean visible = true;

	ConfigurationNode(ConfigurationNode parent, String name)
	{
		if (name.contains("."))
			throw new RuntimeException(new InvalidNameException("A node name may not contain '.'"));

		this.parent = parent;
		this.name = name;
	}

	public void delete()
	{
		this.deleted = true;
		this.visible = false;

		if (this.parent != null)
			this.parent.removeChild(this.getName());

		//Instantiate a new List which holds the iteration in order to avoid ConcurrentModificationException
		new ArrayList<>(this.children.values()).forEach(ConfigurationNode::delete);
	}

	public ConfigurationNode createPathFromNode(String path) { return this.getTree().createPath(this.getTreePath() +'.'+ path); }

	public ConfigurationNode createChild(String name)
	{
		this.addChild(new ConfigurationNode(this, name));
		return this.getChild(name);
	}

	protected void removeChildren(String ... names) { for (String name : names) this.removeChild(name); }
	protected void removeChildren(Collection<String> names) { names.forEach(this::removeChild); }
	protected ConfigurationNode removeChild(String name)
	{
		if (this.deleted)
			throw new UnsupportedOperationException("You shan't use a deleted node!");

		return this.children.remove(name);
	}

	protected void addChildren(Collection<ConfigurationNode> childs) { childs.forEach(this::addChild); }
	protected void addChildren(ConfigurationNode ... childs) { for (ConfigurationNode child : childs) this.addChild(child); }
	protected boolean addChild(ConfigurationNode child)
	{
		if (this.deleted)
			throw new UnsupportedOperationException("You shan't use a deleted node!");

		return this.children.putIfAbsent(child.getName(), child) == null;
	}

	public Configuration<?> asConfiguration() { return null; }

	public ConfigurationNode getChild(String name) { return this.children.get(name); }
	public Map<String, ConfigurationNode> getChildren() { return Collections.unmodifiableMap(this.children); }
	public <T> Configuration<T> getConfiguration(String name) { return (Configuration<T>) this.children.get(name); }

	public String getName() { return this.name; }
	public String getPath() { return this.parent.getPath() +'.'+ this.name; }
	public String getTreePath() { return this.getPath().substring(this.getTree().getName().length()); }

	public ConfigurationNode getParent() { return this.parent; }
	public ConfigurationTree getTree() { return this.parent.getTree(); }

	public boolean isConfiguration() { return false; }
	public boolean isDeleted() { return this.deleted; }
	public boolean isVisible() { return this.visible && this.parent.isVisible(); }

	public void setVisible(boolean visible) { this.visible = visible; }
}
