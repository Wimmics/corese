package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class NSDatatypeMap extends DatatypeMap {
    
    static NSManager nsm = NSManager.create();
    
    
    public static void defPrefix(String p, String ns){
        nsm.defPrefix(p, ns);
    }
    
    // foaf:name
    public static IDatatype createQName(String qname){
        return createResource(nsm.toNamespace(qname));
    }
    
    

}
