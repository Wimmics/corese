package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgenv.eval.SQLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.EntityImpl;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.EdgeIterator;
import fr.inria.edelweiss.kgraph.core.Index;
import fr.inria.edelweiss.kgraph.core.NodeImpl;

/**
 * Producer
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class ProducerImpl implements Producer {
	static final int IGRAPH 	= Graph.IGRAPH;
	static final String TOPREL 	= Graph.TOPREL;
	
	List<Entity> empty = new ArrayList<Entity>();
	EdgeIterator ei;
	
	Graph graph, 
	// cache for handling (fun() as var) created Nodes
	local;
	Mapper mapper;
	MatcherImpl match;
	QueryEngine qengine;
	
	// if true, perform local match
	boolean isMatch = false;
	
	
	
	public ProducerImpl(){
		this(Graph.create());
	}
	
	public ProducerImpl(Graph g){
		graph = g;
		local = Graph.create();
		mapper = new Mapper(this);
		ei = EdgeIterator.create(g);
	}
	
	public static ProducerImpl create(Graph g){
		ProducerImpl p = new ProducerImpl(g);
		return p;
	}
	
	public void setMode(int n){
	}
	
	void setMatch(boolean b){
		isMatch = b;
	}
	
	boolean isMatch(){
		return isMatch;
	}
	
	public void set(Matcher m){
		if (m instanceof MatcherImpl){
			match = (MatcherImpl) m;
		}
	}
	
	public void set(QueryEngine qe){
		qengine = qe;
	}
	
	public Graph getGraph(){
		return graph;
	}
	
	public Graph getLocalGraph(){
		return local;
	}
	
	public void set(Mapper m){
		mapper = m;
	}
	
	Node getNode(Edge edge, Node gNode, int i){
		if (i == IGRAPH){
			return gNode;
		}
		else return edge.getNode(i);
	}
	
	Node getValue(Node qNode, Environment env){
		Node node = env.getNode(qNode);
		if (node == null && qNode.isConstant()){
			node = qNode;
		}
		return node;
	}
	
	
	boolean isType(Edge edge, Environment env){
		return graph.isType(edge) || env.getQuery().isRelax(edge);
	}
	
	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge,
			Environment env) {
					
		Node predicate = getPredicate(edge, env);
		if (predicate == null){
			return empty;
		}

		Node node = null, node2 = null;
		int n = 0;

		boolean isType = false;
		
		for (Index ei : graph.getIndexList()){
			// last index is IGRAPH
			
			int i = ei.getIndex();
			if (i < edge.nbNode()){
				// Edge has a node that is bound or constant ?
				Node qNode = getNode(edge, gNode, i);
				if (qNode!=null){
					if (i == 1 && 
						qNode.isConstant() && 
						isType(edge, env) && 
						graph.hasEntailment()){
						// RDFS entailment on ?x rdf:type c:Engineer
						// no dichotomy on c:Engineer to get subsumption
					}
					else {
						node = getValue(qNode, env);
						if (node != null){
							n = i;
							if (i == 0 && ! isType(edge, env)){
								node2 = getValue(edge.getNode(1), env);
							}
							break;
						}
					}
				}
			}
		}
						
		
		if (node == null  && from.size()>0){
			// from named <uri>
			// graph ?g { }
			// <<bind>> ?g to uri 
			if (gNode != null) {
				return complete(getEdgesFrom(predicate, from), gNode, edge, env);
			}
			else if (! isFromOK(from)){
				// from are unknown
				return 	empty;
			}
		}		
						
		Iterable<Entity> it = graph.getEdges(predicate, node, node2, n);
		
//		if (isType){
//			// deprecated
//			it = getTypeEdges(predicate, node, env);
//		}
//		else { //if (gNode != null || from.size()>0){ // || ! graph.hasDefault()){
//			it = graph.getEdges(predicate, node, node2, n);
//		}
//		else {
//			// deprecated
//			it = graph.getDefaultEdges(predicate, node, node2, n);
//			if (it == null){
//				return empty;
//			}
//			it = complete(it, edge, env);
//			return it;
//		}
		
		// check gNode/from/named
		it = complete(it, gNode, getNode(gNode, env), from);
		// in case of local Matcher
		it = complete(it, gNode, edge, env);
		return it;
	}
	
	
	/**
	 * Iterator of Entity that performs local match.match()
	 * Enable to have a local ontology in case of several graphs with local ontologies
	 */
	Iterable<Entity> complete(Iterable<Entity> it, Node gNode, Edge edge, Environment env){
		if (isMatch){
			MatchIterator mit = new MatchIterator(it, gNode, edge, graph, env, match);
			return mit;
		}
		else {
			return it;
		}
	}
	
	
	
	Node getNode(Node gNode, Environment env){
		if (gNode == null) return null;
		return env.getNode(gNode);
	}
	
	/**
	 * Iterate edges in from/named
	 * Retrieve edges from index IGRAPH with node bound to from
	 */
	Iterable<Entity> getEdgesFrom(Node predicate, List<Node> from){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();

		for (Node src : from){
			if (graph.isGraphNode(src)){
				src = graph.getGraphNode(src.getLabel());
				Iterable<Entity> it = graph.getEdges(predicate, src, IGRAPH);
				if (it!=null){
					meta.next(it);
				}
			}
		}

		if (meta.isEmpty()) return empty;
		else return meta;
	}
	
	
