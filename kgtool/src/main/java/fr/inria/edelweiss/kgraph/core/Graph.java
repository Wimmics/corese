package fr.inria.edelweiss.kgraph.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.api.Tagger;
import fr.inria.edelweiss.kgraph.api.ValueResolver;
import fr.inria.edelweiss.kgraph.logic.*;
import java.util.Map;

/**
 * Graph Manager
 * Edges are stored in an index
 * An index is a table: predicate -> List<Edge>	
 * Edge List are sorted 
 * Join on a Node is computed by dichotomy
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Graph //implements IGraph 
{
	private static Logger logger = Logger.getLogger(Graph.class);	
	
	public static final String TOPREL = 
		fr.inria.acacia.corese.triple.cst.RDFS.RootPropertyURI;
        
        public static boolean valueOut = !true;
	
	public static final int IGRAPH = -1;
	// NB of Index (subject, object, graph)
	public static final int LENGTH = 3;
	
	static final int COPY 	= 0;
	static final int MOVE 	= 1;
	static final int ADD  	= 2;
	static final int CLEAR  = 3;
	
	static long blankid = 0;
	static final String BLANK  = "_:b";
	static final String SKOLEM = ExpType.SKOLEM;

	private static final String NL = System.getProperty("line.separator");

	static final int TAGINDEX = 2;
	
	/**
	 * Synchronization:
	 * 
	 * several read in // ; only one write
	 * lock read:  Query (QueryProcess)
	 * lock write: Load (Load), Update (QueryProcess), Rule (RuleEngine)
	 * synchronized: Entailment synchronized in read, hence only one entailment can occur
	 * synchronized: indexNode (index of nodes for path)
	 * synchronized: synGetCheck (EdgeIndex) may generate index of nth arg during read
	 * see occurrences of graph.readLock() graph.writeLock()
	 **/
	 
	ReentrantReadWriteLock lock;
	
	ArrayList<Index> tables; 
	// default graph (deprecated)
	Index[] dtables;
	// Table of index 0
	Index table, 
	// for rdf:type, no named graph to speed up type test
	dtable;
        // key -> Node
	Hashtable<String, Entity> individual;
        // label -> Node
	Hashtable<String, Entity> blank;
	SortedMap<IDatatype, Entity> literal;
        // key -> Node
        Map<String, Entity> vliteral;
	// graph nodes: key -> Node
	Hashtable<String, Node> graph;
	// property nodes: label -> Node (for performance)
	Hashtable<String, Node> property;
	NodeIndex gindex;
        ValueResolver values;
	Log log;
	List<GraphListener> listen;
	Workflow manager;
	Tagger tag;
	Entailment inference, proxy;
	EdgeFactory fac;
	private Distance classDistance, propertyDistance;
        private boolean isSkolem = false;
	// true when graph is modified and need index()
	boolean 
        isUpdate = false, 
	isDelete = false,
	// any delete occurred ?
	isDeletion = false,
	isIndex  = true, 
	// automatic entailment when init()
	isEntail = true,
	isDebug  = !true,
	hasDefault = !true;
	// number of edges
	int size = 0;
	
	int nodeIndex = 0;

	private int tagCount = 0;

	private String key;
        private String name;

	private boolean hasTag = false;

        public static final String SYSTEM = ExpType.KGRAM + "system";
	
	// SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...))

    /**
     * @return the isSkolem
     */
    public boolean isSkolem() {
        return isSkolem;
    }

    /**
     * @param isSkolem the isSkolem to set
     */
    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Contain undefined datatype
     */
    public boolean isFlawed() {
        for (Entity ent : getLiteralNodes()){
            IDatatype dt = (IDatatype) ent.getNode().getValue();
            if (DatatypeMap.isUndefined(dt)){
                return true;
            }
        }
        return false;
    }
    
    public boolean typeCheck(){
        if (inference == null){
            return true;
        }
        return inference.typeCheck();
    }

 
	
	class TreeNode extends TreeMap<IDatatype, Entity>  {
		
		TreeNode(){
			super(new Compare());
		}
						
	}
	
        /**
         * This Comparator enables to retrieve an occurrence of a given Literal
         * already existing in graph in such a way that two occurrences of same Literal 
         * be represented by same Node in graph
         * It (may) represent (1 integer) and (1.0 float) as two different Nodes
         * Current implementation of EdgeIndex sorted by values ensure join (by dichotomy ...)
         */
	class Compare implements Comparator<IDatatype> {
		
		public int compare(IDatatype dt1, IDatatype dt2){
			
			// xsd:integer differ from xsd:decimal 
			// same node for same datatype 
			if (dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null){
				int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
				if (cmp != 0){
					return cmp;
				}
			}
									
			int res =  dt1.compareTo(dt2);
			return res;
		}
	}
			
	Graph(){
           this(LENGTH); 
        }
        
	Graph(int length){
		lock = new ReentrantReadWriteLock();
		
		tables  = new ArrayList<Index>(length);
		for (int i=0; i<length; i++){
			// One table per node index
			// edges are sorted according to ith Node
			int j = i;
			if (i == length - 1){
				j = IGRAPH;
			}
			setIndex(i, new EdgeIndex(this, j));	
		}
		table 		= getIndex(0);
		literal 	= Collections.synchronizedSortedMap(new TreeNode());
                vliteral 	= Collections.synchronizedMap(new HashMap<String, Entity>());

		individual 	= new Hashtable<String, Entity>();
		blank 		= new Hashtable<String, Entity>();
		graph 		= new Hashtable<String, Node>();
		property 	= new Hashtable<String, Node>();
		gindex 		= new NodeIndex();
                values          = new ValueResolverImpl();
		fac 		= new EdgeFactory(this);
		manager 	= new Workflow(this);
		key = hashCode() + ".";
	}
	
	public static Graph create(){
		return new Graph();
	}
        
	/**
	 * b = true for RDFS entailment
	 */
	public static Graph create(boolean b){
		Graph g = new Graph();
		if (b) g.setEntailment();
		return g;
	}
	
	public EdgeFactory getEdgeFactory(){
		return fac;
	}
	
	public void setOptimize(boolean b){
	}
        
        public static void setValueTable(boolean b){
            valueOut = b;
            if (! b){
                setCompareKey(false);
            }
        }
        
        public static void setCompareKey(boolean b){
            EdgeIndex.byKey = b;
            if (b){
                setValueTable(true);
            }
        }
	
	public boolean isLog(){
		return log != null;
	}
	
	public Log getLog(){
		return log;
	}
	
	public void setLog(Log l){
		log = l;
	}
	
	public void log(int type, Object obj){
		if (log != null){
			log.log(type, obj);
		}
	}
	
	public void log(int type, Object obj1, Object obj2){
		if (log != null){
			log.log(type, obj1, obj2);
		}
	}
	
	public void addEngine(Engine e){
		manager.addEngine(e);
	}
	
	public void removeEngine(Engine e){
		manager.removeEngine(e);
	}
	
	public Workflow getWorkflow(){
		return manager;
	}
	
	public void setWorkflow(Workflow wf){
		manager = wf;
	}
	
	public void setClearEntailment(boolean b){
		manager.setClearEntailment(b);
	}
	
	/**
	 * Process entailments
	 */
	public synchronized void process(){
		manager.process();
	}
	
	public synchronized void process(Engine e){
		manager.process(e);
	}
	
	/**
	 * Remove entailments
	 */
	public synchronized void remove(){
		manager.remove();
	}
	
	public void addListener(GraphListener gl){
		if (listen == null){
			listen = new ArrayList<GraphListener>();
		}
		if (! listen.contains(gl)){
			listen.add(gl);
			gl.addSource(this);
		}
	}
	
	public void removeListener(GraphListener gl){
		if (listen != null){
			listen.remove(gl);
		}
	}
	
	public List<GraphListener> getListeners(){
		return listen;
	}
	
	public void setTagger(Tagger t){
		tag = t;
		if (t != null){
			setTag(true);
		}
	}
	
	public Tagger getTagger(){
		return tag;
	}
	
	public Lock readLock(){
		return lock.readLock();
	}
	
	public Lock writeLock(){
		return lock.writeLock();
	}
	
	public ReentrantReadWriteLock getLock(){
		return lock;
	}
	
	
	void clearDistance(){
		setClassDistance(null);
		setPropertyDistance(null);
	}
	
	public Entailment getEntailment(){
		return inference;
	}
	
	/**
	 * b=true require entailments to be performed before next query
	 */
	public void setEntail(boolean b){
		isEntail = b;
	}
        
        public boolean isEntail(){
            return isEntail;
        }
	
	public void setEntailment(Entailment i){
		inference = i;
		manager.addEngine(i);
	}
	
	/**
	 * Set RDFS entailment
	 */
	public void setEntailment(){
		Entailment entail = Entailment.create(this);
		setEntailment(entail);		
	}
	
	/**
	 * (des)activate RDFS entailment
	 */
	public void setEntailment(boolean b){
		if (inference != null){
			inference.setActivate(b);
		}
	}
	
	
	public void set(String property, boolean value){
		localSet(property, value);
		if (inference!=null){
			inference.set(property, value);
		}
	}
	
	void localSet(String property, boolean value){
		if (property.equals(Entailment.DUPLICATE_INFERENCE)){
			for (Index t : tables){
				t.setDuplicateEntailment(value);
			}
		}
	}
	
	public void entail(){
		if (inference!=null){
			inference.process();
		}
	}
	
