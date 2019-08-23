
package fr.inria.corese.test.dev;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Function Parser to check syntax
 * @author corby
 */
public class CompilerChecker {
    static final String WHERE = "/home/corby/NetBeansProjects/corese-github-v4/corese-core/src/main/resources/function/";

    public static void main(String[] args) {
        new CompilerChecker().process();
    }
    
    void process() {
        process(WHERE + "server/logger.rq");
    }
    
    
    void process(String name) {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        try {
            String str = ql.readWE(name);
            Query q = exec.compile(str);
            System.out.println(q.getAST());
        } catch (EngineException | LoadException ex) {
            Logger.getLogger(CompilerChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
