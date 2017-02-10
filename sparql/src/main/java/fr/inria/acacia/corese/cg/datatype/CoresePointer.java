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
    
    @Override
    public IDatatype getDatatype() {
        return dt;
    }
    
    @Override
    public Pointerable getPointerObject(){
        return pobject;
    }
    
    @Override
    public int pointerType(){
        return pobject.pointerType();
    }
    
    @Override
    public boolean isPointer(){
        return true;
    }
    
    @Override
    public Object getObject(){
        return pobject;
    }
    
    @Override
    public boolean isLoop(){
        return pobject != null && pobject instanceof Loopable;
    }
    
    @Override
    public Iterable getLoop(){
        return ((Loopable) pobject).getLoop();
    }

}
