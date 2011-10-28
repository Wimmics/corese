package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;

/**
 * Record intermediate nodes that are visited by a loop path expression
 * such as exp+ exp* exp{n,}
 * Prevent loop among intermediate nodes
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Visit {
	public static boolean speedUp = false;
	
	Hashtable<State, List<Node>> table;
	Hashtable<Regex, List<Node>> etable;
	Hashtable<Regex, Table> eetable;
	Hashtable<Regex, Integer> ctable;
	ArrayList<Regex> list;

	boolean isReverse = false;
	
	Visit(boolean b){
		table   = new Hashtable<State, List<Node>>();
		etable  = new Hashtable<Regex, List<Node>>();
		eetable = new Hashtable<Regex, Table>();
		ctable  = new Hashtable<Regex, Integer> ();
		list    = new ArrayList<Regex>();
		isReverse = b;
	}
	
	interface TTable {
		
		Node put(Node n1, Node n2);
		Node remove(Node n1);
		boolean containsKey(Node node);
		
	}
	
	class Table1 extends Hashtable<Node, Node>  {}
	
	class Table extends TreeMap<Node, Node>  {	
		
		Table(){
			super(new Compare());
		}
	}
	
	class Compare implements Comparator<Node> {

		// TODO: xsd:integer & xsd:decimal may be considered as same node
		// in loop checking
		public int compare(Node o1, Node o2) {
			return o1.compare(o2);
		}
	}
	
	static Visit create(boolean b){
		return new Visit(b);
	}
	
	void clear(){
		table.clear();
	}
	
	List<Node> get(State state){
		return table.get(state);
	}
	
	void put(State state, List<Node> list){
		table.put(state, list);
	}

	public void add(State state, Node node) {
		List<Node> list = get(state);
		if (list == null){
			list = new ArrayList<Node>();
			put(state, list);
		}
		list.add(node);
	}

	public void remove(State state, Node node) {
		List<Node> list = get(state);
		if (! list.get(list.size()-1).equals(node)){
			System.out.println("** ERROR Visit: " + node + " " + list);
		}
		list.remove(list.size()-1);
	}

	
	/**
	 * Check whether the last visited node has already been 
	 * visited as an intermediate node 
	 * use case: 
	 * ?x p{2,} ?y   :  skip the first two nodes (no loop with two first)
	 * ?x p{2,} ex:uri : walk backward through the list 
	 * 
	 */
	public boolean loop(State state) {
		if (! state.isCheckLoop()) return false;
		
		List<Node> list = get(state);
		if (list == null || list.size()<2) return false;
		int last = list.size()-1;
		int min = 0;
		if (state.isBound() && ! state.isPlus()) min = state.getMin();

		if (isReverse){
			// use case: ?s {2,} ex:uri
			// Walk through the list backward (from <uri> to ?s), skip 2 first nodes
			// cf W3C pp test case
			Node node = null;
			int count = 0;
			for (int i = last; i>=0; i--){
				if (count<min) {count++; continue;}
				else if (count == min){ 
					node = list.get(i);
				}
				else {
					if (list.get(i).equals(node)){
						return true;
					}
				}
				count++;
			}
		}
		else 
		{
			Node node = list.get(last);		
			for (int i = 0; i<last; i++){
				if (i<min) continue;
				if (list.get(i).equals(node)){
					return true;
				}
			}
		}
		
		return false;
	} 

	public boolean loop2(State state) {
		List<Node> list = get(state);
		if (list == null || list.size()<2) return false;
		int last = list.size()-1;
		Node node = list.get(last);
		for (int i = 0; i<last; i++){
			if (list.get(i).equals(node)){
				return true;
			}
		}
		return false;
	} 
	
	
	
	
	
	/**************************************
	 * 
	 * With Regex
	 * 
	 */
	
	
	void ladd(Regex exp, Node node){
		List<Node> list = etable.get(exp);
		if (list == null){
			list = new ArrayList<Node>();
			etable.put(exp, list);
		}
		list.add(node);
	}
	
	
	void eadd(Regex exp, Node node){
		if (speedUp){
			node.setProperty(Node.STATUS, true);
			return;
		}
		Table table = eetable.get(exp);
		if (table == null){
			table = new Table();
			eetable.put(exp, table);
		}
		table.put(node, node);
	}

	
	boolean knows(Regex exp){
		return list.contains(exp);
	}
	
	void insert(Regex exp, Node start){
		if (start == null){
			// subscribe exp to start() above because start node is not bound yet
			list.add(exp);
		}
		else {
			add(exp, start);
		}
	}
	
	void set(Regex exp, List<Node> list){
		if (list!=null){
			etable.put(exp, list); 
		}
	}
	
	List<Node> unset(Regex exp){
		List<Node> list = etable.get(exp);
		etable.remove(exp);
		return list;
	}
	
	/**
	 * use case:
	 * ?x p* ?y
	 * ?x is not bound when path starts
	 * first occurrence of p binds ?x
	 * node is ?x
	 * exp is p*
	 * add node to the visited list of exp to prevent loops
	 */
	void start(Node node){
		for (Regex exp : list){
			add(exp, node);
		}
	}
	
	/**
	 * same use case as start() above
	 * leave the binding
	 */
	void leave(Node node){
		for (Regex exp : list){
			remove(exp, node);
		}
	}
	
	void add(Regex exp, Node node){
		//ladd(exp, node);
		if (isReverse){
			ladd(exp, node);
		}
		else {
			eadd(exp, node);
		}
	}
	
	/**
	 * Remove node from visited list
	 */
	void remove (Regex exp, Node node){
		if (node == null){
			list.remove(exp);
			return;
		}
		
		//lremove(exp, node);

		if (isReverse){
			lremove(exp, node);
		}
		else {
			eremove(exp, node);
		}
	}
	
	void lremove(Regex exp, Node node){
		List<Node> list = etable.get(exp);
		if (! list.get(list.size()-1).equals(node)){
			System.out.println("** ERROR Visit: " + node + " " + list);
		}
		list.remove(list.size()-1);
	}
	
	void eremove(Regex exp, Node node){
		if (speedUp){
			node.setProperty(Node.STATUS, false);
			return;
		}
		Table table = eetable.get(exp);
		table.remove(node);
	}
	
	
	/**
	 * Test whether path loops 
	 */
	boolean loop(Regex exp, Node start) {
		if (isReverse){
			insert(exp, start);
			if (revLoop(exp)){
				remove(exp, start);
				return true;
			}
		}
		else if (eLoop(exp, start)){
			return true;
		}
		else {
			insert(exp, start);
		}
	
		return false;
	}
	 
	boolean eLoop(Regex exp, Node start){
		if (speedUp){
			return (start!= null && start.getProperty(Node.STATUS) == Boolean.TRUE);
		}
		Table table = eetable.get(exp);
		if (table == null) {
			return false;
		}
		return table.containsKey(start);
	}
	
	 boolean stdLoop(Regex exp, Node start){
		 List<Node> list = etable.get(exp);
		 if (list == null) return false;
		 int size = list.size();
		 
		 for (int i = 0; i<size; i++){
			 if (list.get(i).equals(start)){
				 return true;
			 }
		 }
		 return false;
	 }
	 
	/**
	 * Test if there is a loop on path of exp
	 * path isReverse
	 */
	 boolean revLoop(Regex exp) {

		List<Node> list = etable.get(exp);
		if (list == null || list.size()<2) return false;
		int last = list.size()-1;

		// use case: ?s {2,} ex:uri
		// Walk through the list backward (from <uri> to ?s), skip 2 first nodes
		// cf W3C pp test case
		Node node = null;
		int count = 0;
		int min = exp.getMin();
		for (int i = last; i>=0; i--){
			if (count<min) {count++; continue;}
			else if (count == min){ 
				node = list.get(i);
			}
			else {
				if (list.get(i).equals(node)){
					return true;
				}
			}
			count++;
		}
					
		return false;
	 } 
	 
	 
	 void set(Regex exp, int n){
		 ctable.put(exp, n);
	 }

	 void count(Regex exp, int n){
		 Integer i = ctable.get(exp);
		 if (i == null) i = 0;
		 ctable.put(exp, i + n);
	 }


	 int count(Regex exp){
		 Integer i = ctable.get(exp);
		 if (i == null) return 0;
		 return i;
	 }
	

}
