package fr.inria.corese.core.print;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * Pretty printing for JSON-LD format
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb 2014
 */
public class JSONLDFormat extends RDFFormat {

    // open close style (OC_)
    public static final int OC_BRACE = 10;// @key:{..},
    public static final int OC_NOCOMMA = 11;// @key:{..}
    public static final int OC_SBRACKET = 20;// @key:[..],
    public static final int OC_NOKEY = 30;// {..},
    public static final int OC_LIST = 31;// [..],
    public static final int OC_NONE = 40;// ..
    public static final int OC_ONELINE = 41;// {..}

    // JSON-LD keywords (KW_)
    public static final String KW_CONTEXT = "\"@context\"";
    public static final String KW_ID = "\"@id\"";
    public static final String KW_VALUE = "\"@value\"";
    public static final String KW_LANGUAGE = "\"@language\"";
    public static final String KW_TYPE = "\"@type\"";
    public static final String KW_CONTAINER = "\"@container\"";
    public static final String KW_LIST = "\"@list\"";
    public static final String KW_SET = "\"@set\"";
    public static final String KW_REVERSE = "\"@reverse\"";
    public static final String KW_INDEX = "\"@index\"";
    public static final String KW_BASE = "\"@base\"";
    public static final String KW_VOCAB = "\"@vocab\"";
    public static final String KW_GRAPH = "\"@graph\"";

    // Seprators (SP_)
    public static final String SP_COLON = ": ";
    public static final String SP_COMMA = ",";
    public static final String BRACE_LEFT = "{";
    public static final String BRACE_RIGHT = "}";
    public static final String SBRACKET_LEFT = "[";
    public static final String SBRACKET_RIGHT = "]";
    public static final String SP_TAB = "\t";
    public static final String SP_NL = System.getProperty("line.separator");

    Graph graph;
    Mapper map;
    NSManager nsm;
    Query query;
    ASTQuery ast;

    JSONLDFormat(NSManager n) {
        super(n);
        nsm = n;
    }

    JSONLDFormat(Graph g, Query q) {
        this(q.getAST().getNSM());
        if (g != null) {
            graph = g;
            // graph.prepare();
            graph.getEventManager().start(Event.Format);
        }
        ast = getAST(q);
        query = q;
    }

    JSONLDFormat(Graph g, NSManager n) {
        this(n);
        if (g != null) {
            graph = g;
            // graph.prepare();
            graph.getEventManager().start(Event.Format);
        }
    }

    public static JSONLDFormat create(Graph g, NSManager n) {
        return new JSONLDFormat(g, n);
    }

