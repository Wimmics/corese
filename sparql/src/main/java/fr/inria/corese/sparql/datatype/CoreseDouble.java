package fr.inria.corese.sparql.datatype;

import java.math.BigDecimal;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.FALSE;
import static fr.inria.corese.sparql.datatype.DatatypeMap.TRUE;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:double datatype used by Corese
 * <br>
 *
 * @author Olivier Savoie
 */
public class CoreseDouble extends CoreseNumber {

    static final CoreseURI datatype = new CoreseURI(RDF.xsddouble);
    static final int code = DOUBLE;
    protected double dvalue = 0;

    CoreseDouble() {}
    
    public CoreseDouble(String label) {
        setLabel(label);
        dvalue = Double.parseDouble(label);
    }

    public CoreseDouble(double val) {
        dvalue = val;
        setLabel(getNormalizedLabel());
    }
    
    // for computing, no generated label
    public static CoreseDouble create(double val) {
        CoreseDouble dt = new CoreseDouble();
        dt.setValue(val);
        return dt;
    }
    
    void setValue(double val){
        dvalue = val;
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
    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(dvalue);
    }

    @Override
    public boolean booleanValue() {
        return dvalue != 0.0;
    }

    @Override
    public double doubleValue() {
        return dvalue;
    }

    @Override
    public float floatValue() {
        return (float) dvalue;
    }

    @Override
    public long longValue() {
        return (long) dvalue;
    }

    @Override
    public int intValue() {
        return (int) dvalue;
    }

    @Override
    public int compare(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                double d1 = iod.doubleValue();
                return (dvalue < d1) ? -1 : (d1 == dvalue ? 0 : 1);
            default:
                throw failure();
        }
    }

    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return dvalue < iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return dvalue <= iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return dvalue > iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return dvalue >= iod.doubleValue();
            default:
                throw failure();
        }
    }

    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return dvalue == iod.doubleValue();

            //case UNDEF:
            case URI:
            case BLANK: case TRIPLE:
                return false;

            default:
                throw failure();
        }
    }
    
     @Override
    public IDatatype eq(IDatatype dt)  {
        switch (dt.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue == dt.doubleValue()) ? TRUE : FALSE;

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
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue == dt.doubleValue()) ? FALSE : TRUE;

            case URI:
            case BLANK: case TRIPLE:
                return TRUE;

            default:
                return null;
        }
    }
    
    @Override
    public IDatatype lt(IDatatype iod) {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue < iod.doubleValue()) ? TRUE : FALSE;
            default:
                return null;
        }
    }

    @Override
    public IDatatype le(IDatatype iod) {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue <= iod.doubleValue()) ? TRUE : FALSE;
            default:
                return null;
        }
    }
    
     @Override
    public IDatatype ge(IDatatype iod) {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue >= iod.doubleValue()) ? TRUE : FALSE;
            default:
                return null;
        }
    }
    
     @Override
    public IDatatype gt(IDatatype iod) {
        switch (iod.getCode()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
                return (dvalue > iod.doubleValue()) ? TRUE : FALSE;
            default:
                return null;
        }
    }
    
    @Override
    public String getLabel() {
        String str = super.getLabel();
        if (str == null) {
            return getNormalizedLabel();
        }
        return str;
    }

    @Override
    public String getNormalizedLabel() {
        return getNormalizedLabel(Double.toString(dvalue));
    }

//    public String getNormalizedLabel2() {
//        String label = Double.toString(dvalue);
//        String str = infinity(label);
//        return (str == null) ? label : str;
//    }

    public String getNormalizedLabel(String label) {
        String str = infinity(label);
        return (str == null) ? label : str;
    }

    static String infinity(String label) {
        if (label.equals("Infinity") || label.equals("+Infinity")) {
            return "INF";
        } else if (label.equals("-Infinity")) {
            return "-INF";
        }
        return null;
    }


    @Override
    public String getLowerCaseLabel() {
        return getLabel();
    }
}