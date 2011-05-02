package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventManager;
import fr.inria.edelweiss.kgram.tool.EdgeInv;
import fr.inria.edelweiss.kgram.tool.EntityImpl;


/**********************************************************
 * 
 * Use case:
 * ?x rdf:resf* / rdf:first
 * 
 * match the regexp (and subproperties)
 * write paths in a synchronized buffer
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 * 
 *********************************************************/

public class PathFinder 
{	
	
	private static Logger logger = Logger.getLogger(PathFinder.class);
	
	static int cc = 0;
	
	// thread that enumerates the path
	private GraphPath path;
	private Visit visited;
	// synchronized buffer between this and projection
	//private Buffer buffer;
	private Buffer mbuffer;
	private Environment memory;
	private EventManager manager;
	private Producer producer;
	private Matcher matcher;
	private Evaluator evaluator;
	private List<Mapping> lMap ;
	private Filter filter;
	private Memory mem;
	private Edge edge;
	private Node gNode, targetNode;
	private List<Node> from;
	private Stack loopStack;
	// index of node in edge that is the start of the path
	private int index = 0;


	// the inverse of the index (i.e. the other arg)
	private int other ;
	private boolean 
	hasEvent = false,
	// true if breadth first (else depth first)
	isBreadth,
	defaultBreadth = !true,
	// true if accept subproperty in regexp
	isSubProperty,
	isRecSource, 
	isReverse,
	isShort,
	isOne, 
	loopNode = false,
	std = !true;
	//isInverse;
	private int  maxLength = 100, 
	min = 0, max = maxLength,
	userMin = -1,
	userMax = -1;
	private int count = 0;
	
	// meta table for start -> distance(start, *)
	private Hashtable<Node, ITable> htmeta;
	// table for distance(start, target)
	private ITable htdistance; 
//	private Integer[] adistance;
	// regexp for path
	private Regex regexp;
	private Automaton  automaton;
	// depth or width first 
	private String mode = "";
	private final static String DEPTH	="d";
	private final static String BREADTH	="b";

	private final static String PROPERTY="p";
	// heuristic to eliminate path of length > path already found to *same* target
	private final static String SHORT	="s";
	// very short: eliminate any path >= best current path ('vs' = previous 's') to *any* target
	private final static String ALL		="a";
	public final static String INVERSE	="i";
	

	
	class ITable extends Hashtable<Node, Integer> {
		
		void setDistance(Node c, int i){
			put(c, i); //adistance[i]);
		}
		
		int getDistance(Node c){
			Integer i =  get(c);
			if (i == null) return -1;
			else return i;
		}
	}
	
	public PathFinder(){}
	
	
	public PathFinder(Producer p, Matcher match, Evaluator eval) {
		producer = p;
		matcher = match;
		evaluator = eval;
		lMap = new ArrayList<Mapping>();
	}
	
	public void setDefaultBreadth(boolean b){
		defaultBreadth = b;
	}
	
	public void setEventManager(EventManager man){
		manager = man;
		hasEvent = true;
	}
	
	public void checkLoopNode(boolean b){
		loopNode = b;
	}
	
	/**
	 * Start/init computation of a new list of path
	 * 
	 */
	public void start(Edge edge, Memory memo, Filter f){
		List<String> lVar = null;
		if (f != null) lVar = f.getVariables();
		lMap.clear();
		this.edge = edge;
		int n = index(edge, memo, lVar);
		start(n);
		index = n;
		targetNode = memo.getNode(edge.getNode(other));
		producer.initPath(edge, index);
		
		if (f != null){
			if (match(edge, lVar, index)){
				filter = f;
				mem = new Memory(matcher, evaluator);
				mem.init(memo.getQuery());
			}
		}
	}
	
	
	boolean match(Edge edge, List<String> lVar, int index){
		return lVar.size() == 1 && 
			edge.getNode(index).isVariable() &&
			edge.getNode(index).getLabel().equals(lVar.get(0));
	}
	
