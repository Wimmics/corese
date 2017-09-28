package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.query.Hierarchy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Emulate method inheritance on datatype hierarchy
 * return datatype list of rdf term
 * datatype hierarchy is:
 * dt:entity
 *  dt:resource
 *      dt:uri
 *      dt:bnode
 *  dt:literal
 *      dt:standard
 *      dt:extended
 * 
 * xsd datatypes are under dt:standard
 * dt  datatypes are under dt:extended
 * 
 * xsd datatype hierarchy is not defined yet, it is flat under dt:standard
 * 
 * method is attached to datatype like this:
 * @type dt:literal
 * @type dt:standard
 * @type xsd:int
 * 
 * method is available for datatypes equal or under the @type
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class DatatypeHierarchy implements Hierarchy {
    
    HashMap<String, List<String>> hierarchy;
    private boolean debug = false;
    
    public DatatypeHierarchy() {
        hierarchy = new HashMap<String, List<String>>();
        init();
    }
    
    void init(){   
        defResource(IDatatype.URI_DATATYPE);
        defResource(IDatatype.BNODE_DATATYPE);                     
    }
    
    public void defSuperType(String name, String sup){
        List<String> list = superTypes(name);
        if (list == null){
            list = new ArrayList<String>();
            hierarchy.put(name, list);
        }
        list.add(sup);
    }
    
    public List<String> superTypes(String name) {
        return hierarchy.get(name);
    }

    @Override
    public List<String> getSuperTypes(DatatypeValue value, DatatypeValue type) {
        List<String> list = getSuperTypes((IDatatype) value);
        if (isDebug()) System.out.println("DH: " + value + " " + list);
        return list;
    }
      
    List<String> getSuperTypes(IDatatype dt) {
        String name = xt_kind(dt).stringValue();
        List<String> list = hierarchy.get(name);
        if (list != null) {
            return list;
        }
        if (name.startsWith(XSD.XSD)) {
            defLiteral(name, IDatatype.STANDARD_DATATYPE);
            return getSuperTypes(dt);
        }
        if (name.startsWith(NSManager.DT)) {
            defLiteral(name, IDatatype.EXTENDED_DATATYPE);            
            return getSuperTypes(dt);
        }
        return list;
    }
    
    void defResource(String name){
        defSuperType(name, name);
        defSuperType(name, IDatatype.RESOURCE_DATATYPE);
        defSuperType(name, IDatatype.ENTITY_DATATYPE);
    }
    
    void defLiteral(String name, String type) {
        defSuperType(name, name);
        defSuperType(name, type);
        defSuperType(name, IDatatype.LITERAL_DATATYPE);
        defSuperType(name, IDatatype.ENTITY_DATATYPE);
    }
   
    IDatatype xt_kind(IDatatype dt) {
        if (dt.isLiteral()) {
            return dt.getDatatype();
        }
        if (dt.isURI()) {
            return DatatypeMap.URI_DATATYPE;
        }
        return DatatypeMap.BNODE_DATATYPE;
    }
    
     /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}

