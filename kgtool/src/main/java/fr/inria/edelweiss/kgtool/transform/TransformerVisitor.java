package fr.inria.edelweiss.kgtool.transform;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Query;

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
        process(query);
    }
    
    
    
    /**
     * When first exp of template-where is graph ?shape { ?sh p v }, tag graph pattern as bgpAble
     * kgram evaluate it as a BGP and computes Mappings that are cached
     * in such a way not to recompute it several times for the same shape ?sh.
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
     * 
     * graph ?shape { ?sh sh:property ?cst }
     */
    void optimize(Query query, String graph, String var) {
        if (query.getBody().size() > 0) {
            fr.inria.edelweiss.kgram.core.Exp exp = query.getBody().get(0);
            if (exp.isGraph() && exp.getGraphName().getLabel().equals(graph)) {
                Node n = query.getNode(var);
                if (n != null) {
                    exp.setNodeList(exp.getNodes());
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
