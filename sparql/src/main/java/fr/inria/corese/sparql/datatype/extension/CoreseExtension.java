package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * Extension datatypes for list, map, xml, json
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 */
public class CoreseExtension extends CoreseUndefLiteral {
       
    public CoreseExtension(String value) {
        super(value);
    }
    
    @Override
    public String toString() {
        return display().toString();
    }
     
    @Override
    public boolean isExtension() {
        return true;
    } 
    
    @Override
    public boolean isUndefined() {
        return false;
    }
    
    @Override
    public IDatatype display() {
        return DatatypeMap.createUndef(getContent(), getDatatypeURI());
    }
    
    /**
     * Corese extension datatypes are comparable even when datatypes are different
     * list has its own equalsWE
     */
    @Override
    public boolean equalsWE(IDatatype dt) throws CoreseDatatypeException {
        if (dt.isExtension()) { // && getDatatype().equals(dt.getDatatype())) {
            return this == dt;
        }
        return super.equalsWE(dt);
    }
    
}
