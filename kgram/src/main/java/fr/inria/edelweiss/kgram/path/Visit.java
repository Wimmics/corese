package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Record intermediate nodes that are visited by a loop path expression
 * such as exp+ exp* exp{n,}
 * Prevent loop among intermediate nodes
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Visit {
	Hashtable<State, List<Node>> table;
	boolean isReverse = false;
	
	Visit(boolean b){
		table = new Hashtable<State, List<Node>>();
		isReverse = b;
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

}
