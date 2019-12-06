package net.gunivers.gunibot.core.datas.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InvalidNameException;

import net.gunivers.gunibot.core.datas.config.ConfigurationTree.ConfigurationRoot;
import net.gunivers.gunibot.core.lib.parsing.Parser;

/**
 * This class is a node within a {@linkplain ConfigurationTree}.
 * It is represented by three values:
 * <ul>
 *  <li>parent: this node's parent, or null if this node is the root</li>
 *  <li>name: this node's String identifier, used to retrieve it from the children's map</li>
 *  <li>children: this node's children</li>
 * </ul>
 * @author AZ
 * @see ConfigurationRoot
 */
public class ConfigurationNode implements Serializable
{
	private static final long serialVersionUID = 1680337493303538189L;

	protected final ConfigurationNode parent;
	protected final String name;
	protected final HashMap<String, ConfigurationNode> children = new HashMap<>();

	protected boolean deleted = false;
	protected boolean visible = true;

	/**
	 * Construct a node with the provided parent and name
	 * @param parent this node's parent
	 * @param name this node's String identifier
	 */
	ConfigurationNode(ConfigurationNode parent, String name)
	{
		if (name.contains("."))
			throw new RuntimeException(new InvalidNameException("A node name may not contain '.'"));

		this.parent = parent;
		this.name = name;
	}

	/**
	 * Delete this node from its tree: beware this operation will render this node unusable then delete its children as well, making them
	 * throw Exceptions upon manipulations
	 */
	public void delete()
	{
		this.deleted = true;
		this.visible = false;

		if (this.parent != null)
			this.parent.removeChild(this.getName());

		//Load a new List which is iterated over so as to prevent ConcurrentModificationException
		new ArrayList<>(this.children.values()).forEach(ConfigurationNode::delete);
	}

	/**
	 * Same as <blockquote>{@code node.getTree().createPath(node.getTreePath() + '.' + path)}</blockquote>
	 * @param path a relative path from this node
	 * @return the last instanciated node
	 * @see ConfigurationTree#createPath(String)
	 */
	public ConfigurationNode createPathFromNode(String path) { return this.getTree().createPath(this.getTreePath() +'.'+ path); }

	/**
	 * Returns a {@linkplain ConfigurationNode} as this node's child, or the child with this name if it exists.
	 * To create a configuration instead, please refer to {@linkplain ConfigurationNode#createConfiguration(String, Parser, String, Object)}
	 * @param name the child's String identifier
	 * @return the child
	 */
	public ConfigurationNode createChild(String name)
	{
		ConfigurationNode node = this.children.get(name);
		if (node != null)
			return node;

		node = new ConfigurationNode(this, name);
		this.addChild(node);
		return node;
	}

	/**
	 * Returns a {@linkplain Configuration} as this node's child, or null if a child with this name already exists.
	 * To create a simple configuration node instead, please refer to {@linkplain ConfigurationNode#createChild(String)}
	 * @param name the child's string identifier
	 * @param parser the child's parser
	 * @param type the child's displayed type
	 * @param defaultValue the child's default value
	 * @return the child
	 */
	public <T> Configuration<T> createConfiguration(String name, Parser<T> parser, String type, T defaultValue)
	{
		if (this.children.containsKey(name))
			return null;

		Configuration<T> config = new Configuration<>(this, name, parser, type, defaultValue);
		this.addChild(config);
		return config;
	}

	/**
	 * Bulk delete a list of children identified by their name
	 * @param names the children name array
	 */
	@Deprecated
	protected void removeChildren(String ... names) { for (String name : names) this.removeChild(name); }

	/**
	 * Bulk delete a list of children identified by their name
	 * @param names the children name {@linkplain Collection}
	 */
	@Deprecated
	protected void removeChildren(Collection<String> names) { names.forEach(this::removeChild); }

	/**
	 * Remove the child from this node's children map. As this method will not delete the child itself, it is better to call
	 * {@code child.delete()} instead
	 * @param name the child's String identifier
	 */
	@Deprecated
	protected void removeChild(String name)
	{
		if (this.deleted)
			throw new UnsupportedOperationException("This node was deleted!");

		this.children.remove(name);
	}

	/**
	 * Bulk add children to this node's children map.
	 * @param children
	 */
	protected void addChildren(Collection<ConfigurationNode> children) { children.forEach(this::addChild); }

	/**
	 * Bulkadd children to this node's children map
	 * @param children
	 */
	protected void addChildren(ConfigurationNode ... children) { for (ConfigurationNode child : children) this.addChild(child); }

	/**
	 * Add a single child to this node's children map.
	 * @param child
	 * @return
	 */
	protected boolean addChild(ConfigurationNode child)
	{
		if (this.deleted)
			throw new UnsupportedOperationException("This node was deleted!");

		return this.children.putIfAbsent(child.getName(), child) == null;
	}

	/**
	 * Return this node as a configuration, or null
	 * @return <code>null</code> by default
	 * @see Configuration#asConfiguration()
	 */
	public Configuration<?> asConfiguration() { return null; }

	/** @return this node's child as midentified by the provided name, or null */
	public ConfigurationNode getChild(String name) { return this.children.get(name); }

	/** @return an immutable Map containing this node's children */
	public Map<String, ConfigurationNode> getChildren() { return Collections.unmodifiableMap(this.children); }

	/**
	 * @return this node's child as a configuration, or null if it doesn't exist or is a simple node
	 */
	public Configuration<?> getConfiguration(String name)
	{
		ConfigurationNode node = this.children.get(name);
		if (node == null)
			return null;

		return node.asConfiguration();
	}

	/** @return this node's String identifier */
	public String getName() { return this.name; }

	/**
	 * Same as <code><blockquote>node.getParent().getPath() +'.'+ node.getName()</blockquote></code>
	 * @return this node's absolute path, used for calls to {@linkplain ConfigurationTree} static methods
	 * @see ConfigurationNode#getTreePath()
	 */
	public String getPath() { return this.parent.getPath() +'.'+ this.name; }

	/**
	 * Same as <code><blockquote>node.getParent().getPath() +'.'+ node.getName()</blockquote></code>
	 * @return this node's path within its tree, used for calls to {@linkplain ConfigurationTree} and {@linkplain ConfigurationNode}
	 * instance methods
	 * @see ConfigurationNode#getPath()
	 */
	public String getTreePath() { return this.parent.getTreePath() +'.'+ this.name; }

	/** @return this node's parent */
	public ConfigurationNode getParent() { return this.parent; }

	/** @return this node's tree */
	public ConfigurationTree getTree() { return this.parent.getTree(); }

	/**
	 * Return wether this node is a Configuration
	 * @return <code>false</code> by default
	 * @see Configuration#isConfiguration()
	 */
	public boolean isConfiguration() { return false; }

	/** @return a boolean, wether this node was deleted */
	public boolean isDeleted() { return this.deleted; }

	/** @return wether this node is visible for the user */
	public boolean isVisible() { return this.visible && this.parent.isVisible(); }

	/**
	 * Set this node's visibility for the user. While invisible, this node's children are unable to be seen as well
	 * @param visible this node's visibility
	 */
	public void setVisible(boolean visible) { this.visible = visible; }
}
