package net.gunivers.gunibot.core.command.nodes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.CommandSyntaxError;
import net.gunivers.gunibot.core.command.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.core.command.keys.KeyEnum;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public abstract class Node {

    private List<Node> children = new LinkedList<>();
    private Method run = null;
    private boolean keepValue = false;
    private String tag;
    private String id;

    public final Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> matches(String string, Command command) {

	Tuple2<String, String> splited = split(string);

	// Si le noeud n'est qu'une référence, on exécute la fonction #matches du noeud
	// pointé
	if (this instanceof NodeReference) {
	    Optional<Node> node = command.getNodeById(((NodeReference) this).getId());
	    if (node.isPresent())
		return node.get().matches(string, command);
	    throw new IllegalStateException(
		    "L'ID " + command.getNodeById(((NodeReference) this).getId()) + " n'est pas référencée.");
	}

	// L'élément courant n'est pas valide
	if ((splited.value1 == null && splited.value2 == null) || !matchesNode(splited.value1))
	    return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.ARG_INVALID, splited.value1));
	// S'il n'y a plus qu'un argument en paramètre alors que la syntaxe est plus
	// longue
	if (splited.value2.equals("") && run == null && children.size() > 0)
	    return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_LONGER, splited.value1));
	// S'il y a plus d'arguments que d'éléments dans la syntaxe
	if (!splited.value2.equals("") && children.size() == 0)
	    return Tuple.newTuple(null, new CommandSyntaxError(SyntaxError.SYNTAX_SHORTER, string));
	// Cas terminal
	if (splited.value2.equals("") && run != null)
	    return Tuple.newTuple(Tuple.newTuple(
		    keepValue ? new LinkedList<>(Arrays.asList(splited.value1)) : new LinkedList<>(), run), null);

	// Cas intermédiaires

	Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> valid = null;
	Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> farthest = Tuple2.newTuple(null,
		new CommandSyntaxError());

	for (Node node : children) {
	    // Récursivité
	    Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> result = node.matches(splited.value2, command);

	    // Si aucun élément n'a encore été validé et que l'élément fils courant est
	    // valide, on le garde de côté
	    if (result.value2 == null && valid == null) {
		valid = result;
	    } else if (result.value2 != null) {
		farthest = result.value2.isDeeperThan(farthest.value2) ? result : farthest;
	    }
	}
	// Si un élément valide a été trouvé, on le retourne
	if (valid != null) {
	    // Si la valeur doit être gardée, on l'ajoute au retour
	    if (keepValue) {
		List<String> list = new LinkedList<>(Arrays.asList(splited.value1));
		list.addAll(valid.value1.value1);
		return Tuple.newTuple(Tuple.newTuple(list, valid.value1.value2), null);
	    }
	    return valid;
	}

	// Sinon, on retourne un tuple composé de l'erreur augmentée
	farthest.value2.addStringToPathFirst(splited.value1);
	return Tuple.newTuple(null, farthest.value2);
    }

    /**
     * @param string une chaîne de caractères
     * @return true si s corresponds au prédicat du noeud, false sinon
     */
    protected abstract boolean matchesNode(String string);

    /**
     * @return une Liste de KeyEnum indiquant les clés qui ne doivent pas apparaître
     *         en même temps que ce type
     */
    public List<KeyEnum> blacklist() {
	return Collections.emptyList();
    }

    /**
     * @return une Liste de KeyEnum indiquant les clés qui doivent obligatoirement
     *         apparaître en même temps que ce type
     */
    public List<KeyEnum> mandatory() {
	return Collections.emptyList();
    }

    public void setTag(String newTag) {
	tag = newTag;
    }

    public String getTag() {
	return tag;
    }

    public void setChildren(List<Node> nodes) {
	children.addAll(nodes);
    }

    public void setKeepValue(boolean newKeepValue) {
	keepValue = newKeepValue;
    }

    public boolean keepValue() {
	return keepValue;
    }

    public void setExecute(Method newMethod) {
	run = newMethod;
    }

    public Method getMethod() {
	return run;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Tuple2<String, String> split(String string) {
	String[] splited = string.split(" ", 2);
	return Tuple.newTuple(splited[0], splited.length > 1 ? splited[1] : "");
    }

    @Override
    public String toString() {
	if (children.size() == 0)
	    return "<" + tag + ">";
	return "<" + tag + ">" + childrenToString();
    }

    protected String childrenToString() {
	if (children.size() == 1) {
	    if (run != null)
		return " [" + children.get(0).toString() + ']';
	    else
		return " " + children.get(0).toString();
	}
	if (children.size() > 1) {
	    if (run != null)
		return " [" + children.stream().map((node) -> node.toString()).collect(Collectors.joining("|")) + "]";
	    else
		return " (" + children.stream().map((node) -> node.toString()).collect(Collectors.joining("|")) + ")";
	} else
	    return "";
    }
}