    public static JSONLDFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            Query q = map.getQuery();
            NSManager nsm = q.getAST().getNSM();
            return create(g, nsm);
        }
        return create(Graph.create());
    }

    public static JSONLDFormat create(Graph g) {
        return new JSONLDFormat(g, NSManager.create().setRecord(true));
    }

    @Override
    public String toString() {
        StringBuilder error = errorString();
        if (error.length() != 0) {
            return error.toString();
        } else {
            return getJsonLdObject().toString();
        }
    }

    /**
     * Get the top level object of JSON-LD
     *
     * @return JSON object
     */
    public JSONLDObject getJsonLdObject() {
        // ****1 check condition
        if (graph == null && map == null) {
            return new JSONLDObject();
        }

        JSONLDObject topLevel = new JSONLDObject(OC_NOKEY);

        JSONLDObject defaultGraph = null;
        List<JSONLDObject> otherGraphs = new ArrayList<JSONLDObject>();

        // ****2. Add graphs
        if (size(graph.getGraphNodes()) <= 1) {// only one graph, put them all together
            defaultGraph = graph(null);
        } else {// multiple graph, read each graph
            for (Node gNode : graph.getGraphNodes()) {
                // 2.1 default graph
                if (graph.isDefaultGraphNode(gNode)) {
                    defaultGraph = graph(gNode);
                } else {
                    // 2.2.0 get the info of graph
                    JSONLDObject graphInfo = jsonldObject(gNode, gNode, true);

                    JSONLDObject other = new JSONLDObject(OC_BRACE);
                    // 2.2.1 add graph info
                    if (graphInfo != null) {
                        graphInfo.setModularType(OC_NONE);
                        other.addObject(graphInfo);
                    }

                    // 2.2.2 add graph
                    other.addObject(graph(gNode));

                    // 2.2.3 add this graph to graph list
                    otherGraphs.add(other);
                }
            }
        }
        if (defaultGraph == null) {
            defaultGraph = new JSONLDObject(KW_GRAPH);
        }

        // ****3. add the other graphs to default graph
        defaultGraph.addObject(otherGraphs);
        defaultGraph.setModularType(OC_SBRACKET);

        // ****3.1 Add context
        topLevel.addObject(context());

        // ****4. add default(all) graph(s) to top level object
        topLevel.addObject(defaultGraph);
        return topLevel;
    }

    // Get the Json Object of "@Context"
    private JSONLDObject context() {
        JSONLDObject context = new JSONLDObject(KW_CONTEXT, OC_BRACE);

        Set<String> ns = nsm.getPrefixSet();

        // 1. add base @base
        String base = nsm.getBase();
        if (base != null) {
            context.addObject(new JSONLDObject(KW_BASE, base));
        }

        // 2. add prefixes
        for (String p : ns) {
            String uri = nsm.getNamespace(p);
            if (nsm.isDisplayable(uri)) {
                context.addObject(new JSONLDObject(quote(p), quote(uri)));
            }
        }

        return context;
    }

    // Get the Json oject of "@Graph" according to graph node:gNode
    // if gNode == null, get all nodes
    private JSONLDObject graph(Node gNode) {
        JSONLDObject jGraph = new JSONLDObject(KW_GRAPH, OC_SBRACKET);

        Iterable<Node> allNode = gNode == null ? graph.getAllNodeIterator() : graph.getNodeGraphIterator(gNode);

        // iterate each node and add to this graph
        for (Node node : allNode) {
            JSONLDObject jo = jsonldObject(gNode, node.getNode(), false);
            if (jo != null) {
                jGraph.addObject(jo);
            }
        }
        return jGraph;
    }

    // compose one object of jsonld from graph
    private JSONLDObject jsonldObject(Node gNode, Node node, boolean graphInfo) {
        if (!graphInfo && size(graph.getNodeEdges(gNode, node)) < 1) {
            return null;
        }
        JSONLDObject jo = new JSONLDObject(OC_BRACE);

        // 1. add node id
        jo.addObject(subjectId(node));

        // 2. add properties and objects
        jo.addObject(propertyAndObject(gNode, node));

        return jo;
    }

    // Get "@id" of the node
    private JSONLDObject subjectId(Node node) {
        JSONLDObject jo = new JSONLDObject(KW_ID);

        IDatatype dt = node.getValue();
        String subject = dt.isBlank() ? dt.getLabel() : nsm.toPrefix(dt.getLabel());
        subject = filter(subject);

        // repalce rdf:type with @type
        if (RDF.TYPE.equals(dt.getLabel())) {
            subject = KW_TYPE;
        }

        jo.setObject(quote(subject));
        return jo;
    }

    // get the list of proerperties and objects according to given node subject id
    private List<JSONLDObject> propertyAndObject(Node gNode, Node node) {
        HashMap<String, List<Object>> map = new HashMap<String, List<Object>>();

        for (Edge ent : graph.getNodeEdges(gNode, node)) {
            if (ent == null) {
                continue;
            }

            Edge edge = ent;

            // 1. get property
            String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());
            // repalce @type
            boolean type = false;
            if (RDF.TYPE.equals(edge.getEdgeNode().getLabel())) {
                type = true;
                pred = KW_TYPE;
            } else {
                pred = quote(filter(pred));
            }

            Object obj = null;

            // 2. get object
            IDatatype dt = edge.getNode(1).getValue();
            if (dt.isLiteral()) {// 2.1 literal
                IDatatype datatype = dt.getDatatype();
                IDatatype lang = dt.getDataLang();
                if (datatype != null || lang != null) {
                    obj = addLiteralInfo(dt);
                } else {
                    obj = dt.getLabel();
                }
            } else {
                String label = null;
                if (dt.isBlank()) {// 2.2 blank node
                    label = dt.getLabel();
                } else if (dt.isURI()) {// 2.3 uri
                    label = nsm.toPrefix(dt.getLabel());
                }
                label = quote(filter(label));

                // add key word @id to these nodes expect those nodes whose
                // properties are @type
                if (!type) {
                    JSONLDObject temp = new JSONLDObject(OC_NOCOMMA);
                    temp.addObject(new JSONLDObject(KW_ID, label));
                    obj = temp;
                } else {
                    obj = label;
                }
            }

            // add to hash map
            if (map.containsKey(pred)) {
                map.get(pred).add(obj);
            } else {
                List<Object> ls = new ArrayList();
                ls.add(obj);
                map.put(pred, ls);
            }
        }

        return toList(map);
    }

    // process list
    // merge several triples that share the same predicates into a list
    // for example a b c; a b d;=>>a b [c,d]
    private List<JSONLDObject> toList(HashMap<String, List<Object>> map) {
        List<JSONLDObject> list = new ArrayList<>();

        for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<Object> ls = entry.getValue();
            JSONLDObject jo = new JSONLDObject(key);
            if (ls.size() == 1) {
                jo.setObject(ls.get(0));
            } else {
                for (Object obj : ls) {
                    jo.addObject(new JSONLDObject("", obj));
                }
                jo.setModularType(OC_SBRACKET);
            }
            list.add(jo);
        }

        return list;
    }

    // Expand the informaion of literal:@value, @type or @value, @langauge
    private JSONLDObject addLiteralInfo(IDatatype literal) {

        JSONLDObject jo = new JSONLDObject(OC_NOCOMMA);

        IDatatype datatype = literal.getDatatype();
        IDatatype lang = literal.getDataLang();

        // 1 add @value
        jo.addObject(new JSONLDObject(KW_VALUE, quote(filter(literal.getLabel()))));

        boolean bType = datatype != null && !datatype.getLabel().isEmpty();
        boolean bLang = lang != null && !lang.getLabel().isEmpty();

        if (bLang) {// 2 add @language tag if any
            String language = nsm.toPrefix(lang.getLabel());
            jo.addObject(new JSONLDObject(KW_LANGUAGE, quote(filter(language))));
        } else if (bType) {// 3 add @type if any
            String type = nsm.toPrefix(datatype.getLabel());
            jo.addObject(new JSONLDObject(KW_TYPE, quote(filter(type))));
        }

        return jo;
    }

    // add qutation marks
    private String quote(String str) {
        return "\"" + str + "\"";
    }

    // return the size of an iterable object
    private int size(Iterable it) {
        int counter = 0;
        for (Object e : it) {
            counter++;
        }

        return counter;
    }

    // append a string with new line
    private void append(StringBuilder sb, Object add) {
        sb.append(add).append(SP_NL);
    }

    // filter string to remove < or >
    private String filter(String label) {
        if (label.contains("<") && label.contains(">")) {
            return label.replaceAll("<|>", "");
        }
        return label;
    }

    /**
     * write Json-ld output to file
     *
     * @param name file name
     * @throws IOException
     */
    public void write(String name) throws IOException {
        StringBuilder sb = this.getJsonLdObject().toStringBuilder();
        FileOutputStream fos = new FileOutputStream(name);
        for (int i = 0; i < sb.length(); i++) {
            fos.write(sb.charAt(i));
        }
        fos.close();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(this.toString().getBytes());
    }

    // error message
    private StringBuilder errorString() {
        StringBuilder error = new StringBuilder();
        boolean bAstError = ast != null && ast.getErrors() != null;
        boolean bQueryError = query != null && query.getErrors() != null;
        boolean bNoGraph = graph == null || size(graph.getGraphNodes()) == 0;

        if (bNoGraph) {
            append(error, "No graph contained in the results..");
        }
        if (bAstError || bQueryError) {

            if (ast.getText() != null) {
                append(error, ast.getText());
            }
            append(error, "");

            if (bAstError) {
                for (String mes : ast.getErrors()) {
                    append(error, mes);
                }
            }
            if (bQueryError) {
                for (String mes : query.getErrors()) {
                    append(error, mes);
                }
            }
            append(error, "");
        }
        return error;
    }
}
