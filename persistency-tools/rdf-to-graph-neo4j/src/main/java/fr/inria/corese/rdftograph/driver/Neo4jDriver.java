/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.neo4j.graphdb.RelationshipType;
import org.openrdf.model.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.FileWriter;
import java.util.StringJoiner;
import java.util.logging.Level;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.as;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

/**
 * @author edemairy
 */
public class Neo4jDriver extends GdbDriver {

	private final FileWriter fw;
	private static final Logger LOGGER = Logger.getLogger(Neo4jDriver.class.getName());
	private static final String VAR_CST = "?_bgpv_";
	private static final String VAR_PRED = "?_bgpe_";

	private static final String VERTEX = RDF_VERTEX_LABEL;
	private static final String VALUE = VERTEX_VALUE;

	private static final int S_P_O = 0;
	private static final int S_P_TO = 1;
	private static final int S_TP_O = 2;
	private static final int S_TP_TO = 3;

	private static final int TS_P_O = 4;
	private static final int TS_P_TO = 5;
	private static final int TS_TP_O = 6;
	private static final int TS_TP_TO = 7;

	SPARQL2Tinkerpop sp2t;
	Map<String, Object> alreadySeen = new HashMap<>();

	public Neo4jDriver() throws IOException {
		super();
		sp2t = new SPARQL2Tinkerpop();
		fw = new FileWriter("/Users/edemairy/tmp/script");
	}

	@Override
	public Graph openDatabase(String databasePath) {
		LOGGER.entering(getClass().getName(), "openDatabase");
		g = Neo4jGraph.open(databasePath);
		return g;
	}

