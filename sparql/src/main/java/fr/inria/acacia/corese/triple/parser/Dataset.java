package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.ExpType;

/**
 * 
 * SPARQL Dataset
 * from or named may be null
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class Dataset {
	protected static final String KG = ExpType.KGRAM;
	static final String EMPTY = KG + "empty";
	static final Constant CEMPTY = Constant.create(EMPTY);

	List<Constant> from, named, with;

        // true when used by update (delete in default graph specified by from)
	// W3C test case is true
	// Protocol is false
	boolean isUpdate = false;
	
	Dataset(){
            this(new ArrayList<Constant>(), new ArrayList<Constant>());
	}
	
	Dataset(List<Constant> f, List<Constant> n){
		from = f;
		named = n;
	}
	
	public static Dataset create(){
		return new Dataset();
	}
	
	public static Dataset create(List<Constant> f, List<Constant> n){
		if (f==null && n==null) return null;
		return new Dataset(f, n);
	}
        
       public static Dataset newInstance(List<String> f, List<String> n) {
        if (f == null && n == null) {
            return null;
        }
        ArrayList<Constant> from = null, named = null;
        if (f != null) {
            from = new ArrayList<Constant>();
            for (String s : f) {
                from.add(Constant.create(s));
            }
        }
        if (n != null) {
            named = new ArrayList<Constant>();
            for (String s : n) {
                named.add(Constant.create(s));
            }
        }

        return new Dataset(from, named);
        }
        
	
	public String toString(){
		String str = "";
		str += "from:  " + from + "\n";
		str += "named: " + named ;
		return str;
	}
	
	public void defFrom(){
		from = new ArrayList<Constant>();
	}
	public void defNamed(){
		named = new ArrayList<Constant>();
	}
	public boolean isUpdate(){
		return isUpdate;
	}
	
	public boolean isEmpty(){
		return ! hasFrom() && ! hasNamed();
	}
	
	public boolean hasFrom(){
		return from != null && from.size() >0;
	}
	
	public boolean hasNamed(){
		return named != null && named.size() >0;
	}
	
	public boolean hasWith(){
		return with != null && with.size() >0;
	}
        
	public void setUpdate(boolean b){
		isUpdate = b;
	}
	
	public List<Constant> getFrom(){
		return from;
	}
	
	public List<Constant> getNamed(){
		return named;
	}
        
        public List<Constant> getWith(){
            return with;
        }
        
        public void setWith(Constant w){
            with = new ArrayList<Constant>(1);
            with.add(w);
        }
	
	public void clean(){
		from.remove(CEMPTY);
	}

	
	public void addFrom(String s){
		addFrom(Constant.create(s));
	}
	
	public void addNamed(String s){
		addNamed(Constant.create(s));

	}
        
        public void addFrom(Constant s){
            if (from == null) defFrom();
		if (! from.contains(s)){
			from.add(s);
		}
        }
        
        public void addNamed(Constant s){
            if (named == null) defNamed();
		if (! named.contains(s)){
			named.add(s);
		} 
        }

	
	/**
	 * Std SPARQL Dataset requires that if from (resp named) is empty in a Dataset
	 * simple query triple (resp graph query triple) fail
	 * In order to make kgram fail accordingly, we add a fake from (resp named) 
	 */
	public void complete(){
		if (hasFrom()  && ! hasNamed()){
			addNamed(CEMPTY);
		}
		else if (! hasFrom() && hasNamed()){
			addFrom(CEMPTY);
		}
	}
		

}
