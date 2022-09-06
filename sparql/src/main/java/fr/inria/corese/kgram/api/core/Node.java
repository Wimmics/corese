package fr.inria.corese.kgram.api.core;

import static fr.inria.corese.kgram.api.core.PointerType.NODE;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.IDatatype.NodeKind;

/**
 * Interface of Node provided by graph implementation
 * and also by KGRAM query Node
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Node extends Pointerable, Comparable {
        public static final String INITKEY = "";

	public static final int DEPTH 	= 0;
	public static final int LENGTH 	= 1;
	public static final int REGEX 	= 2;
	public static final int OBJECT 	= 3;

	
	public static final int PSIZE 	= 4;

	public static final int STATUS 	= 4;
        
        default NodeKind getNodeKind() {
            return getValue().getNodeKind();
        }
        
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
        
        @Override
        default int compareTo(Object node) {
            if (node instanceof Node) {
                return compareTo((Node)node);
            }
            return -1;
        }
        
        default int compareTo(Node node) {
            return getDatatypeValue().compareTo(node.getDatatypeValue());
        }

	String getLabel();
		
	boolean isVariable();
	
	boolean isConstant();
	
	boolean isBlank();
        
        boolean isFuture();
        
        default boolean isMatchNodeList() { return false; }
        
        default boolean isMatchCardinality() { return false; }
	
	// the target value for Matcher and Evaluator
	// for KGRAM query it returns IDatatype
	IDatatype getValue();
        
        IDatatype getDatatypeValue();
                
        default void setDatatypeValue(IDatatype dt) {}
        
        Node getGraph();
        
        @Override
        Node getNode();
	
	Object getNodeObject();
	void setObject(Object o);
                      
        Path getPath();
        
        @Override
        TripleStore getTripleStore();	
	
//	Object getProperty(int p);	
//	void setProperty(int p, Object o);
        
        // tagged as triple reference
        default boolean isTriple() {
            return getDatatypeValue().isTriple();
        }
        
        // triple reference with edge inside
	default boolean isTripleWithEdge() {
            return isTriple() && getEdge() != null;
        }
        
        default void setEdge(Edge e) {
            getDatatypeValue().setEdge(e);
        }
        
        default boolean isTripleNode() {
            return false;
        }
}
