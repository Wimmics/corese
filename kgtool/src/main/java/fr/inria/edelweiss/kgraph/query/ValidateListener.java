package fr.inria.edelweiss.kgraph.query;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;


/**
 * Validate a query
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class ValidateListener extends EvalListener {
	static final int MAX = 100;
	Graph graph;
	Entailment ontology;
	
	boolean[] visited, defined;
	int cardinality[];
	Edge[] edges;
	
	
	ValidateListener(){
		start();
	}
	
	void start(){
		visited = new boolean[MAX];
		cardinality = new int[MAX];
		edges = new Edge[MAX];
		
		for (int i = 0; i<visited.length; i++){
			visited[i] = false;
			cardinality[i] = 0;
		}
	}
	
	public static ValidateListener create(){
		return new ValidateListener();
	}
	
	public boolean send(Event e){
		
		switch (e.getSort()){
		
		
		case Event.ENUM:
			
			Edge edge = e.getExp().getEdge();
			
			if (! visited[edge.getIndex()] && graph != null){
				visited[edge.getIndex()] = true;
				validate(edge);
			}
			
		}
		
		
		return true;
		

	}
	
	public String toString(){
		String str = "";
		for (int i = 0; i<cardinality.length; i++){
			if (visited[i]){
				String t = Integer.toString(i);
				if (i <= 9){
					t = "0" + t;
				}
				str += t + " " + cardinality[i] + " " + edges[i] + "\n";
			}
		}
		return str;
	}
	
	void validate(Edge edge){
		int i = edge.getIndex();
		edges[i] = edge;
		Node pred = edge.getEdgeNode();
		cardinality[i] = graph.size(pred);
		if (ontology!=null){
			define(edge);
		}
	}
	
	
	// defined class/property/instance
	void define(Edge edge){
		if (ontology.isType(edge) && edge.getNode(1).isConstant()){
			check(edge.getNode(1));
		}
	}
	
	
	/**
	 * 
	 * @param type is a class
	 * is it defined ?
	 * does it have instances ?
	 */
	void check(Node type){
		Node rdfsClass = graph.getNode(RDFS.CLASS);
		Node rdftype   = graph.getPropertyNode(RDF.TYPE);
		Iterable<Entity> it = graph.getEdges(rdftype, type, rdfsClass, 0);
		if (it == null){
			// undefined class
			System.out.println("** Validate: undefined class: " + type);
		}
	}
	
	
	public void setObject(Object obj) {
		super.setObject(obj);
		init();
	}
	
	void init(){
		if (getKGRAM() != null){
			Producer p = getKGRAM().getProducer();
			if (p instanceof ProducerImpl){
				ProducerImpl prod = (ProducerImpl) p;
				graph = prod.getGraph();
				ontology = graph.getEntailment();
			}
			
		}
	}

}
