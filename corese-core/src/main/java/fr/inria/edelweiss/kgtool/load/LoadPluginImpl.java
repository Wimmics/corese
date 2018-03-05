package fr.inria.edelweiss.kgtool.load;


/**
 * Load Plugin to preprocess base and source 
 * Used by RBP
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 * 
 */
public class LoadPluginImpl implements LoadPlugin {

	public String dynSource(String str) {
		return str;
	}

	public String statSource(String str) {
		return str;
	}

	public String statBase(String str) {
		return str;
	}
	
}
