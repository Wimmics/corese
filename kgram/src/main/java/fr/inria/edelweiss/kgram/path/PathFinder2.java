package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;

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
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventManager;
import fr.inria.edelweiss.kgram.tool.EdgeInv;
import fr.inria.edelweiss.kgram.tool.EntityImpl;
import org.apache.logging.log4j.LogManager;


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

public class PathFinder2 
{	
	
	private static Logger logger = LogManager.getLogger(PathFinder.class);
	
	static int cc = 0;
	static boolean isStack = true;

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
	private Query query;
	private List<Mapping> lMap ;
	private Filter filter;
	private Memory mem;
	private Edge edge;
	private Node gNode, targetNode, regexNode, varNode;
	private List<Node> from;
	private Stack loopStack;
	// index of node in edge that is the start of the path
	private int index = 0;


	// the inverse of the index (i.e. the other arg)
	private int other;
	private boolean 
	isStop = false,
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
	private int  maxLength = Integer.MAX_VALUE, 
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
	private Regex regexp0, regexp1, regexp;
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
	
	public PathFinder2(){}
	
	
	public PathFinder2(Producer p, Matcher match, Evaluator eval, Query q) {
		query = q;
		producer = p;
		matcher = match;
		evaluator = eval;
		lMap = new ArrayList<Mapping>();
	}
	
