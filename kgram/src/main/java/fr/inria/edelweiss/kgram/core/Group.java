package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.NodeImpl;

/**
 * process select distinct ?x ?y and also group by ?x ?y
 * group Mappings that share same value for ?x and ?y
 * 1 group Mappings that share same value for first elem ?x
 * 2 create a list of list of such mapping that share same ?x and put the list in a table
 * for each value of ?x
 * Then each sublist differs on other values (?y)
 * 
 * x1 -> {(x1, y1) (x1, y1)} {(x1, y2)}
 * x2 -> {(x2, y1) (x2, y1)} {(x2, y2)}
 *
 */
public class Group implements Comparator<Mappings>{
	
	//TableMapping table;
	TreeMapping table;
	ListMappings list;

	List<Exp> criteria;
	Node fake;
	
	boolean isDistinct = false, 
		isDuplicate = !true;

	/**
	 * 
	 * group by ?x ?y
	 * list of Mappings that have same value for ?x but different values for ?y
	 * 
	 * WARNING: must be public because Group uses values() -- as iterator of List<Mappings>
	 */
	public class ListMappings extends ArrayList <Mappings> {
		
		/**
		 * Find the index of map in list by dichotomy
		 */
		int find(Mapping map, int first, int last){
			if (first >= last) {
				return first;
			}
			else {
				int mid = (first + last) / 2;
				Mapping fmap = get(mid).get(0);
				int res = compare(map, fmap);
				if (res <= 0) {
					return find(map, first, mid);
				}
				else {
					return find(map, mid+1, last); 
				}
			}		
		}
		
	}
	
	/**
	 * 
	 * group by ?x ?y
	 * associate list of Mappings to values of ?x
	 */
	class TableMapping extends Hashtable<Node, ListMappings> {	
	}
	
	class TreeMapping extends TreeMap<Node, ListMappings> {	
		
		TreeMapping(){
			super(new Compare());
		}
	}
	
	class Compare implements Comparator<Node> {

		@Override
		public int compare(Node o1, Node o2) {
			// TODO Auto-generated method stub
			return o1.compare(o2);
		}
		
	}
	
	Group(){
		
	}
	
	public static Group create(List<Node> lNode){
		List<Exp> list = new ArrayList<Exp>();
		for (Node node : lNode){
			list.add(Exp.create(Exp.NODE, node));
		}
		return new Group(list);
	}
	
	Group(List<Exp> list){
		criteria = list;
		//table = new TableMapping();
		table = new TreeMapping();
	}
	
	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
	public void setDuplicate(boolean b){
		isDuplicate = b;
	}
	
	public Collection<ListMappings> values(){
		return table.values();
	}
	
	// TODO
	// process isDuplicate
	boolean accept(Node node){
		boolean b = ! table.containsKey(node);
		return b;
	}
	
	List<Mappings> list(){
		ListMappings lMaps = new ListMappings();
		for (List<Mappings> ll : values()){
			for (Mappings lm : ll){
				lMaps.add(lm);
			}
		}
		return lMaps;
	}
	
	List<Mappings> sort(){
		List<Mappings>  lMaps = list();
		Collections.sort(lMaps, this);
		return lMaps;
	}
	
	public int compare(Mappings lm1, Mappings lm2){
		Mapping m1 = lm1.get(0);
		Mapping m2 = lm2.get(0);
		return lm1.compare(m1, m2);
	}
	
	ListMappings get(Node node){
		ListMappings lm = table.get(node);
		return lm;
	}

	

	Node getGroupBy(Mapping map, Node qNode, int n){
		if (isDistinct){
			return map.getNode(qNode);
		}
		else {
			return map.getGroupBy(n);
		}
	}
	
	
	public boolean add(Mapping map){
		// Mappings are grouped according to first select/groupBy variable
		Node qNode = null;
		if (criteria.size()>0){
			qNode = criteria.get(0).getNode();
		}
		Node node = null;
		if (qNode != null){
			//node = map.getNode(qNode);
			node = getGroupBy(map, qNode, 0);
		}
		if (node == null){
			// fake node to represent null value in hashtable
			if (fake==null){
				fake = new NodeImpl("_kgram_null_");
			}
			node = fake;
		}

		ListMappings list = get(node);

		if (list == null){
			// node is new: add it
			list = new ListMappings();
			table.put(node, list);
			Mappings group = new Mappings();
			group.add(map);
			list.add(group);
			return true;
		}
		else if (isDistinct && criteria.size()==1){
			// node already present, there is only one distinct node: exit
			return false;
		}
		
		int i = list.find(map, 0, list.size());
		
		if (i >= list.size()){
			Mappings group = new Mappings();
			group.add(map);
			list.add(i, group);
			return true;
		}
		
		Mapping fmap = list.get(i).get(0);
		int res = compare(map, fmap);
		
		if (res == 0){
			if (isDistinct){
				return false;
			}
			else {
				list.get(i).add(map);
			}
		}
		else {
			Mappings group = new Mappings();
			group.add(map);
			list.add(i, group);
		}
		
		return true;
	}
	

	int compare(Mapping m1, Mapping m2){
		for (int i = 1; i<criteria.size(); i++){
			// skip node 0, they are the same by construction
			Node qNode = criteria.get(i).getNode();
			//int res = compare(m1.getNode(qNode), m2.getNode(qNode));
			int res = compare(getGroupBy(m1, qNode, i), getGroupBy(m2, qNode, i));
			if (res != 0) return res;
		}
		return 0;
	}
	
	int compare(Node n1, Node n2){
		if (n1 == null){
			if (n2 == null) return 0;
			else return -1;
		}
		else if (n2 == null){
			return +1;
		}
		else return n1.compare(n2);
	}
	
	

	
}

