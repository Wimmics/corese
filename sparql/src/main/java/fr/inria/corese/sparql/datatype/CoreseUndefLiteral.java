package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * This is used for unknown literals that carry their own datatype
 * <br>
 *
 * @author Olivier Corby
 */
public class CoreseUndefLiteral extends CoreseStringLiteral {

    static final int code = UNDEF;
    //static final String FUTURE = "Future";

    IDatatype datatype = null;
//    private Object object;
//    private boolean isFuture = false;
    static final CoreseUndefLiteral ERROR, UNBOUND;

    static {
        ERROR = new CoreseUndefLiteral("Error", IDatatype.SYSTEM);
        UNBOUND = new CoreseUndefLiteral("Unbound", IDatatype.SYSTEM);
    }

    public CoreseUndefLiteral(String value) {
        super(value);
    }

    public CoreseUndefLiteral(String value, String dt) {
        super(value);
        setDatatype(dt);
    }

//  public CoreseUndefLiteral() {
//      super(FUTURE);
//  }
    @Override
    public void setDatatype(String uri) {
        datatype = getGenericDatatype(uri);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }

    @Override
    public boolean isUndefined() {
        return true;
    }
    
    @Override
    public boolean isGeneralized() {
        return true;
    }

    @Override
    public boolean isTrue() throws CoreseDatatypeException {
        throw failure();
    }

    @Override
    public boolean isTrueAble() {
        return false;
    }

    void check(IDatatype iod) throws CoreseDatatypeException {
        if (iod.getCode() == UNDEF && getDatatype() != iod.getDatatype()) {
            throw failure();
        }
    }

    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {

        switch (iod.getCode()) {
            case URI:
            case BLANK:
                return false;
            // special case with literal !!!
            case LITERAL:
                return iod.equalsWE(this);

            case UNDEF:
                if (getDatatype() != iod.getDatatype()) {
                    throw failure();
                }
                break;

            default:
                throw failure();
        }

        boolean b = getLabel().equals(iod.getLabel());
        if (!b) {
            throw failure();
        }
        return b;

    }

    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
        return result(iod, getLabel().compareTo(iod.getLabel()) < 0);
    }

    @Override
    public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
        return result(iod, getLabel().compareTo(iod.getLabel()) <= 0);
    }

    @Override
    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
        return result(iod, getLabel().compareTo(iod.getLabel()) > 0);
    }

    @Override
    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
        return result(iod, getLabel().compareTo(iod.getLabel()) >= 0);
    }

    boolean result(IDatatype dt, boolean b) throws CoreseDatatypeException {
        if (isCompatible(dt)) {
            return b;
        }
        throw failure();
    }

    boolean isCompatible(IDatatype dt) {
        return dt.isGeneralized()&& getDatatypeURI().equals(dt.getDatatypeURI());
    }

//    /**
//     * @return the object
//     */
//    @Override
//    public Object getObject() {
//        return object;
//    }
//
//    /**
//     * @param object the object to set
//     */
//    @Override
//    public void setObject(Object object) {
//        this.object = object;
//    }
//
//    /**
//     * @return the isFuture
//     */
//    @Override
//    public boolean isFuture() {
//        return isFuture;
//    }
//
//    /**
//     * @param isFuture the isFuture to set
//     */
//    public void setFuture(boolean isFuture) {
//        this.isFuture = isFuture;
//    }
}
