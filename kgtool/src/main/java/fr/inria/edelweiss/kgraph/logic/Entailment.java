package fr.inria.edelweiss.kgraph.logic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeCore;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * RDFS Entailment
 * 
 * rdfs:domain rdfs:range 
 * rdfs:subPropertyOf rdfs:subClassOf
 * owl:SymmetricProperty owl:inverseOf
 * 
 * subPropertyOf & subClassOf are not transitive in the graph
 * but their instances are typed according to transitivity
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Entailment {
	static final String W3C = "http://www.w3.org";
	public static final String KGRAPH2 		= "http://ns.inria.fr/edelweiss/2010/kgraph#";
	public static final String KGRAPH 		= ExpType.KGRAM;

	public static String DEFAULT 	= KGRAPH + "default";
	public static String ENTAIL 	= KGRAPH + "entailment";
	public static String RULE 		= KGRAPH + "rule";
	public static String[] GRAPHS   = {DEFAULT, ENTAIL, RULE};


	public static final String RDF   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS  =  "http://www.w3.org/2000/01/rdf-schema#";
	public static final String XSD   =  "http://www.w3.org/2001/XMLSchema#";
	public static final String OWL   =  "http://www.w3.org/2002/07/owl#";

	public static final String RDFSSUBPROPERTYOF = RDFS + "subPropertyOf";
	public static final String RDFSSUBCLASSOF 	 = RDFS + "subClassOf";
	public static final String RDFSDOMAIN 		 = RDFS + "domain";
	public static final String RDFSRANGE 		 = RDFS + "range";
	public static final String RDFSMEMBER 		 = RDFS + "member";
	public static final String RDFBLI 		 	 = RDF  + "_";
	public static final String RDFSCLASS 	 	 = RDFS + "Class";
	public static final String RDFSRESOURCE		 = RDFS + "Resource";
	public static final String RDFSLABEL 	 	 = RDFS + "label";
	public static final String RDFSCOMMENT 	 	 = RDFS + "comment";

	public static final String RDFTYPE 			 = RDF + "type";
	public static final String RDFPROPERTY 	 	 = RDF + "Property";

	public static final String OWLINVERSEOF  = OWL + "inverseOf";
	public static final String OWLSYMMETRIC  = OWL + "SymmetricProperty";
	public static final String OWLTRANSITIVE = OWL + "TransitiveProperty";
	public static final String OWLREFLEXIVE  = OWL + "ReflexiveProperty";
	public static final String OWLCLASS  	 = OWL + "Class";
	
	public static final String DATATYPE_INFERENCE 		 = KGRAPH + "DATATYPE";

	

	Signature domain, range, inverse, symetric, subproperty;
	Graph graph, target;
	Node hasType, subClassOf, graphNode;
	Edge last, current;
	
	Hashtable<Node, Integer> count; 

	boolean	
		// generate rdf:type wrt rdfs:subClassOf
		isSubClassOf 	 = !true,
		isSubPropertyOf = true,
		// entailments in default graph
		isDefaultGraph  = true,
		// infer datatype from property range for literal (à la corese)
		isDatatypeInference = false,
		isRDF  = true,
		isRDFS = true;
	
	// deprecated
	boolean recurse = false,
	isDebug = false;
	
	class Signature extends Hashtable<Node, List<Node>> {
		
		void define(Node pred, Node value){
			List<Node> list = get(pred);
			if (list == null){
				list = new ArrayList<Node>();
				put(pred, list);
			}
			if (! list.contains(value)){
				list.add(value);
			}
		}
		
	}
	
	
	public static Entailment create(Graph g){
		return new Entailment(g);
	}
	
	Entailment(Graph g){
		graph = g;
		target = g;
		symetric 	= new Signature();
		inverse  	= new Signature();
		domain 	 	= new Signature();
		range 	 	= new Signature();
		subproperty = new Signature();
		count = new Hashtable<Node, Integer>();
		hasType = graph.addProperty(RDFTYPE);
	}
	
	public void clear(){
		symetric.clear();
		inverse.clear();
		domain.clear();
		range.clear();
		subproperty.clear();
	}

	public void set(String name, boolean b){
		if (name.equals(RDFSSUBCLASSOF)) 		  isSubClassOf = b;
		else if (name.equals(RDFSSUBPROPERTYOF))  isSubPropertyOf = b;
		else if (name.equals(DATATYPE_INFERENCE)) isDatatypeInference = b;
		else if (name.equals(ENTAIL)) 			  isDefaultGraph = b;
		else if (name.equals(RDFS))				  isRDFS = b;
	}
	
	public boolean isDatatypeInference(){
		return isDatatypeInference;
	}
	
	public boolean isSubClassOfInference(){
		return isSubClassOf;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	/**
	 * External entry point
	 * Record ontology definitions such as range
	 */
	public void process(Node gNode, Edge edge){
		define(gNode, edge);
	}
	
	/**
	 * clear tables of meta statements (domain, range, etc.)
	 * fill these tables with current graph
	 */
	public void reset(){
		clear();
		define();
	}
	
	/**
	 * Record definitions corresponding to ontological edges from graph:
	 * pp rdfs:range rr
	 * use case: add Entailment on existing graph
	 * use case: redefine after delete 
	 */
	public void define(){
		for (Node pred : graph.getProperties()){
			for (Entity ent : graph.getEdges(pred)){
				Edge edge = ent.getEdge();
				boolean isMeta = define(ent.getGraph(), ent.getEdge());
				if (! isMeta){
					if (edge.getLabel().equals(RDFTYPE)){
						// continue for rdf:type owl:Symmetric
					}
					else {
						break;
					}
				}
			}
		}
	}
	
	
	public int entail(){
		int nb = inference();
		return nb;
	}
	
	
	
	
	/**
	 * Internal process of entailed edge 
	 */
	void recordWithoutEntailment(Node gNode, Edge ee, EdgeImpl edge){
		Entity ent = target.add(edge);
	}
	
	void recordWithEntailment(Node gNode, Edge ee, EdgeImpl edge){
		Entity ent = target.add(edge);
		define(gNode, edge);
	}
	
	EdgeImpl create(Node src, Node sub, Node pred, Node obj){
		return EdgeCore.create(src, sub, pred, obj);
	}
	
	/**
	 * Store property domain, range, subPropertyOf, symmetric, inverse
	 */
	public boolean define(Node gNode, Edge edge){
		//if (! edge.getLabel().startsWith(W3C)) return;
		boolean isMeta = false;
//		if (edge.getNode(1).isBlank()){
//			// DRAFT: do nothing
//		}
//		else 
		if (hasLabel(edge, RDFTYPE)){
			if (edge.getNode(1).getLabel().equals(OWLSYMMETRIC)){
				symetric.define(edge.getNode(0), edge.getNode(0));
				isMeta = true;			
			}
		}
		else if (hasLabel(edge, OWLINVERSEOF)){
			inverse.define(edge.getNode(0), edge.getNode(1));
			inverse.define(edge.getNode(1), edge.getNode(0));
			isMeta = true;			
		}
		else if (hasLabel(edge, RDFSDOMAIN)){
			domain.define(edge.getNode(0), edge.getNode(1));
			isMeta = true;			
		}
		else if (hasLabel(edge, RDFSRANGE)){
			range.define(edge.getNode(0), edge.getNode(1));
			isMeta = true;			
		}
		else if (hasLabel(edge, RDFSSUBPROPERTYOF)){
			subproperty.define(edge.getNode(0), edge.getNode(1));
			isMeta = true;			
		}
		else if (hasLabel(edge, RDFSSUBCLASSOF)){
			subClassOf = edge.getEdgeNode();
			isMeta = true;			
		}
		return isMeta;
	}
	
	/**
	 * Add RDFS entailment to the graph, given edge and RDFS Schema
	 * 
	 * Entail domain, range, subPropertyOf, symmetric, inverse
	 * 
	 */
	public void entail(Node gNode, Edge edge){
		property(gNode, edge);
		signature(gNode, edge);
		subsume(gNode, edge);
	}

	/**
	 *  graph creates new property pNode
	 *  infer:
	 *  pNode rdf:type rdf:Property
	 *  TODO: BUG: concurrent modification while entailment
	 *  TODO: move at entailment time
	 */
	void defProperty(Node pNode) {
		Node gNode = graph.addGraph(ENTAIL);
		Node tNode = graph.addResource(RDFPROPERTY);
		EdgeImpl ee =  create(gNode, pNode, hasType, tNode);
		recordWithoutEntailment(gNode, null, ee);
		
		if (isRDFS && pNode.getLabel().startsWith(RDFBLI)){
			// rdf:_i rdfs:subPropertyOf rdfs:member
			tNode    = graph.addResource(RDFSMEMBER);
			Node sub = graph.addProperty(RDFSSUBPROPERTYOF);
			ee =  create(gNode, pNode, sub, tNode);
			recordWithEntailment(gNode, null, ee);
		}
	}
	
	void property(Node gNode, Edge edge){	
		inverse(gNode, edge, symetric);
		inverse(gNode, edge, inverse);
		
		subproperty(gNode, edge);
	}
	
	
	void inverse(Node gNode, Edge edge, Signature table){
		Node pred = edge.getEdgeNode();
		List<Node> 	list = table.get(pred);
		if (list != null){
			for (Node type : list){
				EdgeImpl ee =  create(gNode, edge.getNode(1), type, edge.getNode(0));
				recordWithoutEntailment(gNode, edge, ee);
			}
		}
	}
	
	void subproperty(Node gNode, Edge edge){
		if (! isSubPropertyOf) return;
		
		Node pred = edge.getEdgeNode();
		List<Node> list = subproperty.get(pred);
		if (list!=null){
			for (Node sup : list){
				EdgeImpl ee =  create(gNode, edge.getNode(0), sup, edge.getNode(1));
				recordWithoutEntailment(gNode, edge, ee);
				if (isMeta(sup)){
					define(gNode, ee);
				}
			}
		}
	}

	
	
	void signature(Node gNode, Edge edge){
		Node pred = edge.getEdgeNode();
		
		infer(gNode, edge, domain.get(pred), 0);

		if (graph.isIndividual(edge.getNode(1))){
			infer(gNode, edge, range.get(pred), 1);
		}
	}
	
	
	void subsume(Node gNode, Edge edge){
		// infer types using subClassOf
		if (isSubClassOf && hasLabel(edge, RDFTYPE)){
			infer(gNode, edge);
		}
	}
	
	boolean differ(Edge edge, Edge last){
		if (last == null) return true;
		return 
			! (edge.getNode(0).same(last.getNode(0)) && 
			   edge.getEdgeNode().same(last.getEdgeNode()));
	}
	
	/**
	 * signature
	 */
	void infer(Node gNode, Edge edge, List<Node> list, int i){
		Node node = edge.getNode(i);
		IDatatype dt = (IDatatype) node.getValue();
		if (i == 1 && dt.isLiteral()) return;
		
		if (list!=null){
			for (Node type : list){
				EdgeImpl ee =  create(gNode, node, hasType, type);
				recordWithoutEntailment(gNode, edge, ee);
			}
		}
	}
	
	/**
	 * edge:   in:aa rdf:type ex:Person
	 * infer super classes
	 */
	void infer(Node gNode, Edge edge){
		if (subClassOf == null) return;
		
		Iterable<Entity> list = graph.getEdges(subClassOf, edge.getNode(1), 0);
		
		if (list!=null){
			for (Entity type : list){
				EdgeImpl ee = 
					 create(gNode, edge.getNode(0), hasType, type.getEdge().getNode(1));
				recordWithoutEntailment(gNode, edge, ee);
			}
		}
	}
	
	public List<Node> getSubClass(Node node){
		ArrayList<Node> list = new ArrayList<Node>();
		getClasses(node, list, true);
		return list;
	}

	public List<Node> getSuperClass(Node node){
		ArrayList<Node> list = new ArrayList<Node>();
		getClasses(node, list, false);
		return list;
	}
	
	public void getClasses(Node node, List<Node> list, boolean isSubClass){
		Iterable<Entity> it = 
			graph.getEdges(graph.getPropertyNode(RDFSSUBCLASSOF), node, (isSubClass)?1:0);
		
		if (it == null) return;
		
		for (Entity ent : it){
			Node nn = ent.getEdge().getNode((isSubClass)?0:1);
			if (! list.contains(nn)){
				list.add(nn);
				getClasses(nn, list, isSubClass);
			}
		}		
	}

	public boolean isSubClassOf(Node node, Node sup){
		if (node.same(sup)) return true;
		Node pred = graph.getPropertyNode(RDFSSUBCLASSOF);
		if (pred == null) return false;
		Iterable<Entity> it = graph.getEdges(pred, node, 0);
		
		if (it == null) return false;
		
		for (Entity ent : it){
			Node nn = ent.getEdge().getNode(1);
			if (nn.same(sup)){
				return true;
			}
			if (nn.same(node)){
				continue;
			}
			if (isSubClassOf(nn, sup)){
				return true;
			}
		}
		
		return false;
	}

	public boolean isType(Edge edge){
		return hasLabel(edge, RDFTYPE);
	}
	
	public boolean isType(Node pred){
		return pred.getLabel().equals(RDFTYPE);
	}
	
	public boolean isSubClassOf(Node pred){
		return pred.getLabel().equals(RDFSSUBCLASSOF);
	}
	
	boolean hasLabel(Edge edge, String type){
		return edge.getLabel().equals(type);
	}
	
	public boolean isSymmetric(Edge edge){
		return symetric.containsKey(edge.getEdgeNode());
	}
	

	
	
	/*********************
	 * 
	 * Entailments
	 * 
	 *********************/
	
	int inference(){	
		target = Graph.create();
		int count = 0;

		// extension of metamodel
		meta();
		List<Entity> lDef = copy(target, graph);
		count += lDef.size();
		
		target = Graph.create();

		// first: entail for all edges in graph
		// and add infered edges in fresh target graph
		graphEntail();

		count += loop();
		
		return count;
	}
	
	
	/**
	 * Complementary entailment from rules on new edge list
	 */
	public int entail(List<Entity> list){
		inference(list);
		return loop();
	}
	
	
	/** 
	 * second: loop on target to infer new edges
	 * until no new edges are infered
	 * new edges are added in list	
	 */
	int loop(){
		int count = 0;

		boolean any = true;

		while (any){

			any = false;

			// Try to add in graph the entailed edges
			// already existing edges are rejected
			// accepted edges are also put in list

			List<Entity> list = copy(target, graph);

			// loop on new infered edges
			if (list.size()>0){
				any = true;
				inference(list);
			}
			
			count += list.size();

		}
		
		return count;
	}
	
	void inference(List<Entity> list){
		target = Graph.create();

		for (Entity ent : list){
			Edge edge = ent.getEdge();
			Node gg = getGraph(ent);

			property(gg, edge);						
			subsume(gg, edge);						
			signature(gg, edge);
		}
	}
	
	/**
	 * Copy entailed edges into graph
	 */
	List<Entity> copy(Graph from, Graph to){
		ArrayList<Entity> list = new ArrayList<Entity>();
		
		for (Node pred : target.getProperties()){

			for (Entity ent : target.getEdges(pred)){

				Edge edge = ent.getEdge();
				Entity ee = graph.add((EdgeImpl)edge);
				if (ee != null){
					list.add((EdgeImpl)edge);
				}
			}
		}
		return list;
	}
	
	
	
	
	/**
	 * Graph where entailed edges are stored
	 * May be default or edge graph
	 */
	Node getGraph(Entity ent){
		if (isDefaultGraph){
			if (graphNode == null){
				graphNode = graph.addGraph(ENTAIL);
			}
			return graphNode;
		}
		return ent.getGraph();
	}
	
	
	/**
	 * First loop on whole graph that was just loaded 
	 * Entailed edges stored in fresh target graph
	 * TODO: defProperty() for rule entail
	 */
	void graphEntail(){
		for (Node pred : graph.getProperties()){
			// ?p rdf:type rdf:Property
			defProperty(pred);
			Entity prev = null;
			for (Entity ent : graph.getEdges(pred)){
				Edge edge = ent.getEdge();
				Node gg = getGraph(ent);

				property(gg, edge);
				subsume(gg, edge);

				if (prev == null){
					signature(gg, edge);
					prev = ent;
				}
				else if (! prev.getEdge().getNode(0).same(ent.getEdge().getNode(0)) ||
						! prev.getGraph().same(gg)){
					signature(gg, edge);
					prev = ent;
				}
			}
		}
	}
	
	
	/**
	 * 
	 * Meta model refinement 
	 * 
	 * Currently:
	 * wrt rdfs:subPropertyOf rdfs:property only
	 * direct subproperties only
	 * 
	 * codomain rdfs:subPropertyOf rdfs:range && 
	 * pp codomain rr 
	 * => 
	 * pp range rr
	 * 
	 * TODO:
	 * subProperty at depth more than 1
	 * hasType inverseOf rdf:type
	 * MySymmetric subClassOf owl:Symmetric
	 * 
	 */
	
	void meta(){
		Node subprop = graph.getPropertyNode(RDFSSUBPROPERTYOF);
		if (subprop != null){
			for (Entity ent : graph.getEdges(subprop)){
				Edge edge = ent.getEdge();
				if (isMeta(edge.getNode(1))){
					// codomain subPropertyOf rdfs:range
					Node pred = edge.getNode(0);
					for (Entity meta : graph.getEdges(pred)){
						// entail: pp codomain dd
						subproperty(getGraph(meta), meta.getEdge());
					}
				}
			}
		}
	}
	
	
	boolean isMeta(Node pred){
		return pred.getLabel().startsWith(RDFS);
	}
	
	public String getRange(String pred){
		if (true)
		return getRange2(pred);
		
		Edge range = graph.getEdge(RDFSRANGE, pred, 0);
		if (range == null) return null;
		return range.getNode(1).getLabel();
	}
	
	
	public String getRange2(String pred){
		Node node = graph.getPropertyNode(pred);
		if (node == null) return null;
		List<Node> list = range.get(node);
		if (list == null) return null;
		return list.get(0).getLabel();
	}
	
	
	
	
	
	
	
	
	
	
	void reject(Edge edge){
		Integer val = count.get(edge.getEdgeNode());
		if (val == null){
			val = 0;
		}
		count.put(edge.getEdgeNode(), ++val);
	}
	
	public String display(){
		String str = "";
		for (Node pred : count.keySet()){
			str += pred + ": " + count.get(pred) + "\n";
		}
		return str;
	}


	
	
	
	
}
