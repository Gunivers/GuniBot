package net.gunivers.gunibot.core;

/**
 * Objet pouvant stocker une valeur. Permet de modifier des valeurs à travers
 * des lambdas.
 * Cet objet et thread-safe et peut être utilisé pour transmettre des valeurs entre plusieurs threads. Faites tout de même attention à l'ordre d'éxécution des threads qui peut varié.
 *
 * @author Sylvain
 * @param <T> Le type de la valeur à stocker
 */
public class Container<T> {

	private volatile T the_value;

	/**
	 * Créer le container contenant la valeur null
	 */
	public Container() {
		the_value = null;
	}

	/**
	 * Créer le container contenant la valeur donné
	 *
	 * @param value la valeur à stocker dans le container
	 */
	public Container(T value) {
		the_value = value;
	}

	/**
	 * Récupère la valeur actuellement stocké dans ce container
	 *
	 * @return la valeur contenu dans le container
	 */
	public synchronized T get() {
		return the_value;
	}

	/**
	 * Modifie la valeur présente dans le container
	 *
	 * @param value la nouvelle valeur à contenir
	 * @return la précédente valeur contenue dans le container
	 */
	public synchronized T set(T value) {
		T old_value = the_value;
		the_value = value;
		return old_value;
	}

	/**
	 * Renvoit le {@link #toString()} de la valeur contenu dans ce container
	 */
	@Override
	public synchronized String toString() {
		return the_value.toString();
	}
}
