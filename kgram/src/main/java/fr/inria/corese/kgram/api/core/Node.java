package fr.inria.corese.kgram.api.core;

import static fr.inria.corese.kgram.api.core.PointerType.NODE;
import fr.inria.corese.kgram.path.Path;

/**
 * Interface of Node provided by graph implementation
 * and also by KGRAM query Node
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Node extends Pointerable {
        public static final String INITKEY = "";

	public static final int DEPTH 	= 0;
	public static final int LENGTH 	= 1;
	public static final int REGEX 	= 2;
	public static final int OBJECT 	= 3;

	
	public static final int PSIZE 	= 4;

	public static final int STATUS 	= 4;
        
        @Override
        default PointerType pointerType(){
            return NODE;
        }

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
	 * sameTerm
	 *
	 */
	boolean same(Node n);
        
        // Node match for Graph match
        boolean match(Node n);
	
	int compare(Node node);

	String getLabel();
		
	boolean isVariable();
	
	boolean isConstant();
	
	boolean isBlank();
        
        boolean isFuture();
        
        default boolean isMatchNodeList() { return false; }
        
        default boolean isMatchCardinality() { return false; }
	
	// the target value for Matcher and Evaluator
	// for KGRAM query it returns IDatatype
	Object getValue();
        
        DatatypeValue getDatatypeValue();
        
        default void setDatatypeValue(DatatypeValue dt) {}
        
        Node getGraph();
        
        Node getNode();
	
	Object getObject();
        
        Path getPath();
        
        TripleStore getTripleStore();
	
	void setObject(Object o);
	
	Object getProperty(int p);
	
	void setProperty(int p, Object o);
		
	
}