	/**
	 * Compute the index of node of edge which will be start of the path
	 * If one of the nodes is bound, this is it
	 * If one of the nodes is a constant, this is it
	 * If there is a filter on a node, this is it
	 * Otherwise it is node at index 0
	 */
	int index(Edge edge, Environment mem, List<String> lVar){
		int n = -1; // index;
		// which arg is bound if any ?
		for (int i=0; i<2; i++){
			if (mem.isBound(edge.getNode(i))){
				n = i;
				break;
			}
		}
		if (n == -1){
			for (int i=0; i<2; i++){
				if (edge.getNode(i).isConstant()){
					n = i;
					break;
				}
			}
		}
		if (n == -1 && lVar != null){
			for (int i=0; i<2; i++){
				if (match(edge, lVar, i)){
					n = i;
					break;
				}
			}
		}
		if (n == -1){
			n = 0;
		}
		return n;
	}
	
	
	void setPathLength(int n){
		maxLength = n;
	}

	
	public Iterable<Mapping> candidate(Node gNode, List<Node> from, Environment memory) {
		this.gNode = gNode;
		this.from = from;
		mstart(memory);
		// return path enumeration (read the synchronized buffer)
		return mbuffer;
	}
	
	void mstart(Environment mem) {
		// buffer store path enumeration
		memory = mem;
		mbuffer = new Buffer();
		// path enumeration in a thread 
		path = new GraphPath(this, mem, mbuffer);
		// launch path computing (one by one) eg launch process() below
		path.start();
	}
	
	public void run(){
		process(memory);
	}
	
	public void stop(){
		if (path != null){
			path.stop();
		}
	}
	
	public void interrupt(){
		if (path != null){
			path.interrupt();
		}
	}
	
	/**
	 * init at creation time, no need to change.
	 * TODO:
	 * Check wether we should clear htmeta when index changes
	 */
	public void init(Regex exp, Object smode, int pmin, int pmax){
		loopStack = new Stack();
		regexp = exp;
		
		mode = (String)smode;
		
		isBreadth = defaultBreadth;
		isSubProperty = true;
		isRecSource = false; //edge.isRecSource();
		isReverse = false;
		isShort = false;
		isOne = false;
		//isInverse = false;
		htmeta = new Hashtable<Node, ITable>();
		other = 1;

		if (mode != null){
			//isBreadth =     mode.indexOf(DEPTH) == -1;
			if (mode.indexOf(DEPTH) >= 0)   isBreadth = false;
			if (mode.indexOf(BREADTH) >= 0) isBreadth = true;
			isSubProperty = mode.indexOf(PROPERTY) == -1;
			isShort =       mode.indexOf(SHORT) != -1;
			//isInverse = 	mode.indexOf(INVERSE) != -1;
//			if (mode.indexOf(INVERSE) != -1){
//				exp.setInverse(true);
//			}
			if (isShort && ! isBreadth){
				isOne = mode.indexOf(ALL) == -1;
			}
		}
			
		userMin = pmin;
		userMax = pmax;
		
		// set min and max path length
		// filter pathLength($path) > val
		// the length of the minimal path that matches regex:
		int length = regexp.regLength();
		min = Math.max(min, length);
		max = Math.max(max, length);
		
		// user default values
		if (userMin != -1) min = Math.max(min, userMin);
		if (userMax != -1) max = userMax;
			
	}
	
	public Edge getEdge(){
		return edge;
	}
	
	/**
	 * start at run time
	 * depends on index : which arg is bound or where we start
	 */
	void start(int n){
		if (n != index){
			// we change index : reset automaton
			// and reverse regex
			regexp = regexp.reverse();
			automaton = null;
			// distances are obsolete: clean table
			htmeta.clear();
		}
		index = n;
		if (index == 1){ 
			other = 0;
			isReverse = true;
		}
		else {
			other = 1;
			isReverse = false;
		}

		if (automaton == null){
			automaton = new Automaton();
			automaton.compile(regexp);
		}

		automaton.start();
		
		if (automaton.isBound()){
			min = 0;
			max = 1000;
		}
//		System.out.println("** PF: \n" + automaton);
//		System.out.println("** PF: " + regexp);
	}
	
	
	// number of enumerated relations
	public int getCount(){
		return count;
	}
	
