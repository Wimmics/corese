package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.UUID;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class UUIDFunction extends TermEval {  
    private static final String URN_UUID = "urn:uuid:";
    
    public UUIDFunction(){}
    
    public UUIDFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        switch (oper()){
            case ExprType.FUUID:return uuid();
            default: return struuid();                
        }
    }
    
    IDatatype struuid() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return DatatypeMap.newLiteral(str);
    }
       
    IDatatype uuid() {
        return DatatypeMap.createResource(getUUID());
    }
    
    public static String getUUID() {
        return URN_UUID + UUID.randomUUID();
    }

   
}
