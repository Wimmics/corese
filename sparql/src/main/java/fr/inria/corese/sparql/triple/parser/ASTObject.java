package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.core.PointerObject;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ASTObject extends PointerObject {
    
    @Override
    public Iterable getLoop(){
        return getList().getValues();       
    }
    
    
    @Override 
    public IDatatype getValue(String var, int n){
        int i = 0;
        for (IDatatype dt : getList().getValues()){
            if (i++ == n){
                return dt;
            }
        }
        return null;
    }
    
    public IDatatype getList(){
            return DatatypeMap.createList();
    }


}
