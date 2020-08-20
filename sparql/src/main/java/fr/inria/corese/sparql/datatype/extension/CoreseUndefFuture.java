package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class CoreseUndefFuture extends CoreseUndefLiteral {
    static final String FUTURE = "Future";
    private Object object;


    public CoreseUndefFuture() {
        super(FUTURE);
    }



    /**
     * @return the object
     */
    @Override
    public Object getObject() {
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