//	public void entail(List<Entity> list){
//		if (isEntail && inference!=null){
//			inference.entail(list);
//		}
//	}
	
	
	public void setDefault(boolean b){
		hasDefault = b;
	}	
	
	public boolean hasDefault(){
		return hasDefault;
	}
	
//	public List<Index> getTables(){
//		return tables;
//	}
	
	public String toString(){
		String str = "";
		str += "Edge:       " 	+ size() + "\n";
		str += "Node:       " 	+ (individual.size() + blank.size() + literal.size()) + "\n";
		str += "Graph:      "	+ graph.size() + "\n";
		str += "Property:   " + table.size() + "\n";
		str += "Literal:    " 	+ literal.size() + "\n";
		str += "Individual: " + individual.size() + "\n";
		str += "Blank: " 	  + blank.size() + "\n";
		str += "Duplicate:  "+ table.duplicate() + "\n";
		return str;
	}
	
	public String toString2(){
		String str = "";
		int uri = 0, blank = 0, string = 0, lit = 0, date = 0, num = 0;
		
		for (Entity e : getNodes()){
			uri++;
		}
		
		for (Entity e : getBlankNodes()){
			blank++;
		}
		
		for (Entity e : getLiteralNodes()){
			IDatatype dt = (IDatatype) e.getNode().getValue();
			if (dt.isNumber()) num++;
			else if (dt.getCode() == IDatatype.STRING) 	string++;
			else if (dt.getCode() == IDatatype.LITERAL) lit++;
			else if (dt.getCode() == IDatatype.DATE) 	date++;

		}
		
		str += "uri: " + uri;
		str += "\nblank: " + blank; 
		
		str += "\nnum: " + num; 
		str += "\nstring: " + string; 
		str += "\nliteral: " + lit; 
		str += "\ndate: " + date; 

		return str;
	}
	
	public String display(){
		String sep = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		
		if (getIndex() instanceof EdgeIndex){
			EdgeIndex ie = (EdgeIndex) getIndex();
			
			for (Node p : getSortedProperties()){
				if (sb.length() > 0){
					sb.append(NL);
				}
				List<Entity> list  = ie.get(p);
				sb.append(p + " (" + list.size() + ") : ");
				sb.append(sep);
				for (Entity ent : list){
					sb.append(ent);
					sb.append(sep);
				}
			}
		}
		
		return sb.toString();
	}


	public Entailment getProxy(){
		if (proxy == null){
			proxy = inference;
			if (proxy == null){
				proxy = Entailment.create(this);
			}
		}
		return proxy;
	}
	
	public boolean isType(Edge edge){
		return getProxy().isType(edge);
	}
	
	public boolean isType(Node pred){
		return getProxy().isType(pred);
	}
	
	public boolean isSubClassOf(Node pred){
		return getProxy().isSubClassOf(pred);
	}
	
	public boolean isSubClassOf(Node node, Node sup){
		return getProxy().isSubClassOf(node, sup);
	}
	
	
	/**************************************************************
	 * 
	 * Consistency Management
	 * 
	 **************************************************************/
	
	/**
	 * send e.g. by kgram eval() before every query execution
	 * restore consistency if updates have been done, perform entailment
	 * when delete is performed, it is the user responsibility
	 * to delete the entailments that depend on it
	 * it can be done using:  drop graph kg:entailment 
	 * Rules are not automatically run, use re.process()
	 */
	
	public synchronized void init(){
		if (isIndex){
                    if (isDebug){
                        logger.info("Graph index");
                    }
                    index();
		}
		
		if (isUpdate){
			// use case: previously load or sparql update
			// clean meta properties 
			// redefine meta properties
			update();
		}

                if (isEntail){
                    if (isDebug){
                        logger.info("Graph entailment");
                    }
                    process();
                    isEntail = false;
		}			

	}

	private void update(){
		isUpdate = false;
		// node index
		clearIndex();
		clearDistance();
		
		if (isDelete){
			manager.onDelete();
			isDelete = false;
		}
	}
	
		
	
	public void setUpdate(boolean b){
		isUpdate = b;
		if (isUpdate){
			setEntail(true);
		}
	}
	
	
	public boolean isUpdate(){
		return isUpdate;
	}
	
	private void setDelete(boolean b){
		setUpdate(b);
		isDelete = b;
		isDeletion = true;
	}
	
	/**
	 * @deprecated
	 */
