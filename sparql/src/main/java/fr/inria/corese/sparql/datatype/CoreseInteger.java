package fr.inria.corese.sparql.datatype;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.FALSE;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.TRUE;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

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
    private static Logger logger = LoggerFactory.getLogger(CoreseInteger.class);
    public static final CoreseInteger ZERO = new CoreseInteger(0);
    public static final CoreseInteger ONE = new CoreseInteger(1);
    public static final CoreseInteger TWO = new CoreseInteger(2);
    public static final CoreseInteger THREE = new CoreseInteger(3);
    public static final CoreseInteger FOUR = new CoreseInteger(4);
    public static final CoreseInteger FIVE = new CoreseInteger(5);
    static final CoreseURI datatype = new CoreseURI(RDF.xsdinteger);
    static final int code = INTEGER;
    long lvalue;

    CoreseInteger() {}
    
    public CoreseInteger(String value) {
        setLabel(value);
        lvalue =  Long.parseLong(value);
    }

    public CoreseInteger(int value) {
        lvalue = value;
        setLabel(Long.toString(value));
    }
    
    public CoreseInteger(long value) {
        lvalue = value;
        setLabel(Long.toString(value));
    }
    
    // for computing, without stored string label
    public static CoreseInteger create(long value) {
        CoreseInteger i = new CoreseInteger();
        i.setValue(value);
        return i;
    }
    
    // for computing, without stored string label
    public static CoreseInteger create(int value) {
        CoreseInteger i = new CoreseInteger();
        i.setValue(value);
        return i;
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
        return booleanValue();
    }
    
    @Override
    public boolean isXSDInteger() { 
        return true;
    }
    
     @Override
    public boolean booleanValue() {
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
    
    @Override
    public void setValue(int n){
        lvalue = n;
    }
    
    void setValue(long n){
        lvalue = n;
    }

    @Override
    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(lvalue);
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
            case BLANK: case TRIPLE:
                return false;
                    
            default:
                throw failure();
        }
    }
    
     @Override 
     public IDatatype eq(IDatatype dt) {  
        switch (dt.getCode()) {
            case INTEGER:
                return (lvalue == dt.longValue()) ? TRUE : FALSE;

            case FLOAT:
            case DECIMAL:
            case DOUBLE:
                return (doubleValue() == dt.doubleValue()) ? TRUE : FALSE ;
                
            case URI:
            case BLANK: case TRIPLE:
                return FALSE;
                    
            default:
                return null;
        }
    }
     
     @Override 
     public IDatatype neq(IDatatype dt) {  
        switch (dt.getCode()) {
            case INTEGER:
                return (lvalue == dt.longValue()) ? FALSE : TRUE;

            case FLOAT:
            case DECIMAL:
            case DOUBLE:
                return (doubleValue() == dt.doubleValue()) ? FALSE : TRUE ;
                
            case URI:
            case BLANK: case TRIPLE:
                return TRUE;
                    
            default:
                return null;
        }
    }
    
     @Override
    public IDatatype lt(IDatatype dt) {  
        switch (dt.getCode()){
            case INTEGER:
                return (lvalue < dt.longValue()) ? TRUE : FALSE;
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (doubleValue() < dt.doubleValue()) ? TRUE : FALSE;    
        }
        return null;
    }
    
    @Override
    public IDatatype le(IDatatype dt) {  
        switch (dt.getCode()){
            case INTEGER:
                return (lvalue <= dt.longValue()) ? TRUE : FALSE;
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (doubleValue() <= dt.doubleValue()) ? TRUE : FALSE;    
        }
        return null;
    }
     
    @Override
    public IDatatype gt(IDatatype dt) {  
        switch (dt.getCode()){
            case INTEGER:
                return (lvalue > dt.longValue()) ? TRUE : FALSE;
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (doubleValue() > dt.doubleValue()) ? TRUE : FALSE;    
        }
        return null;
    }
    
    @Override
    public IDatatype ge(IDatatype dt) {
        switch (dt.getCode()) {
            case INTEGER:
                return (lvalue >= dt.longValue()) ? TRUE : FALSE;
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (doubleValue() >= dt.doubleValue()) ? TRUE : FALSE;
        }
        return null;
    }
    
//    @Override    
//    public IDatatype plus(IDatatype dt) {
//        switch (dt.getCode()) {
//            case DOUBLE:
//                return CoreseDouble.create(doubleValue() + dt.doubleValue());
//            case FLOAT:
//                return CoreseFloat.create(floatValue() + dt.floatValue());
//            case DECIMAL:
//                return CoreseDecimal.create(doubleValue() + dt.doubleValue());
//            case INTEGER:
//                try {
//                    return CoreseInteger.create(Math.addExact(longValue(), dt.longValue()));
//                } catch (ArithmeticException e) {
//                    return null;
//                }
//        }
//        return null;
//    }
//    
//    @Override
//    public IDatatype minus(IDatatype dt) {
//        switch (dt.getCode()) {
//            case DOUBLE:
//                return CoreseDouble.create(doubleValue() - dt.doubleValue());
//            case FLOAT:
//                return new CoreseFloat(floatValue() - dt.floatValue());
//            case DECIMAL:
//                return CoreseDecimal.create(doubleValue() - dt.doubleValue());
//            case INTEGER:
//                try {
//                    return CoreseInteger.create(Math.subtractExact(longValue(), dt.longValue()));
//                } catch (ArithmeticException e) {
//                    return null;
//                }
//        }
//        return null;
//    }
    
    @Override
    public IDatatype minus(long val) {
        return CoreseInteger.create(longValue() - val);
    }

    @Override
    public String getLowerCaseLabel() {
        return getLabel();
    }
    
    @Override
    public String getLabel() {
        String str = super.getLabel();
        if (str == null){
            str = Long.toString(longValue());
            setLabel(str);
        }
        return str;
    }

    @Override
    public boolean isInteger() {
        return true;
    }
}