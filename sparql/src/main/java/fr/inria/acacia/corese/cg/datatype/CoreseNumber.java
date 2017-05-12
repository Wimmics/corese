package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

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
    public IDatatype cast(IDatatype target, IDatatype javaType) {
        return cast(target, javaType.getLabel());
    }   
    
    public IDatatype cast(IDatatype target, String javaType) {
        switch (DatatypeMap.getCode(target.getLabel())){
            case INTEGER: return DatatypeMap.newInteger(longValue());
            case LONG:    return DatatypeMap.newInstance(longValue());
            case DOUBLE:  return DatatypeMap.newInstance(doubleValue());
            case FLOAT:   return DatatypeMap.newInstance(floatValue());
            case DECIMAL: return DatatypeMap.newInstance(doubleValue(), RDF.xsddecimal);
            case GENERIC_INTEGER: 
                return DatatypeMap.newInstance(Integer.toString(intValue()), target.getLabel()); 
                
            case STRING:  return DatatypeMap.newInstance(getLabel());
            case BOOLEAN: return castBoolean();
               
            default:                                               
                return super.cast(target.getLabel(), javaType);
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
    public IDatatype cast(IDatatype datatype){
        String javaType = DatatypeMap.getJavaType(datatype.getLabel());
        if (javaType == null){return null;}
        return cast(datatype, javaType);
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
    public IDatatype getDatatype() {
        return datatype;
    }

    @Override
    public IDatatype plus(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                return new CoreseDouble(doubleValue() + dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() + dt.doubleValue());
                    default:
                        return new CoreseFloat(floatValue() + dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() + dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() + dt.floatValue());
                    default:
                        return new CoreseDecimal(doubleValue() + dt.doubleValue());
                }

            case LONG:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() + dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() + dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() + dt.doubleValue());
                    case LONG:     
                    case INTEGER:
                        return plus(longValue(), dt.longValue()); 
                }

            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() + dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() + dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() + dt.doubleValue());
                    case INTEGER:
                        return  new CoreseInteger(longValue() + dt.longValue());                   
                    case LONG:
                        return plus(longValue(), dt.longValue()); 
                }
        }
        return null;
    }
 
    IDatatype datatype(long res){
         return DatatypeMap.newInteger(res);
    }

    IDatatype plus(int x, int y) {
        try {
            return DatatypeMap.newInstance(Math.addExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    IDatatype plus(long x, long y) {
        try {
            return new CoreseLong(Math.addExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    @Override
    public IDatatype minus(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                return new CoreseDouble(doubleValue() - dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() - dt.doubleValue());
                    default:
                        return new CoreseFloat(floatValue() - dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() - dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() - dt.floatValue());
                    default:
                        return new CoreseDecimal(doubleValue() - dt.doubleValue());
                }

            case LONG:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() - dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() - dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() - dt.doubleValue());
                    case INTEGER:  
                    case LONG:
                        return minus(longValue(), dt.longValue()); 
                }

            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() - dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() - dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() - dt.doubleValue());
                    case INTEGER:
                        return new CoreseInteger(longValue() - dt.longValue()); 
                    case LONG:
                        return minus(longValue(), dt.longValue()); 
                }
        }
        return null;
    }

    IDatatype minus(int x, int y) {
        try {
            return DatatypeMap.newInstance(Math.subtractExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    IDatatype minus(long x, long y) {
        try {
            return new CoreseLong(Math.subtractExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    @Override
    public IDatatype mult(IDatatype dt) {
        switch (getCode()) {
            case DOUBLE:
                return new CoreseDouble(doubleValue() * dt.doubleValue());

            case FLOAT:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() * dt.doubleValue());
                    default:
                        return new CoreseFloat(floatValue() * dt.floatValue());
                }

            case DECIMAL:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() * dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() * dt.floatValue());
                    default:
                        return new CoreseDecimal(doubleValue() * dt.doubleValue());
                }

            case LONG:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() * dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() * dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() * dt.doubleValue());
                    case INTEGER:
                        return mult(longValue(), dt.longValue());
                    case LONG:
                        return mult(longValue(), dt.longValue());
                }

            case INTEGER:
                switch (dt.getCode()) {
                    case DOUBLE:
                        return new CoreseDouble(doubleValue() * dt.doubleValue());
                    case FLOAT:
                        return new CoreseFloat(floatValue() * dt.floatValue());
                    case DECIMAL:
                        return new CoreseDecimal(doubleValue() * dt.doubleValue());
                    case INTEGER:
                        return new CoreseInteger(longValue() * dt.longValue());
                    case LONG:
                        return mult(longValue(), dt.longValue());
                }
        }
        return null;
    }

    IDatatype mult(int x, int y) {
        try {
            return DatatypeMap.newInstance(Math.multiplyExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    IDatatype mult(long x, long y) {
        try {
            return new CoreseLong(Math.multiplyExact(x, y));
        } catch (ArithmeticException e) {
            return null;
        }
    }

    @Override
    public IDatatype div(IDatatype dt) {

        if (dt.doubleValue() == 0.0) {
            return null;
        }

        try {

            switch (getCode()) {
                case DOUBLE:
                    return new CoreseDouble(doubleValue() / dt.doubleValue());

                case FLOAT:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return new CoreseDouble(doubleValue() / dt.doubleValue());
                        default:
                            return new CoreseFloat(floatValue() / dt.floatValue());
                    }

                case DECIMAL:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return new CoreseDouble(doubleValue() / dt.doubleValue());
                        case FLOAT:
                            return new CoreseFloat(floatValue() / dt.floatValue());
                        default:
                            return new CoreseDecimal(doubleValue() / dt.doubleValue());
                    }

                case LONG:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return new CoreseDouble(doubleValue() / dt.doubleValue());
                        case FLOAT:
                            return new CoreseFloat(floatValue() / dt.floatValue());
                        case DECIMAL:
                            return new CoreseDecimal(doubleValue() / dt.doubleValue());
                        case INTEGER:
                        case LONG:
                            return new CoreseDecimal(longValue() / dt.doubleValue());
                    }

                case INTEGER:
                    switch (dt.getCode()) {
                        case DOUBLE:
                            return new CoreseDouble(doubleValue() / dt.doubleValue());
                        case FLOAT:
                            return new CoreseFloat(floatValue() / dt.floatValue());
                        case DECIMAL:
                            return new CoreseDecimal(doubleValue() / dt.doubleValue());
                        case INTEGER:
                        case LONG:
                            return new CoreseDecimal(longValue() / dt.doubleValue());
                    }
            }
        } catch (java.lang.ArithmeticException a) {
        }
        return null;
    }

    /**
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
}