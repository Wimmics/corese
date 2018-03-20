package fr.inria.corese.core.load.jsonld;

import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.AddTripleHelper;
import fr.inria.corese.core.load.ILoadSerialization;
import java.util.List;

/**
 * Implementation of interface from Jsonld-java (json-ld parser) for adding
 * triples to corese graph
 *
 * @author Fuqi Song, Wimmics inria i3s
 * @date 10 Feb. 2014 new
 */
public class CoreseJsonTripleCallback implements JSONLDTripleCallback {

    private AddTripleHelper helper;
    private Graph graph;
    private Node graphSource, defaultGraphSource;
    private final static String JSONLD_DEFAULT_GRAPH = "@default";
    private final static String JSONLD_BNODE_PREFIX = ":_";

    public CoreseJsonTripleCallback(Graph graph, String source) {
        this.graph = graph;

        helper = AddTripleHelper.create(this.graph);
        //get default graph source
        defaultGraphSource = helper.getGraphSource(graph, source);
        graphSource = defaultGraphSource;
    }

    @Override
    public Object call(RDFDataset dataset) {

        for (String graphName : dataset.graphNames()) {

            //add graphs
            if (JSONLD_DEFAULT_GRAPH.equals(graphName)) {
                graphSource = defaultGraphSource;
            } else if (graphName.startsWith(JSONLD_BNODE_PREFIX)) {
                graphSource = graph.addBlank(helper.getID(graphName));
                graph.addGraphNode(graphSource);
            } else {
                graphSource = graph.addGraph(graphName);
            }

            //add all triples to this graph
            final List<RDFDataset.Quad> quads = dataset.getQuads(graphName);
            for (final RDFDataset.Quad quad : quads) {

                String subject = quad.getSubject().getValue();
                String predicate = quad.getPredicate().getValue();
                RDFDataset.Node objectNode = quad.getObject();
                String object = objectNode.getValue();
                String lang = objectNode.getLanguage();
                String type = objectNode.getDatatype();

                int tripleType;
                if (objectNode.isLiteral()) {
                    tripleType = ILoadSerialization.LITERAL;
                } else {
                    tripleType = ILoadSerialization.NON_LITERAL;
                }

                helper.addTriple(subject, predicate, object, lang, type, tripleType, graphSource);
            }
        }

        return graph;
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
}