	public static PathFinder create(Producer p, Matcher match, Evaluator eval, Query q) {
		return new PathFinder(p, match, eval, q);
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
	public void start(Edge edge, Node node, Memory memo, Filter f){
		regexNode = node;
		List<String> lVar = null;
		if (f != null) lVar = f.getVariables();
		lMap.clear();
		this.edge = edge;
		int n = index(edge, memo, lVar);
		start(n);
		index = n;
		targetNode = memo.getNode(edge.getNode(other));
		varNode = edge.getEdgeVariable();
		//producer.initPath(edge, index);
		
		if (f != null){
			if (match(edge, lVar, index)){
				filter = f;
				init(memo);
			}
		}
		if (mem == null && node!=null){
			init(memo);
		}
	}
	
	void init(Memory memo){
		mem = new Memory(matcher, evaluator);
		mem.init(memo.getQuery());
		mem.init(memo);
		mem.setFake(true);
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
		isStop = false;
		// buffer store path enumeration
		memory = mem;
		mbuffer = new Buffer();
		// path enumeration in a thread 
		// TODO: rem comment
		//path = new GraphPath(this, mem, mbuffer);
		// launch path computing (one by one) eg launch process() below
		path.start();
	}
	
	public void run(){
		process(memory);
	}
	
	public void stop(){
//		if (path != null){
//			path.stop();
//		}
		isStop = true;
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
		regexp0 = exp;
		regexp1 = exp.transform();
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
		regexp = regexp0;
		if (n != 0){
			// we change index : reset automaton
			// and reverse regex
			regexp = regexp0.reverse();
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
		
		if (!isStack && automaton.isBound()){
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
		visited =  Visit.create(isReverse, true);
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
			
			if (isStack){
				path.setMax(max);
				//System.out.println("** Path  : " + max);
				eval(regexp1, path, cstart, csource);
			}
			else {
				path(cstart, csource, path,  min, imin);
			}
			
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
				Regex exp = step.getRegex();
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
				
				for (Entity ent : pp.getEdges(gg, ff, ee, env, exp, csrc, cstart, ii)){
					
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
					
//					if (exp.getExpr() != null){
//						// use case: ?x c:isMemberOf[?this != <http://www.inria.fr] + ?y
//						boolean b = test(exp.getExpr().getFilter(), regexNode, rel.getNode(oo));
//						//System.out.println("** PF: " + b + " " + rel.getNode(oo) + " " + exp.getExpr());
//						if (! b) continue;
//					}
					
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
						path.add(ent);
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

						path.remove();
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
			n1 = path.get(fst).getEdge().getNode(0);
			n2 = path.get(lst).getEdge().getNode(1);
		}
		
		if (isShort){
			// eliminate current path to n2 if it is longer than a previous path to n2
			if (n2.getObject()==null){
				n2.setObject(path.size());
			}
			else {
				Integer l = (Integer) n2.getObject();
				if (path.size()>l){
					return null;
				}
				else {
					n2.setObject(path.size());
				}
			}
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
		tNodes[2] = getPathNode();
	
		if (src!=null){
			qNodes[3] = gNode;
			tNodes[3] = src;
		}
		
		Path edges = path.copy();
		if (isReverse) edges.reverse();

		Path[] lp = new Path[length];
		lp[2] = edges;
		map.setPath(lp);
		
		return map;
	}
	
	/**
	 * Generate a unique Blank Node wrt query that represents the path
	 * Use a predefined filter pathNode()
	 */
	Node getPathNode(){
		Filter f  = query.getGlobalFilter(Query.PATHNODE);
		Node node = evaluator.eval(f, memory, producer);
		return node;
	}

	
	/**
	 * Create a new Mapping 
	 * draft: or reuse an existing one that have been used already by Eval (not used)
	 */
	private Mapping getMapping(int length){
		Node[] qNodes = new Node[length];
		Node[] tNodes = new Node[length];
		Mapping map =  Mapping.create(qNodes, tNodes);	
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
	
	/**
	 * exp @{?this rdf:type c:Person}
	 */
	boolean test(Filter filter, Path path, Node qNode, Node node){
		mem.push(qNode, node);
		if (varNode != null){
			// TODO: fix it
			mem.push(varNode, varNode);
			mem.pushPath(varNode, path);
		}
		boolean test = evaluator.test(filter, mem);
		mem.pop(qNode);
		if (varNode != null){
			mem.pop(varNode);
			mem.popPath(varNode);
		}
		return test;
	}
	
	boolean test(Node node){
		Node qNode = edge.getNode(index);
		mem.push(qNode, node);
		boolean test = evaluator.test(filter, mem);
		mem.pop(qNode);
		return test;
	}
	
	
/*********************************************************************************
 * 
 * New version interprets regex directly with a stack 
 * 
 ********************************************************************************/
	
	
	/**
	 *  rewrite   ! (^ p)   as   ^ (! p)
	 *  rewrite   ^(p/q)    as   ^q/^p
	 */
	void eval(Regex exp, Path path, Node start, Node src){
//		System.out.println(exp);
		Record stack = new Record().push(exp);
		try {
			eval(stack, path, start, src, false);
		}
		catch (StackOverflowError e){
			logger.error("** Property Path Error: \n" + e);
		}
	}
	
	
	/**
	 * top of stack is current exp to eval
	 * rest of stack is in sequence
	 * path may be walked left to right if start is bound or right to left if end is bound
	 * in the later case,  index = 1
	 */
	void eval(Record stack, Path path, Node start, Node src, boolean inv){
		
		if (isStop) return;
		
		if (stack.isEmpty()){
			result(path, start, src);
			return;
		}
			
		Regex exp = stack.pop();
					
		switch (exp.retype()){
			
		case Regex.TEST: {
			// exp @[ ?this != <John> ]
			
			boolean b = true;
			
			if (start != null){
				b = test(exp.getExpr().getFilter(), path, regexNode, start);
			}
			
			if (b){
				eval(stack, path, start, src, inv);
			}
			stack.push(exp);
		}
		break;
		
			
		case Regex.LABEL:
		case Regex.NOT: {
			
			if (path.size() >= path.getMax()){
				stack.push(exp);
				return;
			}
			
			boolean inverse = exp.isInverse() || exp.isReverse();
			Producer pp = producer;
			List<Node> ff = from;
			Edge ee = edge;
			Environment env = memory;
			int ii = index, oo = other;
			int size = path.size();
			boolean 
				hasFilter = filter!=null,
				isStart   = start == null,
				hasSource = size == 0 && src == null && gNode != null,
				hasShort  = isShort;

			Visit visit = visited;
			Node gg = gNode;
						
			for (Entity ent : pp.getEdges(gg, ff, ee, env, exp, src, start, ii)){
				
				if (isStop) return;

				if (ent == null){
					continue;
				}
				
				//System.out.println(ent);
				
				Edge rel = ent.getEdge();
				Node node = rel.getNode(ii);

				if (inverse){
					rel = new EdgeInv(rel);	
					node = rel.getNode(ii);
				}
				
				if (hasFilter && isStart){
					// test a filter on the index node
					boolean test = test(rel.getNode(ii));
					if (! test) continue;
				}
				
				if (hasSource){
					// first time: bind the common source of current path
					src = ent.getGraph();
				}
				else if (src != null && ! ent.getGraph().same(src) ){ 
					// all relations need same source in one path
					continue;
				}				
								
				if (isStart){
					visit.start(node);
					if (hasShort) {
						producer.initPath(edge, 0);
					}
				}
				
				path.add(ent);
				eval(stack, path, rel.getNode(oo), src, inv);
				path.remove();
				
				if (isStart){
					visit.leave(node);
				}
			}
			
			stack.push(exp);
		}
		break;
		
		case Regex.SEQ: {
			
			int fst = 0, rst = 1;
			if (isReverse){
				// path walk from right to left
				// index = 1
				// hence sequence walk from right to left
				// use case: ?x p/q <uri>
				fst=1;
				rst=0;
			}

			stack.push(exp.getArg(rst));
			stack.push(exp.getArg(fst));
			
			eval(stack, path, start, src, inv);
			
			stack.pop();
			//stack.pop();
			stack.set(exp);
			
		}
		break;
		
		
		case Regex.PLUS:
			// exp+
			if (start == null && visited.knows(exp)){
				stack.push(exp);
				return;
			}
			plus(exp, stack, path, start, src, inv);
		break;
		
		
		case Regex.COUNT:
			// exp{1,n}
			count(exp, stack, path, start, src, inv);
			break;
		
			
		case Regex.STAR: 	
			// exp*
			if (start == null && visited.knows(exp)){
				stack.push(exp);
				return;
			}
			star(exp, stack, path, start, src, inv);			
		break;
		
		
		case Regex.ALT:
			
			stack.push(exp.getArg(0));
			eval(stack, path, start, src, inv);
			stack.pop();
			
			stack.push(exp.getArg(1));
			eval(stack, path, start, src, inv);
			stack.pop();
			
			stack.push(exp);
		break;
		
		
		case Regex.OPTION:
			
			eval(stack, path, start, src, inv);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src, inv);	
			
			stack.pop();
			stack.push(exp);
		break;
		
		}
		
	}
	
	
	
	Regex test(Regex exp){
		
		return exp;
	}
	
	
	int reverse(int i){
		switch (i){
		case 0: return 1;
		case 1: return 0;
		}
		return 0;
	}
	
	void result(Path path, Node start, Node src){
		if (path.size()>0){

			Mapping map = result(path, gNode, src, start, isReverse);
			//System.out.println("** PF: \n" + map);
			if (map != null){
				mbuffer.put(map, true);
			}
		}
		else {
			for (Entity ent : getStart(start, src)){
				
				if (isStop) return;
				
				if (ent != null){
					Node node = ent.getNode();
					if (gNode != null){ 
						src = ent.getGraph();
					}
					Mapping m = result(path, gNode, src, node, isReverse);

					if (m != null){
						mbuffer.put(m, true);
					}
				}
			}
		}
	}
	
	
	/**
	 * exp*
	 */
	void star(Regex exp, Record stack, Path path, Node start, Node src, boolean inv){
		
		if (visited.loop(exp, start)){
			// start already met in exp path: stop
			stack.push(exp);
			return;
		}		
		
		// use case: (p*/q)*
		// we must save each visited of p*
		// because it expands to p*/q . p*/q ...
		// and each occurrence of p* has its own visited 
		//List<Node> save = visited.unset(exp);
		//eval(stack, path, start, src);
		//visited.set(exp, save);

		// first step: zero length 
		eval(stack, path, start, src, inv);

		// restore exp*
		stack.push(exp);
		// push exp
		stack.push(exp.getArg(0));
		// second step: eval exp once more
		eval(stack, path, start, src, inv);
		// restore stack (exp* on top)
		stack.pop();

		visited.remove(exp, start);	
	}
	
	
	
	/**
	 * exp+
	 */
	void plus(Regex exp, Record stack, Path path, Node start, Node src, boolean inv){
		
		if (count(exp) == 0){
			// first step
			stack.push(exp);

			count(exp, +1);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src, inv);
			stack.pop();
			count(exp, -1);		
		}
		else {
			
			if (visited.loop(exp, start)){
				stack.push(exp);
				return;
			}
			
			// (1) leave
			eval(stack, path, start, src, inv);

			// (2) continue
			stack.push(exp);

			//count(exp, +1);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src, inv);
			stack.pop();
			//count(exp, -1);

			visited.remove(exp, start);		
		}
	}

	
	
	/**
	 * exp{n,m}
	 * exp{n,}
	 */
	void count(Regex exp, Record stack, Path path, Node start, Node src, boolean inv){
		
		if (count(exp) >= exp.getMin()){			
			
			if (! hasMax(exp) && visited.loop(exp, start)){
				stack.push(exp);
				return;
			}
			
			// min length is reached, can leave
			int save = count(exp);
			set(exp, 0);
			eval(stack, path, start, src, inv);
			set(exp, save);

			stack.push(exp);
			
			if (count(exp) < exp.getMax()){
				// max length not reached, can continue
				
				count(exp, +1);
				stack.push(exp.getArg(0));
				eval(stack, path, start, src, inv);
				stack.pop();
				count(exp, -1);

			}
			
			if (! hasMax(exp)) visited.remove(exp, start);		
			
		}
		else {
			// count(exp) < exp.getMin()
			
			if (isReverse && ! hasMax(exp)){
				// use case: ?x exp{2,} <uri>
				// path goes backward
				visited.insert(exp, start);
			}
			
			stack.push(exp);

			count(exp, +1);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src, inv);
			stack.pop();
			count(exp, -1);	
			
			if (isReverse && ! hasMax(exp)){
				// use case: ?x exp{2,} <uri>
				// path goes backward
				visited.remove(exp, start);
			}
			
		}
	} 
	
	
	int count(Regex exp){
		return visited.count(exp);
	}
	
	void count(Regex exp, int n){
		visited.count(exp, n);
	}
	
	void set(Regex exp, int n){
		visited.set(exp, n);
	}
	
	Regex star(Regex exp){
		return exp;
	}
	
	boolean hasMax(Regex exp){
		return exp.getMax()!=-1 && exp.getMax()!= Integer.MAX_VALUE;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
