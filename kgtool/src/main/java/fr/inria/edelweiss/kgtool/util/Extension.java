package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.InputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {
  	private static final Logger logger = LogManager.getLogger(Extension.class);
  
    private static final String[] NAMES = 
    { "system.rq", "extension.rq", "calendar.rq", "calendar2.rq", "spqr.rq" };
    
    public void process(){
        for (String name : NAMES){
            process("/query/" + name);
        }
    }
    
     void process(String name) {
        InputStream in = Extension.class.getResourceAsStream(name);
        if (in == null){
            logger.error("QueryProcess resource not found: " + name);
            return;
        }
        try {
            QueryLoad ql = QueryLoad.create();
            String str = ql.readWE(in);
            QueryProcess exec = QueryProcess.create(Graph.create());
            try {
                exec.compile(str);
            } catch (EngineException ex) {
                logger.error(name);
                logger.error(ex);
            }
        } catch (LoadException ex) {
            logger.error(ex);
        }

    }

}
