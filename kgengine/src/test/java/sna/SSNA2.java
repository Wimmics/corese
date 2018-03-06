package sna;

import fr.inria.corese.kgram.api.core.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.path.Path;

/**
 * Process several Mappings, each one is result of a query where ?x is bound:
 * 
 * ?x foaf:knows+ :: $path ?y
 * group by ?x ?y
 * bindings ?x {(<someone>)}
 * 
 * Olivier Corby, Edelweiss, INRIA, 2011
 *
 */
public class SSNA2 implements ResultListener {
	
	static int MAXLENGTH = 100;
	static int MAXNODE 	 = 1000;

	boolean isFake = false;
	
	Integer index = 0;
		
	int atcmin[], amin[], acmin[], acdeg[];
	float adeg[];
	
	Node[] 
	     // nodes between given ?x and ?y
	     intermediateNodes, 
	     // all nodes of network
	     allNodes;
	
	
	// Node is the target node of path
	HashMap<Node, ListPath> tpath;
	
	// SNA result handler
	SSNAHandler handler;
	
	Node start;
	
	
	int count = 0, 
	maxPathLength = 0, 
	countPath 	= 0, 
	countResult = 0, 
	totalResult = 0;

    @Override
    public Exp listen(Exp exp, int n) {
        return exp;
    }

