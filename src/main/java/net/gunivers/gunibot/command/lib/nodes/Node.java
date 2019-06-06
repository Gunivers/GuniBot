package net.gunivers.gunibot.command.lib.nodes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import discord4j.core.object.entity.Guild;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.command.lib.nodes.interfaces.Matchable;
import net.gunivers.gunibot.command.lib.nodes.interfaces.NeedGuild;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public abstract class Node implements Matchable
{	
	private List<Node> children = new LinkedList<Node>();
	private Method run = null;
	private String tag;

	@SuppressWarnings({ "rawtypes", "serial" })
	public final Tuple2<Tuple2<Map<TypeNode,Object>, Method>, CommandSyntaxError> matches(Guild guild, String[] s)
	{	
		CommandSyntaxError err = this instanceof NeedGuild ? ((NeedGuild<?>) this).matchesNode(guild, s[0]) : matchesNode(s[0]);
		
		if (err != null)
			return Tuple.newTuple(null, new CommandSyntaxError(err.toString(), SyntaxError.ARG_INVALID, s[0]));
		// S'il n'y a plus qu'un argument en paramètre alors que la syntaxe est plus longue
		if (s.length == 1 && run == null && children.size() > 0)
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_LONGER, s[0]));
		// S'il y a plus d'arguments que d'éléments dans la syntaxe
		if (s.length > 1 && children.size() == 0  && !(this instanceof NodeInfinity))
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_SHORTER, s));
		// Cas terminal
		if ((s.length == 1 && run != null) || this instanceof NodeInfinity)
		{
			final TypeNode<?> node = (this instanceof TypeNode) ? (TypeNode<?>) this : null;
			String toparse = this instanceof NodeInfinity ? String.join(" ", s) : s[0];
			
			return Tuple.newTuple(Tuple.newTuple
				(
					node != null && node.keepValue ?
						new HashMap<TypeNode,Object>()
						{{
							put(node, node instanceof NeedGuild ? ((NeedGuild) node).getFrom(guild, toparse) : node.getFrom(toparse));
						}} : new HashMap<>(), run
				), null);
		}

		// Cas intermédiaires

		Tuple2<Tuple2<Map<TypeNode,Object>, Method>, CommandSyntaxError> valid = null;
		Tuple2<Tuple2<Map<TypeNode,Object>, Method>, CommandSyntaxError> furthest = Tuple2.newTuple(null,
				new CommandSyntaxError());

		for (Node n : children) {
			// Récursivité
			Tuple2<Tuple2<Map<TypeNode,Object>, Method>, CommandSyntaxError> res = n.matches(guild, Arrays.copyOfRange(s, 1, s.length));

			// Si aucun n'élément n'a encore été validé et que l'élément fils courant est
			// valide, on le garde de côté
			if (res._2 == null && valid == null)
				valid = res;
			// Sinon, si l'élément fils courant a pu aller plus loin dans la récursive que
			// les autres, on le garde de côté
			else
				furthest = res._2.isDeeperThan(furthest._2) ? res : furthest;
		}
		// Si un élément valide a été trouvé, on le retourne
		if (valid != null)
		{
			// Si la valeur doit être gardée, on l'ajoute au retour
			if (this instanceof TypeNode && ((TypeNode<?>) this).keepValue)
			{
				final TypeNode<?> node = (TypeNode<?>) this;
				final String toparse = this instanceof NodeInfinity ? String.join(" ", s) : s[0];
				
				HashMap<TypeNode,Object> m = new HashMap<TypeNode,Object>() {{
					put(node, node instanceof NeedGuild ? ((NeedGuild<?>) node).getFrom(guild, toparse) : node.getFrom(toparse));
				}};
				
				m.putAll(valid._1._1);
				return Tuple.newTuple(Tuple.newTuple(m, valid._1._2), null);
			}
			return valid;
		}

		// Sinon, on retourne un tuple composé de l'erreur augmentée
		furthest._2.addStringToPathFirst(s[0]);
		return Tuple.newTuple(null, furthest._2);
	}

	public void setTag(String s) {
		tag = s;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setChild(List<Node> nodes) {
		children.addAll(nodes);
	}

	public void setExecute(Method m) {
		run = m;
	}

	public Method getMethod() {
		return run;
	}

	@Override
	public String toString() {
		if(children.size() == 0)
			return "<" + tag + ">";
		return "<" + tag + ">" + childrenToString();
	}
	
	protected String childrenToString() {
		if (children.size() == 1) {
			if (run != null)
				return " [" + children.get(0).toString() + ']';
			else
				return " " + children.get(0).toString();
		} if(children.size() > 1) {
			if (run != null)
				return " [" + children.stream().map((n) -> n.toString()).collect(Collectors.joining("|")) + "]";
			else
				return " (" + children.stream().map((n) -> n.toString()).collect(Collectors.joining("|")) + ")";
		} else
			return "";
	}
}
