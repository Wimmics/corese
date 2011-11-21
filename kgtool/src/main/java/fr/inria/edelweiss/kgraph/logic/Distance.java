package fr.inria.edelweiss.kgraph.logic;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Semantic distance & similarity with Corese 2.4 Algorithm
 * 
 * PRAGMA: 
 * need a graph with Node set/get Depth 
 * need a root class, default is rdfs:Resource or owl:Thing
 * 
 * If a class is not subClassOf root, depth simulate to 1
 * 
 * Generalized to property hierarchy with a step of 1/8^n instead of 1/2^n
 * steps can be changed with pragma:
 * kg:similarity kg:cstep 3.0
 * kg:similarity kg:pstep 4.0
 * 
 * TODO:
 * Expensive with Entailment because of rdf:type generated rdfs:domain and rdfs:range
 * A resource may have several types, in Corese 2.4 types were synthetized into one type
 * 
 * @author Olivier Corby & Fabien Gandon, Edelweiss INRIA 2011
 */

public class Distance {
	// property step is: 1 / 8^n
	private static double PSTEP = 8;
	// class step is: 1 / 2^n
	private static double CSTEP = 2;
	
	private static int FIRST_STEP = 5;

	Graph graph;
	Node  root, subEntityOf;
	NodeList topList;
	Integer MONE = new Integer(-1);
	Integer ONE = new Integer(1);

	int depthMax = 0;
	private double step = CSTEP;
	double K = 1, dmax = 0;
	boolean isProperty = false;
	
	private Hashtable<Node, Node> table;

	static Logger logger = Logger.getLogger(Distance.class);
	
	
	
	void init(Graph g, List<Node> top, boolean isProp){
		isProperty = isProp;
		String ako;
		if (isProperty){
			ako = RDFS.SUBPROPERTYOF;
			setStep(PSTEP);
		}
		else {
			ako = RDFS.SUBCLASSOF;
			setStep(CSTEP);
		}
		graph = g;
		topList = new NodeList(top);
		table = new Hashtable<Node, Node>();
		subEntityOf = graph.getPropertyNode(ako);
		if (graph.getEntailment() == null){
			//logger.info("Distance set Graph with Entailment");
			graph.setEntailment();
		}
		
		reset();
		init();
	}
	
	public static Distance classDistance(Graph g){
		Distance dist = new Distance();
		ArrayList<Node> top = new ArrayList<Node>();
		top.add(g.getTopClass());
		dist.init(g, top, false );
		return dist;
	}
	
	/**
	 * Manage two hierarchies with topObject and topData
	 * 
	 */
	public static Distance propertyDistance(Graph g){
		Distance dist = new Distance();
		List<Node> top = g.getTopProperties();
		dist.init(g, top, true );
		return dist;
	}
	
	
	void setProperty(boolean b){
		isProperty = b;
	}
	
	void setStep(double f){
		step = f;
	}
	
	public static void setPropertyStep(double d){
		PSTEP = d;
	}
	
	public static void setClassStep(double d){
		CSTEP = d;
	}
	
	Integer getDepth(Node n){
		Integer d = (Integer) n.getProperty(Node.DEPTH) ;
		return d ;
	}
	
	void setDepth(Node n, Integer i){
		n.setProperty(Node.DEPTH, i);
	}
	
	void init(){
		depthMax = 0;
		dmax = 0;
		K = 1;
		initDepth();
	}
	
	void reinit(){
		reset();
		init();
	}
	
