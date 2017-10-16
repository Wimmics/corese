package fr.inria.corese.triple.function.core;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.UUID;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class UUIDFunction extends TermEval {  
    private static final String URN_UUID = "urn:uuid:";
    
    public UUIDFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
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
        UUID uuid = UUID.randomUUID();
        String str = URN_UUID + uuid;
        return DatatypeMap.createResource(str);
    }

   
}
