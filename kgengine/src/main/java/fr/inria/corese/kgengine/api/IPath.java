package fr.inria.corese.kgengine.api;

import java.util.Iterator;


public interface IPath {
	
	// resources and  properties in the path 
	// x p y q z
	public Iterator<IResultValue> getResultValues();
	
	public Iterator<IResource> elements();
	
	
	// src1 x p y
	// src2 y q z
	public Iterator<IRelation> relations();
	
	public int length();
	
	public int nbValues();
	
	public int radius();

}
