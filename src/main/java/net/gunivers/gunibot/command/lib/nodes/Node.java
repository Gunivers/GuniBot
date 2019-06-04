package net.gunivers.gunibot.command.lib.nodes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public abstract class Node {

	private List<Node> children = new LinkedList<Node>();
	private Method run = null;
	private boolean keepValue = false;
	private String tag;

	public final Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> matches(String[] s) {

		// L'élément courant n'est pas valide
		if (!matchesNode(s[0]))
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.ARG_INVALID, s[0]));
		// S'il n'y a plus qu'un argument en paramètre alors que la syntaxe est plus
		// longue
		if (s.length == 1 && run == null && children.size() > 0)
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_LONGER, s[0]));
		// S'il y a plus d'arguments que d'éléments dans la syntaxe
		if (s.length > 1 && children.size() == 0) {
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_SHORTER, s));
		}
		// Cas terminal
		if (s.length == 1 && run != null)
			return Tuple.newTuple(
					Tuple.newTuple(keepValue ? new LinkedList<>(Arrays.asList(s[0])) : new LinkedList<>(), run), null);

		// Cas intermédiaires

		Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> valid = null;
		Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> farthest = Tuple2.newTuple(null,
				new CommandSyntaxError());

		for (Node n : children) {
			// Récursivité
			Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> res = n
					.matches(Arrays.copyOfRange(s, 1, s.length));

			// Si aucun n'élément n'a encore été validé et que l'élément fils courant est
			// valide, on le garde de côté
			if (res._2 == null && valid == null)
				valid = res;
			// Sinon, si l'élément fils courant a pu aller plus loin dans la récursive que
			// les autres, on le garde de côté
			else
				farthest = res._2.isDeeperThan(farthest._2) ? res : farthest;
		}
		// Si un élément valide a été trouvé, on le retourne
		if (valid != null) {
			// Si la valeur doit être gardée, on l'ajoute au retour
			if (keepValue) {
				List<String> l = new LinkedList<String>(Arrays.asList(s[0]));
				l.addAll(valid._1._1);
				return Tuple.newTuple(Tuple.newTuple(l, valid._1._2), null);
			}
			return valid;
		}

		// Sinon, on retourne un tuple composé de l'erreur augmentée
		farthest._2.addStringToPathFirst(s[0]);
		return Tuple.newTuple(null, farthest._2);
	}

	/**
	 * @param s une chaîne de caractères
	 * @return true si s corresponds au prédicat du noeud, false sinon
	 */
	protected abstract boolean matchesNode(String s);

	public void setTag(String s) {
		tag = s;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setChild(List<Node> nodes) {
		children.addAll(nodes);
	}

	public void setKeepValue(boolean b) {
		keepValue = b;
	}

	public boolean keepValue() {
		return keepValue;
	}

	public void setExecute(Method m) {
		run = m;
	}

	public Method getMethod() {
		return run;
	}


}
