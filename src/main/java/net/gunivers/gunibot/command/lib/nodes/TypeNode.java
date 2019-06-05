package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.interfaces.Gettable;

public abstract class TypeNode<T> extends Node implements Gettable<T>
{
	protected boolean keepValue = false;
	
	/** Permet de parser la valeur de la clé dans la syntaxe JSON de la commande.
	 * @param s une chaîne de caractères
	 * @throws JsonCommandFormatException si le format de s ne corresponds pas aux attentes
	 */
	public abstract void parse(String s) throws JsonCommandFormatException;

	public void setKeepValue(boolean b) { keepValue = b; }
	public boolean keepValue() { return keepValue; }
}