	Node get(Environment memory, int i){
		if (edge == null) return null;
		Node qc = edge.getNode(i);
		//if (qc == null) return null;
		Node node = memory.getNode(qc);
		return node;
	}
	
	
	void process(Environment memory){
		// Is the source of edge bound ?
		// In which case all path relations come from same source 
		//Node csource = get(memory, IRelation.ISOURCE);
		Node csource = null;
		if (gNode!=null){
			csource = memory.getNode(gNode);
		}
		
		// the start concept for path
		Node cstart = get(memory, index);

		// the path that is recursively built
		Path path = new Path();
		visited =  Visit.create(isReverse);
		//path.checkLoopNode(loopNode);
		// try paths with length between min and max
		int imin = min, imax = max;
//		adistance = new Integer[max+2];
//		for (int i=0; i<adistance.length; i++){
//			adistance[i]=new Integer(i);
//		}
		if (! isBreadth){
			// depth first: one rec call explore all depths until max
			// breadth first: one call for each depth until max
			imin = max;
		}
		
		if (std){
			// enumerate start nodes first
			for (int i=imin; i<=imax; i++){
				// try paths with length i:
				//System.out.println("** Path length : " + i);
				for (Entity ent : getStart(cstart, csource)){
					
					if (ent != null){
						Node cnext = ent.getNode();
						//System.out.println("** PF: " + cnext + " " + index + " " + imax);
						// each start node has its distance table:
						if (isShort){
							htdistance = getTable(cnext);
							htdistance.setDistance(cnext, 0); //adistance[0]);
						}
						automaton.start();
						visited.clear();
						loopStack.clear();
						path.clear();
						if (hasEvent){
							send(Event.PATH, regexp, cnext, i);
						}
						path(cnext, csource, path,  min, i);
					}
				}
			}
		}
		else {
			// if cstart is not bound,
			// start nodes will be determined by edge enumeration
			// if zero length path, all nodes will be enumerated
			// depth first only
			// TODO: check distance
			automaton.start();
			visited.clear();
			loopStack.clear();
			path.clear();
			if (hasEvent){
				send(Event.PATH, regexp, cstart, imin);
			}
			path(cstart, csource, path,  min, imin);
		}

		// in order to stop enumeration, return null
		mbuffer.put(null, false);
		
	}
	

	
	
