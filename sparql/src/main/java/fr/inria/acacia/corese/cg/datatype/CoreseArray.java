package fr.inria.acacia.corese.cg.datatype;

import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;


public class CoreseArray extends CoreseBlankNode {
	private IDatatype[] array;
	private static int count = 0;
	private static final String SEED = RDF.BLANKSEED;
	
	public CoreseArray(String value) {
		super(value);
	}
	
	public CoreseArray() {
		super(SEED + count++);
	}
	
	public CoreseArray(IDatatype[] dt) {
		super(SEED + count++);
		array = dt;
	}
	
	public static CoreseArray create(List<IDatatype> vec) {
		IDatatype[] adt = new IDatatype[vec.size()];
		int i = 0;
		for (IDatatype dt : vec){
			adt[i++] = dt;
		}
		return new CoreseArray(adt);
	}
	
	public static CoreseArray create(IDatatype[] dts){
		return new CoreseArray(dts);
	}
	
	public static CoreseArray empty(){
		CoreseArray dt = new CoreseArray();
		dt.set(new IDatatype[0]);
		return dt;
	}
	
	public String toString(){
		String str = super.toString() +"[";
		for (IDatatype dt : array){
			str +=  dt + " ";
		}
		str += "]";
		return str;
	}
	
	public boolean isArray(){
		return true;
	}
	
	public CoreseArray getArray(){
		return this;
	}
	
	public void set(IDatatype[] dts){
		array = dts;
	}
	
	public  IDatatype[] getValues(){
		return array;
	}
	
	public IDatatype get(int n){
		return array[n];
	}
	
	public int size(){
		if (array == null) return 0;
		else return array.length;
	}
	
	// the total recursive number of elements including nested arrays
	public int gsize(){
		if (array == null) return 0;
		else {
			int n = 0;
			for (IDatatype dt : array){
				if (dt.isArray()){
					n += ((CoreseArray) dt).gsize();
				}
				else n += 1;
			}
			return n;
		}
	}
	 

	public boolean isTrue() throws CoreseDatatypeException {
		return array != null && array.length>0;
	}
	
	public boolean isTrueAble()  {
		return true;
	}
	
	
	
}
