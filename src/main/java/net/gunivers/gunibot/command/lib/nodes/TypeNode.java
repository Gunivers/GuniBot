package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;

public abstract class TypeNode extends Node {

	/** Permet de parser la valeur de la clé dans la syntaxe JSON de la commande.
	 * @param s une chaîne de caractères
	 * @throws JsonCommandFormatException si le format de s ne corresponds pas aux attentes
	 */
	public abstract void parse(String s) throws JsonCommandFormatException;
}
