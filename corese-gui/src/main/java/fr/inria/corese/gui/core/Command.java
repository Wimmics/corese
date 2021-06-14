package fr.inria.corese.gui.core;

import fr.inria.corese.core.Graph;
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
    public static final String SUPER_USER       = "-su";
    public static final String READ_FILE        = "-read";
    public static final String METADATA         = "-metadata";
    public static final String STRING           = "-string";
    public static final String WORKFLOW         = "-wf";
    public static final String REENTRANT        = "-re";
    public static final String RDF_STAR         = "-rdfstar";
    public static final String ACCESS           = "-access";
    public static final String PARAM            = "-param";
    public static final String LOAD             = "-load";
    public static final String LOAD_QUERY       = "-query";
        
    String[] args;
    private String query;
    
    Command (String[] args) {
        this.args = args;
    }
    
    Command init() {
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
                    
                case PARAM:
                   put(PARAM, args[i++]);
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
                    
                case LOAD:
                    put(LOAD, args[i++]);
                    break;
                    
                case LOAD_QUERY:
                    setQuery(args[i++]);
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
        return this;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    

}