//	Iterable<Entity> getTypeEdges(Node predicate, Node type, Environment env){
//		MetaIterator<Entity> meta = new MetaIterator<Entity>();
//		
//		for (Node node : graph.getTypeNodes()){
//
//			if (node.same(type) || match.isSubClassOf(node, type, env)){
//				Iterable<Entity> it = graph.getEdges(predicate, node, 1);
//				if (it != null){
//					meta.next(it);
//				}
//			}
//		}
//		
//		if (meta.isEmpty()){
//			return empty;
//		}
//		
//		return meta;
//	}

	
	
	
	/**
	 * Check from/named
	 * if no gNode, eliminate duplicate successive edges
	 */
	Iterable<Entity> complete(Iterable<Entity> it, Node gNode, Node sNode,
			List<Node> from){
		if (it == null){
			it = empty;
		}
		else if (gNode == null){
			// eliminate similar edges
			// check from 
			it = new EdgeIterator(graph, it, from, false);
		}
		else if (from.size()>0 && sNode == null){
			// check from [named]			
			it = new EdgeIterator(graph, it, from, true);
		}
		return it;
	}

	
	/**
	 * Return Node that represents the predicate of the Edge 
	 */
	Node getPredicate(Edge edge, Environment env){
		Node var = edge.getEdgeVariable();
		Node predicate = edge.getEdgeNode();
		if (var != null){
			predicate = env.getNode(var);
			if (predicate == null){
				predicate = edge.getEdgeNode();
			}
		}
		String name = predicate.getLabel();
		if (! name.equals(TOPREL)){
			predicate = graph.getPropertyNode(name);
		}
		return predicate;
	}
		
	
	boolean isFromOK(List<Node> from){
		for (Node node : from){
			if (graph.isGraphNode(node)){
				return true;
			}
		}
		return false;
	}

	
	boolean isFrom(Node ent, List<Node> from){
		for (Node node : from){
			if (ent.same(node)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Edges for Property Path Regex 
	 */
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env,
			 Regex exp, Node src, Node start, int index) {
				
		if (start == null){
			Node qNode = edge.getNode(index);
			if (qNode.isConstant()){
				start = qNode;
			}
		}

		if (exp.isReverse()){
			// ?x ^p ?y
			// start node is now reverse(index) node
			index = (index==0)?1:0;
		}
		
		if (exp.isNot()){
			return getNegEdges(gNode, from, edge, env, exp, src, start, index);
		}
		
		Node predicate = graph.getPropertyNode(exp.getLongName());
		if (predicate == null){
			return empty;
		}
		
		// draft: computes edges with construct-where queries
//		if (qengine!=null){
//			return construct(start, exp, index);
//		}
		

		Iterable<Entity> it = graph.getEdges(predicate, start, null, index);
		
//		if (gNode != null || from.size()>0 || ! graph.hasDefault()){
//			it = graph.getEdges(predicate, start, null, index);
//		}
//		else {
//			it = graph.getDefaultEdges(predicate, start, null, index);			
//			return it;
//		}
		// gNode, from
		it = complete(it, gNode, src, from);
		return it;
	}

	
	
	// draft: computes edges with construct-where queries
	Iterable<Entity> construct(Node start, Regex exp, int index){
		Mappings map = qengine.process(start, exp.getLongName(), index);
		if (map == null || map.getGraph() == null) return empty;
		Graph g = (Graph) map.getGraph();
		g.prepare();
		return g.getEdges();		
	}

	
	/**
	 * regex is a negation
	 * ?x ! rdf:type ?y
	 * ?x !(rdf:type|rdfs:subClassOf) ?y
	 * enumerate properties but those in the negation
	 */
	public Iterable<Entity> getNegEdges(Node gNode, List<Node> from, Edge edge, Environment env,
		Regex exp, Node src, Node start, int index){
		
		exp = exp.getArg(0);
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		for (Node pred : graph.getProperties()){
			if (match(exp, pred)){
				// exclude
			}
			else {
				Iterable<Entity> it = graph.getEdges(pred, start, index);
				if (it != null){
					meta.next(it);
				}
			}
		}
		if (meta.isEmpty()) return empty;
		Iterable<Entity> it = complete(meta, gNode, src, from);
		return it;
	}
	
		
	boolean match(Regex exp, Node pred){
		if (exp.isConstant()){
			return exp.getLongName().equals(pred.getLabel());
		}
		for (int i = 0; i<exp.getArity(); i++){
			if (exp.getArg(i).getLongName().equals(pred.getLabel())){
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * use case:
	 * path finder requires all nodes for zero length path
	 * 
	 * TODO: env.isBound(gNode) is not thread safe
	 */
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge edge,
			Environment env, List<Regex> exp, int index) {

		Node node = edge.getNode(index);
		if (node.isConstant()){
			Node nn = graph.copy(node);
			ArrayList<Entity> list = new ArrayList<Entity>();
			list.add(EntityImpl.create(null, nn));
			return list;
		}
		else if (gNode == null){
			if (from.size()>0){
				return getNodes(gNode, from, env);
			}
			else {
				return graph.getAllNodes();
			}
		}
		else {
			if (env.isBound(gNode)){
				node = env.getNode(gNode);
				return graph.getNodes(node);
			}
			else if (gNode.isConstant()){
				node = graph.getGraphNode(gNode.getLabel());
				if (node != null){
					return graph.getNodes(node);
				}
			}
			else if (from.size() > 0){
				return getNodes(gNode, from, env);
			}
			else {
				return graph.getAllNodes();
			}
		}
		
		return new ArrayList<Entity>();
	}
	
	/**
	 * Does graph gNode contain node
	 * use case: 
	 * graph ?g {?x :p* ?y}
	 * ?g and ?x are bound
	 * 
	 */
	public boolean contains(Node gNode, Node node){
		return true;
	}
	
	/**
	 * Enumerate nodes from graphs in the list 
	 * gNode == null : from
	 * gNode != null : from named
	 */
	Iterable<Entity> getNodes(Node gNode, List<Node> from, Environment env){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		for (Node gn : getGraphNodes(gNode, from, env)){
			meta.next(graph.getNodes(gn));
		}
		if (meta.isEmpty()) return new ArrayList<Entity>();
		if (gNode == null){
			// eliminate duplicates
			return getNodes(meta);
		}
		return meta;
	}
	
	/**
	 * Use case:
	 * 
	 * from <g1>
	 * from <g2>
	 * ?x :p{*} ?y
	 * 
	 * enumerate nodes from g1 and g2 and eliminate duplicates
	 */
	Iterable<Entity> getNodes(final Iterable<Entity> iter){
		
		return new Iterable<Entity>(){

			public Iterator<Entity> iterator() {
				
				final Hashtable<Node, Node> table = new Hashtable<Node, Node>();
				final Iterator<Entity> it = iter.iterator();
				
				return new Iterator<Entity>(){

					public boolean hasNext() {
						return it.hasNext();
					}

					public Entity next() {
						while (hasNext()){
							Entity ent = it.next();
							if (ent == null) return null;
							if (! table.contains(ent.getNode())){
								table.put(ent.getNode(), ent.getNode());
								return ent;
							}
						}
						return null;
					}

					public void remove() {						
					}
					
				};
			}
			
		};
	}
	

	@Override
	public Iterable<Node> getGraphNodes(Node node, List<Node> from,
			Environment env) {
		// TODO check from
		if (from.size()>0) return getGraphNodes2(node, from, env);
		
		return graph.getGraphNodes();
	}
	
	
	public Iterable<Node> getGraphNodes2(Node node, final List<Node> from,
			Environment env) {
		
		List<Node> list = new ArrayList<Node>();
		for (Node nn : from){
			Node target = graph.getGraphNode(nn.getLabel());
			if (target!=null){
				list.add(target);
			}
		}
		
		return list;
	}
	

	public Node getNode(Object value) {
		// TODO Auto-generated method stub
		if (! (value instanceof IDatatype)){
			return null;
		}
		IDatatype dt = (IDatatype) value;
		Node node = graph.getNode(dt, false, false);
				
		if (node == null){
			if (dt.isBlank() && dt.getLabel().startsWith(Query.BPATH)){
				// blank generated for path node: do not store it
				return  NodeImpl.create(dt);
			}
			else {
				node = local.getNode(dt, true, true);
			}			
		}
		return node;
	}

	@Override
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode,
			Environment env) {
		// TODO Auto-generated method stub
		return empty;
	}


	@Override
	public void init(int nbNodes, int nbEdges) {
		// TODO Auto-generated method stub
		graph.init();
	}

	@Override
	public void initPath(Edge edge, int index) {
		//graph.initPath();
	}

	@Override
	public boolean isBindable(Node node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
		// TODO Auto-generated method stub
		Node node = env.getNode(gNode);
		if (! graph.isGraphNode(node)) return false;
		if (from.size()==0) return true;
		
		return ei.isFrom(from, node);
	}
	
	@Override
	public Mappings map(List<Node> nodes, Object object) {
		// TODO Auto-generated method stub
		if (object instanceof IDatatype){
			return map(nodes, (IDatatype) object);
		}
		else if (object instanceof SQLResult){
			// sql()
			Mappings lMap = mapper.sql(nodes, (SQLResult) object);
			return lMap;
		}
		else if (object instanceof Mappings){
			return map(nodes, (Mappings) object);
		}
		return new Mappings();
	}
	
	Mappings map(List<Node> lNodes, Mappings lMap){
		for (Mapping map : lMap){
			map.setNodes(lNodes);
		}
		return lMap;
	}
	
	
	Mappings map(List<Node> nodes, IDatatype dt){
		Node[] qNodes = new Node[nodes.size()];
		int i = 0;
		for (Node qNode : nodes){
			qNodes[i++] = qNode;
		}
		Mappings lMap = new Mappings();
		List<Node> lNode = toNodeList(dt);
		for (Node node : lNode){
			Node[] tNodes = new Node[1];
			tNodes[0] = node;
			Mapping map =  Mapping.create(qNodes, tNodes);
			lMap.add(map);
		}
		return lMap;
	}

	@Override
	public List<Node> toNodeList(Object obj){
		IDatatype dt = (IDatatype) obj;
		List<Node> list = new ArrayList<Node>();
		if (dt.isArray()){
			for (IDatatype dd : dt.getValues()){
				if (dd.isXMLLiteral() && dd.getLabel().startsWith("http://")){
					// try an URI
					dd = DatatypeMap.newResource(dd.getLabel());

				}
				list.add(getNode(dd));
			}
		}
		else {
			list.add(getNode(dt));
		}
		return list;
	}
	
	
	void filter(Environment env){
		// KGRAM exp for current edge
		Exp exp = env.getExp();
		List<String> lVar = new ArrayList<String>();
		List<Node> lNode = new ArrayList<Node>();
		
		for (Filter f : exp.getFilters()){
			// filters attached to current edge
			if (f.getExp().isExist()){
				// skip exists { PAT }
				continue;
			}
			
			// function exp.bind(f) tests whether current edge binds all variable of filter f
			System.out.println("** P: " + exp + " " + f + " " + exp.bind(f));
			
			for (String var : f.getVariables()){
				if (! lVar.contains(var)){
					Node node = env.getNode(var);
					if (node != null){
						lVar.add(var);
						lNode.add(node);
					}
				}
			}
		}
		
		System.out.print("bindings ");
		for (String var : lVar){
			System.out.print(var + " ");
		}
		System.out.println("{(");
		for (Node node : lNode){
			System.out.print(node + " ");
		}
		System.out.println(")}");

	}

}