//	public boolean isEntailment(){
//		return isEntail;
//	}
	
	public boolean hasEntailment(){
		return inference!=null;
	}
	

	
	// true when index must be sorted 
	public boolean isIndex(){
		return isIndex;
	}
        
        public void setIndex(boolean b){
            isIndex = b;
        }

	/**
	 * Property Path start a new shortest path
	 * Only with one user (no thread here)
	 * @deprecated
	 */
	public void initPath(){
		
	}
	
	
	
	/*************************************************************************/
	
	
	public ValueResolver getValueResolver(){
            return values;
        }
	
	public Index getIndex(){
		return table;
	}
	
	/**
	 * When load is finished,  sort edges
	 */
	public void index(){
		for (Index ei : getIndexList()){
			ei.index();
		}
//		if (hasDefault){
//			for (int i=0; i<tables.size(); i++){
//				dtables[i].index();
//			}
//		}
		isIndex = false;
	}
	
	public void prepare(){
		if (isIndex){
			index();
		}
	}
	
	void clearIndex(){
		gindex.clear();
	}
	
	synchronized void indexNode(){
		if (gindex.size() == 0){
			table.indexNode();
		}
	}
	
	void define(Entity ent){
		gindex.add(ent);
	}
	
	public Iterable<Node> getProperties(){
		return table.getProperties();
	}
	
	public Iterable<Node> getSortedProperties(){
		return table.getSortedProperties();
	}
	
	/**
	 * Pragma: nodes are already nodes of the graph
	 */
	public Entity add(EdgeImpl edge){
		// store edge in index 0
		Entity ent = table.add(edge);
		// tell other index that predicate has instances
		if (ent != null){
			addGraphNode(edge.getGraph());
			addPropertyNode(edge.getEdgeNode());
			
			for (Index ei : getIndexList()){
				if (ei.getIndex() != 0){
					ei.declare(edge);
				}
			}
			size++;
		}
		return ent;
	}
	
	public boolean exist(Entity edge){
		return table.exist(edge);
	}
	
	public Entity addEdgeWithNode(Edge edge){
		if (edge instanceof EdgeImpl){
			EdgeImpl ee = (EdgeImpl) edge;
			addGraphNode(ee.getGraph());
			addPropertyNode(ee.getEdgeNode());
			for (int i=0; i<ee.nbNode(); i++){
				add(ee.getNode(i));
			}
			return addEdge(ee);
		}
		return null;
	}

	
	public Entity addEdge(EdgeImpl edge){
		Entity ent = add(edge);
		if (ent != null){
			setUpdate(true);
			//OC:
			manager.onInsert(ent.getGraph(), edge);
//			if (inference!=null){
//				inference.onInsert(ent.getGraph(), edge);
//			}
		}
		return ent;
	}
	


	
	public EdgeImpl create(Node source, Node subject, Node predicate, Node value){
		return fac.create(source, subject, predicate, value);
	}
	
	public EdgeImpl createDelete(Node source, Node subject, Node predicate, Node value){
		return fac.createDelete(source, subject, predicate, value);
	}
		
	public EdgeImpl create(Node source, Node predicate, List<Node> list){
		return fac.create(source, predicate, list);
	}
		
	public EdgeImpl createDelete(Node source, Node predicate, List<Node> list){
		return fac.createDelete(source, predicate, list);
	}
	
	public EdgeImpl create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value){
		return null;
	}
	
	public int size(){
		return size;
	}
	
	public int nbIndividuals(){
		return individual.size();
	}
	
	public int nbBlanks(){
		return blank.size();
	}
	
	public int nbLiterals(){
		return literal.size();
	}
	
	void setSize(int n){
		size = n;
	}
	
	
	public Node copy(Node node){
		return getNode((IDatatype) node.getValue(), true, false);
	}
	
	
	public Node getTopClass(){
		Node n = getNode(OWL.THING);
		if (n == null){
			n = getNode(RDFS.RESOURCE);
		}
		if (n == null){
			n = createNode(RDFS.RESOURCE);
		}
		return n;
	}
	
	public Node getTopProperty(){
		Node n = getNode(TOPREL);
		if (n == null){
			n = createNode(TOPREL);
		}
		return n;
	}
	
	
	public List<Node> getTopProperties(){
		List<Node> nl = new ArrayList<Node>();
		Node n;
		
//		n = getNode(OWL.TOPOBJECTPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
//		n = getNode(OWL.TOPDATAPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
		
		if (nl.size() == 0){
			n = getTopProperty();
			nl.add(n);
		}
		
		return nl;
	}
	
		
	// all nodes
	// TODO: check producer
	public Node addNode(IDatatype dt){
		return getNode(dt, true, true);
	}
	
	// used by construct
	public Node getNode(Node gNode, IDatatype dt, boolean create, boolean add){
		if (dt.isBlank() && isSkolem()){
                    dt = skolem(dt);
		}
		return getNode(dt, create, add);
	}
	
	/**
	 * Given a constant query node, return the target node in current graph 
	 * if it exists
	 * 
	 */
	public Node getNode(Node node){
		IDatatype dt = (IDatatype) node.getValue();
		return getNode(dt, false, false);
	}
	
	// used by construct
	public Node getNode(IDatatype dt, boolean create, boolean add){
		if (dt.isLiteral()){
			return getLiteralNode(dt, create, add);
		}
		else if (dt.isBlank()){
			return getBlankNode(dt, create, add);
		}
		else {
			return getResourceNode(dt, create, add);
		}
	}
	
	
	public Node getResourceNode(IDatatype dt, boolean create, boolean add){
            String key = getKey(dt);
		Node node = getNode(key, dt.getLabel());
		if (node != null) return node;
		node = getResource(key, dt.getLabel());
		if (node == null && create){
			node = createNode(key, dt);
		}
		if (add){
                    add(dt, node);
                }
		return node;
	}
	
	
	public Node getBlankNode(IDatatype dt, boolean create, boolean add){
		Node node = getBlankNode(dt.getLabel());
		if (node != null) return node;
		if (node == null && create){
			node = createNode(dt);
		}
		if (add) {
                    add(dt, node);
                }
		return node;
	}
	
	public Node getLiteralNode(IDatatype dt, boolean create, boolean add){
            String key = getKey(dt);
		Node node = getLiteralNode(key, dt);
		if (node != null) return node;
		if (create){ 
			node = createNode(key, dt);
			if (add){
                            addLiteralNode(dt, node);
                        }
		}
		return node;
	}
	


	/**
	 * Retrieve Node or create it (but not add it into graph)
	 */
