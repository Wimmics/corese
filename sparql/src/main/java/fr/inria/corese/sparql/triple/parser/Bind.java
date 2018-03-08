package fr.inria.corese.sparql.triple.parser;

import java.util.Enumeration;
import java.util.Hashtable;



public class Bind extends Hashtable<String,String> {
	
	
	Bind merge(Bind b){
		for (Enumeration<String> en = b.keys(); en.hasMoreElements();){
			String var = en.nextElement();
			this.put(var, var);
		}
		return this;
	}
	
	void bind(String var){
		put(var, var);
	}
	
	/**
	 * for all var in env2
	 * if var !in env1 && var in this : error
	 * exp1 optional exp2
	 */
	boolean check(Bind env1, Bind env2){
		for (Enumeration<String> en = env2.keys(); en.hasMoreElements();){
			String var = en.nextElement();
			if (! env1.contains(var) && this.contains(var)){
				return false;
			}
		}
		return true;
	}
	
	public String toString(){
		String str = "";
		for (Enumeration<String> en = this.keys(); en.hasMoreElements();){
			str  += en.nextElement() + " ";
		}
		return str;
	}
}
