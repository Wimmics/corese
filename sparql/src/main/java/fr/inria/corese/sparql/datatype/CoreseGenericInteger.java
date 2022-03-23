package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;

/**
 * int short ...
 *
 * @author corby
 *
 */
public class CoreseGenericInteger extends CoreseInteger {

    IDatatype datatype;
    
    CoreseGenericInteger(){}

    public CoreseGenericInteger(String label, String uri) {
        super(label);
        setDatatype(uri);
    }

    public CoreseGenericInteger(String label) {
        super(label);
        // by safety:
        datatype = super.getDatatype();
    }

    public CoreseGenericInteger(int n, String uri) {
        super(n);
        setDatatype(uri);
    }
    
    public CoreseGenericInteger(long n) {
        super(n);
        setDatatype(XSD.xsdlong);
    } 
    
    // for computing, without label
    public static CoreseGenericInteger create(long n) {
        CoreseGenericInteger i = new CoreseGenericInteger();
        i.setValue(n);
        i.setDatatype(XSD.xsdlong);
        return i;
    }

    @Override
    public void setDatatype(String uri) {
        datatype = getGenericDatatype(uri);
    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }
    
    @Override
    public boolean isXSDInteger() { 
        return false;
    }

    @Override
    public IDatatype typeCheck() {
        if (validate(getDatatypeURI())){
            return this;
        }
        else {
            return DatatypeMap.createUndef(getLabel(), getDatatypeURI());
        }
    }
    
    boolean validate(String datatype){
        switch (datatype){
            
            case RDF.xsdbyte:
                return intValue() <= 127 && intValue() >= -128;
                
            case RDF.xsdshort:
                return intValue() <= 32767 && intValue() >= -32768;
                
             case RDF.xsdint:
                return intValue() <= 2147483647 && intValue() >= -2147483648;    
                
            case RDF.xsdpositiveInteger:
                return intValue() > 0; 
                
            case RDF.xsdnegativeInteger:
                return intValue() < 0; 
                
            case RDF.xsdnonNegativeInteger:
                return intValue() >= 0; 
                
            case RDF.xsdnonPositiveInteger:
                 return intValue() <= 0; 
                
            case RDF.xsdunsignedByte: 
                return validate(RDF.xsdbyte) && intValue() >= 0;
                
            case RDF.xsdunsignedInt:  
                return validate(RDF.xsdint) && intValue() >= 0;
                
            case RDF.xsdunsignedLong: 
                return validate(RDF.xsdlong) && intValue() >= 0;
                
            case RDF.xsdunsignedShort:
                return validate(RDF.xsdshort) && intValue() >= 0;
                
        }
        return true;
    }
    
    
}