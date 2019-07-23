package fr.inria.corese.core.util;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {
  	private static final Logger logger = LoggerFactory.getLogger(Extension.class);
  
    private static final String[] NAMES = 
    { "system.rq", "extension.rq", "shape.rq", "calendar.rq", "calendar2.rq", "spqr.rq" };
    
    public void process(){
        for (String name : NAMES){
            process("/function/system/" + name);
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
                logger.error(name, ex);
            }
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
        }

    }

}
