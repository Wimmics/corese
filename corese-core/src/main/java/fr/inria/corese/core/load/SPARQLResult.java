package fr.inria.corese.core.load;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.compiler.result.XMLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

public class SPARQLResult extends XMLResult {

    private Graph graph;
    private Graph local;

    SPARQLResult(Graph g) {
        super();
        graph = g;
        local = Graph.create();
    }

    public static SPARQLResult create(Graph g) {
        return new SPARQLResult(g);
    }

    @Override
    public Node getURI(String str) {
        Node n = getLocal().getResource(str);
        if (n != null) {
            return n;
        }
        n = getGraph().getResource(str);
        if (n != null) {
            return n;
        }

        IDatatype dt = DatatypeMap.createResource(str);
        n = getLocal().getNode(dt, true, true);
        return n;
    }

    @Override
    public Node getBlank(String str) {
        Node n = getLocal().getBlankNode(str);
        if (n != null) {
            return n;
        }

        IDatatype dt = DatatypeMap.createBlank(str);
        n = getLocal().getNode(dt, true, true);
        return n;
    }

    @Override
    public Node getLiteral(String str, String datatype, String lang) {
        IDatatype dt = DatatypeMap.createLiteral(str, datatype, lang);
        Node n = getGraph().getNode(dt, false, false);
        if (n == null) {
            n = getLocal().getNode(dt, true, true);
        }
        return n;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * @return the local
     */
    public Graph getLocal() {
        return local;
    }

    /**
     * @param local the local to set
     */
    public void setLocal(Graph local) {
        this.local = local;
    }

}
