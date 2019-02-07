package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class QueryProcessThread extends Thread {

    
    QueryProcess exec;
    ASTQuery ast;
    Mappings map;
    Constant predicate;
    private boolean join = false;
    
    
    QueryProcessThread(Graph g, ASTQuery ast, Constant p) {
        exec = QueryProcess.create(g);
        this.ast = ast;
        predicate = p;
    }
    
    void setVerbose(boolean b) {
        exec.getGraph().setVerbose(b);
    }
    
    @Override
    public void run() {
        process();
    }
    
    void process() {
        map = exec.query(ast);
    }
    
    Mappings getMappings() {
        return map;
    }
    
    ASTQuery getAST() {
        return ast;
    }
    
    Constant getPredicate() {
        return predicate;
    }
    
    
    /**
     * @return the join
     */
    public boolean isJoin() {
        return join;
    }

    /**
     * @param join the join to set
     */
    public void setJoin(boolean join) {
        this.join = join;
    }

}
