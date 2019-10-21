package fr.inria.corese.core.transform;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.ExpHandler;
import fr.inria.corese.kgram.core.Query;

/**
 * QueryVisitor for Transformation templates
 * May optimize template execution
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TransformerVisitor implements QueryVisitor {
    static final String GRAPH = "?shape";
    static final String SHAPE = "?sh";
    boolean optimize = false;
    
    public TransformerVisitor(boolean b){
        optimize = b && ! Eval.NAMED_GRAPH_DEFAULT;
    }
    
    /**
     * ast is a template
     * @param ast 
     */
    @Override
    public void visit(ASTQuery ast) {
        
    }

    /**
     * query is a template
     * @param query 
     */
    @Override
    public void visit(Query query) {
        if (optimize){
            process(query);
        }
    }
    
    
    
    /**
     * When first exp of template-where is graph ?shape { ?sh p v }, tag graph pattern as bgpAble
     * kgram evaluate it as a BGP, computes Mappings and cache it in a table: ?sh -> Mappings
     * next evaluation get Mappings from cache
     */
    void process(Query query){
        optimize(query);
        for (Query q : query.getSubQueryList()){
            optimize(q);
        }
    }
    
    
    void optimize(Query query){
        optimize(query, GRAPH, SHAPE);
    }
    
    
    /**
     * is it: graph ?shape { ?sh sh:property ?cst }
     */
    void optimize(Query query, String graph, String var) {
        if (query.getBody().size() > 0) {
            fr.inria.corese.kgram.core.Exp exp = query.getBody().get(0);
            if (exp.isGraph() && exp.getGraphName().getLabel().equals(graph)) {
                Node n = query.getNode(var);
                if (n != null) {
                    // bind exists node 
                    exp.setNodeList(exp.getTheNodes(new ExpHandler(true, false, true, false)));
                    exp.setBGPAble(true);
                    exp.cache(n);
                }
            }
        }
    }
           
    
    void process(ASTQuery ast) {
        Exp body = ast.getBody();
        if (body.size() > 0) {
            Exp exp = body.get(0);
            if (exp.isGraph()) {
                System.out.println(exp);
                System.out.println();
            }
        }
    }
    
    

}
