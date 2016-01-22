package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {
  	private static final Logger logger = Logger.getLogger(Extension.class);
  
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
            String str = ql.read(in);
            QueryProcess exec = QueryProcess.create(Graph.create());
            try {
                exec.compile(str);
            } catch (EngineException ex) {
                logger.error(name);
                logger.error(ex);
            }
        } catch (IOException ex) {
            logger.error(ex);
        }

    }

}
