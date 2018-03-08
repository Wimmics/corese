package fr.inria.corese.kgengine.junit;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.util.SPINProcess;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class Workflow {
    
    public static void main(String args[]) throws EngineException{
        new Workflow().test();
    }
    
    void test() throws EngineException{
        String q = "select * where {?x ?p ?y}";
                
        SPINProcess sp = SPINProcess.create();
        String spin = sp.toSpin(q);
        System.out.println(spin);
        
        String sparql = sp.toSparql(spin);
        System.out.println(sparql);
    }
    
    void rule() throws EngineException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        
        ld.load("data.rdf");
        
        ld.load("rule.rul");
        
        RuleEngine re = ld.getRuleEngine();
        re.process();
        
        String q = "select * "
                + "from kg:rule "
                + "where {?x ?p ?y }";
        
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        System.out.println(map);
        
    }

}
