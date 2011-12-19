package fr.inria.edelweiss.kgtool.print;

import fr.inria.edelweiss.kgram.core.Mappings;

/**
 * RDF/XML or SPARQL XML Result format according to query
 * Olivier Corby, Edelweiss INRIA 2011
 */
public class ResultFormat {
	
	Mappings map;
	
	ResultFormat(Mappings m){
		map = m;
	}
	
	static public ResultFormat create(Mappings m){
		return new ResultFormat(m);
	}
	
	public String toString(){
		if (map.getQuery()==null) return "";
		
		if (map.getQuery().isConstruct()){
			return RDFFormat.create(map).toString();
		}
		else {
			return XMLFormat.create(map).toString();
		}
	}

}
