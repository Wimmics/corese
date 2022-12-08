
package fr.inria.corese.test.storage;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.jena.JenaTdb1DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 */
public class ThreadWithStorage {
    
    private static final String STORAGE = "/user/corby/home/AADemoNew/storage/humantest";
    static final String UPDATE = "load </user/corby/home/AADemoNew/human/human.rdf>";
    static final String QUERY = "select * where {?s ?p ?o} limit 10";
    
    JenaTdb1DataManager dataManager;
    
    public static void main(String[] args) throws LoadException, EngineException {
        new ThreadWithStorage().test();
    }
    
    
    void test() {
        init();
        
        for (int i=0; i<100; i++) {
            double dice = Math.random();
            boolean bupdate = dice > 0.66;
            boolean bquery  = dice < 0.33;
            boolean btrans = dice >= 0.33 && dice <= 0.66;
                    
            String query = UPDATE;
            if (i>0) {
                query = bupdate ? UPDATE : QUERY;
            }
            if (btrans) {
                //new ThreadTransform(i).start();
            }
            else {
                new ThreadTest(i, query).start();            
            }
        }
    }
    
    
    class ThreadTest extends Thread {
        
        String query;
        int num;
        
        ThreadTest(int n, String q) {
            query = q;
            num = n;
        }
    
        @Override
        public void run() {
            QueryProcess exec = QueryProcess.create(Graph.create(), dataManager);
            try {
                Mappings map = exec.query(query);
                System.out.println(num + " " + query);
            } catch (EngineException ex) {
                Logger.getLogger(ThreadWithStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
    
    class ThreadTransform extends Thread {
        int num;
        
        ThreadTransform(int n) {
            num = n;
        }
        
        @Override
        public void run() {
            try {
                Transformer t = Transformer.createWE(QueryProcess.create(dataManager), Transformer.TURTLE);
                t.transform();
                System.out.println("transform : " + num);
            } catch (EngineException|LoadException ex) {
                Logger.getLogger(ThreadWithStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    void init()  {
        dataManager = new JenaTdb1DataManagerBuilder().storagePath(STORAGE).build();
//        Graph g = Graph.create();
//        Load ld = Load.create(g, dataManager);
//        ld.parse("/user/corby/home/AADemoNew/human/human.rdf");
//        
//        RuleEngine re = RuleEngine.create(g, dataManager);
//        re.setProfile(RuleEngine.Profile.OWLRL);
//        re.setTrace(true);
//        re.process();
    }
    
    
    
    
}
