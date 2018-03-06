package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/***********************************************
 * 
 * group by any
 * group Mapping who share one node (from any variable)
 * Connected components
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 ***********************************************/

public class Merge extends Group {
	
	Mappings lMap;
	ListMappings list;
	
	
	Merge(Mappings lm){
		lMap = lm;
	}
	
	
	public class ListMappings extends ArrayList <Mappings> {
	
	/**
	 * Find the index of map in list by dichotomy
	 */
//	int find(Mapping map, int first, int last){
//		if (first >= last) {
//			return first;
//		}
//		else {
//			int mid = (first + last) / 2;
//			Mapping fmap = get(mid).get(0);
//			int res = comparator(map, fmap);
//			if (res <= 0) {
//				return find(map, first, mid);
//			}
//			else {
//				return find(map, mid+1, last); 
//			}
//		}		
//	}
	
}
	
	
	public Collection<ListMappings> values(){
		List<ListMappings> ll = new ArrayList<ListMappings>();
		ll.add(list);
		return ll;
	}
	
	public Iterable<Mappings> getValues(){
		return list;
	}
	
	void merge(){
		list = merge(lMap);
		list = merge(list);
	}
	
	
	/**
	 * Initialize
	 */
	ListMappings merge(Mappings lMap){
		ListMappings list = new ListMappings();
		
		for (Mapping map : lMap){
			boolean found = false;
			for (Mappings lm : list){
				if (match(lm, map)){
					lm.add(map);
					found = true;
					break;
				}
			}
			
			if (! found){
				Mappings lm = new Mappings();
				lm.add(map);
				list.add(lm);
			}
			
		}
		
		return list;
	}
	

	
	/**
	 * Every Mappings is made of connected Mapping 
	 * Try to Merge Mappings that are connected
	 */
	ListMappings merge(ListMappings list){
		ListMappings nlist = null;
		boolean merge = true;
		int count = 0;
		
		while (merge){
			nlist = new ListMappings();
			merge = false;
			for (Mappings lm1 : list){

				boolean found = false;
				for (Mappings lm2 : nlist){
					if (match(lm1, lm2)){
						lm2.add(lm1);
						found = true;
						merge = true;
						break;
					}
				}

				if (! found){
					nlist.add(lm1);
				}
			}
			
			list = nlist;
		}
		
		return nlist;
	}
	
	
	boolean match(Mappings lm1, Mappings lm2){
		for (Mapping map : lm1){
			if (match(lm2, map)){
				return true;
			}
		}
		return false;
	}
	
	
	// one node of map is contained in one Mapping of lm
	boolean match(Mappings lmap, Mapping map){
		for (Mapping m : lmap){
			if (map.match(m)){
				return true;
			}
		}
		return false;
	}

}
