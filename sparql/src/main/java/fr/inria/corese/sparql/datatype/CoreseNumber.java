package fr.inria.corese.sparql.datatype;

import java.math.BigDecimal;

import fr.inria.corese.sparql.api.IDatatype;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An abstract class, super-class of all CoreseNumber datatype; it defines
 * static methods
 * <br>
 *
 * @author Olivier Savoie
 */
public abstract class CoreseNumber extends CoreseDatatype {

    static final int code = NUMBER;
    private String label;

    /**
     * Cast a number to integer means take the integer part, not just parsing
     * the string label<br />
     * Cast a number to a boolean is always allowed and should always give a
     * result
     */

    @Override
    public IDatatype cast(String target){ 
        switch (DatatypeMap.getCode(target)){
            case INTEGER: return DatatypeMap.newInteger(longValue());
            case DOUBLE:  return DatatypeMap.newInstance(doubleValue());
            case FLOAT:   return DatatypeMap.newInstance(floatValue());
            case DECIMAL: return DatatypeMap.newDecimal(decimalValue());
            case GENERIC_INTEGER: 
                return DatatypeMap.newInstance(Integer.toString(intValue()), target); 
                
            case STRING:  return DatatypeMap.newInstance(getLabel());
            case BOOLEAN: return castBoolean();
   
            default:                                               
                return super.cast(target);
        }        
    }
    
    IDatatype castBoolean() {
        if (longValue() == 0) {
            return CoreseBoolean.FALSE;
        } else if (longValue() == 1) {
            return CoreseBoolean.TRUE;
        } else {
            return null;
        }
    }
    

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean isTrueAble() {
        return true;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override    
    public IDatatype plus(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                return CoreseDouble.create(doubleValue() + dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() + dt.doubleValue());
                    default:
                        return CoreseFloat.create(floatValue() + dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() + dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() + dt.floatValue());
                    case DECIMAL:
                    case INTEGER:
                        return CoreseDecimal.create(decimalValue().add(dt.decimalValue()));
                    default:
                        return CoreseDecimal.create(decimalValue().add(BigDecimal.valueOf(dt.doubleValue())));
                }
           
            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() + dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() + dt.floatValue());
                    case DECIMAL:
                        return CoreseDecimal.create(decimalValue().add(dt.decimalValue()));
                    case INTEGER:
                        try {
                            long l = Math.addExact(longValue(), dt.longValue());
                            return  result(dt, l);
                        }
                        catch (ArithmeticException e) {
                            return null;
                        }
                }
        }
        return null;
    }
    
    // integer type promotion
    IDatatype result(IDatatype dt, long res) {
        if (isXSDInteger() && dt.isXSDInteger()) {
            return CoreseInteger.create(res);
        }
        if (getDatatypeURI().equals(XSD.xsdlong) && dt.getDatatypeURI().equals(XSD.xsdlong)) {
            return  CoreseGenericInteger.create(res);
        }
        return CoreseInteger.create(res);
    }
    
    @Override
    public IDatatype minus(long val) {
        switch (getCode()) {
            case DOUBLE:
                return CoreseDouble.create(doubleValue() - val);
            case FLOAT:
                return CoreseFloat.create(floatValue() - val);
            case DECIMAL:
                return CoreseDecimal.create(decimalValue().subtract(new BigDecimal(val)));
            case INTEGER:
                return CoreseInteger.create(longValue() - val);
        }
        return null;
    }

    @Override
    public IDatatype minus(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                return CoreseDouble.create(doubleValue() - dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() - dt.doubleValue());
                    default:
                        return CoreseFloat.create(floatValue() - dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() - dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() - dt.floatValue());
                    case DECIMAL:
                    case INTEGER:
                        return CoreseDecimal.create(decimalValue().subtract(dt.decimalValue()));
                    default:
                        return CoreseDecimal.create(doubleValue() - dt.doubleValue());
                }          

            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() - dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() - dt.floatValue());
                    case DECIMAL:
                        return CoreseDecimal.create(decimalValue().subtract(dt.decimalValue()));
                    case INTEGER:
                        try {
                            long l = Math.subtractExact(longValue(), dt.longValue());
                            return  result(dt, l);    
                        }
                        catch (ArithmeticException e){
                            return null;
                        }                   
                }
        }
        return null;
    }


    @Override
    public IDatatype mult(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                    return CoreseDouble.create(doubleValue() * dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() * dt.doubleValue());
                    default:
                        return CoreseFloat.create(floatValue() * dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() * dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() * dt.floatValue());
                    case INTEGER:
                    case DECIMAL:
                        return CoreseDecimal.create(decimalValue().multiply(dt.decimalValue()));
                    default:
                        return CoreseDecimal.create(doubleValue() * dt.doubleValue());
                }          

            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return CoreseDouble.create(doubleValue() * dt.doubleValue());
                    case FLOAT:
                        return CoreseFloat.create(floatValue() * dt.floatValue());
                    case DECIMAL:
                        return CoreseDecimal.create(decimalValue().multiply(dt.decimalValue()));
                    case INTEGER:
                        try {
                            long l = Math.multiplyExact(longValue(), dt.longValue());
                            return  result(dt, l);
                        } catch (ArithmeticException e) {
                            return null;
                        }
                }
        }
        return null;
    }
  

    @Override
    public IDatatype div(IDatatype dt) {

        if (dt.doubleValue() == 0.0) {
            return null;
        }

        try {

            switch (getCode()) {
                case DOUBLE:
                        return CoreseDouble.create(doubleValue() / dt.doubleValue());

                case FLOAT:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return CoreseDouble.create(doubleValue() / dt.doubleValue());
                        default:
                            return CoreseFloat.create(floatValue() / dt.floatValue());
                    }

                case DECIMAL:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return CoreseDouble.create(doubleValue() / dt.doubleValue());
                        case FLOAT:
                            return CoreseFloat.create(floatValue() / dt.floatValue());
                        case DECIMAL:
                        case INTEGER:
                            // decimal divide fail on:
                            // java.lang.ArithmeticException: Non-terminating decimal expansion; 
                            // no exact representable decimal result. "1.0"^^xsd:decimal 6
                            // CoreseDecimal.create(new BigDecimal(doubleValue()).divide(new BigDecimal(dt.doubleValue())));
                            return CoreseDecimal.create(doubleValue() / dt.doubleValue());
                        default:
                            return CoreseDecimal.create(doubleValue() / dt.doubleValue());
                    }
              
                case INTEGER:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return CoreseDouble.create(doubleValue() / dt.doubleValue());
                        case FLOAT:
                            return CoreseDecimal.create(decimalValue().divide(dt.decimalValue()));
                        case DECIMAL:
                        case INTEGER:                            
                            return CoreseDecimal.create(doubleValue() / dt.doubleValue());
                        default:                            
                            return CoreseDecimal.create(longValue() / dt.doubleValue());
                    }
            }
        } catch (java.lang.ArithmeticException a) {
            logger.error(a.toString() + " " + this + " " + dt);
        }
        return null;
    }

   
    @Override
    public String getLabel() {
        return label;
    }

    
    public void setLabel(String label) {
        this.label = label;
    }

}