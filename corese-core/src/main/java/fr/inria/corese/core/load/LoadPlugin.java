package fr.inria.corese.core.load;

/**
 * Load Plugin to preprocess base and source 
 * Used by RBP
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public interface LoadPlugin {

	/**
	 * use case: cos:graph (named graph in RDF/XML)
	 * enables to translate relative to absolute URI
	 */
	String dynSource(String str);

	/**
	 * Tune the source
	 */
	String statSource(String str);
	
	/**
	 * Tune the base
	 */
	String statBase(String str);

}
