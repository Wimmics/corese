/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import java.util.HashMap;

/**
 * Execution Context for SPARQL Query and Template
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Context {

    public static final String STL          = NSManager.STL;
    public static final String STL_QUERY    = STL + "query";
    public static final String STL_NAME     = STL + "name"; // query path name
    public static final String STL_SERVICE  = STL + "service";
    public static final String STL_PROFILE  = STL + "profile";
    public static final String STL_TRANSFORM= STL + "transform";
    public static final String STL_URI      = STL + "uri";
    
    HashMap<String, IDatatype> table;

    public Context() {
        table = new HashMap();
    }

    public Context set(String name, IDatatype value) {
        table.put(name, value);
        return this;
    }

    public Context set(String name, String str) {
        table.put(name, DatatypeMap.newInstance(str));
        return this;
    }

    public Context setURI(String name, String str) {
        table.put(name, DatatypeMap.newResource(str));
        return this;
    }

    public IDatatype get(String name) {
        return table.get(name);
    }
}
