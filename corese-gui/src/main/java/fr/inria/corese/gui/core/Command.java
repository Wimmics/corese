package fr.inria.corese.gui.core;

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
        
    
    Command (String[] args) {
        init(args);
    }
    
    void init(String[] args) {
        int i = 0;
        while (i < args.length){
            String str = args[i++];
            switch (str) {
                case VERBOSE:
                case "-v":
                    put(VERBOSE, "true");
                    break;                                  
                    
                case MAX_LOAD:
                   put(MAX_LOAD, args[i++]);
                   break; 
                   
                default:
                    put(str, "true");
                    break;   
            }
        }
    }

}
