package fr.inria.corese.core.extension;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.Cleaner;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.kgram.api.query.ASTQ;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Java Extension function public class
 * prefix fun: <function://fr.inria.corese.core.extension.Extension>
 * fun:test(xt:graph())
 * 
 * Provide access to query execution environment
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class Extension extends Core {
    

    
    
    // clean OWL ontology: remove duplicate statements
    public IDatatype clean() {
        Cleaner clean = new Cleaner(getGraph());
        clean.setVisitor(new QuerySolverVisitorRule(new RuleEngine(), getEval()));
        try {
            clean.clean();
        } catch (IOException ex) {
            Logger.getLogger(Extension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(Extension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DatatypeMap.TRUE;
    }
    
    
 
    
    public IDatatype mytest() {
        try {
            QueryProcess exec = QueryProcess.create(getGraph());
            System.out.println("before mytest");
            Mappings map = exec.query("insert { graph us:g1 { [] rdf:value ?v } } where { bind (rand() as ?v) }");
            System.out.println("after mytest");
        } catch (EngineException ex) {
            Logger.getLogger(Extension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DatatypeMap.TRUE;
    }
    
    public IDatatype mytest2() {
        Service s = new Service("http://localhost:8080/sparql");
        try {
            System.out.println("before mytest");
            Mappings map = s.select("insert { graph us:g1 { [] rdf:value ?v } } where { bind (rand() as ?v) }");
            System.out.println("after mytest");
        } catch (LoadException ex) {
            System.out.println(ex);
        }
        return DatatypeMap.TRUE;
    }
    
     public IDatatype mytest1() {
        System.out.println("mytest 1");
        return DatatypeMap.TRUE;
    }
    
    
    // example    
    IDatatype test(IDatatype dt) {
        getEnvironment();
        getProducer();
        getGraph();
        getEval();
        return DatatypeMap.TRUE;
    }
    
   ProcessVisitor getVisitor() {
       return getEval().getVisitor();
   } 
    
   public IDatatype closeVisitor() {
       getEval().setVisitor(new ProcessVisitorDefault());
       getVisitor().setDefaultValue(TRUE);
       return visitor();
   } 
   
   public IDatatype openVisitor() {
       getEval().setVisitor(new QuerySolverVisitor(getEval()));
       getVisitor().init(getEnvironment().getQuery());
       return visitor();
   } 
    
    /**
     * Accessor
     * fun:visitor()
     */
    public IDatatype visitor() {
        return cast(getVisitor());
    }
    
    public IDatatype query() {
        return cast(getQuery());
    }
    
    public IDatatype ast() {
        return cast(getAST());
    }
    
    Query getQuery() {
        return getEnvironment().getQuery();
    }
    
    ASTQuery getAST() {
        return (ASTQuery) getEnvironment().getQuery().getAST();
    }
    
    IDatatype cast(Object obj) {
        return DatatypeMap.getValue(obj);
    }
    
    
    
    public IDatatype fib(IDatatype n) {
        switch (n.intValue()) {
            case 0: 
            case 1: return n;
            default: return fib(n.minus(DatatypeMap.ONE)).plus(fib(n.minus(DatatypeMap.TWO)));
        }
    }
    
    public IDatatype fibJava(IDatatype n) {
        return DatatypeMap.newInstance(fib(n.intValue()));
    }

    
    int fib(int n) {
        switch (n) {
            case 0: 
            case 1: return n;
            default: return fib(n-1) + fib(n-2);
        }
    }
    
    
    
}
