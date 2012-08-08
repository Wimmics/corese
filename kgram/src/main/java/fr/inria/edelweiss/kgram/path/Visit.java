package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Collection;
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
	ETable ttable;
	Hashtable<Regex, Integer> ctable;
	DTable tdistinct;
	LTable ltable;
	ArrayList<Regex> regexList;

	boolean isReverse = false,
	// isCounting = false : new sparql semantics  
	isCounting = false;
	
	Visit(boolean isRev, boolean isCount){
		isCounting  = isCount;
		isReverse 	= isRev && isCount;
		table   	= new Hashtable<State, List<Node>>();
		etable  	= new Hashtable<Regex, List<Node>>();
		eetable 	= new Hashtable<Regex, Table>();
		ttable  	= new ETable(isReverse);
		ctable  	= new Hashtable<Regex, Integer> ();
		regexList   = new ArrayList<Regex>();
		tdistinct 	= new DTable();
		ltable 		= new LTable();
	}
	
	class Table1 extends Hashtable<Node, Node>  {}
	
	
	class Table extends TreeMap<Node, Node>  {	
		
		Table(){
			super(new Compare());
		}
	}
	
	class ETable extends Hashtable<Regex, TTable> {
		boolean isReverse = false;
		
		ETable(boolean b){
			isReverse = b;
		}
		
		void add(Regex exp, Node n){
			TTable t = get(exp);
			if (t == null){
				t = new TTable(isReverse);
				put(exp, t);
			}
			t.add(n);
		}
		
		void remove(Regex exp, Node n){
			TTable t = get(exp);
			t.remove(n);
		}
		
		
		boolean loop(Regex exp, Node start){
			TTable table = get(exp);
			if (table == null) {
				return false;
			}
			boolean b = table.loop(exp, start);
			return b;
		}
		
		boolean exist(Regex exp){
			return containsKey(exp);
		}
		
	}
	
	TTable getTable(Regex exp){
		return ttable.get(exp);
	}
	
	class TTable    {	
		Table table;
		List<Node> list;
		boolean isReverse = false;
		
		TTable(boolean b){
			isReverse = b;
			if (isReverse){
				list = new ArrayList<Node>();
			}
			else {
				table = new Table();
			}
		}
		
		Collection<Node> values(){
			return table.values();
		}
		
		void add(Node n){
			if (isReverse){
				list.add(n);
			}
			else {
				table.put(n, n);
			}
		}
		
		void remove(Node n){
			if (isReverse){
				if (! list.get(list.size()-1).equals(n)){
					System.out.println("** ERROR Visit: " + n + " " + list);
				}
				list.remove(list.size()-1);			}
			else {
				table.remove(n);
			}
		}
		
		boolean loop(Regex exp, Node n){
			if (isReverse){
				if (list == null || list.size()<2) return false;
				int last = list.size()-1;

				// use case: ?s {2,} ex:uri
				// Walk through the list backward (from <uri> to ?s), skip 2 first nodes
				// cf W3C pp test case
				Node node = null;
				int count = 0;
				int min = exp.getMin();
				if (min == -1){
					min = 0;
				}

				for (int i = last; i>=0; i--){
					if (count<min) {
						// continue
					}
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
			else {
				return table.containsKey(n);
			}
		}
		
	}
	
	
	class DTable extends TreeMap<Node, Table>  {	
		
		DTable(){
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
	
	static Visit create(boolean isReverse, boolean isCount){
		return new Visit(isReverse, isCount);
	}
	
	void clear(){
		table.clear();
	}
	
	
	/**************************************
	 * 
	 * With Regex
	 * 
	 **************************************/
	
	/**
	 * Test whether path loops 
	 */
	boolean nloop(Regex exp, Node start) {
		if (isReverse){
			ninsert(exp, start);
			if (ttable.loop(exp, start)){
				ttable.remove(exp, start);
				return true;
			}
		}
		else if (ttable.loop(exp, start)){
			return true;
		}
		else {
			ninsert(exp, start);
		}
	
		return false;
	}
	
	boolean nfirst(Regex exp){
		return ! ttable.exist(exp);
	}
	
	void ninsert(Regex exp, Node start){
		if (start == null){
			// subscribe exp to start() above because start node is not bound yet
			declare(exp);
		}
		else {
			ttable.add(exp, start);
		}
	}
	
	
	void declare(Regex exp){
		if (! regexList.contains(exp)){
			regexList.add(exp);
		}
	}
	
	boolean knows(Regex exp){
		return regexList.contains(exp);
	}
	
	void nset(Regex exp, TTable t){
		if (t!=null){
			ttable.put(exp, t); 
		}
	}
	
	TTable nunset(Regex exp){
		TTable t = ttable.get(exp);
		ttable.remove(exp);
		return t;
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
	void nstart(Node node){
		for (Regex exp : regexList){
			ttable.add(exp, node);
		}
	}
	

	
	/**
	 * same use case as start() above
	 * leave the binding
	 */
	void nleave(Node node){
		for (Regex exp : regexList){
			ttable.remove(exp, node);
		}
	}

	
	/**
	 * Remove node from visited list
	 */
	void nremove (Regex exp, Node node){
		if (node == null){
			regexList.remove(exp);
			return;
		}
		if (isCounting){
			ttable.remove(exp, node);
		}
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
	 
	 /****************************************
	  * 
	  * ?x distinct(exp+) ?y
	  * 
	  */
	 
	 boolean isDistinct(Node start, Node node){
		 Table t = tdistinct.get(start);
		 if (t == null) return true;
		 return ! t.containsKey(node);
	 }
	 
	 void addDistinct(Node start, Node node){
		 Table t = tdistinct.get(start);
		 if (t == null){
			 t = new Table();
			 tdistinct.put(start, t);
		 }
		 t.put(node, node);
	 }
	 
	 
	
	 /**********************************************
	  * ?x short(regex) ?y
	  * ?x distinct(regex) ?y
	  *
	  */
	 
	 
	 /**
	  * Table for managing path length for each node 
	  */
	 class LTable extends TreeMap<Node, Record>  {	
			
			LTable(){
				super(new Compare());
			}
			
			
			void setLength(Node n, Regex exp, int l){
				Record r = get(n);
				if (r == null){
					r = new Record(exp, l);
					put(n, r);
				}
				else if (r.exp == exp){
					r.len = l;
				}
			}
			
			Integer getLength(Node n, Regex exp){
				Record r = get(n);
				if (r != null && r.exp == exp){
					return r.len;
				}
				else {
					return null;
				}
			}	
	}
	 
	 class Record {
		 Regex exp;
		 Integer len;
		 
		 Record(Regex e, Integer l){
			 exp = e;
			 len = l;
		 }
		 
	 }
	 
	 void initPath(){
		 ltable.clear();
	 }

	 void start(){
		 if (! isCounting){
			 ttable.clear();
		 }
	 }

	 
	 void setLength(Node n, Regex exp, int l){
//		 if (speedUp){
//			 Regex e = getRegex(n);
//			 if (e == null || e == exp){
//				 n.setProperty(Node.LENGTH, l);
//				 setRegex(n, exp);
//			 }
//		 }
//		 else 
		 {
			 ltable.setLength(n, exp, l);
		 }
	 }

	 Integer getLength(Node n, Regex exp){
//		 if (speedUp){
//			 Regex e = getRegex(n);
//			 if (e == null || e == exp){
//				 return (Integer) n.getProperty(Node.LENGTH);
//			 }
//			 return null;
//		 }
//		 else 
		 {
			 return ltable.getLength(n, exp);
		 }
	 }

	 void setRegex(Node n, Regex e){
		 n.setProperty(Node.REGEX, e);
	 }

	 Regex getRegex(Node n){
		 return (Regex) n.getProperty(Node.REGEX);
	 }

	
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	
	
	/*************************************
	 * Old version
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
	 
	void start(Node node){
		for (Regex exp : regexList){
			add(exp, node);
		}
	}
	void leave(Node node){
		for (Regex exp : regexList){
			remove(exp, node);
		}
	}
	
	void set(Regex exp, Table t){
		if (t!=null){
			eetable.put(exp, t); 
		}
	}
	
	Table unset(Regex exp){
		Table t = eetable.get(exp);
		eetable.remove(exp);
		return t;
	}
	
	void insert(Regex exp, Node start){
		if (start == null){
			// subscribe exp to start() above because start node is not bound yet
			regexList.add(exp);
		}
		else {
			add(exp, start);
		}
	}
	
	void add(Regex exp, Node node){
		if (isReverse){
			ladd(exp, node);
		}
		else {
			eadd(exp, node);
		}
	}	

	// reverse case: add node in list
	void ladd(Regex exp, Node node){
		List<Node> list = etable.get(exp);
		if (list == null){
			list = new ArrayList<Node>();
			etable.put(exp, list);
		}
		list.add(node);
	}
	
	// forward case: add in table
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

	void remove (Regex exp, Node node){
		if (node == null){
			regexList.remove(exp);
			return;
		}
		
		if (isReverse){
			lremove(exp, node);
		}
		else {
			eremove(exp, node);
		}
	}
	
	// reverse case
	void lremove(Regex exp, Node node){
		List<Node> list = etable.get(exp);
		if (! list.get(list.size()-1).equals(node)){
			System.out.println("** ERROR Visit: " + node + " " + list);
		}
		list.remove(list.size()-1);
	}
	
	// forward case
	void eremove(Regex exp, Node node){
		if (speedUp){
			node.setProperty(Node.STATUS, false);
			return;
		}
		Table table = eetable.get(exp);
		table.remove(node);
	}
	
	
	// forward case
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
	 
	 
	
	 
	 
		/*********************
	 *
	 * States are deprecated
	 */
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
	 * @deprecated
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

	/**
	 *  @deprecated
	 */
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
	
}
