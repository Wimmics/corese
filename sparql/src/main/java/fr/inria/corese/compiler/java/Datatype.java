package fr.inria.corese.compiler.java;

import fr.inria.acacia.corese.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Datatype {

    
     String toJava(IDatatype dt) {
        switch (dt.getCode()){
            case IDatatype.URI:     return newResource(dt.stringValue());
            case IDatatype.INTEGER: return newInstance(dt.intValue());
            case IDatatype.DOUBLE:  return newInstance(dt.doubleValue());
            case IDatatype.FLOAT:   return newInstance(dt.floatValue());
            case IDatatype.BOOLEAN: return newInstance(dt.booleanValue());
            case IDatatype.STRING:  return newInstance(dt.stringValue());
            case IDatatype.LITERAL:
            case IDatatype.DATE:
            case IDatatype.DATETIME: return newLiteral(dt);
        }

        return newInstance(dt.stringValue());
    }

    String newResource(String val){
        return String.format("DatatypeMap.newResource(\"%s\")", val);        
    }
    
    String newLiteral(IDatatype dt){
        if (dt.hasLang()){
            return String.format("DatatypeMap.newInstance(\"%s\", %s , \"%s\")", dt.stringValue(), null, dt.getLang());
        }
        else {
         return String.format("DatatypeMap.newInstance(\"%s\", \"%s\")", dt.stringValue(), dt.getDatatypeURI());
        }
    }

    String newInstance(int val){
        switch (val){
            case 0: return "DatatypeMap.ZERO";
            case 1: return "DatatypeMap.ONE";
        }
        return String.format("DatatypeMap.newInstance(%s)", val);
    }

    String newInstance(String val){
        return String.format("DatatypeMap.newInstance(\"%s\")", val);
    }

    String newInstance(double val){
        return String.format("DatatypeMap.newInstance(%s)", val);
    }

    String newInstance(float val){
        return String.format("DatatypeMap.newInstance(%s)", val);
    }

     String newInstance(boolean val){
         if (val){
            return "DatatypeMap.TRUE";
         }
         else {
            return "DatatypeMap.FALSE";
         }
    }
    
    
}