//	public Node getCreateResource(String name){
//		Node node = getResource(name);
//		if (node == null){ 
//			node = createNode(name);
//		}
//		return node;
//	}
	
	/**
	 * Retrieve a node/graph node/property node 
	 */
	public Node getResource(String name){
            return getResource(getID(name), name);
        }
            
        Node getResource(String key, String name){
		Node node = getNode(key, name);
		if (node == null) node = getGraphNode(key, name);
		if (node == null){
			node = getPropertyNode(name);
		}
		return node;
	}
	

	// resource or blank
	public boolean isIndividual(Node node) {
		return individual.containsKey(getID(node)) || 
                            blank.containsKey(node.getLabel());
        }
        
	// resource node
	public Node getNode(String name){
            return getNode(getID(name), name);
	}
        
        Node getNode(String key, String name){
		return (Node) individual.get(key);
	}
        
        void addNode(IDatatype dt, Node node){
		individual.put(getID(node), (Entity) node);
        }      
	
	public Node getBlankNode(String name){
            return (Node) blank.get(name);
	}
        
        void addBlankNode(IDatatype dt, Node node){
            blank.put(node.getLabel(), (Entity) node);
        }
        
        
        String getID(Node node){
            if (valueOut){
                return node.getKey();
            }
            else {
                return node.getLabel();
            }
        }
        
        String getID(String str){
            if (valueOut){
                return values.getKey(str);
            }
            else {
                return str;
            }
        }
        
        String getKey(IDatatype dt){
            if (valueOut){
                return values.getKey(dt);
            }
            else {
                return dt.getLabel();
            }
        }
        
		
	Node basicAddGraph(String label){
                String key = getID(label);
		Node node = getGraphNode(key, label);
		if (node != null) return node;
		node = getResource(key, label);
                if (node == null){
                    IDatatype dt = DatatypeMap.createResource(label);
                    node = createNode(key, dt); 
                    indexNode(dt, node);
                }
		//graph.put(label, node);	               
		graph.put(key, node);	               
		return node;
	}

    Node basicAddResource(String label) {
        String key = getID(label);
        Node node = getResource(key, label);
        if (node != null) {
            return node;
        }
        IDatatype dt = DatatypeMap.createResource(label);
        node = createNode(key, dt);
        add(dt, node);
        return node;
    }


	Node basicAddProperty(String label){
		Node node = getPropertyNode(label);
		if (node != null){
                    return node;
		}
		node = getResource(label);
                if (node == null){
                    IDatatype dt = DatatypeMap.createResource(label);
                    node = createNode(dt); 
                    indexNode(dt, node);
                }
                property.put(label, node);
		return node;
	}

	Node basicAddBlank(String label){
		Node node = getBlankNode(label);
		if (node == null){
			IDatatype dt = DatatypeMap.createBlank(label);
			if (dt != null){
                            node = createNode(dt);
                            indexNode(dt, node);
                            addBlankNode(dt, node);
                        }
		}
		return node;
	}       
	
	public void add(Node node){
            IDatatype  dt = (IDatatype) node.getValue();
            add(dt, node);
        }
                
                
	 void add(IDatatype dt, Node node){
		if (dt.isLiteral()){
			addLiteralNode(dt, node);
		}
		else if (dt.isBlank()){
			addBlankNode(dt, node);
			indexNode(dt, node);
		}
		else {
			addNode(dt, node);
			indexNode(dt, node);
		}
	}

	public void addLiteralNode(IDatatype dt, Node node){
                if (valueOut){
                    vliteral.put(node.getKey(), (Entity) node);
                }
                else {
                    literal.put(dt, (Entity) node);
                }
		indexNode(dt, node);
	}
        
  
	public Node getLiteralNode(IDatatype dt){
            return getLiteralNode(getKey(dt), dt);
        }

	public Node getLiteralNode(String key, IDatatype dt){
            if (valueOut){
               	return (Node) vliteral.get(key);
            }
            else {
              	return (Node) literal.get(dt);  
            }
	}	
	
	public Node getGraphNode(String label){
		return getGraphNode(getID(label), label);
	}
        
        Node getGraphNode(String key, String label){
		return graph.get(key);
	}
	
	public void addGraphNode(Node gNode){
		if (! isGraphNode(gNode)){
			//graph.put(gNode.getLabel(), gNode);
			graph.put(getID(gNode), gNode);
			indexNode((IDatatype) gNode.getValue(), gNode);
		}
	}
	
	public boolean isGraphNode(Node node){
		//return graph.containsKey(node.getLabel());
		return graph.containsKey(getID(node));
	}
	
	public Node getPropertyNode(String label){
		return property.get(label);
	}
	
	public Node getPropertyNode(Node p){
		return property.get(p.getLabel());
	}
	
	public void addPropertyNode(Node pNode){
		if (! property.containsKey(pNode.getLabel())){
			property.put(pNode.getLabel(), pNode);
			indexNode((IDatatype) pNode.getValue(), pNode);
		}
	}
	

	

	
	public Iterable<Entity> getEdges(){
		Iterable<Entity> ie = table.getEdges();
		if (ie == null) return new ArrayList<Entity>();
		return ie;
	}
	
	public Edge getEdge(Node pred, Node node, int index){
		Iterable<Entity> it = getEdges(pred, node, index);
		if (it == null) return null;
 		for (Entity ent : it){
			return ent.getEdge();
		}
		return null;
	}
	
	public Edge getEdge(String name, Node node, int index){
		Node pred = getPropertyNode(name);		
		return getEdge(pred, node, index);
	}

	
	public Edge getEdge(String name, String arg, int index){
		Node pred = getPropertyNode(name);		
		Node node = getNode(arg);
		if (pred==null || node==null) return null;
		Edge edge = getEdge(pred, node, index);
		return edge;
	}
        
      public IDatatype getValue(String name, IDatatype dt) {
            Node node = getNode(dt);
            if (node == null) {
                return null;
            }
            Edge edge = getEdge(name, node, 0);
            if (edge == null) {
                return null;
            }
            return (IDatatype) edge.getNode(1).getValue();
        }

	
	public Iterable<Node> getNodes(Node pred, Node node, int n){
		Iterable<Entity> it = getEdges(pred, node, n);
		if (it == null){
			return new ArrayList<Node>();
		}
		int index = (n == 0) ? 1 : 0;
		return NodeIterator.create(it, index);
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, int n){
		return getEdges(predicate, node, null, n);
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, Node node2, int n){
		if (isTopRelation(predicate)){
			return getEdges(node, n);
		}
		else {
			return basicEdges(predicate, node, node2, n);
		}
	}
	
	public Iterable<Entity> basicEdges(Node predicate, Node node, Node node2, int n){
		return getIndex(n).getEdges(predicate, node, node2);
	}

	
	/**
	 * with rdfs:subPropertyOf
	 */
	public Iterable<Entity> getAllEdges(Node predicate, Node node, Node node2, int n){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		
		for (Node pred : getProperties(predicate)){
			Iterable<Entity> it = getIndex(n).getEdges(pred, node);
			if (it != null){
				meta.next(it);
			}
		}
		if (meta.isEmpty()) return new ArrayList<Entity>();
		return meta;
	}
	
	public Iterable<Node> getProperties(Node p){
		ArrayList<Node> list = new ArrayList<Node>();
		for (Node n : getProperties()){
			if (getEntailment().isSubPropertyOf(n, p)){
				list.add(n);
			}
		}
		return list;
	}	
        
        /**
         * Return start blank node for all lists
         */
        public List<Node> getLists(){
            List<Node> list = new ArrayList<Node>();
            for (Entity ent : getEdges(RDF.FIRST)){
                Node start = ent.getNode(0);
                Edge edge = getEdge(RDF.REST, start, 1);
                if (edge == null){
                    list.add(start);
                }
            }
            return list;
        }

        /**
         * 
         * Return the root of the graph, when it is a tree (e.g. SPIN Graph)
         */
        public Node getRoot(){
            for (Entity ent : getBlankNodes()){
                Node node = ent.getNode();
                if (! hasEdge(node, 1)){
                    return node;
                }
            }
            return null;
        }
        
        public boolean hasEdge(Node node, int i){
             Iterable<Entity> it = getEdges(node, 1);
             return it.iterator().hasNext();
        }
	
	public List<Node> getList(Node node){
		List<Node> list = new ArrayList<Node>();
		list(node, list);
		return list;
	}

	/**
	 * node is a list Node
	 * compute the list of elements
	 */
	void list(Node node, List<Node> list){
		if (node.getLabel().equals(RDF.NIL)){
		}
		else {
			Edge first = getEdge(RDF.FIRST, node, 0);
                        if (first != null){
                            list.add(first.getNode(1));
                        }
			Edge rest  = getEdge(RDF.REST, node, 0);
                        if (rest != null){
                            list(rest.getNode(1), list);
                        }
		}
	}
	
	
	boolean isTopRelation(Node predicate){
		return predicate.getLabel().equals(TOPREL);
	}
	
	// without duplicates 
	public Iterable<Entity> getNodeEdges(Node node){
		return EdgeIterator.create(this, getEdges(node, 0));
	}
	
	public Iterable<Entity> getNodeEdges(Node gNode, Node node){
		EdgeIterator it = EdgeIterator.create(this, getEdges(node, 0));
		it.setGraph(gNode);
		return it;
	}
	

	Index getIndex(int n, boolean def){
		if (def){
			return dtables[n];
		}
		return getIndex(n);
	}	
	
	public List<Index> getIndexList(){
		return tables;
	}
	
	// synchronized
	public Index getIndex(int n){
		if (n == IGRAPH){
			return tables.get(tables.size()-1);
		}
		if (n+1 >= tables.size() ){
			//setIndex(n, new EdgeIndex(this, n));	
		}
		return tables.get(n);
	}
	
	void setIndex(int n, Index e){
		tables.add(n, e);
	}
	
	public Iterable<Entity> getEdges(Node node, int n){
		return getSortedEdges(node, n);
	}
	
	
	public Iterable<Entity> getSortedEdges(Node node, int n){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		
		for (Node pred : getSortedProperties()){
			Iterable<Entity> it = getIndex(n).getEdges(pred, node);
			if (it != null){
				meta.next(it);
			}
		}
		if (meta.isEmpty()) return new ArrayList<Entity>();
		return meta;
	}
	
        public Iterable<Entity> getEdges(String p){
            Node predicate = getPropertyNode(p);
            if (predicate == null){
                return new ArrayList<Entity>();
            }
            return getEdges(predicate);
        }
        
	public Iterable<Entity> getEdges(Node predicate){
		Iterable<Entity> it = getEdges(predicate, null, 0);
		if (it == null) it = new ArrayList<Entity>();
		return it;
	}
	
	public int size (Node predicate){
		if (isTopRelation(predicate)) return size();
		Node pred = getPropertyNode(predicate.getLabel());
		if (pred == null) return 0;
		return table.size(pred);
	}
	
	public Iterable<Node> getGraphNodes(){
		return graph.values();
	}
	
	public Iterable<Node> getTypeNodes(){
		return table.getTypes();
	}
	
	public Iterable<Entity> getNodes(){
		return individual.values();
	}
	
	public Iterable<Entity> getBlankNodes(){
		return blank.values();
	}
	
	/**
	 *  resource & blank
	 *  TODO: a node may have been deleted (by a delete triple)
	 *  but still be in the table
	 */
	public Iterable<Entity> getRBNodes(){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		meta.next(getNodes());
		meta.next(getBlankNodes());
		return meta;
	}
	
	public Iterable<Entity> getLiteralNodes(){
            if (valueOut){
                return vliteral.values();  
            }
            return literal.values();
	}
		
	public Iterable<Entity> getAllNodes(){
		if (isDeletion){
			// recompute existing nodes (only if it has not been already recomputed)
			return getAllNodesIndex();
		}
		else {
			// get nodes from tables
			return getAllNodesDirect();
		}
	}
	
	/**
	 * 	TODO: a node may have been deleted (by a delete triple)
	 *  but still be in the table
	 */
	public Iterable<Entity> getAllNodesDirect(){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		meta.next(getNodes());
		meta.next(getBlankNodes());
		meta.next(getLiteralNodes());
		return meta;
	}
	
	/**
	 * Prepare an index of nodes for each graph, enumerate all nodes
	 * TODO: there are duplicates (same node in several graphs) 
	 */
	public Iterable<Entity> getAllNodesIndex(){
		indexNode();
		return gindex.getNodes();
	}
	
	
	public Iterable<Entity> getNodes(Node gNode){
		indexNode();
		return gindex.getNodes(gNode);
	}

	
	
	/**
	 * May infer datatype from property range
	 */
	public Node addLiteral(String pred, String label, String datatype, String lang){
            String range = null;
		if (lang == null && 
				inference!=null && inference.isDatatypeInference()){
                        range = inference.getRange(pred);
			if (range != null 
                                && ! range.startsWith(Entailment.XSD)){
				range = null;
			}
		}
                if (datatype == null){ 
                    if (range != null){
                        datatype = range;
                    }
                }
                
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
		if (dt == null) return null;
		return addNode(dt);
	}
	

	
	public String newBlankID(){
            if (isSkolem){
                return skolem(blankID());
            }
            else {
		return blankID();
            }
	}
	
        String blankID(){
            return BLANK + blankid++;
        }
        
        public String skolem(String id){
            String str = values.getKey(key + id);
            return SKOLEM + str;
        }
	
        public IDatatype skolem(IDatatype dt) {
            if (! dt.isBlank()) {
                return dt;
            }
            String id = skolem(dt.getLabel());
            return createSkolem(id);
        }
        
         public Node skolem(Node node) {
            if (! node.isBlank()) {
                return node;
            }
            String id = skolem(node.getLabel());
            return NodeImpl.create(createSkolem(id));
        }
         
         IDatatype createSkolem(String id){
             return DatatypeMap.createSkolem(id);
         }
	
	public void deleteGraph(String name){
            graph.remove(getID(name));
            //graph.remove(name);
//		Node node = getGraphNode(name);
//		if (node != null){
//			graph.remove(node);
//		}
	}
	
	void indexNode(IDatatype dt, Node node){
		index(dt, node);
	}
        
        void index(IDatatype dt, Node node){
//		if (node.getIndex() == -1){
//			node.setIndex(nodeIndex++);
//		}
	}

	
        /** 
         * Only for new node that does not exist
         */
	Node createNode(IDatatype dt){
            return createNode(getKey(dt), dt);
        }
        
        Node createNode(String key, IDatatype dt){
		Node node;
                if (valueOut){
                    node = NodeImpl.create(this, dt);
                    node.setKey(key);
                    values.setValue(key, dt);
                }
                else {
                    node = NodeImpl.create(dt);
                }

                return node;
	}
        
        
        public IDatatype getValue(Node node){
           return values.getValue(node.getKey());
	}
	
	// resource nodes
	public Node createNode(String name){
		IDatatype dt = DatatypeMap.createResource(name);
		if (dt == null) return null;
		return createNode(dt);
	}
	
	/****************************************************************
	 * 
	 * Graph operations
	 * 
	 ****************************************************************/
	
   
        
	public boolean compare(Graph g){
		return compare(g, false);
	}
	
	public boolean compare(Graph g2, boolean isGraph){
		Graph g1 = this;
//		if (isDebug){
//			logger.debug(g1.getIndex());
//			logger.debug(g2.getIndex());
//		}
		if (g1.isIndex()) index();
		if (g2.isIndex()) g2.index();
		if (g1.size() != g2.size()){
			if (isDebug) logger.debug("** Graph Size: " + size() + " " + g2.size());
//			System.out.println(g1.display());
//			System.out.println("___");
//			System.out.println(g2.display());
						
			return false;
		}
		
		
		boolean ok = true;
		for (Node pred1  : g1.getProperties()){
			Node pred2 = g2.getPropertyNode(pred1.getLabel());
			int s1 = g1.size(pred1);
			int s2 = g2.size(pred2);
			if (s1 != s2){
				ok = false;
				System.out.println(pred1 + ": " + s1 + " vs " + s2);
			}
		}
		
		if (! ok){
			return false;
		}

		
		TBN t = new TBN();

		for (Node pred1  : g1.getProperties()){
			
			Iterable<Entity> l1 = g1.getEdges(pred1);
			
			Node pred2 = g2.getPropertyNode(pred1.getLabel());
			
			if (pred2 == null){
				if (l1.iterator().hasNext()){
					if (isDebug)  logger.debug("Not found: " + pred1);
					return false;
				}
			}
			else {
				Iterable<Entity> l2 = g2.getEdges(pred2);

				Iterator<Entity> it = l2.iterator();

				for (Entity ent1 : l1){
                                    
                                    if (true){
                                        // node index
                                        boolean b = compare(g2, pred2, t, ent1, isGraph);
                                        if (! b){
                                            if (isDebug){
						logger.debug(ent1);
                                            }
                                            return false;
                                        }
                                    }
                                    else {
                                        // node value 
					if (! it.hasNext()){
                                            return false;
                                        }

					Entity ent2 = it.next();
					if (! compare(ent1, ent2, t, isGraph)){
						if (isDebug){
							logger.debug(ent1);
							logger.debug(ent2);
						}
						return false;
					}
                                    }
				}
			}

		}
		return true;
	}
        
        
        boolean compare(Graph g2, Node pred2, TBN t, Entity ent1, boolean isGraph){
            Iterable<Entity> l2 = g2.getEdges(pred2);
            Iterator<Entity> it = l2.iterator();
            
            for (Entity ent2 : l2){
                if (compare(ent1, ent2, t, isGraph)){
                    return true;
                }
            }
            return false;
        }

	
	boolean compare(Entity ent1, Entity ent2, TBN t, boolean isGraph){
		
		for (int j=0; j<ent1.getEdge().nbNode(); j++){
			
			Node n1 = ent1.getEdge().getNode(j);
			Node n2 = ent2.getEdge().getNode(j);

			if (! compare(n1, n2, t)){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Blanks may have different label but should be mapped to same blank
	 */
	boolean compare(Node n1, Node n2, TBN t){
		boolean ok = false;
		if (n1.isBlank()){
			if (n2.isBlank()){
				// blanks may not have same ID but 
				// if repeated they should  both be the same
				ok = t.same(n1, n2);
			}
		}
		else if (n2.isBlank()){
		}
		else {
			ok = n1.same(n2);
		}
		
		return ok;
	}
	
	
	class TBN extends Hashtable<Node,Node>{

		boolean same(Node dt1, Node dt2){
			if (containsKey(dt1)){
				return get(dt1).same(dt2);
			}
			else {
				put(dt1, dt2);
				return true;
			}
		}
	}
	
	/**
	 * Create a graph for each named graph
	 */
	public List<Graph> split(){
		
		if (graph.size() == 1){
			ArrayList<Graph> list = new ArrayList<Graph>();
			list.add(this);
			return list;
		}
		
		return gSplit();
	}
		
	
	List<Graph> gSplit(){
	
		GTable map = new GTable();
		
		for (Entity ent : getEdges()){
			Graph g = map.getGraph(ent.getGraph());
			g.addEdgeWithNode(ent.getEdge());
		}
		
		ArrayList<Graph> list = new ArrayList<Graph>();
		for (Graph g : map.values()){
			list.add(g);
		}
		
		return list;
		
	}
	
	
	class GTable extends HashMap<Node, Graph> {

		public Graph getGraph(Node gNode) {
			Graph g = get(gNode);
			if (g == null){
				g = Graph.create();
				put(gNode, g);
			}
			return g;
		}
	}
	                   
        
       public List<Entity> getEdgeList(Node n){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for (Entity e : getEdges(n, 0)){
                list.add(e);
            }
            return list;
        }
       
       /**
        * 
        * Without rule entailment
        */
        public List<Entity> getEdgeListSimple(Node n){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for (Entity e : getEdges(n, 0)){
                if (! getProxy().isRule(e)){
                    list.add(e);
                }
            }
            return list;
        }

	
	/*****************************************************************
	 * 
	 * Update
	 * 
	 *****************************************************************/
        
        public Entity insert(Entity ent){
            if (! (ent instanceof EdgeImpl)){
			return null;
            }
            return addEdge((EdgeImpl) ent);
        }

	
	public List<Entity> delete(Entity ent){
		if (! (ent instanceof EdgeImpl)){
			return null;
		}
		return delete((EdgeImpl) ent);
	}

	public List<Entity> delete(EdgeImpl edge){
		List<Entity> res = null;
		
		if (edge.getGraph() == null){
			res = deleteAll(edge);
		}
		else {
			Entity ee = basicDelete(edge);
			if (ee != null){
				res = new ArrayList<Entity>();
				res.add(ee);
			}
		}
		
		if (res != null){
			deleted(res);
		}
		return res;
	}

	
	public List<Entity> delete(EdgeImpl edge, List<Constant> from){
		List<Entity> res = null;	

		for (Constant str : from){
			Node node = getGraphNode(str.getLabel());

			if (node != null){
				edge.setGraph(node);
				Entity ent = basicDelete(edge);
				if (ent != null){
					if (res == null){
						res = new ArrayList<Entity>();
					}
					res.add(ent);
					setDelete(true);
				}				
			}
		}

		if (res != null){
			deleted(res);
		}
		return res;
	}
	
	
	/**
	 * Does not delete nodes
	 */
	Entity basicDelete(EdgeImpl edge){
		Entity res = null;
		
		for (Index ie : tables){
				Entity ent = ie.delete(edge);
				if (isDebug){
					logger.debug("delete: " + ie.getIndex() + " " + edge);
					logger.debug("delete: " + ie.getIndex() + " " + ent);
				}
				if (ent != null){
					setDelete(true);
					res = ent;
				}
		}
		return res;
	}
	
	
	
	/**
	 * delete occurrences of this edge in all graphs
	 */
	List<Entity> deleteAll(EdgeImpl edge){
		ArrayList<Entity> res = null;

		for (Node graph : getGraphNodes()){
			edge.setGraph(graph);
			Entity ent = basicDelete(edge);
			if (ent != null){
				if (res == null) {
					res = new ArrayList<Entity>();
				}
				res.add(ent);
				setDelete(true);
			}
		}

		return res;
	}
	
	/**
	 * This edge has been deleted
	 * TODO: Delete its nodes from tables if needed
	 */
	void deleted(List<Entity> list){
		for (Entity ent : list){
			Edge edge = ent.getEdge();
			
			for (int i=0; i<edge.nbNode(); i++){
				delete(edge.getNode(i));
			}
		}
	}
	
	void delete(Node node){
		
	}
	
	// clear all except graph names.
	// they must be cleared explicitely
	void clear(){
		clearIndex();
		clearNodes();
		for (Index t : tables){
			t.clear();
		}
		manager.onClear();
		//OC:
//		if (inference!=null){
//			inference.onClear();
//		}
		clearDistance();
		isIndex = true;
		isUpdate = false; 
		isDelete = false; 
		size = 0;
	}
	
	void clearNodes(){
		individual.clear();
		blank.clear();
		literal.clear();
		property.clear();
	}
	
	public boolean clearDefault(){
		clear();
		return true;
	}
	
	public boolean clearNamed(){
		clear();
		return true;
	}
	
	public boolean dropGraphNames(){
		graph.clear();
		return true;
	}
	
	public boolean clear(String uri, boolean isSilent){
		if (uri != null){
			Node gg = getGraphNode(uri);
			if (isDebug) logger.debug("** clear: " + gg);
			if (gg != null){
				setDelete(true);
				getIndex(IGRAPH).clear(gg);
			}
		}
			
		return true;
	}
	
	public boolean update(String source, String target, boolean isSilent, int mode){
		Node g1 = getGraphNode(source);
		Node g2 = getGraphNode(target);
		
		if (g1 == null){
			return false;
		}
		setUpdate(true);

		if (g2 == null){
			g2 = addGraph(target);
		}
		
		switch (mode){
		case ADD:  getIndex(IGRAPH).add(g1, g2); break;
		case MOVE: getIndex(IGRAPH).move(g1, g2); break;
		case COPY: getIndex(IGRAPH).copy(g1, g2); break;

		}
		
		return true;
	}
	
	public boolean add(String source, String target, boolean isSilent){
		return update(source, target, isSilent, ADD);
	}
	
	public boolean move(String source, String target, boolean isSilent){
		return update(source, target, isSilent, MOVE);
	}
	
	public boolean copy(String source, String target, boolean isSilent){
		return update(source, target, isSilent, COPY);
	}

	
	/*********************************************************
	 * 
	 * Distance
	 * 
	 ********************************************************/
	
	
	public void setClassDistance(Distance distance) {
		this.classDistance = distance;
	}
	
	synchronized public Distance setClassDistance(){
		if (classDistance != null){
			return classDistance;
		}
		setClassDistance(Distance.classDistance(this));
		return classDistance;
	}

	public Distance getClassDistance() {
		return classDistance;
	}
	
	public void setPropertyDistance(Distance distance) {
		this.propertyDistance = distance;
	}
	
	synchronized public Distance setPropertyDistance(){
		if (propertyDistance != null){
			return propertyDistance;
		}
		setPropertyDistance(Distance.propertyDistance(this));
		return propertyDistance;
	}

	public Distance getPropertyDistance() {
		return propertyDistance;
	}
	
	
	/****************************************************
	 * 
	 *                  User API
	 *                  
	 *  TODO: 
	 *  no code here, use call to basic methods
	 *  secure addEdge wrt addGraph/addProperty
	 *  api with IDatatype
	 *  
	 * **************************************************/
	
	/**
	 * Add a copy of the entity edge
	 * Use case: entity comes from another graph, create a local copy of nodes
	 */
	public Edge copy(Entity ent){
		Node g = basicAddGraph(ent.getGraph().getLabel());
		Node p = basicAddProperty(ent.getEdge().getEdgeNode().getLabel());
		
		ArrayList<Node> list = new ArrayList<Node>();
		
		for (int i = 0; i < ent.nbNode(); i++){
			Node n = addNode((IDatatype) ent.getNode(i).getValue());
			list.add(n);
		}
		
		Edge e = addEdge(g, p, list);
		return e;	
	}
        
    /**
     * Copy g into this
     */
    public List<Entity> copy(List<Entity> list) {
        for (Index id : tables) {
            if (id.getIndex() != 0) {
                id.clearCache();
            }
        }

        if (isDebug) {
            logger.info("Copy: " + list.size());
        }

        isIndex = true;
        for (Entity ent : list) {
            add((EdgeImpl) ent.getEdge());
        }
        isIndex = false;


        table.index();

        return list;
    }
    
    public List<Entity> copy(Graph g, boolean b) {
        ArrayList<Entity> list = new ArrayList<Entity>();

        for (Index id : tables){
            if (id.getIndex() != 0){
                id.clearCache();
            }
        }
        
        for (Node pred : g.getProperties()) {
            if (isDebug) {
                logger.info("Copy: " + pred + " from " + g.size(pred) + " to " + size(pred));
            }

            for (Entity ent : g.getEdges(pred)) {
                
                 if (! exist(ent)) {
                    list.add(ent);
                }                             
            }
            
            isIndex = true;
            for (Entity ent : list){
                add((EdgeImpl) ent.getEdge());
            }
            isIndex = false;
        }
        
        table.index();

        return list;
    }
    
    
    
    public List<Entity> copy2(Graph g, boolean b) {
        ArrayList<Entity> list = new ArrayList<Entity>();

        for (Node pred : g.getProperties()) {
            if (isDebug) {
                logger.info("Copy: " + pred + " from " + g.size(pred) + " to " + size(pred));
            }

            for (Entity ent : g.getEdges(pred)) {
                Edge edge = ent.getEdge();
                Entity ee = add((EdgeImpl) edge);
                if (ee != null){
                    list.add(ee);
                }         
            }
        }

        return list;
    }
	
	public void copy(Graph g){
		for (Entity ent : g.getEdges()){
			copy(ent);
		}
	}
	
	
	void copyEdge(Entity ent){
		
	}
	
	
	public Edge addEdge(Node source, Node subject, Node predicate, Node value){
		EdgeImpl e = fac.create(source, subject, predicate, value);
		Entity ee = addEdge(e);
		if (ee != null){
			return ee.getEdge();
		}
		return null;
	}
	
	public Edge addEdge(Node subject, Node predicate, Node value){
		Node g = addGraph(Entailment.DEFAULT);
		return addEdge(g, subject, predicate, value);
	}
	
	// tuple
	public Edge addEdge(Node source, Node predicate, List<Node> list){
		EdgeImpl e;
		if (list.size() == 2){
			e = fac.create(source, list.get(0), predicate, list.get(1));
		}
		else {
			e = fac.create(source, predicate, list);
		}
		
		Entity ee = addEdge(e);
		if (ee != null){
			return ee.getEdge();
		}
		return null;
	}
	
	
	/**
	 * Graph in itself is not considered as a graph node for SPARQL path
	 * unless explicitely referenced as a subject or object
	 * Hence ?x :p* ?y does not return graph nodes
	 */
	public Node addGraph(String name){
		return basicAddGraph(name);
	}

	public Node addResource(String label){
		return basicAddResource(label);
	}

	/**
	 * Property in itself is not considered as a graph node for SPARQL path
	 * unless explicitely referenced as a subject or object
	 * Hence ?x :p* ?y does not return property nodes
	 */
	public Node addProperty(String label){
		return basicAddProperty(label);
	}

        /**
         * label *must* have been generated by newBlankID()
         */
	public Node addBlank(String label){
            if (isSkolem){
		return basicAddResource(label);
            }
            else {
              	return basicAddBlank(label); 
            }
	}
        
        public IDatatype createBlank(String label){
            if (isSkolem){
		return createSkolem(label);
            }
            else {
		return DatatypeMap.createBlank(label);
            }
        }


	public Node addBlank(){
		return addBlank(newBlankID());
	}


	public Node addLiteral(String label, String datatype, String lang){
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
		if (dt == null) return null;
		return addNode(dt);
	}
	
	public Node addLiteral(String label, String datatype){
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, null);
		if (dt == null) return null;
		return addNode(dt);
	}
	
	public Node addLiteral(String label){
		return addLiteral(label, null, null);
	}
	
	public Node addLiteral(int n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(long n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(double n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(float n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(boolean n){
		return addNode(DatatypeMap.newInstance(n));
	}

	public void setDebug(boolean b) {
		isDebug = b;
		manager.setDebug(b);
                if (inference != null){
                    inference.setDebug(b);
                }
	}

	/**********************************************************/
	
	
	/**
	 * 
	 * Generate a unique tag for each triple
	 */
	Node tag(){
		IDatatype dt = DatatypeMap.newInstance(tagString());
		Node tag = getNode(dt, true, true);
		return tag;
	}
	
	String tagString(){
		Tagger t = getTagger();
		if (t == null){
			return key + tagCount++;
		}
		return t.tag();
	}
	
	public boolean hasTag() {
		return hasTag ;
	}
	
	boolean needTag(Entity ent) {
		return hasTag() && 
			ent.nbNode() == TAGINDEX && 
			! getProxy().isEntailed(ent.getGraph());
	}
	
	public void setTag(boolean b){
		hasTag = b;
	}
	
	/**
	 * This log would be used to broadcast deletion to peers
	 */
	void logDelete(Entity ent){
		if (listen != null){
			for (GraphListener gl : listen){
				gl.delete(this, ent);
			}
		}
	}
	
	void logInsert(Entity ent){
		if (listen != null){
			for (GraphListener gl : listen){
				gl.insert(this, ent);
			}		
		}
	}
        
        public void logStart(Query q){
            if (listen != null){
			for (GraphListener gl : listen){
				gl.start(this, q);
			}		
		}
        }
	
	public void logFinish(Query q){
            logFinish(q, null);
        }
        
	public void logFinish(Query q, Mappings m){
             if (listen != null){
			for (GraphListener gl : listen){
				gl.finish(this, q, m);
			}		
		}
        }
        
        public void logLoad(String path){
             if (listen != null){
			for (GraphListener gl : listen){
				gl.load(path);
			}		
		}
        }
        
	boolean onInsert(Entity ent){
		if (listen != null){
			for (GraphListener gl : listen){
				if (! gl.onInsert(this, ent)){
					return false;
				}
			}		
		}
		return true;
	}
	
	
	
	/**
	 * Check if query may succeed on graph
	 * PRAGMA: no RDFS entailments, simple RDF match
	 */
			
	public boolean check(Query q){
		return check(q, q.getBody());
	}
	
	boolean check(Query q, Exp exp){
		
		switch (exp.type()){

		case ExpType.EDGE: 
			Edge edge = exp.getEdge();
			Node pred = edge.getEdgeNode();
			Node var  = edge.getEdgeVariable();

			if (var == null){
				
				if (getPropertyNode(pred) == null){
					// graph does not contain this property: fail now
					return false;
				}
				else if (isType(pred)){ 
					Node value = edge.getNode(1);
					// ?c a owl:TransitiveProperty
					if (value.isConstant()){
						if (getNode(value) == null){
							return false;
						}
					}
					else if (q.getBindingNodes().contains(value) && q.getMappings() != null){
						// ?c a ?t with bindings
						for (Mapping map : q.getMappings()){

							Node node = map.getNode(value);
							if (node != null && getNode(node) != null){
								// graph  contain node
								return true;
							}
						}
						return false;
					}
				}
			}
			else if (q.getBindingNodes().contains(var) && q.getMappings() != null){
				// property variable with bindings: check the bindings
				for (Mapping map : q.getMappings()){

					Node node = map.getNode(var);
					if (node != null && getPropertyNode(node) != null){
						// graph  contain a property
						return true;
					}
				}
				
				return false;
			}
			
			break;
			
			
		case ExpType.UNION: 
			
			for (Exp ee : exp.getExpList()){	
				if (check(q, ee)){
					return true;
				}
			}
			return false;

			
		case ExpType.AND: 	
		case ExpType.GRAPH: 	

			for (Exp ee : exp.getExpList()){
				boolean b = check(q, ee);
				if (! b){
					return false;
				}
			}	
		}
		
		return true;
	}

	
    public Graph getNamedGraph(String name){
        return null;
    }
	
}
