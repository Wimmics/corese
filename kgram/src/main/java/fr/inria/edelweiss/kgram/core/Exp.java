package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.ExpPattern;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Query.VString;

/**
 * KGRAM expressions
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Exp implements ExpType, ExpPattern, Iterable<Exp> {
	static Exp empty = new Exp(EMPTY);
	
	int type, index;
	// optional success
	boolean 
		// default status must be false (for order by desc())
		status = false, 
	// for WATCH: skip exp if reach CONTINUE/BACKJUMP
		skip,
		isFail = false,
		isPath = false,
		isFree = false,
		isAggregate = false;
	VExp args;
	Edge edge;
	Node node;
	List<Node> lNodes;
	Filter filter;
	List<Filter> lFilter;
	// min(?l, expGroupBy(?x, ?y))
	List<Exp> expGroupBy;
	// for UNION
	Stack stack;
	// for EXTERN 
	Object object;
	Regex regex;
	Exp next;

	
	int min=-1, max=-1;
	
	class VExp extends ArrayList<Exp> {}
	
	Exp(){
	}
	
	 Exp (int t){
		type = t;
		args = new VExp();
		lFilter = new ArrayList<Filter>();
	 }
	
	 Exp (int t, Exp e1, Exp e2){
		this(t);
		args.add(e1);
		args.add(e2);
	}
	
	 Exp (int t, Exp e){
		this(t);
		args.add(e);
	}
	
	public static Exp create(int t){
		return new Exp(t);
	}
	
	public static Exp create(int t, Exp e1, Exp e2){
		Exp e = create(t);
		e.add(e1);
		e.add(e2);
		return e;
	}
	
	public static Exp create(int t, Exp e1, Exp e2, Exp e3){
		Exp e = create(t);
		e.add(e1);
		e.add(e2);
		e.add(e3);
		return e;
	}
	
	public static Exp create(int t, Exp e1){
		Exp e = create(t);
		e.add(e1);
		return e;
	}
	
	public static Exp create(int t, Node n){
		Exp exp = create(t);
		exp.setNode(n);
		return exp;
	}
	
	public static Exp create(int t, Edge e){
		Exp exp = create(t);
		exp.setEdge(e);
		return exp;
	}
	
	public static Exp create(int t, Filter e){
		Exp exp = create(t);
		exp.setFilter(e);
		return exp;
	}
	
	
	
	
	public boolean hasArg(){
		return args.size()>0;
	}
	
	public int size(){
		return args.size();
	}
	
	public void add(Exp e){
		args.add(e);
	}
	
	public void add(Edge e){
		args.add(create(EDGE, e));
	}
	
	public void add(Node n){
		args.add(create(NODE, n));
	}
	
	public void add(Filter f){
		args.add(create(FILTER, f));
	}
	
	void set(int n, Exp e){
		args.set(n, e);
	}
	
	public Query getQuery(){
		return null;
	}
	
	public void insert(Exp e){
		if (type() == AND && e.type()==AND){
			for (Exp ee : e){
				insert(ee);
			}
		}
		else args.add(e);
	}
	
	public void add(int n, Exp e){
		args.add(n, e);
	}
	
	public boolean remove(Exp e){
		return args.remove(e);
	}
	
	/**
	 * Add a bind exp at the beginning this exp
	 * qnode will be bound with node in the stack 
	 * before processing this exp
	 */
	public Exp bind(Node qnode, Node node){
		Exp bind  = create(NODE, qnode);
		Exp value = create(NODE, node);
		bind.add(value);
		Exp and = this;
		if (this.type() != AND){
			and = create(AND, this);
		}
		and.add(0, bind);
		return and;
	}
	
	public String toString(){
		String str = title() + "{";
		if (edge != null){
			str += edge;
			if (size()>0) str += " ";
		}
		if (node != null){
			str += node + " ";
			if (size()>0) str += " ";
		}
		if (filter != null){
			str += filter;
			if (size()>0) str += " ";
		}

		if (type == WATCH || type == CONTINUE || type == BACKJUMP){
			// skip because loop
			//str += TITLE[type];
		}
		else for (Exp e : this){
			str += e + " ";
		}
		str += "}";
		return str;
	}
	
	String title(){
		return TITLE[type];
	}
	
	public void skip(boolean b) {
		// TODO Auto-generated method stub
		skip = b;
	}

	
	public boolean skip() {
		// TODO Auto-generated method stub
		return skip;
	}
	
	public void status(boolean b) {
		// TODO Auto-generated method stub
		status = b;
	}

	
	public boolean status() {
		// TODO Auto-generated method stub
		return status;
	}

	public void setFree(boolean b) {
		isFree = b;
	}
	
	public boolean isFree() {
		return isFree;
	}
	
	public int type() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setIndex(int n){
		index = n;
	}
	
	public boolean isFilter(){
		return type == FILTER;
	}
	
	public boolean isAggregate(){
		return isAggregate;
	}
	
	public void setAggregate(boolean b){
		isAggregate = b;
	}
	
	public boolean isNode(){
		return type == NODE;
	}
	
	public boolean isEdge(){
		return type == EDGE;
	}
	
	public boolean isOption(){
		return type == OPTION || type == OPTIONAL;
	}
	
	public boolean isGraph(){
		return type == GRAPH;
	}
	
	public boolean isUnion(){
		return type == UNION;
	}
	
	public boolean isQuery(){
		return type == QUERY;
	}
	
	public boolean isAtomic(){
		return 
		type == FILTER || type == EDGE || type == NODE ||
		type == ACCEPT;
	}
	
	public void setType(int n){
		type = n;
	}
	
	Exp getNext(){
		return next;
	}
	
	void setNext(Exp e){
		next = e;
	}

	public List<Exp> getExpList(){
		return args;
	}
	
	public Exp first() {
		if (args.size()>0) return args.get(0);
		else return empty;
	}
	
	
	public Exp rest() {
		if (args.size()>1) return args.get(1);
		else return null;
	}
	
	public Exp last() {
		if (args.size()>2) return args.get(2);
		else return empty;
	}
	
	
	public Iterator<Exp> iterator() {
		return args.iterator();
	}
	
	
	public Exp get(int n){
		return args.get(n);
	}
	
	
	public Edge getEdge(){
		return edge;
	}
	
	
	public void setEdge(Edge e){
		edge = e;
	}
	
	public Regex getRegex(){
		return regex;
	}
	
	public void setRegex(Regex f){
		regex = f;
	}
	
	public Filter getFilter(){
		return filter;
	}
	
	public void setFilter(Filter f){
		filter = f;
		if (f.isRecAggregate()) setAggregate(true);
	}
	
	void addFilter(Filter f){
		lFilter.add(f);
	}
	
	public List<Filter> getFilters(){
		return lFilter;
	}
	
	public boolean isExpGroupBy(){
		return expGroupBy!=null;
	}
	
	public void setExpGroupBy(List<Exp> l){
		expGroupBy = l;
	}
	
	public List<Exp> getExpGroupBy(){
		return expGroupBy;
	}
	
	public boolean isFail(){
		return isFail;
	}
	
	public void setFail(boolean b){
		 isFail = b;
	}
	
	public boolean isPath(){
		return type == PATH;
	}

	public void setPath(boolean b){
		isPath = b;
	}
	
	public Node getGraphName(){
		return first().first().getNode();
	}
	
	public Node getGraphNode(){
		return node;
	}
	
	public Node getNode() {
		return node;
	}

	
	public void setNode(Node n) {
		node = n;
	}
	
	public List<Node> getNodeList() {
		return lNodes;
	}
	
	public void setNodeList(List<Node> l){
		lNodes = l;
	}

	public void addNode(Node n) {
		if (lNodes == null) lNodes = new ArrayList<Node>();
		lNodes.add(n);
	}
	
	public void setObject(Object o){
		object = o;
	}
	
	public Object getObject(){
		return object;
	}
	
	public List<Object> getValues(){
		if (object instanceof List){
			return (List<Object>) object;
		}
		else return new ArrayList<Object>();
	}
	
	public void setMin(int i){
		min = i;
	}
	
	public int getMin(){
		return min;
	}
	
	public void setMax(int i){
		max = i;
	}
	
	public int getMax(){
		return max;
	}
	
	public Stack getStack() {
		return stack;
	}

	
	public void setStack(Stack st) {
		stack = st;
	}
	
	
