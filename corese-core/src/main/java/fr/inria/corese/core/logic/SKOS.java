package fr.inria.corese.core.logic;

import fr.inria.corese.sparql.triple.parser.NSManager;


public interface SKOS {
    
    public static String NS = NSManager.SKOS;
    
    public static String BROADER  = NS + "broader";  // has for broader = subConceptOf
    public static String NARROWER = NS + "narrower"; // has for narrower
    
}
