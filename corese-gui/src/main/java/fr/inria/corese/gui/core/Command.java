package fr.inria.corese.gui.core;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import java.util.HashMap;

/**
 * Command line parser
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Command extends HashMap<String, String> {
    public static final String VERBOSE          = "-verbose";
    public static final String DEBUG            = "-debug";
    public static final String MAX_LOAD         = "-maxload";
    public static final String LINKED_FUNCTION  = "-linkedfunction";
    public static final String METADATA         = "-metadata";
    public static final String STRING           = "-string";
    public static final String WORKFLOW         = "-wf";
    public static final String REENTRANT        = "-re";
    public static final String RDF_STAR         = "-rdfstar";
        
    
    Command (String[] args) {
        init(args);
    }
    
    void init(String[] args) {
        int i = 0;
        while (i < args.length){
            String str = args[i++];
            switch (str) {
                case METADATA:
                case "-m":
                    put(METADATA, "true");
                    break;
                case VERBOSE:
                case "-v":
                    put(VERBOSE, "true");
                    break;  
                    
                case STRING:
                    put(STRING, "true");
                    break;
                    
                case MAX_LOAD:
                   put(MAX_LOAD, args[i++]);
                   break; 
                   
                case WORKFLOW:
                   put(WORKFLOW, args[i++]);
                   break; 
                   
                case REENTRANT:
                    put(REENTRANT, "true");
                    break;
                    
                 case RDF_STAR:
                    put(RDF_STAR, "true");
                    Graph.setEdgeMetadataDefault(true);
                    break;    
                                      
                default:
                    put(str, "true");
                    break;   
            }
        }
    }

}
