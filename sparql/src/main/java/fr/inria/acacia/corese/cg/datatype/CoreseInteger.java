package fr.inria.acacia.corese.cg.datatype;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:integer datatype used by Corese
 * <br>
 *
 * @author Olivier Savoie
 */
public class CoreseInteger extends CoreseNumber {

    /**
     * logger from log4j
     */
    private static Logger logger = LogManager.getLogger(CoreseInteger.class);
    public static final CoreseInteger ZERO = new CoreseInteger(0);
    public static final CoreseInteger ONE = new CoreseInteger(1);
    public static final CoreseInteger TWO = new CoreseInteger(2);
    public static final CoreseInteger THREE = new CoreseInteger(3);
    public static final CoreseInteger FOUR = new CoreseInteger(4);
    public static final CoreseInteger FIVE = new CoreseInteger(5);
    static final CoreseURI datatype = new CoreseURI(RDF.xsdinteger);
    static final int code = INTEGER;
    long lvalue;

    public CoreseInteger(String value) {
        setLabel(value);
        lvalue = new Integer(value).intValue();
    }

    public CoreseInteger(int value) {
        lvalue = value;
        setLabel(Integer.toString(value));
    }
    
     public CoreseInteger(long value) {
        lvalue = value;
        setLabel(Long.toString(value));
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
    public boolean isTrue() {
        return lvalue != 0;
    }

    @Override
    public long longValue() {
        return lvalue;
    }

    @Override
    public int intValue() {
        return (int) lvalue;
    }
    
    void setValue(int n){
        lvalue = n;
    }

    @Override
    public double doubleValue() {
        return (double) lvalue;
    }

    @Override
    public float floatValue() {
        return (float) lvalue;
    }

    @Override
    public double getdValue() {
        return doubleValue();
    }

    @Override
    public int getiValue() {
        return intValue();
    }

    @Override
    public long getlValue() {
        return longValue();
    }

    @Override
    public int compare(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                long l = iod.longValue();
                return (lvalue < l) ? -1 : (l == lvalue ? 0 : 1);

            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                double d1 = iod.doubleValue();
                return (doubleValue() < d1) ? -1 : (d1 == doubleValue() ? 0 : 1);
            default:
                throw failure();
        }
    }

    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                return lvalue < iod.longValue();
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return doubleValue() < iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                return lvalue <= iod.longValue();
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return doubleValue() <= iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                return lvalue > iod.longValue();
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return doubleValue() > iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                return lvalue >= iod.longValue();
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return doubleValue() >= iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
                return lvalue == iod.longValue();

            case FLOAT:
            case DECIMAL:
            case DOUBLE:
                return doubleValue() == iod.doubleValue();
                
            //case UNDEF:
            case URI:
            case BLANK:
                return false;
                    
            default:
                throw failure();
        }
    }

    @Override
    public String getLowerCaseLabel() {
        return getLabel();
    }
    
    @Override
    public String getLabel() {
        String str = super.getLabel();
        if (str == null){
            return Long.toString(lvalue);
        }
        return str;
    }
}