	@Override
	public Graph createDatabase(String databasePath) throws IOException {
		LOGGER.entering(getClass().getName(), "createDatabase");
		super.createDatabase(databasePath);
		try {
			g = Neo4jGraph.open(databasePath);
//           getNeo4jGraph().cypher(String.format("CREATE INDEX ON :%s(%s, %s, %s, %s)", RDF_EDGE_LABEL, EDGE_S, EDGE_P, EDGE_O, EDGE_G));
//			String[] edges = {EDGE_S, EDGE_P, EDGE_O, EDGE_G};
//			for (int i = 1; i < ((1 << edges.length) - 1); i++) {
//				StringJoiner joiner = new StringJoiner(",");
//				StringBuilder indexCreation = new StringBuilder("CREATE INDEX ON :").append(RDF_EDGE_LABEL).append("(");
//				int nbEdges = 0;
//				for (int e = 0; e < edges.length; e++) {
//					if ((i & (1 << e)) != 0) {
//						nbEdges++;
//						joiner.add(edges[e]);
//					}
//				}
//				indexCreation.append(joiner.toString());
//				indexCreation.append(")");
//				if (nbEdges <= 2) {
//					Logger.getGlobal().log(Level.INFO, "Cypher: {0}", indexCreation.toString());
//					getNeo4jGraph().cypher(indexCreation.toString());
//				}
//			}
//			getNeo4jGraph().cypher(String.format("CREATE INDEX ON :edge_value_p(value)"));	
//			getNeo4jGraph().cypher(String.format("CREATE INDEX ON :edge(%s)", EDGE_P));
//			getNeo4jGraph().cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_VERTEX_LABEL, VERTEX_VALUE));
//			getNeo4jGraph().cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_VERTEX_LABEL, KIND));
//			getNeo4jGraph().cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_VERTEX_LABEL, TYPE));
//			for (String edge : edges) {
//				getNeo4jGraph().cypher(String.format("CREATE CONSTRAINT ON (e:%s) ASSERT exists(e.%s)", RDF_VERTEX_LABEL, edge));
//			}
//			getNeo4jGraph().cypher(String.format("CREATE CONSTRAINT ON (n:%s) ASSERT (n.%s, n.%s, n.%s, n.%s) IS NODE KEY", RDF_EDGE_LABEL, EDGE_S, EDGE_P, EDGE_O, EDGE_G));
			g.tx().commit();
			return g;
		} catch (Exception e) {
			LOGGER.severe(e.toString());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void closeDatabase() throws Exception {
		LOGGER.entering(getClass().getName(), "closeDatabase");
		try {

			g.tx().commit();
			while (g.tx().isOpen()) {
				g.tx().commit();
			}
		} finally {
			g.close();
		}
	}

	@Override
	public boolean isGraphNode(String label) {
		return g.traversal().V().hasLabel(RDF_EDGE_LABEL).has(EDGE_G, label).hasNext();
	}

	@Override
	public Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties) {
		Object result;

		Vertex vSource = createOrGetNode(sourceId);
		Vertex vObject = createOrGetNode(objectId);

		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(EDGE_S);
		p.add(makeSafeValue(sourceId.stringValue()));
		p.add(EDGE_P);
		p.add(predicate);
		p.add(EDGE_O);
		p.add(makeSafeValue(objectId.stringValue()));
		p.add(T.label);
		p.add(RDF_EDGE_LABEL);

		Vertex e = g.addVertex(p.toArray());
		e.addEdge(SUBJECT_EDGE, vSource);
		e.addEdge(OBJECT_EDGE, vObject);

		vSource.addEdge("edge", vObject, EDGE_P, predicate, EDGE_G, properties.getOrDefault(EDGE_G, ""));
		result = e.id();
//		getNeo4jGraph().cypher(String.format("merge (ed:edge_value_p{value:\"%s\"});", predicate));//CREATE INDEX ON :edge(%s)", EDGE_P));
//		getNeo4jGraph().cypher(String.format("match (ed:edge_value_p{value:\"%s\"}),(e:rdf_edge{s_value:\"%s\",p_value:\"%s\",o_value:\"%s\",g_value:\"%s\"}) create (ed)-[:linked]->(e);", predicate, sourceId.stringValue(), predicate, objectId.stringValue(), properties.getOrDefault(EDGE_G, "")));
		return result;
	}

	private String makeSafeValue(String value) {
		if (value.length() >= MAX_INDEXABLE_LENGTH) {
			return Integer.toString(value.hashCode());
		} else {
			return value;
		}
	}

	@Override
	public void commit() {
		g.tx().commit();
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(String key, String s, String p, String o, String g) {
		return getFilter(null, key, s, p, o, g);
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(Exp exp, String key, String s, String p, String o, String g) {
		Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> filter;
		switch (key) {
			case "GSPO":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_G, g).has(EDGE_S, s).has(EDGE_P, p).has(EDGE_O, o);
				break;
			case "GSP?o":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_G, g).has(EDGE_S, s).has(EDGE_P, p);
				break;
			case "?g?sPO":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_O, o).has(EDGE_P, p);
				break;
			case "?g?sP?o":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_P, p);
				break;
			case "?g?s?pO":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_O, o);
				break;
			case "?gSPO":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p).has(EDGE_O, o);
				break;
			case "?gSP?o":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p);
				break;
			case "?gS?pO":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_O, o);
				break;
			case "?gS?p?o":
				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s);
				break;
			case "G?sP?o":
				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
				break;
			case "?g?s?p?o":
			default:
				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL);
		}
		return filter;
	}

	int getKey(DatatypeValue s, DatatypeValue p, DatatypeValue o) {
		int key = 0;
		key += (o == null) ? 0 : 1;
		key += (p == null) ? 0 : 10;
		key += (s == null) ? 0 : 100;
		return key;
	}

	String getKeyString(DatatypeValue s, DatatypeValue p, DatatypeValue o) {
		StringBuilder sb = new StringBuilder();
		sb.append((s == null) ? "?s" : "S");
		sb.append((p == null) ? "?p" : "P");
		sb.append((o == null) ? "?o" : "O");
		return sb.toString();
	}

	P getPredicate(DatatypeValue dt) {
		if (dt == null) {
			return P.test(SPARQL2Tinkerpop.atrue, "");
		}
		return P.eq(dt.stringValue());
	}

	GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(GraphTraversal<? extends Element, ? extends Element> p) {
		return getVertexPredicate(p, null);
	}

	GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(GraphTraversal<? extends Element, ? extends Element> p, DatatypeValue dt) {
		if (p == null) {
			if (dt == null) {
				return __.has(VALUE, P.test(SPARQL2Tinkerpop.atrue, ""));
			} else {
				return getVertexPredicate(dt);
			}
		}
		return p;
	}

	GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(DatatypeValue dt) {
		return sp2t.getVertexPredicate(P.eq(dt.stringValue()), dt);
	}

	GraphTraversal<? extends Element, ? extends Element> getEdgePredicate(GraphTraversal<? extends Element, ? extends Element> p) {
		return getEdgePredicate(p, null);
	}

	GraphTraversal<? extends Element, ? extends Element> getEdgePredicate(GraphTraversal<? extends Element, ? extends Element> p, DatatypeValue dt) {
		if (p != null) {
			return p;
		}
		return __.has(EDGE_P, getPredicate(dt));
	}

	GraphTraversal<? extends Element, ? extends Element> getEdgePredicateOpt(GraphTraversal<? extends Element, ? extends Element> p, DatatypeValue dt) {
		if (p != null) {
			return p;
		}
		if (dt != null) {
			return __.has(EDGE_P, getPredicate(dt));
		}
		return null;
	}

	GraphTraversal<? extends Element, ? extends Element> getPredicate(Exp exp, int index) {
		GraphTraversal<? extends Element, ? extends Element> p = sp2t.getPredicate(exp, index);
		fr.inria.edelweiss.kgram.api.core.Node node = exp.getEdge().getNode(index);
		DatatypeValue dt = (node.isConstant()) ? node.getDatatypeValue() : null;
		if (p == null && dt != null) {
			p = getVertexPredicate(p, dt);
		}
		return p;
	}

	/**
	 * Implements getMappings by returning Iterator<Map<String, Vertex>>
	 * Generate a Tinkerpop BGP query
	 *
	 * @param exp is a BGP
	 * @return TODO getFilter constant in first edge complete getEdge
	 * factorize constant and filter in getEdge
	 */
	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends Element, Map<String, Object>>> getFilter(Exp exp) {

		GraphTraversal<? extends Element, ? extends Element> ps = getPredicate(exp.get(0), Exp.SUBJECT);
		GraphTraversal<? extends Element, ? extends Element> po = getPredicate(exp.get(0), Exp.OBJECT);
		GraphTraversal<? extends Element, ? extends Element> pt = (po == null) ? ps : po;

		ArrayList<GraphTraversal> edgeList = new ArrayList<>();
		VariableTable varList = new VariableTable();
		int i = 0;
		// swap = true:
		// first edge pattern starts with object because there is a filter on object
		boolean swap = po != null;
		if (exp.isDebug()) {
			System.out.println("Neo fst predicate: " + pt + " swap: " + swap);
		}
		for (Exp e : exp.getExpList()) {
			if (e.isEdge()) {
				edgeList.add(getEdge(exp, e, varList, i++, swap));
				swap = false;
			}
		}

		GraphTraversal[] query = new GraphTraversal[edgeList.size()];
		edgeList.toArray(query);
		String[] select = new String[varList.getList().size()];
		varList.getList().toArray(select);
		int limit = (exp.getExternQuery() == null) ? Integer.MAX_VALUE : exp.getExternQuery().getLimit();

		switch (varList.getList().size()) {
			case 1:
				return t -> {
					return t.V().hasLabel(VERTEX).match(query).limit(limit).select(varList.get(0));
				};

			default:

				if (pt == null) {
					return t -> {
						return t.V().hasLabel(VERTEX).match(query).limit(limit).select(varList.get(0), varList.get(1), select);
					};
				} else {
					return t -> {
						return t.V().hasLabel(VERTEX).where(pt).match(query).limit(limit).select(varList.get(0), varList.get(1), select);
					};
				}
		}
	}

	/**
	 * ?x p ?x compiled as: ?x p ?xx where(P.eq(?x)) ?x p ?y ?y q ?x
	 * compiled as: ?x p ?y ?y q ?xx where(P.eq(?x))
	 */
	GraphTraversal getEdge(Exp body, Exp exp, VariableTable varList, int n, boolean swap) {
		fr.inria.edelweiss.kgram.api.core.Edge edge = exp.getEdge();
		fr.inria.edelweiss.kgram.api.core.Node ns = edge.getNode(0);
		fr.inria.edelweiss.kgram.api.core.Node no = edge.getNode(1);
		fr.inria.edelweiss.kgram.api.core.Node np = edge.getPredicate();

		String s = varList.varName(ns, n, 0);
		String o = varList.varName(no, n, 1);
		String p = propertyName(np, n);

		boolean duplicate = false;
		boolean same = ns.isVariable() && no.isVariable() && ns.equals(no);
		P p2 = null;

		if (same) {
			// ?x p ?x
			// -> ?x ?p ?x_x . where(P.eq(?x))
			o = varNameSame(no, n);
			p2 = P.eq(s);
		} else if (varList.contains(o)) {
			// select start node: subject or object according to which is already bound
			// in previous triples
			if (varList.contains(s)) {

				if (hasSubject(body, ns, n)) {
					// subject and object are bound and subject was already a subject in a previous triple
					// Tinkerpop requires to travel from object to subject in this case
					// triple pattern from object to subject
					swap = true;
				}

				duplicate = true;
				// subject and object are bound,
				// rename snd node variable and generate P.eq(node)
				if (swap) {
					// o p s
					p2 = P.eq(s);
					s = varNameDuplicate(s, n);
				} else {
					// s p o
					p2 = P.eq(o);
					o = varNameDuplicate(o, n);
				}
			} else {
				// triple pattern from object to subject
				swap = true;
			}
		}

		varList.select(s);
		varList.select(o);
		varList.select(p);

		GraphTraversal ps = getPredicate(exp, Exp.SUBJECT);
		GraphTraversal po = getPredicate(exp, Exp.OBJECT);
		GraphTraversal pp = getEdgePredicateOpt(sp2t.getPredicate(exp, Exp.PREDICATE), getValue(np));

		int kind = getKind(ps, pp, po);

		System.out.println("Neo: " + kind);

		if (same || duplicate) {
			if (swap) {
				switch (kind) {
					case TS_P_O:
					case S_P_O:
						return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX).where(p2);
					case TS_P_TO:
					case S_P_TO:
						return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX).where(p2);
					case TS_TP_O:
					case S_TP_O:
						return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX).where(p2);
					case TS_TP_TO:
					case S_TP_TO:
					default:
						return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX).where(p2);
				}
			} else {
				switch (kind) {
					case S_P_O:
					case S_P_TO:
						return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX).where(p2);
					case S_TP_O:
					case S_TP_TO:
						return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX).where(p2);
					case TS_P_O:
					case TS_P_TO:
						return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX).where(p2);
					case TS_TP_O:
					case TS_TP_TO:
					default:
						return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX).where(p2);
				}
			}
		} else if (swap) {
			switch (kind) {
				case S_P_O:
					System.out.println("Neo: " + p);
					return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX);
				case S_P_TO:
					return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX);
				case S_TP_O:
					return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX);
				case S_TP_TO:
					return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX);
				case TS_P_O:
					return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX).where(ps);
				case TS_P_TO:
					return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).outV().as(s).hasLabel(VERTEX).where(ps);
				case TS_TP_O:
					return as(o).hasLabel(VERTEX).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX).where(ps);
				case TS_TP_TO:
				default:
					return as(o).hasLabel(VERTEX).where(po).inE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).outV().as(s).hasLabel(VERTEX).where(ps);
			}
		} else {

			switch (kind) {
				case S_P_O:
					return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX);
				case S_P_TO:
					return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX).where(po);
				case S_TP_O:
					return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX);
				case S_TP_TO:
					return as(s).hasLabel(VERTEX).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX).where(po);
				case TS_P_O:
					return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX);
				case TS_P_TO:
					return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).inV().as(o).hasLabel(VERTEX).where(po);
				case TS_TP_O:
					return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX);
				case TS_TP_TO:
				default:
					return as(s).hasLabel(VERTEX).where(ps).outE().hasLabel(RDF_EDGE_LABEL).as(p).where(pp).inV().as(o).hasLabel(VERTEX).where(po);
			}
		}
	}

	int getKind(GraphTraversal ps, GraphTraversal pp, GraphTraversal po) {
		if (ps == null) {
			if (pp == null) {
				if (po == null) {
					return S_P_O;
				} else {
					return S_P_TO;
				}
			} else {
				if (po == null) {
					return S_TP_O;
				} else {
					return S_TP_TO;
				}
			}
		} else if (pp == null) {
			if (po == null) {
				return TS_P_O;
			} else {
				return TS_P_TO;
			}
		} else if (po == null) {
			return TS_TP_O;
		} else {
			return TS_TP_TO;
		}
	}

	DatatypeValue getValue(fr.inria.edelweiss.kgram.api.core.Node node) {
		return (node.isVariable()) ? null : node.getDatatypeValue();
	}

	boolean hasSubject(Exp exp, fr.inria.edelweiss.kgram.api.core.Node s, int n) {
		for (int i = 0; i < n; i++) {
			if (exp.get(i).isEdge()) {
				if (s.equals(exp.get(i).getEdge().getNode(0))) {
					return true;
				}
			}
		}
		return false;
	}

	String propertyName(fr.inria.edelweiss.kgram.api.core.Node node, int n) {
		if (node.isVariable()) {
			return node.getLabel();
		}
		return VAR_PRED.concat(Integer.toString(n));
	}

	String varNameSame(fr.inria.edelweiss.kgram.api.core.Node node, int n) {
		return VAR_CST.concat(node.getLabel()).concat("_").concat(Integer.toString(n));
	}

	String varNameDuplicate(String var, int n) {
		return VAR_CST.concat(var).concat(var).concat(Integer.toString(n));
	}

	/**
	 * Exploir relevant filters for edge exp = Exp(EDGE)
	 */
	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>>
		getFilter(Exp exp, DatatypeValue dts, DatatypeValue dtp, DatatypeValue dto, DatatypeValue dtg) {
		Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> filter;

		String s = (dts == null) ? "?s" : dts.stringValue();
		String p = (dtp == null) ? "?p" : dtp.stringValue();
		String o = (dto == null) ? "?o" : dto.stringValue();
		String g = (dtg == null) ? "?g" : dtg.stringValue();

		//System.out.println(getKeyString(dts, dtp, dto));
		switch (getKeyString(dts, dtp, dto)) {

			case "?sPO":
			case "?s?pO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL);
				break;

			case "?sP?o":
			case "?s?p?o":
			default:
				GraphTraversal<? extends Element, ? extends Element> ps = sp2t.getPredicate(exp, Exp.SUBJECT);
				GraphTraversal<? extends Element, ? extends Element> po = sp2t.getPredicate(exp, Exp.OBJECT);
				GraphTraversal<? extends Element, ? extends Element> pp = sp2t.getPredicate(exp, Exp.PREDICATE);

				if (po != null) {
					filter = t -> {
						return t.V().hasLabel(RDF_VERTEX_LABEL).where(po)
							.inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
							.where(outE(SUBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).where(getVertexPredicate(ps)));
					};
				} else if (ps != null) {
					filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).where(ps)
						.inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
				} else if (exp.getEdge().getNode(0).equals(exp.getEdge().getNode(1))) {
					// ?x ?p ?x
					filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).as("s")
						.inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
						.where(outE(OBJECT_EDGE).inV().hasLabel(VERTEX_VALUE).as("o")
							.where(P.eq("s")));
				} else {
					filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
				}

				break;

			case "SPO":
			case "S?pO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp))
					.where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
				break;

			case "SP?o":
			case "S?p?o":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp));
				break;
		}
		return filter;
	}

	@Override
	public EdgeQuad buildEdge(Element e) {
		Vertex nodeEdge = (Vertex) e;
		EdgeQuad result = EdgeQuad.create(
			DatatypeMap.createResource(nodeEdge.value(EDGE_G)),
			buildNode(nodeEdge.edges(Direction.OUT, SUBJECT_EDGE).next().inVertex()),
			DatatypeMap.createResource(nodeEdge.value(EDGE_P)),
			buildNode(nodeEdge.edges(Direction.OUT, OBJECT_EDGE).next().inVertex())
		);
		return result;
	}

	@Override
	public fr.inria.edelweiss.kgram.api.core.Node buildNode(Element e) {
		Vertex node = (Vertex) e;
		String id = node.value(VERTEX_VALUE);
		switch ((String) node.value(KIND)) {
			case IRI:
				return DatatypeMap.createResource(id);
			case BNODE:
				return DatatypeMap.createBlank(id);
			case LITERAL:
				String label = node.value(VERTEX_VALUE);
				String type = node.value(TYPE);
				VertexProperty<String> lang = node.property(LANG);
				if (lang.isPresent()) {
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
					return DatatypeMap.createLiteral(label, type);
				}
			case LARGE_LITERAL:
				label = node.value(VERTEX_LARGE_VALUE);
				type = node.value(TYPE);
				lang = node.property(LANG);
				if (lang.isPresent()) {
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
					return DatatypeMap.createLiteral(label, type);
				}
			default:
				throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
		}
	}

	public Neo4jGraph getNeo4jGraph() {
		return (Neo4jGraph) g;
	}

	private enum RelTypes implements RelationshipType {
		CONTEXT
	}

	/**
	 * Manage select variables and variables generated for constant nodes
	 */
	class VariableTable {

		ArrayList<String> list;
		HashMap<String, String> table, ptable;

		VariableTable() {
			list = new ArrayList<>();
			table = new HashMap<>();
			ptable = new HashMap<>();
		}

		List<String> getList() {
			return list;
		}

		HashMap<String, String> getTable() {
			return table;
		}

		String get(int i) {
			return list.get(i);
		}

		boolean contains(String s) {
			return list.contains(s);
		}

		// same variable for same literal
		String getVariable(fr.inria.edelweiss.kgram.api.core.Node node, int n, int rank) {
			String value = node.getDatatypeValue().toString();
			String var = table.get(value);
			if (var == null) {
				var = VAR_CST.concat(Integer.toString(2 * n + rank));
				table.put(value, var);
			}
			return var;
		}

		String varName(fr.inria.edelweiss.kgram.api.core.Node node, int n, int rank) {
			if (node.isVariable()) {
				return node.getLabel();
			}

			return getVariable(node, n, rank);
		}

		String propertyName(String name, int n) {
			if (contains(name)) {
				String var = VAR_PRED + name + n;
				ptable.put(var, name);
				name = var;
			}
			return name;
		}

		void select(String s) {
			if (s != null && !list.contains(s)) {
				list.add(s);
			}
		}

	}
}