	void initDepth(){
		for (Node sup :topList){
			initDepth(sup);
		}
	}

	
	void initDepth(Node sup){
		setDepth(sup, 0);
		table.clear();
		depth(sup);
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
//		if (isProperty && depth == 0){
//			// in the property  hierarchy we set the first step to 5
//			dd = FIRST_STEP;
//		}
		for (Node sub : getSubEntities(sup)){
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
	
	void reset(){
		for (Node sup : topList){
			if (getDepth(sup)!=null){
				reset(sup);
			}
		}
	}
	
	void reset(Node sup){
		setDepth(sup, null);
		for (Node sub : getSubEntities(sup)){
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
		Integer d = getDepth(n);
		if (d == null){
			d = ONE;
		}
		return d ;
	}
	
	
	double step(Node f) {
		return step(getDDepth(f));
	}
	
	double step(int depth){
		 return (1 / Math.pow(step, depth));
	 }
	
	void setDmax(int max){
		dmax = 0;
		for (int i=1; i <= max; i++){
			dmax += step(i);
		}
		dmax = 2 * dmax;
		K = Math.pow(step, max) / 100.0;
	}
	
	public double maxDistance(){
		return dmax;
	}
	
	public double similarity(double distance){
		return similarity(distance, 1);
	}
	
	public double similarity(double distance, int num){
		 double dist = distance / (dmax * num);
		 double sim = 1 / ( 1 + (K * dist)); //  1/1+0=1 1/1+1 = 1/2    1/1+2 = 1/3
		 return sim;
	 }
	 
	
	boolean isRoot(Node n){
		for (Node sup : topList){
			if (n.same(sup)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Node with no super class is considered subClassOf root
	 */
	public Iterable<Node> getSuperEntities(Node node){
		if (subEntityOf == null) return topList;
		Iterable<Node> it = graph.getNodes(subEntityOf, node, 0);
		if (! it.iterator().hasNext()){
			return topList;
		}
		return it;
	}
	
	public Iterable<Node> getSubEntities(Node node){
		if (subEntityOf == null) return new ArrayList<Node>();
		return graph.getNodes(subEntityOf, node, 1);
	}
	
	
	/**
	 * Return ontological distance between two concepts
	 * Distance is the sum of distance (by steps) to the deepest common ancestor
	 * Compute the deepest common ancestor by climbing step by step through ancestors
	 * always walk through deepest ancestors of c1 or c2 (i.e. delay less deep ancestors)
	 * hence the first common ancestor is the deepest
	 */
	
	public Double sdistance(Node c1, Node c2)	{
		Entailment ee = graph.getEntailment();
		if (ee.isSubClassOf(c1, c2) || ee.isSubClassOf(c2, c1)){
			return 0.0;
		}
		return distance(c1, c2);
	}
	
	public double similarity(Node c1, Node c2){
		if (c1.equals(c2)) return 1;
		Double d = distance(c1, c2);
		return similarity(d, 1);
	}
	
	public double distance(Node c1, Node c2)	{
		return distance(c1, c2, true);
	}
	
	
	
	
	
	
	
	
	
	
	class NodeList extends ArrayList<Node> {
		
		NodeList(){}
		
		NodeList(Node n){
			add(n);
		}
		
		NodeList(List<Node> l){
			for (Node n : l){
				add(n);
			}
		}
	}
	
	
	class Table extends Hashtable<Node, Double> {
		
		boolean hasRoot = false;
		
		public Double put(Node n, Double d){
			if (isRoot(n)){
				hasRoot = true;
			}
			return super.put(n, d);
		}
		
		public boolean contains(Node n){
			if (containsKey(n)){
				return true;
			}
			return false;
		}
		
	}
	
	
	double distance(Node n1, Node n2, boolean step)	{
		
		Table hct1 = new Table();
		Table hct2 = new Table();
		
		NodeList ct1current = new NodeList();
		NodeList ct2current = new NodeList();

		boolean end = false;
		boolean endC1 = false;
		boolean endC2 = false;
		int max1, max2; // maximal (deepest) depth of current
		
		Double i = 0.0;
		Double j = 0.0;
		Node common=null;
		int count=0;
		hct1.put(n1, i);
		hct2.put(n2, j);
		max1=getDDepth(n1); 
		max2=getDDepth(n2); 
		ct1current.add(n1);
		ct2current.add(n2);
		
		if (max1 == 0) {
			if (isRoot(n1)){ 
				endC1 = true; 
			}
			else {
				return 0.0;
			}
		}
		
		if (max2 == 0){
			if (isRoot(n2)){
				endC2 = true;
			}
			else {
				return 0.0;
			}
		}
		
		if (hct1.contains(n2)) end = true;
		
		while (!end) {
			
			if (count++ > 10000) {
				logger.debug("** Node distance suspect a loop " + n1 + " " + n2);
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
				if (hct1.contains(ct2current.get(d))){
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
				if (hct2.contains(ct1current.get(d))){
					common=ct1current.get(d);
					break;
				}
			}
			if (common!=null){
				return distance(common, hct1, hct2);
			}
		}
		return 0.0;
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
	
	
	double distance(Node c, Table hct1, Table hct2){
		return hct1.get(c) + hct2.get(c);
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
	boolean distance(NodeList current, Table ht, int max, boolean step){
		NodeList father=new NodeList();
		boolean endC1=false;
		father.clear();
		double i,d;
		Node node;
		// Calcul des peres des derniers concepts traites
		while (current.size() > 0)   {
			node=current.get(0);
			if (getDDepth(node) < max){
				// process only deepest types ( >= max depth)
				break;
			}
			current.remove(node);
			i = ht.get(node);
			// distance of the fathers of ct
			for (Node sup : getSuperEntities(node))  {
				d = i + ((step) ? step(sup) : 1);
				if (ht.get(sup) == null){
					father.add(sup);
					ht.put(sup, d);
				}
				else { // already passed through father f, is distance best (less) ?
					if (d < ht.get(sup)){
						//logger.debug(f + " is cheaper ");
						ht.put(sup, d);
					}
				}
			}
		}
		
		// concepts courants += concepts peres
		sort(current, father);
		if (current.size() > 0){
			node =  current.get(0);
			if (isRoot(node)) {
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
