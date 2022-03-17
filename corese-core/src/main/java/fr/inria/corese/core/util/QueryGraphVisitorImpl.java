package fr.inria.corese.core.util;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.QueryGraphVisitor;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * QueryGraph Visitor for query(RDF Graph)
 * Replace RDF List by rdf:rest* / rdf:first
 * in query graph
 * Match lists in different order: (a b) match (b a)
 * Use Case: match SPIN Graph
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class QueryGraphVisitorImpl implements QueryGraphVisitor {
    
    private static final String REST  = NSManager.RDF + "rest";
    private static final String FIRST = NSManager.RDF + "first";
    private static final String PATH = "?_kg_path_";
    private static final String VAR = "?_kg_var_";

    ASTQuery ast;
    Graph graph;
    Query query;
    Table table;
    ArrayList<String> predicates;

    int count = 0;
    
    private boolean debug = false;
    
    class Table extends HashMap<Node, Node> { }

    public static QueryGraphVisitorImpl create(){
        return new QueryGraphVisitorImpl();
    }
    
    QueryGraphVisitorImpl(){
        table = new Table();
        predicates = new ArrayList<String>();
    }
    
    public void addPredicate(String name){
        predicates.add(name);
    }

    
    
    @Override
    public Graph visit(Graph g) {
        g.init();
        graph = g;
        return g;
    }
    
   
    @Override
    public ASTQuery visit(ASTQuery a) {
        ast = a;
        ast.setSelectAll(true);
        return a;
    }

    /**
     * Eliminate rdf:rest and rdf:first edges
     * They will be replaced by rdf:rest* / rdf:first path edges 
     */
    @Override
    public Edge visit(Edge ent) {
        String label = ent.getEdgeNode().getLabel();

        if (label.equals(REST) || label.equals(FIRST)) {
            return null;
        }
        
        process(ent);
        
        return ent;
    }
    
    
     /**
     * add a path edge: ?start (rdf:rest* / rdf:first) ?elem
     * for each element of each list
     * 
     */
   @Override
   public Query visit(Query q) {
        query = q;
        list(graph);
        return q;
    }
    
   
   
    /*****************************************************************/
     
    /**
     * Replace Node by Variable
     * Use Case: [ sp:varName "x" ] -> [ sp:varName ?x ]
     */
    Edge process(Edge ent) {

        if (!(ent instanceof EdgeImpl)) {
            return ent;
        }

        EdgeImpl edge = (EdgeImpl) ent;

        if (! predicates.contains(edge.getEdgeLabel())) {
            return ent;
        }
        
        Node n = ent.getNode(1);
        Node v = getVariable(n);
        edge.setNode(1, v);
        
        return ent;
    }

    
    private Node getVariable(Node n) {
        Node v = table.get(n);
        if (v == null) {
            //String name = VAR + count ++;
            v = NodeImpl.createVariable("?"+n.getLabel());
            table.put(n, v);
        }
        return v;
    }

   
    
    
     /**
     * Retrieve blank nodes that start RDF List
     * Retrieve list elements
     * create a path edge for each list element
     * add path edge into query
     */
    void list(Graph g){
        // Blank Nodes that start RDF List
        List<Node> list = g.getLists();
        for (Node node : list){
           list(g, node);
        }
    }
    
    /**
     * Generate: node rdf:rest* / rdf:first elem
     * for each elem of list starting at node
     */
    void list(Graph g, Node node){
         List<Node> nl = g.getList(node);
         if (isDebug()){
             System.out.println("QV: " + node + " " + nl);
         }
         for (Node elem : nl){
             create(node, elem);
         }
    }
    
    /**
     * node: start blank node of a list
     * elem: element of the list
     * create a path edge: ?node rdf:rest* / rdf:first ?elem
     * add path edge in query
     */
    private Exp create(Node node, Node elem) {
       Node var = NodeImpl.createVariable(variable());
        fr.inria.corese.compiler.parser.EdgeImpl edge = 
               fr.inria.corese.compiler.parser.EdgeImpl.create(var, node, elem);
       Term re = list();
        try {
            re.compile(ast);
        } catch (EngineException ex) {
        }
       Exp exp = Exp.create(Exp.PATH, edge);
       exp.setRegex(re);
       if (isDebug()){
           System.out.println("QV: " + exp);
       }
       query.getBody().add(exp);
       return exp;
    }
    
    String variable(){
        return PATH + count++ ;
    }
    
    // rdf:rest*/rdf:first
    Term list(){
        return Term.create(Term.RE_SEQ, Term.function(Term.STAR, Constant.create(REST)), 
                Constant.create(FIRST));
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    

}
