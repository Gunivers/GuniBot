package net.gunivers.gunibot.core.main_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Vector;

/**
 * Classe hérité de vecteur.
 * Peut écrire et lire un fichier dont chaque ligne correspond à un String du vecteur.
 * Ex : l'index 0 contient la première ligne du fichier, l'index 1 contient la 2nd ligne, etc...
 * L'objet peut donc être traité de la même manière qu'un vecteur,
 * à l'exception qu'il est nécessaire d'utiliser la fonction write() pour que java écrit le contenu du vecteur dans le fichier assigner.
 * @author Sylvain
 */
public class FileManip extends Vector<String>{

	private static final long serialVersionUID = 1L;
	private String leNomFichier;

	/**
	 * Crée le vecteur qui va lire/crée le fichier indiquer en paramètre.
	 * @param nom le fichier à lire/écrire.
	 * @throws IOException Si une erreur à lieu durant la lecture du fichier.
	 */
	public FileManip(String nom) throws IOException{

		super(100);
		if((nom==null)||(nom=="")){
			throw new IllegalArgumentException("invalid or null file name");
		}
		leNomFichier=nom;
		lecture_fichier();
	}

	/**
	 * Crée le vecteur qui va lire/crée le fichier pointé par l'objet File du paramètre.
	 * @param file l'objet File pointant le fichier à créé/lire.
	 * @throws IOException Si une erreur à lieu pour lire/créer le fichier.
	 */
	public FileManip(File file) throws IOException {

		super(100);
		if(file==null){
			throw new IllegalArgumentException("null file reference");
		}
		leNomFichier = file.getCanonicalPath();
		lecture_fichier();
	}

	/**
	 * Constructeur par copie.
	 * Va créer un objet ayant le même contenu et poitant le même fichier que l'objet passé en paramètre.
	 * @param file l'objet à copier.
	 */
	public FileManip(FileManip file){

		super(100);
		if(file==null){
			throw new IllegalArgumentException("null FileManip object");
		}

		for(String ligne:file){
			add(ligne);
		}
		leNomFichier=file.leNomFichier;
	}

	private void lecture_fichier() throws IOException{

		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		String ligne;

		try {
			fis = new FileInputStream(leNomFichier);
		} catch (FileNotFoundException e) {
			try {
				new FileOutputStream(leNomFichier).close();
			} catch (IOException e1) {
				throw new IOException("Erreur durant la création du fichier: "+leNomFichier,e1);
			}
			return ;
		}
		isr = new InputStreamReader(fis);
		br = new BufferedReader(isr);

		try {
			ligne= br.readLine();
			while (ligne != null){
				add(ligne);
				ligne = br.readLine();
			}
		} catch (IOException e) {
			br.close();
			throw new IOException("Erreur de lecture du fichier: "+leNomFichier,e);
		}
		br.close();
	}

	public void afficher(){
		int taille;
		taille = size();
		for (int i=0;i<taille;i++){
			System.out.println(i+" | "+get(i));
		}
	}

	/**
	 * Ordonne à l'objet d'écrire la totalité de son vecteur dans son fichier.
	 * Doit être appelé à chaque fois que vous avez modifié le vecteur si vous souhaitez que le fichier conserve toutes les données du vecteur.
	 * @throws IOException si une erreur d'écriture à lieu.
	 */
	public void write() throws IOException{
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		int taille;

		try {
			fos = new FileOutputStream(leNomFichier);
		} catch (IOException e) {
			throw new IOException("Erreur d'écriture pour le fichier : "+leNomFichier,e);
		}
		osw = new OutputStreamWriter(fos);
		bw = new BufferedWriter(osw);
		taille=size();

		try{
			for (int i=0;i<taille;i++){
				bw.write(get(i));
				bw.newLine();
			}
		}
		catch (IOException e){
			bw.close();
			throw new IOException("Erreur d'écriture dans le fichier : "+leNomFichier,e);
		}
		try {
			bw.flush();
		} catch (IOException e) {
			bw.close();
			throw new IOException("Erreur de sauvegarde du fichier : "+leNomFichier,e);
		}

		bw.close();
	}

	/**
	 * crée un nouveau vecteur avec tout les lignes vides supprimés.
	 * @return le nouvel objet avec son vecteur traité.
	 */
	public FileManip supprimeLignesVides(){

		FileManip output=new FileManip(this);
		String ligne;
		int taille = output.size();

		for (int i=0;i<taille;i++){
			ligne=output.get(i);
			if (ligne.equals("")){
				output.remove(i);
				i--;
			}
		}
		return output;
	}

	/**
	 * crée un nouveau vecteur avec tout les espaces supprimés.
	 * @return le nouvel objet avec son vecteur traité.
	 */
	public FileManip supprimeEspaces(){
		FileManip output = new FileManip(this);
		String ligne;
		int taille = output.size();

		for (int i=0;i<taille;i++){
			ligne=output.get(i);
			if (ligne.contains(" ")){
				String new_ligne = ligne.trim();
				output.set(i, new_ligne);
			}
		}
		return output;
	}

	/**
	 * vide le vecteur ET le fichier.
	 */
	@Override
	public void clear(){
		super.clear();
		try {
			write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Crée et renvoie un objet File pointant le même fichier
	 * @return l'objet File traité par cet objet
	 */
	public File getFile(){
		return new File(leNomFichier);
	}

	/**
	 * Supprime le dossier et tout son contenu (dossier et fichier).
	 * @param folder le dossier à supprimé.
	 * @throws IOException Si une erreur à lieu durant la suppression
	 */
	public static List<File> fileRecursivelyRemove(File folder) throws IOException{

		Vector<File> output = new Vector<>();

		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File file:files){
				output.addAll(fileRecursivelyRemove(file));
			}
		}
		java.nio.file.Files.delete(folder.toPath());
		output.add(folder);
		return output;
	}

	/**
	 * Liste tout les fichiers et dossiers contenu dans le dossier indiqué.
	 * @param folder le dossier racine de la recherche.
	 * @return la liste complète des fichiers et dossiers.
	 */
	public static List<File> listAllFilesAndFolder(File folder){
		Vector<File> output = new Vector<>();

		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File file:files){
				output.addAll(listAllFilesAndFolder(file));
			}
		}
		else{
			output.add(folder);
		}
		return output;
	}

}