	/**
	 * Compute paths of size<=max, starting from bound concept of edge
	 * or from last concept in current path,
	 * from left to right or right to left (according to index of Candidate)
	 * write the path in a synchronized buffer 
	 * exp/next : regular expression the path must match
	 * use case : rdf:rest* / rdf:first
	 * This function interprets a finite state automaton that codes
	 * the regex with states and transitions
	 * It follows SPARQL 1.1 Property Path semantics
	 * TODO:
	 * (exp1/exp2+)+
	 * In this case, loop check has bug as we manage only one list of visited nodes for all
	 * occurrences of exp2+
	 * But this case is lousy
	 *
	 */
	private	void path(Node cstart, Node csrc, Path path,  int min, int max){
		Automaton automata = automaton;
		Stack stack = loopStack;
		Node gg = gNode;
		int size = path.size();
		
		boolean trace = !true ;
		//if (trace) System.out.println("** Path in : " + size + " " + exp + " " + next + " " + min + " " + max);
		
		// when true, we need to bind the source with first relation source
		boolean hasSource = size == 0 && csrc == null && gg != null;
		
		// where to enumerate candidate relations for current step
		State current = automata.getCurrent();
		
		if (trace) System.out.println("** Path fst: " + current);

		if (current.isFinal() &&
				((isBreadth && size == max) || (! isBreadth && size >= min))){

			if (trace) System.out.println("** Path: OK " + size + " " + max);

			if (size > 0 || std){
				Mapping map = result(path, gg, csrc, cstart, isReverse);

				if (map != null){
					mbuffer.put(map, true);
				}
			}
			else {
				// path of length zero: enumerate all nodes
				// TODO: case where csrc is bound
				for (Entity ent : getStart(cstart, csrc)){
					if (ent != null){
						Node node = ent.getNode();
						Node src = csrc;
						if (gg != null){ 
							src = ent.getGraph();
						}
						Mapping m = result(path, gg, src, node, isReverse);

						if (m != null){
							mbuffer.put(m, true);
						}
					}
				}
			}
		}
		
		State next;
		boolean epsilon;
		if (trace) System.out.println("** Path loop transitions of: " + current);
		
		if (current.isFirst() && cstart!=null){
			//System.out.println("** visit: " +cstart);
			// exp*  exp+ exp{n,}
			// record visited nodes in order to prevent loop
			visited.add(current, cstart);
		}
		
		if (current.isFirst() && visited.loop(current)){
			// skip
		}
		else for (Step step : current.getTransitions()){
					
			if (trace) System.out.println("** Path transition: " + step);
			epsilon = step.isEpsilon();
			
			next = step.getState();
			if (trace) System.out.println("** Path next:" + next + " " + size + " " + max);
		
			if (epsilon){
				// non deterministic empty transition to next step
				// use case: p*   exp{n,m}
				
				if (stack.loop(step, path)){
					// we do the empty transition only if we have not 
					// already done it or if a relation has been considered 
					// since last time, otherwise we enter an infinite loop
					// use case: star(p1 || star(p2))				
					continue;
				}
				
				boolean ok = true;
				int save = -1;
//				List<Node> list = null;
				if (step.isEnter()){
					// enter a loop: exp{1, 5}
					// save current counter
					// use case: (p{n,m}/q)*
					// the first loop is reentrant hence we save current counter
					// and restore it when finished (see below)
					if (next.isBound()){
						save = next.getCount();
						next.setCount(0);
					}
//					if (next.isFirst()){
//						list = visited.get(next);
//						visited.put(next, new ArrayList<Node>());
//					}
				}
				else if (step.isLeave()){
					// leave a loop: exp{1, 5}
					if (current.isBound() && 
						current.getCount() < current.getMin()){
						// have not reached min, do not leave yet
						ok = false;
					}
				}
				else if (current.isBound() && 
						 current.getCount() >= current.getMax()){ //current.endLoop()){
					// exp{1, 5} reach 5 loops, do not enter this epsilon
					ok = false;
				}
								
				if (ok){ 

					automata.setCurrent(next);
					stack.push(step, path);
					path(cstart, csrc, path, min, max);
					
					stack.pop(step, path);
					if (save != -1){
						next.setCount(save);
					}

					automata.setCurrent(current);
				}
				
				// consider  next transitions
				continue;
				// end of an epsilon transition
			}
			
			
			
			if (size == max || 
					(current.isBound() && current.getCount() >= current.getMax())){
				// consider  next transition (because it may be epsilon)
				if (trace) System.out.println("** Path: cont " + size + " " + max + " " + cstart);
				continue;
			}
			
			if (size < max){
				// enumerate relations of current concept with current candidate
				if (trace) logger.debug("** Path: start relations");
				int dist = size+1;
				//Edge rel;
				boolean swap = false, go = true;
				Regex exp = step.getProperty();
				boolean inverse = exp.isInverse() || exp.isReverse();
				Producer pp = producer;
				List<Node> ff = from;
				Edge ee = edge;
				Environment env = memory;
				ITable htd = htdistance;
				int ii = index, oo = other;
				int evPATHSTEP = Event.PATHSTEP;
				boolean isEvent = hasEvent, is = isShort, 
					hasFilter = filter!=null;
				Visit visit = visited;
				
				for (Entity ent : pp.getEdges(gg, ff, ee, env, exp, cstart, ii)){
					
					if (ent == null){
						continue;
					}
					
					Edge rel = ent.getEdge();
					Node node = rel.getNode(ii);

					if (inverse){
						// deprecate Corese isInverse()
						//&& ! cstart.equals(rel.getNode(ii))){
						// this relation is inverse wrt to cstart: 
						rel = new EdgeInv(rel);	
						node = rel.getNode(ii);
					}
					
					if (hasFilter && cstart == null){
						// test a filter on the index node
						boolean test = test(rel.getNode(ii));
						if (! test) continue;
					}

					if (trace)  System.out.println("** Path rel: " + size +  " " + rel);

					if (eliminate(rel, dist) ){
						// may eliminate by shorter distance
						continue;
					}
					//count++;

					if (hasSource){
						// first time: bind the common source of current path
						csrc = ent.getGraph();
					}

					Node graph = ent.getGraph();
					if (csrc != null && ! graph.same(csrc) ){ 

						// all relations need same source in one path
						if (trace) System.out.println("** Path : skip src");
					}
//					else if (loopNode &&  path.loop(rel, cstart, ii, oo)){
//						// there is a loop: skip rel
//					}
					else {
						if (isEvent){
							send(evPATHSTEP, cstart, exp, rel);
						}

						// candidate relation is OK
						if (is){
							// store distance from start to this node
							htd.setDistance(rel.getNode(oo), dist);
						}

						if (trace) System.out.println("** Path: rec 1 " + rel);
						// recurse one step ahead
						path.add(rel);
						automata.setCurrent(next);

						if (next.isBound()) next.incCount(+1);
						if (cstart == null && current.isFirst()){
							visit.add(current, node);
						}
						
						path(rel.getNode(oo), csrc, path,  min, max);
						
						if (cstart == null && current.isFirst()){
							visit.remove(current, node);
						}						
						if (next.isBound()) next.incCount(-1);

						path.remove(size);
					}
				}


			}

			automata.setCurrent(current);
		}
		
		if (current.isFirst() && cstart!=null){
			visited.remove(current, cstart);
		}
	}

