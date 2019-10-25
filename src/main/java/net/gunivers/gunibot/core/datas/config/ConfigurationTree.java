package net.gunivers.gunibot.core.datas.config;

import java.util.NoSuchElementException;

import net.gunivers.gunibot.core.datas.DataGuild;

public class ConfigurationTree
{
	public static ConfigurationTree opt(DataGuild guild, String name, ConfigurationTree byDefault)
	{
		ConfigurationTree tree = guild.getConfiguration().get(name);
		if (tree == null) return byDefault;
		return tree;
	}

	public static ConfigurationTree get(DataGuild guild, String name)
	{
		ConfigurationTree tree = guild.getConfiguration().get(name);
		if (tree == null) throw new NoSuchElementException("The guild "+ guild.getEntity().getName() +" have no '"+ name +"' ConfigurationTree");
		return tree;
	}

	public static ConfigurationTree getOrNew(DataGuild guild, String name)
	{
		ConfigurationTree tree = guild.getConfiguration().get(name);
		if (tree == null) tree = new ConfigurationTree(guild, name);
		return tree;
	}

	public static ConfigurationNode optAbsoluteNode(DataGuild guild, String path, ConfigurationNode byDefault)
	{
		if (path.isEmpty())
			return byDefault;

		String[] names = path.split("\\.");
		ConfigurationTree tree = guild.getConfiguration().get(names[0]);

		if (tree == null)
			return byDefault;

		if (names.length == 1)
			return tree.getRoot();

		return tree.optNode(path.substring(tree.getName().length() +1), byDefault);
	}

	public static ConfigurationNode getAbsoluteNode(DataGuild guild, String path)
	{
		ConfigurationNode node = ConfigurationTree.optAbsoluteNode(guild, path, null);
		if (node == null) throw new NoSuchElementException("There is no node at path '"+ path +"'");
		return node;
	}

	public static ConfigurationNode createAbsolutePath(DataGuild guild, String path)
	{
		if (path.isEmpty()) return null;
		ConfigurationTree tree = ConfigurationTree.getOrNew(guild, path.split("\\.")[0]);
		return tree.createPath(path.substring(tree.getName().length()) +1);
	}

	private final DataGuild guild;
	private final ConfigurationRoot root;

	private ConfigurationTree(DataGuild guild, String name)
	{
		this.guild = guild;
		this.root = new ConfigurationRoot(name, this);

		guild.getConfiguration().put(name, this);
	}

	public ConfigurationNode createPath(String path)
	{
		ConfigurationNode node = this.root;
		for (String name : path.split("\\."))
			node = node.createChild(name);
		return node;
	}

	public ConfigurationNode optNode(String path, ConfigurationNode byDefault)
	{
		if (path.isEmpty())
			return this.root;

		ConfigurationNode node = this.root;
		for (String name : path.split("\\."))
		{
			node = node.getChild(name);
			if (node == null)
				return byDefault;
		}
		return node;
	}

	public ConfigurationNode getNode(String path)
	{
		ConfigurationNode node = this.optNode(path, null);
		if (node == null) throw new NoSuchElementException("There is no node at path '"+ path +'\'');
		return node;
	}

	@SuppressWarnings("unchecked")
	public <T> Configuration<T> optConfiguration(String path, Configuration<T> byDefault)
	{
		ConfigurationNode node = this.optNode(path, byDefault);
		if (node instanceof Configuration) return (Configuration<T>) node;
		return byDefault;
	}

	public <T> Configuration<T> getConfiguration(String path)
	{
		Configuration<T> node = this.optConfiguration(path, null);
		if (node == null) throw new NoSuchElementException("There is no node at path '"+ path +"' or it isn't a Configuration");
		return node;
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
			this.tree.guild.getConfiguration().remove(this.name);
		}

		@Override public String getPath() { return this.name; }
		@Override public boolean isVisible() { return this.visible; }
		@Override public ConfigurationTree getTree() { return this.tree; }
	}
}
