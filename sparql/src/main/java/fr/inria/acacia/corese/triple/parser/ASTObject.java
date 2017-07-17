package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.core.PointerObject;

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
