package net.gunivers.gunibot.command.lib.nodes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.command.lib.keys.KeyEnum;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public abstract class Node {

	private List<Node> children = new LinkedList<Node>();
	private Method run = null;
	private boolean keepValue = false;
	private String tag;

	public final Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> matches(String s) {

		Tuple2<String, String> splited = split(s);
		
		// L'élément courant n'est pas valide
		if ((splited._1 == null || splited._2 == null) || !matchesNode(splited._1))
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.ARG_INVALID, splited._1));
		// S'il n'y a plus qu'un argument en paramètre alors que la syntaxe est plus
		// longue
		if (splited._2.equals("") && run == null && children.size() > 0)
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_LONGER, splited._1));
		// S'il y a plus d'arguments que d'éléments dans la syntaxe
		if (!splited._2.equals("") && children.size() == 0) {
			return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_SHORTER, s));
		}
		// Cas terminal
		if (splited._2.equals("") && run != null)
			return Tuple.newTuple(
					Tuple.newTuple(keepValue ? new LinkedList<>(Arrays.asList(splited._1)) : new LinkedList<>(), run), null);

		// Cas intermédiaires

		Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> valid = null;
		Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> farthest = Tuple2.newTuple(null,
				new CommandSyntaxError());

		for (Node n : children) {
			// Récursivité
			Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> res = n
					.matches(splited._2);

			// Si aucun élément n'a encore été validé et que l'élément fils courant est
			// valide, on le garde de côté
			if (res._2 == null && valid == null)
				valid = res;
			// Sinon, si l'élément fils courant a pu aller plus loin dans la récursive que
			// les autres, on le garde de côté
			else if(res._2 != null){
//				System.out.println(res._2);
				farthest = res._2.isDeeperThan(farthest._2) ? res : farthest;
			}
		}
		// Si un élément valide a été trouvé, on le retourne
		if (valid != null) {
			// Si la valeur doit être gardée, on l'ajoute au retour
			if (keepValue) {
				List<String> l = new LinkedList<String>(Arrays.asList(splited._1));
				l.addAll(valid._1._1);
				return Tuple.newTuple(Tuple.newTuple(l, valid._1._2), null);
			}
			return valid;
		}

		// Sinon, on retourne un tuple composé de l'erreur augmentée
		farthest._2.addStringToPathFirst(splited._1);
		return Tuple.newTuple(null, farthest._2);
	}

	/**
	 * @param s une chaîne de caractères
	 * @return true si s corresponds au prédicat du noeud, false sinon
	 */
	protected abstract boolean matchesNode(String s);
	
	/**
	 * @return une Liste de KeyEnum indiquant les clés qui ne doivent pas apparaître en même temps que ce type
	 */
	public List<KeyEnum> blacklist() { return Collections.emptyList(); }

	public void setTag(String s) {
		tag = s;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setChildren(List<Node> nodes) {
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
	
	public Tuple2<String, String> split(String s) {
		String[] splited = s.split(" ", 2);
		return Tuple.newTuple(splited[0], splited.length > 1 ? splited[1] : "");
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
				return " (" + children.stream().map((n) -> { /*System.out.println(n);*/ return n.toString(); }).collect(Collectors.joining("|")) + ")";
		} else
			return "";
	}
}
