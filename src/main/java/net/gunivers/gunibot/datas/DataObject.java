package net.gunivers.gunibot.datas;

import java.util.HashMap;

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
	private HashMap<String,JSONObject> wrappedDatas;
	//TODO Future system : Wrapped Data

	/**
	 * Créer l'objet Data et enregistre Entity.
	 * @param entity l'entité caractéristique de cet objet.
	 */
	public DataObject(E entity) {
		this.entity = entity;
		wrappedDatas = new HashMap<>();
	}

	/**
	 * Récupère l'entité caractéristique de cet objet.
	 * @return l'entité caractéristique de l'objet.
	 */
	public E getEntity() {
		return entity;
	}

	/**
	 * Vérifie si des données sont enregistrés avec la clef indiqué
	 * @param key la clef des données encapsulés
	 * @return {@code true} si la clef indiqué existe, {@code false} sinon
	 */
	public boolean hasWrappedData(String key) {
		return wrappedDatas.containsKey(key);
	}

	/**
	 * Enregistre ces données json aux données encapsulés avec la clef indiqué
	 * @param key la clef des données encapsulés
	 * @param json les données à encapsulé
	 */
	public void wrapDatas(String key, JSONObject json) {
		wrappedDatas.put(key, json);
	}

	/**
	 * Supprimes les données enregistrés avec cette clef
	 * @param key la clef des données encapsulés
	 */
	public void removeWrappedDatas(String key) {
		wrappedDatas.remove(key);
	}

	/**
	 * Récupère les données encapsulés enregistré avec cette clef
	 * @param key la clef des données à récupéré
	 * @return les données encapsulés
	 */
	public JSONObject unwrapDatas(String key) {
		return wrappedDatas.get(key);
	}

	/**
	 * Récupère les données encapsulés enregistré avec cette clef, ou les données par défauts si cette clef n'existe pas
	 * @param key la clef des données à récupérés
	 * @param default_json les données par défaut si la clef n'existe pas
	 * @return les données encapsulés, ou par défaut si inexistant
	 */
	public JSONObject unwrapOrDefaultDatas(String key, JSONObject default_json) {
		return wrappedDatas.getOrDefault(key, default_json);
	}

	/**
	 * Fonction de sauvegarde de donnée appelé par DataCenter.
	 * Les données json sont récupérés pour être enregistré dans la base de donnée.
	 * ATTENTION : Pour la surcharge : super.save() doit être appelés et retransmit afin que le système de "Wrapped Datas" fonctionne !
	 * @return les données json qui seront sauvegardés.
	 */
	public JSONObject save() {
		JSONObject output = new JSONObject();
		output.put("wrapped_datas", wrappedDatas);
		return output;
	}

	/**
	 * Fonction de chargement des données appelé par DataCenter.
	 * Les données json de la base de donnée sont transmises à cette fonction.
	 * ATTENTION : Pour la surcharge : super.load(json) doit être appelés avec les données json d'origine afin que le système de "Wrapped Datas" fonctionne !
	 * @param json les données json de la base de donnée.
	 */
	public void load(JSONObject json) {
		JSONObject json_wrapped_datas = json.optJSONObject("wrapped_datas");
		if(json_wrapped_datas != null) {
			for(String internal_key:json_wrapped_datas.keySet()) {
				wrappedDatas.put(internal_key, json_wrapped_datas.getJSONObject(internal_key));
			}
		}
	}

	@Override
	public String toString() {
		if (this.getEntity() == null) return "NO_DATA";
		return this.getEntity().toString().substring(this.getEntity().toString().indexOf('{')) +": "+ this.getEntity().getId();
	}
}
