package sna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Path;

/**
 * Process several Mappings, each one is result of a query where ?x is bound:
 * 
 * ?x foaf:knows+ :: $path ?y
 * group by ?x ?y
 * bindings ?x {(<someone>)}
 *
 */
public class SSNA implements ResultListener {
	
	HashMap<Node, Integer> 
	// min path length between x ?b y
	tmin, 
	// nb path with min length between x ?b y 
	tcmin, 
	// total nb of all degree of node ?b (for all ?x ?b ?y)
	tcdeg;
	
	HashMap<Node, Float> 
	// total sum of all degree of node ?b (for all ?x ?b ?y)
	tdeg;
	
	HashMap<Integer, Integer>
	// total nb path of length min between x y
	// key is min, value is nb
	ttcmin;
	
	// Node is the target node of path
	HashMap<Node, List<Path>> tpath;
	
	
	int maxPathLength = 0, 
	countPath 	= 0, 
	countResult = 0, 
	totalResult = 0;
	
	
	SSNA(){
		
		ttcmin = new HashMap<Integer, Integer>();
		tmin   = new HashMap<Node, Integer>();
		tcmin  = new HashMap<Node, Integer>();
		
		tcdeg  = new HashMap<Node, Integer>();
		tdeg   = new HashMap<Node, Float>();
		
		tpath  = new HashMap<Node, List<Path>>();
	}
	
	public static SSNA create(){
		return  new SSNA();
	}
	
	
	/**
	 * ?x knows+ :: $path ?y
	 * group by ?x ?y
	 * 
	 * Mappings with same value of ?x
	 */
	public void process(){
				
		for (Node target : getPathNodes()){
			// for each target ?y
			clear();
			List<Path> list = getPathList(target);

			//System.out.println(lm + " " + qp);
			
			boolean suc = path(list);
			if (suc){
				analysis();
			}
			
		}
		
	}
	
	
	

	
	/**
	 * ?x foaf:knows+ ?y
	 * Mappings lm with same value of ?x, ?y
	 */
	boolean path(List<Path> list){
				
		for (Path pp : list){

			// same values of ?x and ?y
			int length = pp.size();
			if (length == 1){
				return false;
			}
			// number of paths with this length  between ?x ?y
			allMinCount(length);
			countPath++;
			
			if (length > maxPathLength){
				maxPathLength = length;
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
		
		return true;
		
	}
	
	
	/**
	 * partial betweenness
	 * ?x foaf:knows+ ?y
	 * Mappings with same value of ?x, ?y
	 */
	void analysis(){
		
		for (Node node : getNodes()){
			// for all node between ?x and ?y
			
			// min path length (by node)
			int min = getMin(node);
			
			// number of path with min length (by node)
			float cmin = getCountMin(node);
			
			// total number of path with length=min between ?x, ?y
			float total = getAllMinCount(min);
			
			float deg = cmin / total;
			
			addDegree(node, deg);
						
		}
	}
	
	

	
	
	List<Node> result(){
		ArrayList<Node> list = new ArrayList<Node>();
		
		for (Node node : getAllNodes()){
			list.add(node);
			setDegree(node, getDegree(node) / getCountDegree(node));
			node.setProperty(Node.DEGREE, getDegree(node));
		}
		
		Collections.sort(list, new  Compare());
		
		Collections.reverse(list);
		
		return list;
	}
	
	
	class Compare implements Comparator<Node> {
		
		public int compare(Node o1, Node o2) {
			int res =  getDegree(o1).compareTo(getDegree(o2));
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
	
	
	
	

	
	void countDegree(Node n){
		Integer i = tcdeg.get(n);
		if (i == null){
			i = 0;
		}
		tcdeg.put(n, i+1);
	}
	
	Integer getCountDegree(Node n){
		return tcdeg.get(n);
	}
	
	void addDegree(Node n, float f){
		Float d = getDegree(n);
		if (d == null){
			d = (float)0;
		}
		setDegree(n, d+f);
		countDegree(n);
	}
	
	void setDegree(Node n, float f){
		tdeg.put(n, f);
	}
	
	Float getDegree(Node n){
		return tdeg.get(n);
	}
	
	int getMaxPathLength(){
		return maxPathLength;
	}
	
	int getCountPath(){
		return countPath;
	}
	
	
	int getMin(Node n){
		return tmin.get(n);
	}
	
	int getCountMin(Node n){
		return tcmin.get(n);
	}
	

	
	Iterable<Node> getNodes(){
		return tmin.keySet();
	}
	
	Iterable<Node> getAllNodes(){
		return tdeg.keySet();
	}
	
	
	void clear(){
		tmin.clear();
		tcmin.clear();
		ttcmin.clear();
	}
	
	void min(Node n, int l){
		Integer i = tmin.get(n);
		if (i == null || l<i){
			tmin.put(n,l);
			tcmin.put(n, 1);
		}
		else if (l == i){
			tcmin.put(n, tcmin.get(n)+1);
		}
	}
	
	int getAllMinCount(int i){
		return ttcmin.get(i);
	}
	
	void allMinCount(int i){
		// nb occurrences of value = i
		Integer c = ttcmin.get(i);
		if (c == null){
			ttcmin.put(i, 1);
		}
		else {
			ttcmin.put(i, c+1);
		}
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
	List<Path> getPathList(Node n){
		return tpath.get(n);
	}
	
	/**
	 * Store path in list of path that share same target Node
	 * If one path has length 1 for this target, the whole list is rejected.
	 */
	void store(Path path){
				
		Node source = path.getEdge(0).getNode(0);
		Node target = path.getEdge(path.size()-1).getNode(1);
		
		if (source.same(target)){
			return;
		}
		
		List<Path> list = tpath.get(target);
		
		if (list == null){
			list = new ArrayList<Path>();
			tpath.put(target, list);
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
		store(path);
		return false;
	}
	

}
