package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author corby
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

    
}
