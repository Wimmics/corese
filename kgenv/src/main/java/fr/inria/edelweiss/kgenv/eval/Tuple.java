package fr.inria.edelweiss.kgenv.eval;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * Tuple of IDatatype values
 * Use case: group_concat(distinct f(?x) f(?y))
 * Check if two tuples are distinct
 * Used by Walker
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class Tuple implements Comparable<Tuple> {
	
	IDatatype[] value;
	
	Tuple (IDatatype[] v){
		value = v;
	}
	
	IDatatype[] getValue(){
		return value;
	}
	
	IDatatype getValue(int i){
		return value[i];
	}
	
	int size(){
		return value.length;
	}

	public int compareTo(Tuple o) {
		return compare(this, o);
	}
	
	
	int compare(Tuple m1, Tuple m2){

		for (int i = 0; i<m1.size(); i++){
			int res = compare(m1.value[i], m2.value[i]);
			if (res != 0){
				return res;
			}
		}
		return 0;
	}


	int compare(IDatatype n1, IDatatype n2){
		if (n1 == n2){
			return 0;
		}
		else if (n1 == null){
			return -1;
		}
		else if (n2 == null){
			return +1;
		}
		else {
			return n1.compare(n2);
		}
	}

}
