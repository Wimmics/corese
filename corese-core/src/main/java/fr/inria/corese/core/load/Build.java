package fr.inria.corese.core.load;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import java.util.List;

/**
 * Translate an RDF/XML document into a Graph
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 * 
 */
public interface Build {
	
	 void statement(AResource subj, AResource pred, ALiteral lit);
	
	 void statement(AResource subj, AResource pred, AResource lit);
	 	 
	 void setSource(String src);

	 void exclude(String ns);
	 void exclude(List<String> list);
	
	 void start();
	 
	 void finish();

         void setLimit(int limit);
         
         int getLimit();
}