    @Override
    public void listen(Expr exp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean listen(Edge edge, Entity ent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
	class ListPath extends ArrayList<Path> {}
	
	
	SSNA2(){
				
		atcmin 	= new int[MAXLENGTH];
		amin 	= new int[MAXNODE];
		acmin 	= new int[MAXNODE];
		
		acdeg 	= new int[MAXNODE];
		adeg 	= new float[MAXNODE];

		intermediateNodes	= new Node[MAXNODE];
		allNodes	= new Node[MAXNODE];
		
		for (int i = 0; i<MAXNODE; i++){
			acdeg[i] = 0;
			adeg[i]  = 0;
		}
		
		init();
		
		tpath  = new HashMap<Node, ListPath>();
		
		handler = new SSNAHandler();
	}
	
	
	public static SSNA2 create(){
		return  new SSNA2();
	}
	
	public void set(SSNAHandler h){
		handler = h;
	}
	
	void setFake(boolean b){
		isFake = b;
	}
	
	boolean isFake(){
		return isFake;
	}
	
	
	/**
	 * ?x knows+ :: $path ?y
	 * group by ?x ?y
	 * 
	 * Mappings with same value of ?x
	 */
	public void process(){

		if (isFake()){
			return;
		}
		
		for (Node target : getPathNodes()){
			if (target!=null){
				// for each target ?y
				clear();
				List<Path> list = getPathList(target);

				if (list!=null && list.size()>0){
					Path path = list.get(0);
					Node source = path.getSource();

					int minLength = path(source, target, list);

					if (minLength > 1){
						between(source, target);
						handler.geodesic(source, target, minLength, getAllMinCount(minLength));
					}
				}
			}
		}
	}
	
	public void start(){
		
	}

	
	public void complete(){
		if (isFake()){
			return;
		}
		
		int n = 0;
		for (Node node : allNodes){
			if (node != null){
				float deg = getDegree(n);
				if (deg!=0){
					float degree = deg / getCountDegree(n); 
					setDegree(n, degree);
					handler.degree(node, degree);
				}
			}
			n++;
		}
	}

	
	/**
	 * ?x foaf:knows+ ?y
	 * Mappings lm with same value of ?x, ?y
	 */
	int path(Node source, Node target, List<Path> list){
		
		int minLength = Integer.MAX_VALUE;
				
		for (Path pp : list){

			// same values of ?x and ?y
			int length = pp.size();
			if (length == 1){
				return 1;
			}
						
			// number of paths with this length  between ?x ?y
			allMinCount(length);
			countPath++;
			
			if (length > maxPathLength){
				maxPathLength = length;
			}
			
			if (length < minLength){
				minLength = length; 
			}
			
			int n = 1;
			
			for (Entity ent : pp.getEdges()){
				// enumerate path edges
				if (n++ < length){
					// skip last edge
					Node node = ent.getNode(1);
					// min path length with this intermediate node
					// ?x .. node .. ?y
					min(node, length);
				}
			}
			
			
		}
		
		return minLength;
		
	}
	
	
	/**
	 * partial betweenness
	 * ?x foaf:knows+ ?y
	 * Mappings with same value of ?x, ?y
	 */
	void between(Node source, Node target){

		int n = 0;
		
		for (Node node : intermediateNodes){ //getNodes()){
			
			if (node != null){
				// for all node between ?x and ?y

				// min path length (by node)
				int min = getMin(n);

				// number of path with min length (by node)
				int cmin = getCountMin(n);

				// total number of path with length=min between ?x, ?y
				int total = getAllMinCount(min);

				float deg = ((float) cmin) / ((float) total);

				addDegree(n, deg);
				allNodes[n] = node;

				handler.geodesic(source, target, node, min, cmin);
			}
			
			n++;

		}
	}

	
	
	List<Node> result(){
		ArrayList<Node> list = new ArrayList<Node>();
		
		for (Node node : allNodes){
			if (node!=null && getDegree(node)>0){
				list.add(node);
			}
		}
		
		Collections.sort(list, new  Compare());
		
		Collections.reverse(list);
		
		return list;
	}
	
	
	class Compare implements Comparator<Node> {
		
		public int compare(Node o1, Node o2) {
			float d1 = getDegree(o1);
			float d2 = getDegree(o2);
			int res = Float.compare(d1, d2);
			if (res == 0){
				return o2.getLabel().compareTo(o1.getLabel());
			}
			return res;
		}
	}
	
	
	void display(List<Node> list){
		for (Node node : list){
			System.out.println(node + " " + getDegree(node));
		}
		System.out.println("___");
	}
	
	
	
	/****************************************************************/
	 

	Integer index(Node node){
		return node.getIndex();
	}

	
	int getCountDegree(int n){
		return acdeg[n];
	}
	
	void addDegree(int n, float f){
		adeg[n] += f;
		acdeg[n] += 1;
	}

	
	float getDegree(int n){
		return adeg[n];
	}
	
	void setDegree(int n, float d){
		adeg[n] = d;
	}
	
	float getDegree(Node n){
		return getDegree(index(n));
	}
	
	int getMaxPathLength(){
		return maxPathLength;
	}
	
	int getCountPath(){
		return countPath;
	}
	
	


	
	
	void clear(){
		init();
	}
	
	void init(){
		for (int i = 0; i<atcmin.length; i++){
			atcmin[i] = 0;
		}
		for (int i = 0; i<MAXNODE; i++){
			acmin[i] = 0;
			amin[i] = 0;
			intermediateNodes[i] = null;
		}
	}
	
	
	int getMin(int id){
		return amin[id];
	}
	
	int getCountMin(int id){
		return acmin[id];
	}
	
	int getMin(Node n){
		int id = index(n);
		return amin[id];
	}
	
	int getCountMin(Node n){
		int id = index(n);
		return acmin[id];
	}
	
	void min(Node n, int l){
		int id = index(n);
		int i = amin[id];
		if (i == 0 || l<i){
			amin[id] = l;
			acmin[id] = 1;
			intermediateNodes[id] = n;
		}
		else if (l == i){
			acmin[id] += 1;
		}
	}

	
	
	int getAllMinCount(int i){
		return atcmin[i];
	}
	
	
	void allMinCount(int i){
		// nb occurrences of value = i
		atcmin[i] += 1;
	}

	
	

	
	/****************************************************
	 * 
	 * SNA as ResultListener
	 * 
	 */
	
	/**
	 * Start a new source Node
	 */
	void reset(){
		tpath.clear();
		countResult = 0;
	}
	
	/**
	 * Number of results between two reset()
	 */
	int nbResult(){
		return countResult;
	}
	
	int totalResult(){
		return totalResult;
	}
	
	/**
	 * Target nodes
	 */
	Iterable<Node> getPathNodes(){
		return tpath.keySet();
	}
	
	/**
	 * List of Path of target Node n
	 */
	ListPath getPathList(Node n){
		return tpath.get(n);
	}
	
	void putPathList(Node n, ListPath l){
		tpath.put(n, l);
	}
	
	/**
	 * Store path in list of path that share same target Node
	 * If one path has length 1 for this target, the whole list is rejected.
	 */
	void store(Path path){
		
		if (path.size()==0) return;
				
		Node source = path.getSource();
		Node target = path.getTarget();
		
		if (source.same(target)){
			return;
		}
		
		path = path.copy();
		
		ListPath list = getPathList(target);
		
		if (list == null){
			list = new ListPath();
			putPathList(target, list);
		}
		else if (list.size() == 0){
			// this list is rejected because a path of length 1 has been found
			return;
		}
		
		if (path.size() == 1){
			// this list is rejected because a path of length 1 has been found
			list.clear();
			return;
		}
		
		list.add(path);
	}
	

	
	/**
	 * kgram call process(env) for each result
	 * here we store the path and that's it
	 */
	public boolean process(Environment env){
		Memory mem = (Memory) env;
		Path path = mem.getPath();
		return process(path);
	}
	
	public boolean process(Path path){
		countResult++;
		totalResult++;
		
		if (isFake()){
			fake(path);
		}
		else {
			store(path);
		}
		
		return false;
	}


	public boolean enter(Node node, Regex exp, int size) {
		//System.out.println("** Enter: " + node + " " + size);
		return false;
	}


	public boolean leave(Node node, Regex exp, int size) {
		//System.out.println("** Leave: " + node + " " + size);
		return false;
	}
	
	public boolean enter(Entity ent, Regex exp, int size) {
		//System.out.println("** Enter: " + node + " " + size);
		return false;
	}


	public boolean leave(Entity ent, Regex exp, int size) {
		//System.out.println("** Leave: " + node + " " + size);
		return false;
	}
	
	void fake(Path path){
		if (path.size() > maxPathLength){
			maxPathLength = path.size();
		}
	}
	
	
	

}
