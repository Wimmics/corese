package fr.inria.edelweiss.kgraph.logic;

import java.util.Hashtable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Semantic distance & similarity
 * Corese Algorithm
 * 
 * PRAGMA: 
 * need a graph where Node setObject (Object depth)
 * need a root class, default is rdfs:Resource
 * 
 * If a class is not subClassOf rdfs:Resource, depth simulate to 1
 * 
 * TODO:
 * Expensive with Entailment because of rdf:type generated rdfs:domain and rdfs:range
 * It loops if there is a loop in the subClassOf hierarchy
 * 
 * @author Olivier Corby & Fabien Gandon, Edelweiss INRIA 2011
 */

public class Distance {
	
	Graph graph;
	Node  root, subClassOf;
	NodeList topList;
	Integer MONE = new Integer(-1);
	Integer ONE = new Integer(1);

	int depthMax = 0;
	double K = 1, dmax = 0;
	boolean isUpdate = false;
	private Hashtable<Node, Node> table;
	
	static Logger logger = Logger.getLogger(Distance.class);
	
	Distance(Graph g, Node r){
		graph = g;
		root = r;
		topList = new NodeList();
		topList.add(root);
		table = new Hashtable<Node, Node>();
		subClassOf = graph.getPropertyNode(Entailment.RDFSSUBCLASSOF);
		if (graph.getEntailment() == null){
			//logger.info("Distance set Graph with Entailment");
			graph.setEntailment();
		}
		init();
	}
	
	public static Distance create(Graph g){
		return new Distance(g, getRoot(g));
	}
	
	public void setUpdate(boolean b){
		isUpdate = b;
	}
	
	static Node getRoot(Graph g){
		Node n = g.getNode(Entailment.RDFSRESOURCE);
		if (n == null){
			n = g.createNode(Entailment.RDFSRESOURCE);
		}
		return n;
	}
	
	public static Distance create(Graph g, Node r){
		return new Distance(g, r);
	}
	
	Integer getDepth(Node n){
		Integer d = (Integer) n.getObject() ;
		return d ;
	}
	
	void setDepth(Node n, Integer i){
		n.setObject(i);
	}
	
	void init(){
		depthMax = 0;
		dmax = 0;
		K = 1;
		depth();
	}
	
	void reinit(){
		reset(root);
		init();
		isUpdate = false;
	}
	
	void depth(){
		setDepth(root, 0);
		table.clear();
		depth(root);
		if (depthMax == 0){
			// in case there is no rdfs:subClassOf, choose  max depth 1
			depthMax = 1;
		}
		setDmax(depthMax);
	}
	
	/**
	 * Recursively compute depth
	 * Take multiple inheritance into account
	 */
	void depth(Node sup){
		visit(sup);
		
		Integer depth = getDepth(sup);
		Integer dd = depth + 1;
		for (Node sub : getSubClasses(sup)){
			if (sub != null && ! visited(sub)){
				Integer d = getDepth(sub);
				if (d == null || dd > d){
					if (dd > depthMax){
						depthMax = dd;
					}
					setDepth(sub, dd);
					depth(sub);
				}
			}
		}
		
		leave(sup);
	}
	
	boolean visited(Node node){
		return table.containsKey(node);
	}
	
	void visit(Node node){
		table.put(node, node);
	}
	
	void leave(Node node){
		table.remove(node);
	}
	
	
	
	void reset(Node sup){
		setDepth(sup, null);
		for (Node sub : getSubClasses(sup)){
			if (sub != null){
				reset(sub);
			}
		}
	}

	
	/**
	 * Used by semantic distance
	 * Node with no depth (not in subClassOf hierarchy of root) is considered at depth 1 just under root (at depth 0)
	 */
	Integer getDDepth(Node n){
		Integer d = (Integer) n.getObject() ;
		if (d == null) return ONE;
		return d ;
	}
	
	
	float step(Node f) {
		return (float)(1 / Math.pow(2, getDDepth(f)));
	}
	
	double step(int depth){
		 return (1 / Math.pow(2, depth));
	 }
	
	void setDmax(int max){
		dmax = 0;
		for (int i=1; i <= max; i++){
			dmax += step(i);
		}
		dmax = 2 * dmax;
		K = Math.pow(2, max) / 100.0;
	}
	
	public double maxDistance(){
		return dmax;
	}
	
	public double similarity(float distance){
		return similarity(distance, 1);
	}
	
	public double similarity(float distance, int num){
		 double sim = distance / (dmax * num);
		 sim = 1 / ( 1 + (K * sim)); //  1/1+0=1 1/1+1 = 1/2    1/1+2 = 1/3
		 return sim;
	 }
	 
	
	boolean isRoot(Node n){
		return n.same(root);
	}
	
	/**
	 * Node with no super class is considered subClassOf root
	 */
	public Iterable<Node> getSuperClasses(Node node){
		if (subClassOf == null) return topList;
		Iterable<Node> it = graph.getNodes(subClassOf, node, 0);
		if (! it.iterator().hasNext()){
			return topList;
		}
		return it;
	}
	
	public Iterable<Node> getSubClasses(Node node){
		if (subClassOf == null) return new ArrayList<Node>();
		return graph.getNodes(subClassOf, node, 1);
	}
	
	
	/**
	 * Return ontological distance between two concepts
	 * Distance is the sum of distance (by steps) to the deepest common ancestor
	 * Compute the deepest common ancestor by climbing step by step through ancestors
	 * always walk through deepest ancestors of c1 or c2 (i.e. delay less deep ancestors)
	 * hence the first common ancestor is the deepest
	 */
	
