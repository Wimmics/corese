package fr.inria.corese.core.util;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import java.util.HashMap;

/**
 * Generate RDF Result Format Graph for Mappings (bindings in RDF)
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class MappingsGraph implements Graphable {
    static final String RDFRESULT = NSManager.RDFRESULT;
    private static final String RESULTSET    = RDFRESULT +"ResultSet";
    private static final String BOOLEAN      = RDFRESULT + "boolean";
    private static final String RESULTVARIABLE = RDFRESULT + "resultVariable";
    private static final String SOLUTION     = RDFRESULT + "solution";
    private static final String INDEX        = RDFRESULT + "index";
    private static final String BINDING      = RDFRESULT + "binding" ;
    private static final String VARIABLE     = RDFRESULT + "variable";
    private static final String VALUE        = RDFRESULT + "value";
    private static final String BNROOT       = "_:br";
    
    Mappings map;
    Graph graph;
    Node root;
    
    Query query;
    ASTQuery ast;
    HashMap<String, Node> var;
    
    int bn = 0;      
    
    MappingsGraph(Mappings m){
        map = m;
        graph = Graph.create();
        query = map.getQuery();
        ast =  query.getAST();
        var = new HashMap<String, Node>();
    }
    
    public static MappingsGraph create(Mappings m){
        MappingsGraph mg = new MappingsGraph(m);
        mg.process();
        return mg;
    }
        
    
    void process(){
       root = graph.addBlank(bnid());
       Node type = graph.addProperty(RDF.TYPE);
       
       graph.addEdge(root, type, graph.addResource(RESULTSET));
               
        if (ast != null && ast.isAsk()){            
            graph.addEdge(root, graph.addProperty(BOOLEAN), graph.addLiteral(map.size() > 0));           
        }
        else {
            body();
        }
       
    }
    
    void body() {
        Node resultVariable = graph.addProperty(RESULTVARIABLE);
       
        for (Node n : query.getSelect()) {
            graph.addEdge(root, resultVariable, getVariable(n));
        }
        int i = 0;
        for (Mapping m : map) {
            process(m,  i++);
        }
    }
    
    void process(Mapping m,  int i){
        Node solution   = graph.addProperty(SOLUTION);        
        Node index      = graph.addProperty(INDEX);
        Node binding    = graph.addProperty(BINDING);
        Node variable   = graph.addProperty(VARIABLE);
        Node value      = graph.addProperty(VALUE);
        
        Node sol = graph.addBlank(bnid());
        graph.addEdge(root, solution, sol);
        graph.addEdge(sol, index, graph.addLiteral(i));
        for (Node n : query.getSelect()){
            if (m.getNode(n) != null){
                Node bind = graph.addBlank(bnid());
                graph.addEdge(sol, binding, bind);
                graph.addEdge(bind, variable, getVariable(n));
                graph.addEdge(bind, value, graph.addNode( m.getValue(n)));               
            }
        }
    }
    
    Node getVariable(Node n){
        Node name = var.get(n.getLabel());
        if (name == null){
            name = graph.addLiteral(getName(n));
            var.put(n.getLabel(), name);
        }
        return name;
    }
    
    String bnid(){
        return BNROOT + bn++; 
    }
    
    String getName(Node n){
        return n.getLabel().substring(1);
    }

    @Override
    public String toGraph() {
        return "";    
    }

    @Override
    public void setGraph(Object obj) {
     }

    @Override
    public Graph getGraph() {
        return graph;    
    }
    
}
