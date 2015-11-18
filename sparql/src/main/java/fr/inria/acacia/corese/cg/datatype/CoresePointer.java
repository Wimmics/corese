package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import static fr.inria.acacia.corese.cg.datatype.CoreseDatatype.getGenericDatatype;
import fr.inria.edelweiss.kgram.api.core.Loopable;
import fr.inria.edelweiss.kgram.api.core.Pointerable;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class CoresePointer extends CoreseUndefLiteral {
    private static final IDatatype dt = getGenericDatatype(IDatatype.POINTER);

    Pointerable pobject;
    
    CoresePointer (Pointerable obj){
        this("pointer", obj);
    }
        
    CoresePointer (String name, Pointerable obj){
        super(name);
        pobject = obj;
    } 
    
    public IDatatype getDatatype() {
        return dt;
    }
    
    public Pointerable getPointerObject(){
        return pobject;
    }
    
    public int pointerType(){
        return pobject.pointerType();
    }
    
    public boolean isPointer(){
        return true;
    }
    
    public Object getObject(){
        return pobject;
    }
    
    public boolean isLoop(){
        return pobject != null && pobject instanceof Loopable;
    }
    
    public Iterable getLoop(){
        return ((Loopable) pobject).getLoop();
    }

}
