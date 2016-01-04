package fr.inria.edelweiss.kgram.api.core;

/**
 * Interface of Node provided by graph implementation
 * and also by KGRAM query Node
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Node {
        public static final String INITKEY = "";

	public static final int DEPTH 	= 0;
	public static final int LENGTH 	= 1;
	public static final int REGEX 	= 2;
	public static final int OBJECT 	= 3;

	
	public static final int PSIZE 	= 4;

	public static final int STATUS 	= 4;

	/**
	 * Query nodes have an index computed by KGRAM
	 * @return
	 */
	int getIndex();
	
	/**
	 * Query nodes have an index computed by KGRAM
	 * @return
	 */
	void setIndex(int n);
        
        String getKey();
        
        void setKey(String str);

	
	/**
	 * Test if two nodes are the same.
	 * Used to check that bindings are preserved :
	 * two occurrences of same query node bound to same target node.
	 * It is possible to consider here a logical equivalence between nodes
	 * e.g. 
	 * a owl:sameAs b
	 * 1^^xsd:integer same 1^^xsd:float
	 * 
	 * @param n
	 * @return
	 */
	boolean same(Node n);
	
	int compare(Node node);

	String getLabel();
		
	boolean isVariable();
	
	boolean isConstant();
	
	boolean isBlank();
        
        boolean isFuture();
	
	// the target value for Matcher and Evaluator
	// for KGRAM query it returns IDatatype
	Object getValue();
        
        DatatypeValue getDatatypeValue();
	
	Object getObject();
	
	void setObject(Object o);
	
	Object getProperty(int p);
	
	void setProperty(int p, Object o);
		
	
}
