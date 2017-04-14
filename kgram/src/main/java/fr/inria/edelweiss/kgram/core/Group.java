package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * select distinct ?x ?y 
 * select (count(distinct *) as ?c)
 * 
 * group by ?x ?y
 * min(?l, groupBy(?x, ?y))
 * 
 * Compare Mapping using a TreeMap
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Group implements Comparator<Mappings>{
	
	TreeMapping table;

	List<Exp> criteria;
	List<Node> nodes;

	Node fake;
	
	boolean isDistinct = false, 
		// min(?l, groupBy(?x, ?y))
		isExtend = false;
    private boolean isFake = false;
    private static boolean compareIndex = false;

    /**
     * @return the afake
     */
    public boolean isFake() {
        return isFake;
    }

    /**
     * @param afake the afake to set
     */
    public void setFake(boolean afake) {
        this.isFake = afake;
    }

    /**
     * @return the compareValue
     */
    public static boolean isCompareIndex() {
        return compareIndex;
    }

    /**
     * @param compareValue the compareValue to set
     */
    public static void setCompareIndex(boolean b) {
        compareIndex = b;
    }

	
	class TreeMapping extends TreeMap<Mapping, Mappings> {	
		
		TreeMapping(List<Node> ln){
			super(new Compare(ln));
		}
	}
	

	class Compare implements Comparator<Mapping> {
		
		List<Node> list;
		int size;
		
		Compare(List<Node> ln){
			list = ln;
			size = list.size();
		}

		@Override
		public int compare(Mapping m1, Mapping m2){                  
			if (isExtend){
				return compareExtend(m1, m2);
			}
			
			if (isDistinct){
				return compareDistinct(m1, m2);
			}                        
			for (int i = 0; i<size; i++){
				Node qNode = list.get(i);
				int res = compare(m1.getGroupBy(qNode, i), m2.getGroupBy(qNode, i));
				if (res != 0) return res;
			}
			return 0;
		}
		
		
		public int compareExtend(Mapping m1, Mapping m2){
			Node[] g1 = m1.getGroupNodes();
			Node[] g2 = m2.getGroupNodes();

			for (int i = 0; i<size; i++){
				int res = compare(g1[i], g2[i]);
				if (res != 0) return res;
			}
			return 0;
		}
		
		public int compareDistinct(Mapping m1, Mapping m2){
			for (int i = 0; i<size; i++){
				int res = compare(m1.getDistinctNode(i), m2.getDistinctNode(i));
				if (res != 0) return res;
			}
			return 0;
		}
		
		
		int compare(Node n1, Node n2){
			if (n1 == n2){
				return 0;
			}
			else if (n1 == null){
				return -1;
			}
			else if (n2 == null){
				return +1;
			}
			else if (isCompareIndex() && n1.getIndex() != -1 && n2.getIndex() != -1){
                            return Integer.compare(n1.getIndex(), n2.getIndex());
			}
                        else {
                            return n1.compare(n2);
                        }
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
	
	public static Group instance(List<Exp> list){
		return new Group(list);
	}
	
	Group(List<Exp> list){
		criteria = list;
		nodes = new ArrayList<Node>();
		for (Exp exp : criteria){
			nodes.add(exp.getNode());
		}
		table = new TreeMapping(nodes);
	}
	
	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
	public void setExtend(boolean b){
		isExtend = b;
	}
	
	public void setDuplicate(boolean b){
	}
	

	// TODO
	boolean accept(Node node){
		return true;
	}
	

	
        @Override
	public int compare(Mappings lm1, Mappings lm2){
		Mapping m1 = lm1.get(0);
		Mapping m2 = lm2.get(0);
		return lm1.compare(m1, m2);
	}
	


	Node getGroupBy(Mapping map, Node qNode, int n){		
		if (isDistinct){
			return map.getDistinctNode(n); 
		}
//		else if (isExtend){
//			return map.getGroupNode(n);
//		}
		else {
			return map.getGroupBy(qNode, n);
		}
	}
	
	
	/**
	 * add map in a group by
	 */
	public boolean add(Mapping map){
		if (isExtend){
			// min(?l, groupBy(?x, ?y))
			// store value of ?x ?y in an array to speed up
			map.setGroup(nodes);
		}
				
		Mappings lm = table.get(map);
		if (lm == null){
			lm = new Mappings();
                        lm.setFake(isFake());
			table.put(map, lm);
		}
		lm.add(map);
		
		return true;
	}
	
	
	// select distinct *
	// select (avg(distinct ?x) as ?a
	public boolean isDistinct(Mapping map){
				
		map.setDistinct(nodes);
						
		if (table.containsKey(map)){
			return false;
		}
		table.put(map, new Mappings(map));		
		return true;
	}

	
	
	Iterable<Mappings> getValues(){
		return table.values();
	}
	
	
	

	
	
	
	
	

	
	
	
	
//	@Deprecated
//	public boolean add2(Mapping map){
//		// Mappings are grouped according to first select/groupBy variable
//		Node qNode = null;
//		if (criteria.size()>0){
//			qNode = criteria.get(0).getNode();
//		}
//		Node node = null;
//		if (qNode != null){
//			//node = map.getNode(qNode);
//			node = getGroupBy(map, qNode, 0);
//		}
//		if (node == null){
//			// fake node to represent null value in hashtable
//			if (fake==null){
//				fake = new NodeImpl("_kgram_null_");
//			}
//			node = fake;
//		}
//
//		ListMappings list = get(node);
//
//		if (list == null){
//			// node is new: add it
//			list = new ListMappings();
//			table.put(node, list);
//			Mappings group = new Mappings();
//			group.add(map);
//			list.add(group);
//			return true;
//		}
//		else if (isDistinct && criteria.size()==1){
//			// node already present, there is only one distinct node: exit
//			return false;
//		}
//		
//		int i = list.find(map, 0, list.size());
//		
//		if (i >= list.size()){
//			Mappings group = new Mappings();
//			group.add(map);
//			list.add(i, group);
//			return true;
//		}
//		
//		Mapping fmap = list.get(i).get(0);
//		int res = comparator(map, fmap);
//		
//		if (res == 0){
//			if (isDistinct){
//				return false;
//			}
//			else {
//				list.get(i).add(map);
//			}
//		}
//		else {
//			Mappings group = new Mappings();
//			group.add(map);
//			list.add(i, group);
//		}
//		
//		return true;
//	}
//	
//	
	
	/**
	 * 
	 * group by ?x ?y
	 * list of Mappings that have same value for ?x but different values for ?y
	 * 
	 * WARNING: must be public because Group uses values() -- as iterator of List<Mappings>
	 */
//	public class ListMappings extends ArrayList <Mappings> {
//		
//		/**
//		 * Find the index of map in list by dichotomy
//		 */
//		int find(Mapping map, int first, int last){
//			if (first >= last) {
//				return first;
//			}
//			else {
//				int mid = (first + last) / 2;
//				Mapping fmap = get(mid).get(0);
//				int res = comparator(map, fmap);
//				if (res <= 0) {
//					return find(map, first, mid);
//				}
//				else {
//					return find(map, mid+1, last); 
//				}
//			}		
//		}
//		
//	}
	
	/**
	 * 
	 * group by ?x ?y
	 * associate list of Mappings to values of ?x
	 */
//	class TableMapping extends Hashtable<Node, ListMappings> {	
//	}
//	
//	class TreeMapping extends TreeMap<Node, ListMappings> {	
//		
//		TreeMapping(){
//			super(new Compare());
//		}
//	}
//	
//	class Compare implements Comparator<Node> {
//
//		@Override
//		public int compare(Node o1, Node o2) {
//			return o1.compare(o2);
//		}
//	}
//	
	
//	List<Mappings> list(){
//	ListMappings lMaps = new ListMappings();
//	for (List<Mappings> ll : values()){
//		for (Mappings lm : ll){
//			lMaps.add(lm);
//		}
//	}
//	return lMaps;
//}

//List<Mappings> sort(){
//	List<Mappings>  lMaps = list();
//	Collections.sort(lMaps, this);
//	return lMaps;
//}
	
//	public Collection<ListMappings> values(){
//	return table.values();
//}

//ListMappings get(Node node){
//ListMappings lm = table.get(node);
//return lm;
//}



	
	
}

