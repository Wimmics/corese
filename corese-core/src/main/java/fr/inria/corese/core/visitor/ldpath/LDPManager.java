package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LDPManager {
    static final String LDP = Result.LDP;    
    Graph graph;
    
    String query = String.format("prefix rs: <%s>", LDP) 
            + "select distinct (aggregate(?e) as ?path) where {"
            + "?x rs:path ?list "
            + "?list rdf:rest*/rdf:first ?e"
            + "}"
            + "group by ?x"
            ;
    
    
    
    public void process(String path) throws LoadException, EngineException {
        graph = load(path);
        QueryProcess exec = QueryProcess.create(graph);
        Mappings map = exec.query(query);
        System.out.println(map);
    }
    
    Graph load(String path) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path);
        return g;
    }
    
    

}
