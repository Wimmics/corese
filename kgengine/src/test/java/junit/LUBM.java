/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package junit;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryEngine;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class LUBM {
    
    String data = "/user/corby/home/Work/uba1.7/";

    @Test
    public void testLUBM() throws EngineException{
        Graph g = Graph.create(true);
        g.setDebug(true);
        Load ld = Load.create(g);
        
        ld.setDebug(true);
        //
       // ld.setMax(1);
        
        //ld.load(data + "University0_0.owl");
        System.out.println("Load data");
        
        Date d1 = new Date();

        String init = "load </home/corby/Work/uba1.7/data>";
       // QueryProcess qp = QueryProcess.create(g);
        //qp.query(init);
       ld.load(data + "data");
        
        Date d2 = new Date();
        System.out.println("** Load Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
        System.out.println("graph size: " + g.size());
        
        ld.setMax(Integer.MAX_VALUE);
        
        
        d1 = new Date();        
        g.init();
        d2 = new Date();       
        System.out.println("** Index + RDFS Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

        //g.getWorkflow().setActivate(false);

        ld.load(data + "query");
        ld.load(data + "rdfs.rul");

        RuleEngine re = ld.getRuleEngine();
        //re.setDebug(true);
        System.out.println("graph size: " + g.size());
        System.out.println("rule engine");
        re.process();
        System.out.println("graph size: " + g.size());
                  
        QueryEngine qe = ld.getQueryEngine();
        
        for (Query qq : qe.getQueries()){
      


//        QueryLoad ql = QueryLoad.create();
//        
//        String q1 = ql.read(data + "query/q14.rq");
//        
            QueryProcess exec = QueryProcess.create(g);

             d1 = new Date();

            Mappings map = exec.query(qq);

             d2 = new Date();

            System.out.println(qq.getAST());
            System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
            System.out.println(map.size());
        }
        //System.out.println(map);

    }

}
