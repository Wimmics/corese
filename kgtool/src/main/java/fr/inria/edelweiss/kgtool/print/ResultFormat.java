package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;

import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

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
		Query q = map.getQuery();
		if (q == null) return "";
		
		if (q.isTemplate() || 
			(q.hasPragma(Pragma.TEMPLATE) && map.getGraph() != null)){
			return TemplateFormat.create(map).toString();
		}
		else if (q.isConstruct()){
			return RDFFormat.create(map).toString();
		}
		else {
			return XMLFormat.create(map).toString();
		}
	}
	
	public void write(String name) throws IOException {				
		FileWriter fw = new FileWriter(name);
		String str = toString();
		fw.write(str);
		fw.flush();
		fw.close();
	}

}
