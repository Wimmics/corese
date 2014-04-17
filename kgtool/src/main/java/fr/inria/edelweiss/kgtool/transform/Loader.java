/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgtool.transform;

import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Loader {
    
    private static Logger logger = Logger.getLogger(Loader.class);
    private String STL = NSManager.STL;
    public static final String PPLIB = "/template/";
    
    
    Graph graph;
    NSManager nsm;
    Transformer trans;
    
    Loader(Transformer t){
        graph = t.getGraph();
        nsm = t.getNSM();
        trans = t;
    }
    
     QueryEngine load(String pp) {
        QueryEngine qe = null; 
        if (pp == null) {
            qe = QueryEngine.create(graph);
        } else {
            Load ld = Load.create(graph);
            try {
                //ld.loadWE(pp);
                load(ld, pp);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                logger.error("Transformer Load Error: " + pp);
            }

            qe = ld.getQueryEngine();

            if (qe == null) {
                qe = QueryEngine.create(graph);

                if (ld.getRuleEngine() != null) {

                    RuleEngine re = ld.getRuleEngine();

                    for (Rule r : re.getRules()) {
                        Query q = r.getQuery();
                        qe.defQuery(q);
                    }
                }
            }

            for (Query q : qe.getTemplates()) {
                q.setPPrinter(pp, trans);
            }
            
            for (Query q : qe.getNamedTemplates()) {
                q.setPPrinter(pp, trans);
            }
            
            qe.sort();
        }
          
        return qe;

    }
    
    
       /**
     * Predefined transformations loaded from Corese resource or ns.inria.fr server
     */
    void load(Load ld, String pp) throws LoadException {
        String name = null;
        if (nsm.inNamespace(pp, STL)) {
            // predefined pprinter: st:owl st:spin
            // loaded from Corese resource
            name = nsm.strip(pp, STL);
        }  else {
            ld.loadWE(pp);
            return;
        }           
        String src = PPLIB + name;

        if (!ld.isRule(src)) {
            src = src + Load.RULE;                  
        }
        InputStream stream = getClass().getResourceAsStream(src);
        if (stream == null) {             
            try {
                URL uri = new URL(pp);                
                stream = uri.openStream();
            } catch (MalformedURLException ex) {
                throw LoadException.create(ex);
            } catch (IOException ex) {                
                throw LoadException.create(ex);
            }
            if (stream == null){
                throw LoadException.create(new IOException(pp));
            }
        }
        // use non synchronized load method because we may be inside a query 
        // with a read lock
        ld.loadRule(new InputStreamReader(stream), src);

    }


}
