package fr.inria.corese.core.extension;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.IOException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 */
public class JavaCompiler extends fr.inria.corese.sparql.compiler.java.JavaCompiler {
    
    public JavaCompiler(String name) {
        super(name);
    }
    
    /**
     * To recursively import functions, use http://ns.inria.fr/sparql-template/function/name
     * 
     */
    public void compile(String path) throws EngineException, IOException {
        String q = String.format("@import <%s> select * where { }", path);
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query qq = exec.compile(q);
        compile(qq);
    }
    
}
