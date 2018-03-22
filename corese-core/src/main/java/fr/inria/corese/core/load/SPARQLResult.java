package fr.inria.corese.core.load;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.compiler.result.XMLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

public class SPARQLResult  extends XMLResult {
	
	Graph graph, local;
	
	SPARQLResult(Graph g){
		super();
		graph = g;
		local = Graph.create();
	}
	
	public static SPARQLResult create(Graph g){
		return new SPARQLResult(g);
	}
	
	public Node getURI(String str){
		Node n = local.getResource(str);
		if (n != null){
			return n;
		}
		n = graph.getResource(str);
		if (n != null){
			return n;
		}

		IDatatype dt = DatatypeMap.createResource(str);
		n = local.getNode(dt, true, true);
		return n;
	}
	
	
	public Node getBlank(String str){
		Node n = local.getBlankNode(str);
		if (n != null){
			return n;
		}
		
		IDatatype dt = DatatypeMap.createBlank(str);
		n = local.getNode(dt, true, true);
		return n;
	}
	
	public Node getLiteral(String str, String datatype, String lang){
		IDatatype dt = DatatypeMap.createLiteral(str, datatype, lang);
		Node n = graph.getNode(dt, false, false);
		if (n == null){
			n = local.getNode(dt, true, true);
		}
		return n;
	}

}
