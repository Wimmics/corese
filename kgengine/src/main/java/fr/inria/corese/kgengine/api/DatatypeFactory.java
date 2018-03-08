package fr.inria.corese.kgengine.api;

import fr.inria.corese.sparql.api.IDatatype;
import java.util.Date;
import java.util.HashMap;

import fr.inria.corese.sparql.cg.datatype.CoreseDatatype;
import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.sparql.cg.datatype.RDF;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * This interface create an instance of a CoreseDatatype<br>
 * It can be used when creating an external function.<br>
 * External function must follow the pattern:<br>
 * <code>IDatatype functionName(IDatatype d1, IDatatype d2) { ... }</code> <i>(with 0, 1 or several arguments)</i><br>
 * Inside the function, you can do what you want, and to get a correct return type, you can do:<br>
 * <ul>
 * 	<li>either <code>return DatatypeFactory.newInstance(result);</code><br>
 * 	result can be a String, a Boolean, a Double, a Float, an Integer, a Long or a Date.</li>
 * 	<li>or <code>return DatatypeFactory.newInstance(result, CoreseType.STRING);</code><br>
 * 	result is a String</li>
 * </ul>
 * @author vbottoll
 *
 */
public class DatatypeFactory {

	private static DatatypeMap dm = new DatatypeMap();
	
	private static HashMap<CoreseType, String> datatypeHM = null; 
	
	private DatatypeFactory() {}
	
	public static enum CoreseType {STRING, BOOLEAN, XMLLITERAL, DOUBLE, FLOAT, 
		DECIMAL, INTEGER, LONG, LITERAL, DATE, URI, BNODE};
	
    public static IDatatype newInstance(String result) throws CoreseDatatypeException {
    	return newInstance(result, CoreseType.STRING);
    }
    
    public static IDatatype newInstance(boolean result) throws CoreseDatatypeException {
    	return newInstance(String.valueOf(result), CoreseType.BOOLEAN);
    }
    
    public static IDatatype newInstance(Date result) throws CoreseDatatypeException {
    	return newInstance(result.toString(), CoreseType.DATE);		
    }
    
    public static IDatatype newInstance(double result) throws CoreseDatatypeException {
    	// return newInstance(String.valueOf(result), CoreseType.DOUBLE);
    	return dm.newInstance(result);
    }
    
    public static IDatatype newInstance(float result) throws CoreseDatatypeException {
    	return dm.newInstance(result);
    }
    
    public static IDatatype newInstance(int result) throws CoreseDatatypeException {
    	return dm.newInstance(result);
    }
    
    public static IDatatype newInstance(long result) throws CoreseDatatypeException {
    	return dm.newInstance(result);
    }
	
    /**
     * create a new Instance for the following types
     * @param result the result we want to transform to a IDatatype
     * @param type the type of the result (between STRING, BOOLEAN, XMLLITERAL, LITERAL, DATE, URI, BNODE, DECIMAL, DOUBLE, FLOAT, INTEGER, LONG)
     * @return the IDatatype object created 
     * @throws CoreseDatatypeException
     */
	public static IDatatype newInstance(String result, CoreseType type) throws CoreseDatatypeException {
		// if type = literal, without language, we change it to String
		if (type == CoreseType.LITERAL) type = CoreseType.STRING;
		// if type = blank node, this is a particular case and we create directly the new CoreseBlankNode
		else if (type == CoreseType.BNODE) return DatatypeMap.createBlank(result); // new CoreseBlankNode(result);
		// if the table hasn't been created yet, we do it now
		if (datatypeHM == null) createTable();
		// then we return the new correct CoreseDatatype
		return CoreseDatatype.create(datatypeHM.get(type) ,result);
	}
	
    /**
     * create a new Instance with a lang
     * @param result the result we want to transform to a IDatatype
     * @param lang the language of the result (ex: "fr", "en") 
     * @return the IDatatype object created 
     * @throws CoreseDatatypeException
     */
	public static IDatatype newInstance(String result, String lang) throws CoreseDatatypeException {
		return DatatypeMap.createLiteral(result, null, lang);
	}
	
	
	public  static IDatatype createLiteral(String label, String datatype, String lang){
		return DatatypeMap.createLiteral(label, datatype, lang);
	}
	
	
	public static IDatatype createResource(String label){
		return DatatypeMap.createResource(label);
	}
	
	public static IDatatype createBlank(String label){
		return DatatypeMap.createBlank(label);
	}
	
	
	
	
	
	private static void createTable() {
	    dm.init();

		datatypeHM = new HashMap<CoreseType, String>();
		datatypeHM.put(CoreseType.STRING, 		dm.getJType(RDF.xsdstring));
	    datatypeHM.put(CoreseType.BOOLEAN , 	dm.getJType(RDF.xsdboolean));
	    datatypeHM.put(CoreseType.XMLLITERAL, 	dm.getJType(RDF.XMLLITERAL));
	    datatypeHM.put(CoreseType.DOUBLE , 		dm.getJType(RDF.xsddouble));
	    datatypeHM.put(CoreseType.FLOAT , 		dm.getJType(RDF.xsdfloat));
	    datatypeHM.put(CoreseType.DECIMAL , 	dm.getJType(RDF.xsddecimal));
	    datatypeHM.put(CoreseType.INTEGER , 	dm.getJType(RDF.xsdinteger));
	    datatypeHM.put(CoreseType.LONG , 		dm.getJType(RDF.xsdlong));	    
	    //datatypeHM.put(CoreseType.UNDEF , 		dm.getJType(RDF.xsd));
	    datatypeHM.put(CoreseType.DATE , 		dm.getJType(RDF.xsddate));
	    datatypeHM.put(CoreseType.URI ,			dm.getJType(RDF.RDFSRESOURCE));
	}
	
}