	private boolean send(int sort, Object exp, Object target){
		boolean b = manager.send(EventImpl.create(sort, exp, target));
		return b;
	}
	
	private boolean send(int sort, Object exp, Object target, Object arg){
		boolean b = manager.send(EventImpl.create(sort, exp, target, arg));
		return b;
	}
	
	/**
	 * Path result as a Mapping
	 * start and last nodes
	 * the path variable whose index is used (e.g. pathLength) to retrieve the list of edges in the memory
	 * the list of edges
	 */
	private Mapping result(Path path, Node gNode, Node src, Node start, boolean isReverse){
		//if (edge.getIndex()==1)System.out.println(producer.getGraphNode(edge, edge) + " " + edge);
		Edge ee = edge;
		int length = 3;
		if (src!=null) length = 4;
		
		int fst = 0, lst = path.size()-1;
		if (isReverse){
			fst = lst;
			lst = 0;
		}
		
		Node n1, n2;
		if (path.size() == 0){
			n1 = start;
			n2 = start;
		}
		else {
			n1 = path.get(fst).getNode(0);
			n2 = path.get(lst).getNode(1);
		}
		
		if (! check(n1, n2)){
			return null;
		}
		
		Mapping map = getMapping(length);
		
		Node[] qNodes = map.getQueryNodes();
		Node[] tNodes = map.getNodes();
		
		qNodes[0] = ee.getNode(0);
		qNodes[1] = ee.getNode(1);
		qNodes[2] = ee.getEdgeVariable();
		
		tNodes[0] = n1;
		tNodes[1] = n2;
		// here we need a proxy Node that fakes the $path target node
		// for pathLength()
		if (path.size() == 0)
			 tNodes[2] = ee.getEdgeNode();
		else tNodes[2] = path.get(lst).getEdgeNode();
		
		if (src!=null){
			qNodes[3] = gNode;
			tNodes[3] = src;
		}
		
		Path edges = path.copy();
		if (isReverse) edges.reverse();
		
		map.setPath(ee.getEdgeVariable(), edges);
		
		//lMap.add(map);
		return map;
	}

	
	/**
	 * Create a new Mapping 
	 * draft: or reuse an existing one that have been used already by Eval (not used)
	 */
	private Mapping getMapping(int length){
		Node[] qNodes, tNodes;
		Mapping map;
		qNodes = new Node[length];
		tNodes = new Node[length];
		map =  Mapping.create(qNodes, tNodes);		
		return map;
	}
	