	public float sdistance(Node c1, Node c2)	{
		Entailment ee = graph.getEntailment();
		if (ee.isSubClassOf(c1, c2) || ee.isSubClassOf(c2, c1)){
			return 0;
		}
		return distance(c1, c2);
	}
	
	public double similarity(Node c1, Node c2){
		if (c1.equals(c2)) return 1;
		float d = distance(c1, c2);
		return similarity(d, 1);
	}
	
	public float distance(Node c1, Node c2)	{
		return distance(c1, c2, true);
	}
	
	
	
	
	
	
	
	
	
	
	class NodeList extends ArrayList<Node> {}
	
	class Table extends Hashtable<Node, Float> {}
	
	
	float distance(Node c1, Node c2, boolean step)	{
		
		Table hct1 = new Table();
		Table hct2 = new Table();
		
		NodeList ct1current = new NodeList();
		NodeList ct2current = new NodeList();

		boolean end = false;
		boolean endC1 = false;
		boolean endC2 = false;
		int max1, max2; // maximal (deepest) depth of current
		
		Float i = new Float(0);
		Float j = new Float(0);
		Node common=null;
		int count=0;
		hct1.put(c1, i);
		hct2.put(c2, j);
		max1=getDDepth(c1); 
		max2=getDDepth(c2); 
		ct1current.add(c1);
		ct2current.add(c2);
		
		if (max1 == 0) {
			if (isRoot(c1)){ 
				endC1 = true; // ???
			}
			else {
				return 0;
			}
		}
		if (max2 == 0){
			if (isRoot(c2)){
				endC2 = true;
			}
			else {
				return 0;
			}
		}
		
		if (hct1.containsKey(c2)) end = true;
		
		while (!end) {
			if (count++ > 10000) {
				logger.debug("** Node distance suspect a loop " + c1 + " " + c2);
				break;
			}
			
			// Traitement du ConceptType c1
			if (! endC1 && max1 >= max2) {
				// distance from current to their fathers
				endC1=distance(ct1current, hct1, max2, step);
				max1=getMax(ct1current); // max depth of current 1
			}
			
			for (int d = 0 ; d < ct2current.size() && getDDepth(ct2current.get(d)) >= max1 ; d++) {
				// on ne considere comme candidat a type commun que ceux qui sont
				// aussi profond que le plus profond des types deja parcourus
				// dit autrement, on ne considere  un type commun qu'apres avoir explore
				// tous les types plus profonds que lui de maniere a trouver en premier
				// le type commun le plus profond
				if (hct1.containsKey(ct2current.get(d))){
					common=ct2current.get(d);
					break;
				}
			}
			
			if (common!=null){
				return distance(common, hct1, hct2);
			}
			
			// Traitement du ConceptType c2
			if (!endC2 && max2 >= max1)     {
				// distance from current to their fathers
				endC2=distance(ct2current, hct2, max1, step);
				max2=getMax(ct2current); // max depth of current 2
			}			
			
			for (int d = 0 ; d < ct1current.size() && getDDepth(ct1current.get(d)) >= max2; d++) {
				if (hct2.containsKey(ct1current.get(d))){
					common=ct1current.get(d);
					break;
				}
			}
			if (common!=null){
				return distance(common, hct1, hct2);
			}
		}
		return 0;
	}
	
	
	
	int getMax(NodeList v) {
		if (v.size() == 0){
			return 0;
		}
		Node ct = v.get(0);
		if (ct != null){
			return  getDDepth(ct); //.depth;
		}
		else {
			return 0;
		}
	}
	
	
	float distance(Node c, Table hct1, Table hct2){
		return ((hct1.get(c)).floatValue() +
				(hct2.get(c)).floatValue());
	}
	
	/**
	 * compute distance from each current to its fathers
	 * <br>side effect : set current to (current's) father list
	 * store in ht the distance from source type to each father
	 * max is the deepest depth of the other list of current
	 * should stay below this minimal depth to target the deepest common
	 * ancestor first
	 *
	 * @return true if reach root 
	 */
	boolean distance(NodeList current, 
		Table ht, int max, boolean step){
		NodeList father=new NodeList();
		boolean endC1=false;
		father.clear();
		Float i;
		float d;
		Node ct;
		// Calcul des peres des derniers concepts traites
		while (current.size() > 0)   {
			ct=current.get(0);
			if (getDDepth(ct) < max){
				// process only deepest types ( >= max depth)
				break;
			}
			current.remove(ct);
			i = ht.get(ct);
			// distance of the fathers of ct
			for (Node f : getSuperClasses(ct))  {
				d = i.floatValue() + ((step) ? step(f) : 1);
				if (ht.get(f)==null){
					father.add(f);
					ht.put(f, new Float(d));
				}
				else { // already passed through father f, is distance best (less) ?
					if (d < (ht.get(f)).floatValue()){
						//logger.debug(f + " is cheaper ");
						ht.put(f, new Float(d));
					}
				}
			}
		}
		
		// concepts courants += concepts peres
		sort(current, father);
		if (current.size() > 0){
			ct =  current.get(0);
			if (isRoot(ct)) {
				endC1 = true;
			}
		}
		return endC1;
	}
	

	/**
	 * sort father by decreasing depth, in order to find the deepest common first
	 */
	void sort(NodeList current, NodeList father){
		int j=0;
		for (int i = 0 ; i < father.size() ; i++) 	{
			for (j = 0 ; j < current.size() && getDDepth(father.get(i))  <= getDDepth(current.get(j)) ; j++) {
				// do nothing
			}
			current.add(j, father.get(i));
		}
	}
	
	

}
