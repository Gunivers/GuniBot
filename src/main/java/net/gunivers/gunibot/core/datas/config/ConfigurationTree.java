package net.gunivers.gunibot.core.datas.config;

public class ConfigurationTree
{
	public static ConfigurationTree get(ConfigurationHolder guild, String name)
	{
		return guild.getConfiguration().get().get(name);
	}

	public static ConfigurationTree getOrNew(ConfigurationHolder guild, String name)
	{
		ConfigurationTree tree = guild.getConfiguration().get().get(name);
		if (tree == null) tree = new ConfigurationTree(guild, name);
		return tree;
	}

	public static ConfigurationNode getAbsoluteNode(ConfigurationHolder guild, String path)
	{
		if (path.isEmpty())
			return null;

		String[] names = path.split("\\.");
		ConfigurationTree tree = guild.getConfiguration().get().get(names[0]);

		if (tree == null)
			return null;

		if (names.length == 1)
			return tree.getRoot();

		return tree.getNode(path.substring(tree.getName().length() +1));
	}

	public static ConfigurationNode createAbsolutePath(ConfigurationHolder guild, String path)
	{
		if (path.isEmpty()) return null;
		ConfigurationTree tree = ConfigurationTree.getOrNew(guild, path.split("\\.")[0]);
		return tree.createPath(path.substring(tree.getName().length()) +1);
	}

	private final ConfigurationHolder guild;
	private final ConfigurationRoot root;

	private ConfigurationTree(ConfigurationHolder guild, String name)
	{
		this.guild = guild;
		this.root = new ConfigurationRoot(name, this);

		guild.getConfiguration().get().put(name, this);
	}

	public ConfigurationNode createPath(String path)
	{
		ConfigurationNode node = this.root;
		for (String name : path.split("\\."))
			node = node.createChild(name);
		return node;
	}

	/**
	 * @param path
	 * @return null if there is no node at specified path, the node otherwise.
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

	/**
	 * @param path
	 * @return null if there is no node at specified path or it isn't a configuration, the node as a configuration otherwise.
	 */
	public Configuration<?> getConfiguration(String path)
	{
		ConfigurationNode node = this.getNode(path);
		if (node == null) return null;
		return node.asConfiguration();
	}

	public void delete() { this.root.delete(); }

	public String getName() { return this.root.getName(); }
	public boolean isVisible() { return this.root.isVisible(); }
	public boolean isDeleted() { return this.root.isDeleted(); }
	public ConfigurationNode getRoot() { return this.root; }


	public static class ConfigurationRoot extends ConfigurationNode
	{
		private final ConfigurationTree tree;

		private ConfigurationRoot(String name, ConfigurationTree tree)
		{
			super(null, name);
			this.tree = tree;
		}

		@Override
		public void delete()
		{
			super.delete();
			this.tree.guild.getConfiguration().get().remove(this.name);
		}

		@Override public String getPath() { return this.name; }
		@Override public boolean isVisible() { return this.visible; }
		@Override public ConfigurationTree getTree() { return this.tree; }
	}
}
