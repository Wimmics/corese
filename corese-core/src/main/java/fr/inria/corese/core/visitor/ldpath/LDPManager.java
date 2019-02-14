package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LDPManager {
    static final String LDP = Result.LDP;  
    String local, endpoint;
    Graph graph;
    LinkedDataPath ldp;
    
    static final String query = String.format("prefix rs: <%s>", LDP) 
            + "select (aggregate(?path) as ?list) where {"
            + "select distinct (aggregate(?e) as ?path) where {"
            + "?x rs:path ?list "
            + "?list rdf:rest*/rdf:first ?e"
            + "}"
            + "group by ?x"
            + "}"
            ;

    
    public LDPManager(LinkedDataPath ldp) {
        this.ldp = ldp;
    }
    
    AST ast() {
        return ldp.ast();
    }
    
    public void process(String q, String path) throws LoadException, EngineException, InterruptedException, IOException {
        IDatatype list = getList(path);
        QueryProcess exec = QueryProcess.create();
        ASTQuery ast = exec.ast(q);
        
        Exp body = ast.where();
        int i = 1;
        for (IDatatype dt : list) {
            if (! acceptable(dt)) {
                continue;
            }
            ASTQuery a = ast().complete(ast, dt.getValues(), ast().length(ast) + 1);
            System.out.println("__________________");
            System.out.println();
            System.out.println(a);
            Result res = ldp.process(ast, ast().length(ast) + 1);
            res.setOutputFile(String.format("/tmp/tmp%s.ttl", i++));
            res.process();
            ast.where(body);
        }
    }
    
    boolean acceptable(IDatatype list) {
        for (IDatatype dt : list) {
            if (! ldp.acceptable(dt.getLabel())) {
                return false;
            }
        }
        return true;
    }
    
    
    void complete(ASTQuery ast, List<IDatatype> list, int varIndex) {
        for (IDatatype dt : list) {
            Exp body = ast.where();
            ASTQuery a = ast().complete(ast, dt.getValues(), varIndex);
            System.out.println(dt);
            System.out.println(a);
            ast.where(body);
        }
    }
    
    public IDatatype getList(String path) throws LoadException, EngineException {
        graph = load(path);
        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(query);
        return (IDatatype) map.getValue("?list");       
    }
    
    Graph load(String path) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path);
        return g;
    }
    
    

}
