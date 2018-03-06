package sna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
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
 */
public class SNA  {
	
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
	
	int maxPathLength = 0, countPath = 0;
	
	
	SNA(){
		
		ttcmin = new HashMap<Integer, Integer>();
		tmin   = new HashMap<Node, Integer>();
		tcmin  = new HashMap<Node, Integer>();
		
		tcdeg  = new HashMap<Node, Integer>();
		tdeg   = new HashMap<Node, Float>();
	}
	
	public static SNA create(){
		return  new SNA();
	}
	
	
	/**
	 * ?x knows+ :: $path ?y
	 * group by ?x ?y
	 * 
	 * Mappings with same value of ?x
	 */
	public void process(Mappings res){
		
		for (Mapping mm : res){
			// for each target ?y
			clear();
			Mappings lm = mm.getMappings();
			Node qp = mm.getQueryPathNode();

			//System.out.println(lm + " " + qp);
			
			if (lm!=null && qp!=null){
				boolean suc = path(lm);
				if (suc){
					analysis();
				}
			}
		}
	}
	
	
	

	
	/**
	 * ?x foaf:knows+ ?y
	 * Mappings lm with same value of ?x, ?y
	 */
	boolean path(Mappings lm){
		
		Node qp = null;
		
		for (Mapping map : lm){
			
			if (qp == null){
				qp = map.getQueryPathNode();
			}

			// same values of ?x and ?y
			Path pp = map.getPath(qp);
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
			//node.setProperty(Node.DEGREE, getDegree(node));
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
	
	
	/**
	 * 
	 */
	
	public boolean process(Environment env){
		Memory mem = (Memory) env;
		Path path = mem.getPath();
		
		
		return true;
	}
	

}
