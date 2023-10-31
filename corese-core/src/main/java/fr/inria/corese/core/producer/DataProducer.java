package fr.inria.corese.core.producer;

import static fr.inria.corese.kgram.api.core.PointerType.PRODUCER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphObject;
import fr.inria.corese.core.edge.EdgeTop;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 * Transient Dataset over graph in order to iterate edges
 * Use case: Producer getEdges()
 * default or named graphs, from or from named
 * Default graph: eliminate duplicate edges during iteration
 * May take edge level into account for RuleEngine optimization
 * Example:
 * graph.getDefault().from(g1).iterate(foaf:knows)
 * graph.getNamed().minus(list(g1 g2)).iterate(us:John, 0)
 * graph.getDefault().iterate().filter(ExprType.GE, 100) -- filter (?object >=
 * 100)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 */
public class DataProducer extends GraphObject
        implements Iterable<Edge>, Iterator<Edge> {

    static final List<Edge> empty = new ArrayList<Edge>(0);

    Iterable<Edge> iter;
    Iterator<Edge> it;
    EdgeTop glast;
    Edge last;
    private Graph graph;
    private DataFilter filter;
    DataFrom from;
    boolean isNamedGraph;
    private boolean skipEdgeMetadata = false;
    private boolean duplicate = false;

    public DataProducer(Graph g) {
        graph = g;
        isNamedGraph = false;
    }

    public static DataProducer create(Graph g) {
        DataProducer ei = new DataProducer(g);
        return ei;
    }

    public DataProducer iterate() {
        return iterate(getGraph().getTopProperty());
    }

    public DataProducer iterate(Node predicate) {
        setIterable(getGraph().getEdges(predicate));
        return this;
    }

    public DataProducer iterate(Node predicate, Node node) {
        return iterate(predicate, node, 0);
    }

    public DataProducer iterate(Node node, int n) {
        return iterate(getGraph().getTopProperty(), node, n);
    }

    public DataProducer iterate(Node predicate, Node node, int n) {
        return iterate(predicate, node, null, n);
    }

    // pragma: if n == 0 && node2!=null then node2=object node
    public DataProducer iterate(Node predicate, Node node, Node node2, int n) {
        // optimize special cases
        if (isNamedGraph) {
            if (node == null && from != null && !from.isEmpty()) {
                // exploit IGraph Index to focus on from
                setIterable(getEdgesFromNamed(from.getFrom(), predicate));
                return this;
            }
        }
        // default graph
        else if (node == null && from != null && !from.isEmpty()) {
            // no query node has a value, there is a from
            if (!from.isFromOK(from.getFrom())) {
                // from URIs are unknown in current graph
                setIterable(empty);
                return this;
            }
        }

        // general case
        setIterable(getGraph().properGetEdges(predicate, node, node2, n));
        return this;
    }

    IDatatype value(Node n) {
        if (n == null) {
            return null;
        }
        return n.getDatatypeValue();
    }

    DataProducer empty() {
        setIterable(new ArrayList<>(0));
        return this;
    }

    // after iterate()
    public int cardinality() {
        int n = 0;
        for (Edge e : this) {
            if (e != null) {
                n++;
            }
        }
        return n;
    }

    public DataProducer iterate(Node s, Node p, Node o) {
        return iterate(s == null ? null : s.getDatatypeValue(),
                p == null ? null : p.getDatatypeValue(),
                o == null ? null : o.getDatatypeValue());
    }

    /**
     * if arg is bnode:
     * if bnode is in target graph, it is considered as bnode of target graph
     * if bnode is not in target graph, it is considered as a joker (a variable) in
     * the triple pattern
     */
    public DataProducer iterate(IDatatype s, IDatatype p, IDatatype o) {
        Node ns = null, np, no = null;
        if (p == null) {
            np = getGraph().getTopProperty();
        } else if (p.isBlank()) {
            // check whether bnode is a graph bnode
            np = getGraph().getNode(p);
            if (np == null) {
                // not graph bnode, it is a joker
                np = getGraph().getTopProperty();
            } else {
                // graph bnode, it cannot be a property
                return empty();
            }
        } else {
            np = getGraph().getPropertyNode(p);
            if (np == null) {
                return empty();
            }
        }
        if (s != null) {
            ns = getGraph().getNode(s);
            if (ns == null && !s.isBlank()) {
                return empty();
            }
        }
        if (o != null) {
            // @todo: getExtNode()
            no = getGraph().getExtNode(o);
            if (no == null && !o.isBlank()) {
                return empty();
            }
        }

        DataProducer dp;
        DataFilterFactory df = new DataFilterFactory();

        if (isVariable(ns)) {
            if (isVariable(no)) {
                dp = iterate(np);
            } else {
                dp = iterate(np, no, 1);
            }
        } else if (isVariable(no)) {
            dp = iterate(np, ns, 0);
        } else {
            dp = iterate(np, ns, 0);
        }

        if (isVariable(ns, s)) {
            if (isPropertyVariable(p) && s.equals(p)) {
                df.edge(ExprType.EQ, DataFilter.SUBJECT, DataFilter.PROPERTY);
            }
            if (isVariable(no, o) && s.equals(o)) {
                df.edge(ExprType.EQ, DataFilter.SUBJECT, DataFilter.OBJECT);
            }
        }
        if (isVariable(no, o)) {
            if (isPropertyVariable(p) && o.equals(p)) {
                df.edge(ExprType.EQ, DataFilter.OBJECT, DataFilter.PROPERTY);
            }
        }
        if (!isVariable(ns) && !isVariable(no)) {
            df.filter(ExprType.EQ, o);
        }

        if (df != null && df.getFilter() != null) {
            dp.filter(df);
        }
        return dp;
    }

    public boolean exist(IDatatype s, IDatatype p, IDatatype o) {
        for (Edge ent : iterate(s, p, o)) {
            return (ent != null);
        }
        return false;
    }

    boolean isPropertyVariable(IDatatype dt) {
        return dt != null && dt.isBlank();
    }

    boolean isVariable(Node n) {
        return n == null;
    }

    // subject/object
    boolean isVariable(Node n, IDatatype dt) {
        return isVariable(n) && dt != null && dt.isBlank();
    }

    /**
     * Iterate predicate from named
     */
    Iterable<Edge> getEdgesFromNamed(List<Node> from, Node predicate) {
        MetaIterator<Edge> meta = new MetaIterator<Edge>();

        for (Node src : from) {
            Node tfrom = getGraph().getGraphNode(src);
            if (tfrom != null) {
                Iterable<Edge> it = getGraph().getEdges(predicate, tfrom, Graph.IGRAPH);
                if (it != null) {
                    meta.next(it);
                }
            }
        }

        if (meta.isEmpty()) {
            return empty;
        } else {
            return meta;
        }
    }

    public void setIterable(Iterable<Edge> it) {
        if (iter == null) {
            iter = it;
        } else if (iter instanceof MetaIterator) {
            ((MetaIterator) iter).next(it);
        } else {
            MetaIterator m = new MetaIterator<>();
            m.next(iter);
            m.next(it);
            iter = m;
        }
    }

    /**
     * RuleEngine require Edge with getIndex() >= n
     * 
     */
    public DataProducer level(int n) {
        setFilter(new DataFilter(ExprType.EDGE_LEVEL, n));
        return this;
    }

    public DataProducer access(byte n) {
        setFilter(new DataFilter(ExprType.EDGE_ACCESS, n));
        return this;
    }

    public DataProducer access(AccessRight ac) {
        setFilter(new DataFilter(ExprType.EDGE_ACCESS, ac));
        return this;
    }

    // nested triple
    public DataProducer status(boolean nested) {
        setFilter(new DataFilter(ExprType.EDGE_NESTED, nested));
        return this;
    }

    public DataProducer named() {
        this.isNamedGraph = true;
        return this;
    }

    public DataFrom getCreateDataFrom() {
        if (from == null) {
            from = new DataFrom(getGraph());
            setFilter(from);
        }
        return from;
    }

    // node list must be sorted
    public DataProducer from(List<Node> list) {
        if (list != null && !list.isEmpty()) {
            getCreateDataFrom().from(list);
        }
        return this;
    }

    public DataProducer from(Node g) {
        if (g != null) {
            getCreateDataFrom().from(g);
        }
        return this;
    }

    public DataProducer from(IDatatype dt) {
        if (dt.isList()) {
            dt.getList().sort();
            return fromList(dt.getValueList());
        }
        Node g = getGraph().getNode(dt, false, false);
        return (g == null) ? this : from(g);
    }

    public DataProducer fromList(List<IDatatype> list) {
        if (list != null && !list.isEmpty()) {
            ArrayList<Node> nodeList = new ArrayList<>();
            for (IDatatype dt : list) {
                Node node = getGraph().getNode(dt, false, false);
                if (node != null) {
                    nodeList.add(node);
                }
            }
            from(nodeList);
        }
        return this;
    }

    // check that node exist in graph
    public DataProducer fromSelect(List<Node> list) {
        if (list != null && !list.isEmpty()) {
            ArrayList<Node> nodeList = new ArrayList<>(list.size());

            for (Node n : list) {
                Node node = getGraph().getNode(n.getDatatypeValue(), false, false);
                if (node != null) {
                    nodeList.add(node);
                }
            }
            if (nodeList.isEmpty()) {
                // no named graph from list exist in target graph: fail
                for (Node node : list) {
                    nodeList.add(node);
                }
            }

            Collections.sort(nodeList);
            from(nodeList);
        }
        return this;
    }

    public DataProducer from(List<Node> list, Node source) {
        if (source == null) {
            return from(list);
        }
        return from(source);
    }

    /**
     * The from clause is taken as skip from
     * the graphs are skipped to answer query
     */
    public DataProducer minus(List<Node> list) {
        getCreateDataFrom().minus(list);
        return this;
    }

    public DataProducer minus(Node node) {
        getCreateDataFrom().minus(node);
        return this;
    }

    @Override
    public Iterator<Edge> iterator() {
        if (from != null && from.isOneFrom() && from.getFromNode() == null) {
            return empty.iterator();
        }
        it = iter.iterator();
        last = null;
        glast = null;
        return this;
    }

    public IDatatype getEdges() {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : this) {
            if (edge != null) {
                list.add(DatatypeMap.createObject(edge));
            }
        }
        return DatatypeMap.newList(list);
    }

    public IDatatype getObjects() {
        return getNodes(1);
    }

    public IDatatype getSubjects() {
        return getNodes(0);
    }

    public IDatatype getNodes(int n) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : this) {
            if (edge != null) {
                list.add(edge.getNode(n).getDatatypeValue());
            }
        }
        return DatatypeMap.newList(list);
    }

    @Override
    public Iterable getLoop() {
        return this;
    }

    @Override
    public PointerType pointerType() {
        return PRODUCER;
    }

    public IDatatype getList() {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : this) {
            if (edge != null) {
                list.add(DatatypeMap.createObject(getGraph().getEdgeFactory().copy(edge)));
            }
        }
        return DatatypeMap.newInstance(list);
    }

    @Override
    public IDatatype getValue(String var, int n) {
        if (n >= 0) {
            int i = 0;
            for (Edge edge : this) {
                if (i++ == n) {
                    if (edge != null) {
                        IDatatype dt = DatatypeMap.createObject(getGraph().getEdgeFactory().copy(edge));
                        return dt;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex() && n1.same(n2);
    }

    @Override
    public int size() {
        boolean b = isDuplicate();
        setDuplicate(false);
        int i = 0;
        for (Edge e : this) {
            if (e != null) {
                i++;
            }
        }
        setDuplicate(b);
        return i;
    }

    /**
     * Main function iterate Edges.
     */
    @Override
    public Edge next() {

        while (hasNext()) {
            Edge edge = it.next();
            if (last == null) {
                // ok
            } else if (isNamedGraph) {

            } else if (different(last, edge)) {
                // ok
            } else {
                continue;
            }

            if (filter != null && !filter.eval(edge)) {
                // filter process from() clause
                if (filter.fail()) {
                    // RuleEngine edge level may fail
                    it = empty.iterator();
                    return null;
                }
                continue;
            }

            record(edge);
            return result(edge);
        }
        return null;
    }

    Edge result(Edge edge) {
        if (isDuplicate()) {
            return getGraph().getEdgeFactory().copy(edge);
        }
        return edge;
    }

    /**
     * Eliminate successive duplicates
     * 
     **/
    boolean different(Edge last, Edge edge) {
        if (edge.getEdgeNode() == null || !same(last.getEdgeNode(), edge.getEdgeNode())) {
            // different properties: ok
            return true;
        }
        if (getGraph().isMetadata()) {
            return metadataDifferent(last, edge);
        }
        if (skipEdgeMetadata) {
            // two edges with same metadata considered as duplicates
            // g1 s p o t . g2 s p o t considered as duplicates
            return metadataDifferent(last, edge);
        }
        int size = last.nbNode();
        if (size == edge.nbNode()) {
            for (int i = 0; i < size; i++) {
                if (!same(last.getNode(i), edge.getNode(i))) {
                    return true;
                }
            }
            return false;
        } else {
            // different nb of nodes => different
            return true;
        }
    }

    boolean metadataDifferent(Edge last, Edge edge) {
        for (int i = 0; i < 2; i++) {
            if (!same(last.getNode(i), edge.getNode(i))) {
                return true;
            }
        }
        return false;
    }

    void record(Edge edge) {
        if (edge.isInternal()) { // (edge.nbNode() == 2 && ! edge.isTripleNode()){
            last = duplicate(edge);
        } else {
            last = edge;
        }
    }

    // record a copy of edge for last
    Edge duplicate(Edge edge) {
        if (glast == null) {
            glast = getGraph().getEdgeFactory().createDuplicate(edge);
        }
        glast.duplicate(edge);
        return glast;
    }

    @Override
    public void remove() {
    }

    /**
     * @return the edgeMetadata
     */
    public boolean isSkipEdgeMetadata() {
        return skipEdgeMetadata;
    }

    /**
     * @param edgeMetadata the edgeMetadata to set
     */
    public DataProducer setSkipEdgeMetadata(boolean b) {
        this.skipEdgeMetadata = b;
        return this;
    }

    /********************************************************
     * 
     * API to add filters to iterate()
     * Use case:
     * 
     * g.getDefault().iterate(foaf:age).filter(new
     * DataFilterFactory().object(ExprType.GE, 50)) -- object >= 50
     * g.getNamed().from(g1).iterate().filter(new
     * DataFilterFactory().compare(ExprType.EQ, 0, 1)) -- subject = object
     * g.getDefault().iterate().filter(new
     * DataFilterFactory().and().subject(AA).object(BB)) -- and/or are binary
     * g.getDefault().iterate().filter(new
     * DataFilterFactory().not().or().subject(AA).object(BB)) -- not is unary
     * 
     *******************************************************/

    public DataProducer filter(DataFilter f) {
        setFilter(f);
        return this;
    }

    public DataProducer filter(DataFilterFactory f) {
        setFilter(f.getFilter());
        return this;
    }

    /**
     * @return the filter
     */
    public DataFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(DataFilter f) {
        if (filter == null || f == null) {
            filter = f;
        } else if (filter.isBoolean()) {
            filter.setFilter(f);
        } else {
            // use case: filter = from(g1)
            filter = new DataFilterAnd(filter, f);
        }
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public DataProducer setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
        return this;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

}