	/**
	 * Check if target node match its query node and its binding
	 */
	private boolean check(Node n0, Node n1){
		if (index == 0){
			if (! matcher.match(edge.getNode(1), n1, memory)){ // n1.match(edge.getNode(1))){
				return false;
			}
			if (targetNode != null && ! targetNode.same(n1)){
				return false;
			}
		}
		else {
			if (! matcher.match(edge.getNode(0), n0, memory)){ //n0.match(edge.getNode(0))){
				return false;
			}
			if (targetNode != null && ! targetNode.same(n0)){
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * dist is length of current path
	 * if target concept of rel already reached (in any previous path)
	 * with length <= dist, skip this rel (because it is redundant)
	 */
	private boolean eliminate(Edge rel, int dist){
		if (isShort){
			// have we already reach this target concept:
			int pdist = htdistance.getDistance(rel.getNode(other));
			//System.out.println(rel.getNode(other) + " " + dist + " " + pdist);
			if (pdist > 0){//(pdist != -1){
				if (isOne){
					if (pdist <= dist){	
						return true;
					}
				}
				else if (pdist < dist){
					// already reach with a smaller distance : skip this edge
					return true;
				}
			}
		}
		return false;
	}

	
	private ITable getTable(Node c){
		ITable t = htmeta.get(c);
		if (t == null){
			t = new ITable();
			htmeta.put(c, t);
		}
		return t;
	}
	
	private int getDistance(Node c1, Node c2){
		ITable t = htmeta.get(c1);
		if (t != null){
			return t.getDistance(c2);
		}
		return -1;
	}
	
	
	
	/**
	 * List of starting nodes for path
	 * TODO: check start memberOf src
	 */
	private Iterable<Entity> getStart(Node cstart, Node csrc){
		if (cstart != null){
			// start is bound
			ArrayList<Entity> vec = new ArrayList<Entity>();
			if (csrc != null){
				// check graph src contains start
				//System.out.println("** PF should check: " + csrc + " contains " + cstart);
			}
			vec.add(EntityImpl.create(csrc, cstart));
			return vec;
		}		
		else return getNodes(csrc, edge, from, automaton.getStart());
	}
	
	
	public Iterable<Entity> getNodes(Node csrc, final Edge edge, List<Node> from, List<Regex> list){	
		
		Iterable<Entity> iter = producer.getNodes(gNode, from, edge, memory, list, index);
		
		if (filter == null) return iter;
		
		final Iterator<Entity> it = iter.iterator();

		return new Iterable<Entity>(){
				
			public Iterator<Entity> iterator(){

				return new Iterator<Entity>(){

					public boolean hasNext() {
						return it.hasNext();
					}
					
					public Entity next() {
						while (hasNext()){
							Entity entity = it.next();
							if (entity == null) return null;
							Node node =  entity.getNode();
							if (test(node)){
								return entity;
							}
						}
						return null;
					}

					@Override
					public void remove() {
						// TODO Auto-generated method stub
						
					}
				};
			}
		};
	}
	
	boolean test(Node node){
		mem.push(edge.getNode(index), node);
		boolean test = evaluator.test(filter, mem);
		mem.pop(edge.getNode(index));
		return test;
	}
	
	
//	public Iterable<Node> getNodes(Node csrc, final Edge edge, List<Node> from, List<Regex> it){	
//		
//		final ArrayList <Iterable<Entity>> vec = new ArrayList <Iterable<Entity>> ();
//		
//		if (it == null){
//			// need all properties
//			Iterable<Entity> nit = producer.getNodes(gNode, from, edge, memory, null, index);
//			if (nit != null){
//				vec.add(nit);
//			}
//		}
//		else for (Regex exp : it){
//			// iterate starting properties
//			Iterable<Entity> nit = producer.getNodes(gNode, from, edge, memory, exp, index);
//			if (nit != null){
//				vec.add(nit);
//			}
//		}
//
//		if (vec.size() == 0){
//			ArrayList<Node> res = new ArrayList<Node>();
//			return res;
//		}
//		
//		/* 
//		 * iterate the vector of PCandidate of first properties
//		 * return the (first) concepts of the relations
//		 */
//		return new Iterable<Node>(){
//			int i = 0;
//			// iterate on PCandidate 
//			Iterator<Entity> it = vec.get(i++).iterator();
//				
//			public Iterator<Node> iterator(){
//
//				return new Iterator<Node>(){
//
//					public boolean hasNext() {
//						if (it.hasNext())
//							return true;
//						else if (i<vec.size()){
//							it = vec.get(i++).iterator();
//							return hasNext();
//						}
//						else return false;
//					}
//
//					public Node next2() {
//						Entity entity = it.next();
//						if (entity == null) return null;
//						return entity.getNode();				
//					}
//					
//					public Node next() {
//						while (hasNext()){
//							Entity entity = it.next();
//							if (entity == null) return null;
//							Node node =  entity.getNode();
//							if (filter == null){
//								return node;
//							}
//							mem.push(edge.getNode(index), node);
//							boolean test = evaluator.test(filter, mem);
//							mem.pop(edge.getNode(index));
//							if (test){
//								return node;
//							}
//						}
//						return null;
//					}
//
//					@Override
//					public void remove() {
//						// TODO Auto-generated method stub
//						
//					}
//				};
//			}
//		};
//	}
	
	
}
