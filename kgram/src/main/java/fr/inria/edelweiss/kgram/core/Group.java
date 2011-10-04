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
	TreeMapping2 table2;
	ListMappings list;

	List<Exp> criteria;
	List<Node> nodes;

	Node fake;
	
	boolean isDistinct = false, 
		isDuplicate = !true,
		// retrieve node in map by name
		isByName = false,
		// min(?l, groupBy(?x, ?y))
		isExtend = false;
	static boolean test = true;

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
				int res = comparator(map, fmap);
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
			return o1.compare(o2);
		}
	}
	
	class TreeMapping2 extends TreeMap<Mapping, Mappings> {	

		TreeMapping2(){
			super(new Compare2());
		}
	}

	class Compare2 implements Comparator<Mapping> {

		@Override
		public int compare(Mapping o1, Mapping o2) {
			return comparator(o1, o2);
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
		nodes = new ArrayList<Node>();
		for (Exp exp : criteria){
			nodes.add(exp.getNode());
		}
		//table = new TableMapping();
		table = new TreeMapping();
		table2 = new TreeMapping2();
	}
	
	public void setDistinct(boolean b){
		isDistinct = b;
		//isByName = b;
	}
	
	public void setExtend(boolean b){
		isExtend = true;
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
//		if (isByName){
//			return map.getNode(qNode); 
//		}
//		else 
		if (isDistinct){
			return map.getDistinctNode(n); 
		}
		else if (isExtend){
			return map.getGroupNode(n);
		}
		else {
			return map.getGroupBy(qNode, n);
		}
	}
	
	
	public boolean add2(Mapping map){
		
		if (isDistinct){
			// select distinct *
			// select (avg(distinct ?x) as ?a)
			map.setDistinct(nodes);
		}
		else if (isExtend){
			// min(?l, groupBy(?x, ?y))
			// store value of ?x ?y in an array to speed up
			map.setGroup(nodes);
		}
		
		if (isDistinct){
			if (table2.containsKey(map)){
				return false;
			}
			table2.put(map, new Mappings(map));
		}
		else {
			Mappings lm = table2.get(map);
			if (lm == null){
				lm = new Mappings();
				table2.put(map, lm);
			}
			lm.add(map);
		}
		
		return true;
	}
	
	Iterable<Mappings> getValues(){
		return table2.values();
	}
	
	
	public boolean add(Mapping map){
		if (test) return add2(map);
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
		int res = comparator(map, fmap);
		
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
	

	int comparator(Mapping m1, Mapping m2){
		int start = 1;
		if (test) start = 0;
		for (int i = start; i<nodes.size(); i++){
			// may skip node 0, if they are the same by construction
			Node qNode = nodes.get(i);
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

