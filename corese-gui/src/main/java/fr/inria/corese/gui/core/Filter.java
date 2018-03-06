package fr.inria.corese.gui.core;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class Filter extends FileFilter{

	private String []extension;
	private String description;
	
	/**
	 * Constructeur a deux argument
	 * @param extension pour sélectionner les extensions que l'on désire uploader
	 * @param description
	 */
	public Filter(String []extension, String description){
		this.description=description;
		this.extension=extension;
	}
	
	
	/**
	 * Permet de parcourir le tableau des extension afin de déterminer si par la suite nous acceptons ou non le fichier
	 * @param monExtension
	 * @return
	 */
	boolean appartient (String monExtension){
		for(int i=0;i<extension.length;i++)
			if(monExtension.equals(extension[i]))
				return true;
			return false;
		
	}
	
	/**
	 * Fonction mère du filtre
	 * Elle détermine la possibilité ou non de charger le fichier
	 */
	@Override
	public boolean accept(File f) {
		if(f.isDirectory()) return true;    //si c'est un repertoire on peut (pas d'extension)
		String extension = null;
		String s = f.getName();		//récupére le nom du fichier
		int i=s.lastIndexOf('.');	//récupére l'index a partir duquel il faut couper
		
		if(i>0 && i<s.length() - 1)
			extension = s.substring(i+1).toLowerCase();		//on récupére l'extension
		return extension!=null && appartient(extension);	//on teste s'il faut accepter ou refuser
	}

	@Override
	public String getDescription() {
		return description;
	}

	
	
	
	
}
