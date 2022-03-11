package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;

/**
 * Encapsulate an expression Expr to be evaluated later such as concat(str, st:number(), str)
 * use case: template with st:number() 
 * Exist only during template processing
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class CoreseUndefFuture extends CoreseUndefLiteral {
    static final String FUTURE = "Future";
    // Expr expression
    private Object object;


    public CoreseUndefFuture() {
        super(FUTURE);
    }



    /**
     * @return the object
     */
    @Override
    public Object getNodeObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    @Override
    public void setObject(Object object) {
        this.object = object;
    }


    /**
     * @return the isFuture
     */
    @Override
    public boolean isFuture() {
        return true;
    }

}
