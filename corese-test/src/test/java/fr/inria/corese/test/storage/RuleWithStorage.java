
package fr.inria.corese.test.storage;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.jena.JenaTdb1DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 */
public class RuleWithStorage {
    
    private static final String STORAGE = "/user/corby/home/AADemoNew/storage/humantest";
    private static final String DATASET = "/user/corby/home/AADemoNew/human/human.rdf";
    
    
    public static void main(String[] args) throws LoadException, EngineException {
        new RuleWithStorage().test();
    }
    
    
    void test() throws LoadException, EngineException {
        DataManager man = init();
        //ruleengine(man);
        transformer(man);
    }
    
    void testGraph() throws LoadException, EngineException {
        Graph g = initGraph();
        //ruleengine(man);
        transformer(g);
    }
    
    void transformer(DataManager man) throws LoadException, EngineException {
        Transformer t = Transformer.createWE(QueryProcess.create(man), Transformer.TURTLE);
        //man.startReadTransaction();
        System.out.println(t.transform());
        //t.transform();
        //man.endReadTransaction();
    }
    
    DataManager init() throws LoadException {
        JenaTdb1DataManager man = new JenaTdb1DataManagerBuilder().storagePath(STORAGE).build();
        Graph g = Graph.create();
        Load ld = Load.create(g, man);
        //ld.parse("/user/corby/home/AADemoNew/human/human.rdf");
        return man;
    }
    
    void transformer(Graph g) throws LoadException, EngineException {
        Transformer t = Transformer.createWE(g, Transformer.TURTLE);
        System.out.println(t.transform());
    }
    
    Graph initGraph() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(DATASET);
        return g;
    }
    
    void ruleengine(DataManager man) throws LoadException, EngineException {                
        RuleEngine re = RuleEngine.create(Graph.create(), man);
        re.setProfile(RuleEngine.Profile.OWLRL);
        re.setTrace(true);
        re.process();
    }
    
}
