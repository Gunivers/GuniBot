package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.Entity;

/**
 * Créer pour chaque objet discord.
 * Permet de gérer et de sauvegardé des données/variables grâce aux fonctions {@link #save()} et {@link #load(JSONObject)}.
 * 
 * @author Syl2010
 * 
 * @param <E> Le type d'objet discord géré par cet objet (héritant de Entity).
 */
public abstract class DataObject<E extends Entity> {

	private E entity;

	/**
	 * Créer l'objet Data et enregistre Entity.
	 * @param entity l'entité caractéristique de cet objet.
	 */
	public DataObject(E entity) {
		this.entity = entity;
	}

	/**
	 * Créer l'object Data, enregistre Entity, et exécute directement la fonction {@link #load(JSONObject)}.
	 * @param entity l'entité caractéristique de cet objet.
	 * @param json les données à chargé dans la fonction {@link #load(JSONObject)}.
	 */
	public DataObject(E entity, JSONObject json) {
		this.entity = entity;
		load(json);
	}

	/**
	 * Récupère l'entité caractéristique de cet objet.
	 * @return l'entité caractéristique de l'objet.
	 */
	public E getEntity() {
		return entity;
	}

	/**
	 * Fonction de sauvegarde de donnée appelé par DataCenter.
	 * Les données json sont récupérés pour être enregistré dans la base de donnée.
	 * @return les données json qui seront sauvegardés.
	 */
	public JSONObject save() {
		return new JSONObject();
	}

	/**
	 * Fonction de chargement des données appelé par DataCenter.
	 * Les données json de la base de donnée sont transmises à cette fonction.
	 * @param json les données json de la base de donnée.
	 */
	public void load(JSONObject json) {}

}