//	public Node getNode(String label){
//		return getNode(label, true);
//	}
	
	/**
	 * isProper = true means do not go in sub query select node
	 * (and we never go in sub query body)
	 */
	public Node getNode(String label, boolean isProper){

		switch (type()){
		
		case NODE:
			if (getNode().getLabel().equals(label)){
				return getNode();
			}
			break;

		case EDGE:
		case PATH:
		case XPATH:
		case EVAL:
			for (int i=0; i<nbNode(); i++){ 
				Node node = getNode(i);
				if (node != null && node.getLabel().equals(label)){
					return node;
				}
			}

			break;
			
		case QUERY:
			if (isProper) break;
			
			Query query = getQuery();
			Exp exp = query.getSelectExp(label);
			if (exp != null) return exp.getNode();
			else return null;
			
		default:
			for (Exp ee : this){
				Node node = ee.getNode(label, isProper);
				if (node != null) return node;
			}
		}
		
		return null;
	}
	
	/**
	 * use case: select distinct ?x where
	 * add an ACCEPT ?x statement to check that
	 * ?x is new
	 */
	boolean distinct(Node qNode){
		switch(type()){

		case UNION:
		//case OPTION:
			boolean success = false;
			for (Exp ee : this){
				success =  ee.distinct(qNode) || success;
			}
			if (success){
				return true;
			}
			break;
		
		case AND: 
		case GRAPH:

			for (int i=0; i<size(); i++){
				Exp exp = get(i);
				switch(exp.type()){
				case EDGE:
				case PATH:
				case XPATH:
				case EVAL:
					//Edge edge = exp.getEdge();
					if (exp.contains(qNode)){
						add(i+1, Exp.create(ACCEPT, qNode));
						return true;
					}
				break;

				case AND:
				case GRAPH: 
				case UNION:
				//case OPTION:
					if (exp.distinct(qNode)){
						return true;
					}
					
				break;
				
				}
			}
		}

		return false;
	}
	

	
	boolean isSortable(){
		return isEdge() || isPath() || isGraph() || type == BIND ;
	}
	
	boolean isSimple(){
		switch(type){
		case EDGE: 
		case PATH:
		case EVAL:
		case BIND: return true;
		default: return false;
		}
	}
	
	
	
	/**
	 * Does edge e have a node bound by map (bindings)
	 */
	boolean bind(Mapping map){
		if (! isEdge()) return false;

		for (int i=0; i<nbNode(); i++){
			Node node = getNode(i);
			if (node.isVariable() && map.getNode(node) != null){
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * check special case:
	 * e1: ?x path ?y
	 * e2: graph path {}
	 * e2 cannot be moved before e1 
	 */
	boolean isGraphPath(Exp e1, Exp e2){
		if (e1.isPath() && e2.isGraph()){
			Node var1 = e1.getEdge().getEdgeVariable();
			Node var2 = e2.getGraphName();
			if (var1 != null && var2.isVariable() && var1.same(var2)){
				return true;
			}
		}
		return false;
	}
	
	
	

	public int nBind(List<Node> lNode, List<String> lVar, List<Exp> lBind){
		if (isSimple()){
			return count(lNode, lVar, lBind);
		}
		else {
			return gCount(lNode, lVar, lBind);
		}
	}
	
	int gCount(List<Node> lNode, List<String> lVar, List<Exp> lBind){
		int n = 0;
		List<Node> list = getNodes();
		for (Node node : list){
			n += member(node, lNode, lVar, lBind);
		}
		return n;
	}

	
	int count(List<Node> lNode, List<String> lVar, List<Exp> lBind){
		int n = 0;
		for (int i=0; i<nbNode(); i++){ 
			n += member(getNode(i), lNode, lVar, lBind);
		}
		return n;
	}
	
	int member(Node node, List<Node> lNode, List<String> lVar, List<Exp> lBind){
		if (node.isConstant()) return 1;
		if (member(node, lBind)){
			return 1;
		}
		if (lNode.contains(node) || lVar.contains(node.getLabel())){
			return 2;				
		}
		return 0;
	}
	
	boolean member(Node node, List<Exp> lBind){
		for (Exp exp : lBind){
			if (node.same(exp.first().getNode())){
				return true;
			}
		}
		return false;
		
	}
	
	/**
	 * list of nodes that are bound by this exp
	 * no minus and no exists
	 * 
	 */
	void bind(List<Node> lNode){
		if (isSimple()){
			for (int i=0; i<nbNode(); i++){ 
				Node node = getNode(i);
				if (node!=null){
					bind(node, lNode);
				}
			}
		}
		else {
			// TODO: check
			List<Node> list = getNodes();
			for (Node node : list){
				bind(node, lNode);
			}
		}
	}
	
	void bind(Node node, List<Node> lNode){
		if (! lNode.contains(node)){
			lNode.add(node);
		}
	}
	
	void addBind(List<String> lVar){
		switch (type()){
			case EDGE:
				for (int i=0; i<nbNode(); i++){ 
					Node node = getNode(i);
					if (node != null){
						addBind(node, lVar);
					}
				}
		}
	}
	
	void addBind(Node node, List<String> lVar){
		if (node.isVariable()){
			lVar.add(node.getLabel());
		}
	}
	
	/**
	 * for EDGE exp
	 * nodes + edgeNode
	 */
	int nbNode(){
		switch(type){
		
		case EDGE: 
		case PATH:
		case EVAL:
			if (edge.getEdgeVariable()==null) return edge.nbNode();
			else return edge.nbNode()+1;
		
		case BIND:
			return size();
		}
		
		return 0;
	}
	
	/**
	 * for EDGE exp
	 * nodes + edgeNode
	 */
	Node getNode(int n){
		switch(type){
		
		case EDGE: 		
		case PATH:
		case EVAL:
	
			if (n < edge.nbNode()) return edge.getNode(n);
			else return edge.getEdgeVariable();
			
		case BIND:
			return get(n).getNode();
		}
		return null;
	}
	
	/**
	 * for EDGE exp
	 * nodes + edgeNode
	 */
	public boolean contains(Node node){
		if (edge.contains(node)) return true;
		Node pNode = edge.getEdgeVariable();
		if (pNode == null) return false;
		return pNode == node;
	}
	
	
	/**
	 * 
	 * @param filterVar: variables of a filter
	 * @param expVar: list of variables bound by expressions
	 * Add in expVar the variables bound by this expression
	 * that are in filterVar
	 * bound means no optional, no union 
	 */
	public void share(List<String> filterVar, List<String> expVar){
		switch (type()){
		
			case FILTER: 
			case BIND:
				break; 
		
			case OPTION: 
				
			break;
			
			case UNION:
				// must be bound in both branches 
				ArrayList<String> lVar1 = new ArrayList<String> ();
				ArrayList<String> lVar2 = new ArrayList<String> ();
				first().share(filterVar, lVar1);
				 rest().share(filterVar, lVar2);
				for (String var : lVar1){
					if (lVar2.contains(var) && ! expVar.contains(var)){
						expVar.add(var);
					}
				}

				break;
				
			case QUERY:
				ArrayList<String> lVar = new ArrayList<String> ();
				getQuery().getBody().share(filterVar, lVar);
				
				for (Exp exp : getQuery().getSelectFun()){
					String name = exp.getNode().getLabel();
					if ((lVar.contains(name) || exp.getFilter()!=null) && ! expVar.contains(name)){
						expVar.add(name);
					}
				}
				break;
				
			case EDGE:
			case PATH:
				for (int i=0; i<nbNode(); i++){ 
					Node node = getNode(i);
					share(node, filterVar, expVar);
				}				
				break;
				
			case NODE:
				share(getNode(), filterVar, expVar);
				break;
				
			case MINUS:
			case OPTIONAL:
				first().share(filterVar, expVar);
				break;
				
			default:
				
				for (Exp exp : this){
					exp.share(filterVar, expVar);
				}
			
		}
		
	}
	
	
	void share(Node node, List<String> fVar, List<String> eVar){
		if (node != null && node.isVariable() &&
				fVar.contains(node.getLabel()) &&
				! eVar.contains(node.getLabel())){
			eVar.add(node.getLabel());
		}
	}
	
	public boolean bound(List<String> fvec, List<String> evec){
		for (String var : fvec){
			if (! evec.contains(var)){
				return false;
			}
		}
		return true;
	}
	
	public boolean bind(Filter f){
		List<String> lVar = f.getVariables();
		List<String> lVarExp = new ArrayList<String>();
		share(lVar, lVarExp);
		return bound(lVar, lVarExp);
	}
	
	
	/**
	 * Return variable nodes of this exp 
	 * use case: find the variables for select *
	 * PRAGMA: 
	 * subquery  :  return only the nodes of the select
	 * return only variables (no cst, no blanks)
	 * minus: return only nodes of first argument
	 */
	void getNodes(List<Node> lNode, List<Node> lSelNode, List<Node> lExistNode){

		switch (type()){
		
		case FILTER:
			// get exists {} nodes
			// draft
			getExistNodes(getFilter().getExp(), lExistNode);
			break;
		
		case NODE:
			add(lNode, getNode());
			break ;

		case EDGE:
		case PATH:
		case XPATH:
		case EVAL:
			for (int i=0; i<nbNode(); i++){ 
				Node node = getNode(i);
				add(lNode, node);
			}			
			break ;
			
		case MINUS:
			// second argument does not bind anything: skip it
			if (first() != null) first().getNodes(lNode, lSelNode, lExistNode);
			break;
			
		case QUERY:

			// use case: select * where { {select ?y fun() as ?var where {}} }
			// we want ?y & ?var for select *			
			for (Exp ee : getQuery().getSelectFun()){
				add(lSelNode, ee.getNode());
			}
			break ;
			
			
		default:
			for (Exp ee : this){
				ee.getNodes(lNode, lSelNode, lExistNode);
			}
		}
		
	}
	
	/**
	 * For modularity reasons, Pattern is stored as ExpPattern interface
	 */
	public Exp getPattern(Expr exp){
		return (Exp) exp.getPattern();
	}
	
	/**
	 * This is a filter
	 * get exists{} nodes if any
	 */
	void getExistNodes(Expr exp, List<Node> lExistNode){
		switch (exp.oper()){
		
		case ExprType.EXIST:
			Exp pat = getPattern(exp);
			List<Node> lNode = pat.getNodes(true);
			for (Node node : lNode){
				add(lExistNode, node);
			}
			break;
		
		default:
			for (Expr ee : exp.getExpList()){
				getExistNodes(ee, lExistNode);
			}
				
		}
	}
	
	
	/**
	 * select *
	 * does not return nodes of second arg of minus
	 * TODO:
	 * check this
	 */
	public List<Node> getNodes(){
		return getNodes(false);
	}
	
	List<Node> getNodes(boolean exist){
		List<Node> lNode    	= new ArrayList<Node>();
		List<Node> lSelNode 	= new ArrayList<Node>();
		List<Node> lExistNode 	= new ArrayList<Node>();

		// go inside query:
		if (isQuery()){
			for (Exp exp : this){
				exp.getNodes(lNode, lSelNode, lExistNode);
			}
		}
		else {
			getNodes(lNode, lSelNode, lExistNode);
		}
		
		
		// add select nodes that are not in lNode
		for (Node qNode : lSelNode){
			if (! contains(lNode, qNode)){
				if (contains(lExistNode, qNode)){
					/**
					 * use case:
					 * select * where {
					 *  {select * where {?x rdf:rest* /rdf:first ?y}} 
					 *  filter(! exists{?x rdf:first ?y}) 
					 * }
					 * lNode = {}
					 * lSelNode = {?x, ?y}
					 * lExistNode = {?x, ?y}
					 * The result of sub query is bound to exists nodes in order to join
					 */
					Node node = get(lExistNode, qNode);
					lNode.add(node);
				}
				else {
					lNode.add(qNode);
				}
			}
		}
		
		if (exist){
			// collect exists {} nodes
			for (Node qNode : lExistNode){
				if (! contains(lNode, qNode)){
					lNode.add(qNode);
				}
			}
		}
		return lNode;
	}
	
	
	void add(List<Node> lNode, Node node){
		if (node != null  && node.isVariable() && ! node.isBlank() && ! contains(lNode, node)){
			lNode.add(node);
		}
	}
	
	boolean contains(List<Node> lNode, Node node){
		for (Node qNode : lNode){
			if (qNode.getLabel().equals(node.getLabel())){
				return true;
			}
		}
		return false;
	}
	
	boolean contain(List<Exp> lExp, Node node){
		for (Exp exp : lExp){
			if (exp.getNode().getLabel().equals(node.getLabel())){
				return true;
			}
		}
		return false;
	}
	
	Node get(List<Node> lNode, Node node){
		for (Node qNode : lNode){
			if (qNode.getLabel().equals(node.getLabel())){
				return qNode;
			}
		}
		return null;
	}
		
	/**
	 * compute the variable list
	 * use case:
	 * filter(exists {?x ?p ?y})
	 * no minus
	 */
	public void getVariables(List<String> list){
		List<Node> lNode = getNodes();
		for (Node node : lNode){
			String name = node.getLabel();
			if (! list.contains(name)){
				list.add(name);
			}
		}
		// go into filters if any
		getFilterVar(list);
	}
	
	
	public void getFilterVar(List<String> list){
		switch(type){
		
		case FILTER:
			List<String> lVar = getFilter().getVariables();
			for (String var : lVar){
				if (! list.contains(var)){
					list.add(var);
				}
			}
			break;

		default: 
			for (Exp exp : getExpList()){
				exp.getFilterVar(list);
			}
		
		}
	}
	
	
	
	
	/**
	 * Add BIND ?x = ?y
	 */
	List<Exp> varBind(){
		List<Exp> lBind = new ArrayList<Exp>();
		for (int i = 0; i<size(); i++){
			Exp f = get(i);
			if (f.isFilter() && f.size()>0){
				Exp bind = f.first();
				if (bind.type() == BIND){
					if (bind.size() == 2){
						// filter has BIND
						add(i, bind);
						i++;
					}
					else {
						lBind.add(bind);
					}
				}
			}
		}
		return lBind;
	}

	/**
	 * If a filter carry a bind, set the bind before filter (and before edge)
	 */
	void cstBind(){
		for (int i=1; i<size(); i++){
			Exp f = get(i);
			if (f.isFilter() && f.size()>0){
				Exp bind = f.first();
				if (bind.type() == BIND && ! getExpList().contains(bind)){
					int j = i-1;
					while (j>0 && get(j).isFilter()){
						j--;
					}
					if (j>=0){
						Exp g = get(j);
						if (g.isEdge() || g.isPath()){
							bind.status(true);
							// add bind before edge:
							add(j, bind);
							// skip the added bind Exp:
							i++;
						}
					}
				}
			}
		}
	}
	
	
	void cstBind2(){
		for (int i=1; i<size(); i++){
			Exp f = get(i);
			if (f.isFilter() && f.size()>0){
				Exp g = get(i-1);
				Exp bind = f.first();
				if (bind.type() == BIND && 
					(g.isEdge() || g.isPath()) &&
					! getExpList().contains(bind)){
					
					bind.status(true);
					// add bind before edge:
					add(i-1, bind);
					// skip the added bind Exp:
					i++;
				}
			}
		}
	}
	
	
	
	
	/**
	 * graph ?g {} filter(f(?g))
	 * add filter to GRAPHNODE(?g, FILTER(f(?g))
	 *  
	 */
	void graphFilter(){
		for (int i=0; i<size(); i++){
			Exp f = get(i);
			if (f.isFilter() && i>=1 && get(i-1).isGraph()){
				Exp graph = get(i-1);
				Node gNode = graph.getGraphName();
				if (match(gNode, f.getFilter())){
					graph.first().add(f);
				}
			}
		}
	}
	
	
	boolean match(Node node, Filter f){
		if (! node.isVariable()) return false;
		List<String> lVar = f.getVariables();
		if (lVar.size()!=1) return false;
		return lVar.get(0).equals(node.getLabel());
	}
	
	
	/**
	 * use case:
	 * 
	 * ?x c:FirstName ?n
	 * filter(?n < 'B')
	 * 
	 * ?x c:FirstName ?n
	 * filter(?n < ?n1)
	 */
	void edgeFilter(){
		for (int i=0; i<size(); i++){
			Exp f = get(i);
			if (f.isFilter() && f.size()>0 && f.get(0).type() == TEST && 
					i>=1 && get(i-1).isEdge()){
				Exp edge = get(i-1);
				if (match(edge, f)){
					edge.add(f);
				}
			}
		}
	}
	
	/**
	 * ?x c:FirstName ?n
	 * ?n < ?n1
	 */
	boolean match(Exp edge, Exp filter){
		Exp test = filter.get(0);
		for (Exp exp : test){
			Node node = exp.getNode();
			if (edge.contains(node)){
				int indexNode = edge.indexNode(node);
				int indexVar = filter.indexVar(node);
				test.setIndex(indexNode);
				test.setObject(indexVar);
				test.setNode(node);
//				if (check(filter, indexVar)){
//					filter.status(true);
//				}
				filter.status(true);
				test.status(order(filter, indexVar));
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * this is FILTER with TEST ?x < ?y
	 */
	public int oper(){
		int ope = getFilter().getExp().oper();
		return ope;
	}
	
	boolean check(Exp filter, int index){
		int oper = filter.oper();
		if (oper == ExprType.LT || oper == ExprType.LE){
			return index == 0;
		}
		else if (oper == ExprType.GT || oper == ExprType.GE){
				return index == 1;
		}
		return false;
	}
	
	boolean order(Exp filter, int index){
		int oper = filter.oper();
		if (oper == ExprType.LT || oper == ExprType.LE){
			return index == 0;
		}
		else if (oper == ExprType.GT || oper == ExprType.GE){
			return index == 1;
		}
		return true;
	}
	
	/**
	 * index of Node in Edge
	 */
	public int indexNode(Node node){
		if (! isEdge()) return -1;
		for (int i = 0; i < nbNode(); i++){
			if (node.same(getNode(i))){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * index of node in FILTER ?x < ?y
	 */
	public int indexVar(Node node){
		Expr ee = getFilter().getExp();
		String name = node.getLabel();
		for (int i=0; i<2; i++){
			if (ee.getExp(i).type() == ExprType.VARIABLE && 
				ee.getExp(i).getLabel().equals(name)){
				return i;
			}
		}
		return -1;
	}
	
	
	
	
	
	
	

}
