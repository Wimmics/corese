package fr.inria.corese.core.load.sesame;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.AddTripleHelper;
import fr.inria.corese.core.load.ILoadSerialization;
import java.util.Optional;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * Handler for adding triples to corese graph, override the default sesame
 * statement handler:handleStatement
 *
 * TurtleTripleHandlerSesame.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date Mar 10, 2014
 */
public class ParserTripleHandlerSesame extends RDFHandlerBase {

	private final AddTripleHelper helper;
	private final Graph graph;
	private Node graphSource, defaultGraphSource;

	/**
	 * Constructor
	 *
	 * @param graph Graph
	 * @param source Name of source graph
	 */
	public ParserTripleHandlerSesame(Graph graph, String source) {
		this.graph = graph;
		helper = AddTripleHelper.create(this.graph);
		//get default graph source
		defaultGraphSource = helper.getGraphSource(graph, source);
		graphSource = defaultGraphSource;
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		//** 1. get triples
		String subject = getValue(st.getSubject());
		String predicate = getValue(st.getPredicate());
		Value object = st.getObject();

		//** 2. get graph
		Resource graphNode = st.getContext();
		//sesame use null to refer to a default graph
		if (graphNode == null) {
			graphSource = defaultGraphSource;
		} else if (graphNode instanceof BNode) {
			graphSource = graph.addBlank(helper.getID(graphNode.toString()));
			graph.addGraphNode(graphSource);
		} else {
			graphSource = graph.addGraph(graphNode.toString());
		}

		//** 3. process literal and add to graph
		if (object instanceof Literal) {
			Literal lit = (Literal) object;
			Optional<String> lang = lit.getLanguage();
			String datatype = getValue(lit.getDatatype());
			helper.addTriple(subject, predicate, lit.getLabel(), lang.orElse(null), datatype, ILoadSerialization.LITERAL, graphSource);
		} else {//non-literal
			helper.addTriple(subject, predicate, getValue((Resource) object), null, null, ILoadSerialization.NON_LITERAL, graphSource);
		}
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		super.handleNamespace(prefix, uri);
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		super.endRDF();
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		super.startRDF();
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		super.handleComment(comment);
	}

	/**
	 * Set parameters for helper class
	 *
	 * @param renameBNode
	 * @param limit
	 */
	public void setHelper(boolean renameBNode, int limit) {
		helper.setRenameBlankNode(renameBNode);
		helper.setLimit(limit);
	}

	private String getValue(Resource r) throws RDFHandlerException {
		if (r instanceof URI) {
			return r.stringValue();
		} else if (r instanceof BNode) {
			return "_:" + r.stringValue();
		} else if (r == null) {
			return null;
		}

		throw new RDFHandlerException("Can not recognize resource type.");
	}
}
