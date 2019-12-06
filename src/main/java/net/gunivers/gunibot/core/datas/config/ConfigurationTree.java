package net.gunivers.gunibot.core.datas.config;

import java.io.Serializable;

public final class ConfigurationTree implements Serializable
{
	private static final long serialVersionUID = -6680165456788637336L;

	/**
	 * Retrieves the tree from the holder's configuration.
	 * Same as <code><blockquote>holder.getConfiguration().asMap().get(name)</blockquote></code>
	 * @param holder the ConfigurationHolder
	 * @param name the tree's String identifier
	 * @return the configuration tree identified by the specified name, or null if it doesn't exist
	 * @see ConfigurationHolder#getConfiguration()
	 * @see ConfigurationTree#getOrNew(ConfigurationHolder, String)
	 */
	public static ConfigurationTree get(ConfigurationHolder holder, String name) { return holder.getConfiguration().get().get(name); }

	/**
	 * Tries to retrieve a configuration tree identified by the specified name, or instanciate a new one with said name upon retrieval failure
	 * so as to ensure the uniqueness of String identifiers.
	 * @param holder the {@linkplain ConfigurationHolder} holding the configuration tree
	 * @param name the String identifier
	 * @return a ConfigurationTree identified by the specified name
	 * @see ConfigurationTree#get(ConfigurationHolder, String)
	 */
	public synchronized static ConfigurationTree getOrNew(ConfigurationHolder holder, String name)
	{
		ConfigurationTree tree = ConfigurationTree.get(holder, name);
		if (tree == null) return new ConfigurationTree(holder, name);
		return tree;
	}

	/**
	 * Retrieves a node from a configuration tree using its absolute path
	 * @param holder the {@linkplain ConfigurationHolder} holding the node
	 * @param path the node's absolute path
	 * @return the retrieved {@linkplain ConfigurationNode}, or null if it doesn't exist
	 * @see ConfigurationNode#getPath()
	 */
	public static ConfigurationNode getAbsoluteNode(ConfigurationHolder holder, String path)
	{
		if (path.isEmpty())
			return null;

		String[] names = path.split("\\.");
		ConfigurationTree tree = holder.getConfiguration().get().get(names[0]);

		if (tree == null) return null;
		if (names.length == 1) return tree.getRoot();

		return tree.getNode(path.substring(tree.getName().length() +1));
	}

	/**
	 * Creates a node (and configuration tree if necessary) using an absolute node's path
	 * @param holder the {@linkplain ConfigurationHolder} holding the node
	 * @param path the node's absolute path
	 * @return the created node, or the preexistant one
	 * @see ConfigurationTree#createPath(String)
	 */
	public static ConfigurationNode createAbsolutePath(ConfigurationHolder holder, String path)
	{
		if (path.isEmpty()) return null;
		ConfigurationTree tree = ConfigurationTree.getOrNew(holder, path.split("\\.")[0]);
		return tree.createPath(path.substring(tree.getName().length()) +1);
	}

	private final ConfigurationHolder holder;
	private final ConfigurationRoot root;

	/**
	 * Constructs a new configuration tree with said name and fill the holder configuration map with the constructed tree.
	 * The tree's root has the same name as the tree.
	 *
	 * @param holder the {@linkplain ConfigurationHolder} holding the tree
	 * @param name the configuration tree's String identifier
	 */
	private ConfigurationTree(ConfigurationHolder holder, String name)
	{
		this.holder = holder;
		this.root = new ConfigurationRoot(name);
		this.holder.getConfiguration().get().put(name, this);
	}

	/**
	 * Creates a node and all its necessary parents to construct a path within this tree.
	 * @param path the node's path within this configuration tree
	 * @return the last created node, or the preexistant one
	 * @see ConfigurationNode#getTreePath()
	 */
	public ConfigurationNode createPath(String path)
	{
		ConfigurationNode node = this.root;
		for (String name : path.split("\\."))
			node = node.createChild(name);
		return node;
	}

	/**
	 * Attempts to retrieve a node from the specified path.
	 * @param path the path within this configuration tree
	 * @return the retrieved node, or null if it doesn't exist.
	 * @see ConfigurationNode#getTreePath()
	 */
	public ConfigurationNode getNode(String path)
	{
		if (path.isEmpty())
			return this.root;

		ConfigurationNode node = this.root;
		for (String name : path.split("\\."))
		{
			node = node.getChild(name);
			if (node == null)
				return null;
		}

		return node;
	}

	/** Completely deletes this tree, you should not attempt to use any of its node thereafter. */
	public void delete() { this.root.delete(); }

	/** @return this tree's String identifier */
	public String getName() { return this.root.getName(); }

	/** @return this tree's visibility for the user */
	public boolean isVisible() { return this.root.isVisible(); }

	/** @return wether this tree was deleted */
	public boolean isDeleted() { return this.root.isDeleted(); }

	/** @return this tree's root */
	public ConfigurationNode getRoot() { return this.root; }


	public class ConfigurationRoot extends ConfigurationNode
	{
		private static final long serialVersionUID = -3981781166531165466L;

		private ConfigurationRoot(String name) { super(null, name); }

		/**
		 * Deletes this node then removes it from its {@linkplain ConfigurationHolder}'s configuration
		 */
		@Override
		public void delete()
		{
			super.delete();
			ConfigurationTree.this.holder.getConfiguration().get().remove(this.name);
		}

		/** @return this node's String identifier, which is the same as its tree */
		@Override public String getPath() { return this.name; }

		/** @return an empty String */
		@Override public String getTreePath() { return ""; }

		/** @return this node's tree's visibility for the user */
		@Override public boolean isVisible() { return this.visible; }

		/** @return this node's tree */
		@Override public ConfigurationTree getTree() { return ConfigurationTree.this; }
	}
}
