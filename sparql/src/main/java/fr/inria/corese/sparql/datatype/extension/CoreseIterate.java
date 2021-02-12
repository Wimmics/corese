package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseInteger;
import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;
import java.util.Iterator;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class CoreseIterate extends CoreseUndefLiteral implements Iterator<IDatatype> {

    private static final IDatatype dt = getGenericDatatype(IDatatype.ITERATE_DATATYPE);
    int start = 0, end = 0, step = 1, i = start;
    CoreseInteger res;
    CoreseInteger next;
    
    public CoreseIterate() {
        this(0, Integer.MAX_VALUE-1);
    }
    
    public CoreseIterate(int start, int end){
        this(start, end, (end < start) ? -1 : 1);
    }
    
    public CoreseIterate(int start, int end, int step){
        super("iterator");
        this.start = start;
        this.end = end;
        this.step = step;
        res  = CoreseInteger.create(0);
        next = CoreseInteger.create(0);
    }
    
    @Override
    public String toString() {
        return String.format("\"(%s,%s,%s)\"^^", start, end, step).concat(nsm().toPrefix(getDatatypeURI()));
    }
    
    @Override
    public boolean isLoop(){
        return true;
    }
    
    @Override
    public boolean isExtension() {
        return true;
    }
    
    void setStep(int step) {
        this.step = step;
    }
    
    @Override
    public IDatatype getDatatype() {
        return dt;
    }
    
     @Override
    public Iterator<IDatatype> iterator() {
         i = start;
         return this;
     }

    @Override
    public boolean hasNext() {
        return (step > 0) ? i <= end : i >= end;
    }

    @Override
    public IDatatype next() {
        res.setValue(i);
        i += step;
        //return this;
        return res;
    }
    
    @Override
    public int intValue(){
        return res.intValue();
    }
    
    @Override
    public IDatatype get(int n){
        switch(n){
            case 0: return res;
            case 1: 
                // because i is already the next value by next() above
                next.setValue(i);               
                return next;
                
            default: return null;
        }
    }
    
}
