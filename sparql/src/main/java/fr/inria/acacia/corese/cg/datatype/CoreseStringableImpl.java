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
 * An implementation of all the datatype that representation is a string:
 * URI, literals, strings for example. These classes have the same implemented functions.<br>
 * It subsumes URI, Literal, xsd:string<br>
 * We can compare URI with URI, string and literal/XMLLiteral with string and
 * literal/XMLLiteral (they can be <= modulo lang)<br>
 * e.g. : titi <= toto@en<br>
 * This class factorize util functions such as contains and plus
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public abstract class CoreseStringableImpl extends CoreseDatatype {

	/** logger from log4j */
	private static Logger logger = LogManager.getLogger(CoreseStringableImpl.class);

	static int code = STRINGABLE;
	public static int count = 0;
	String value = "";

	public CoreseStringableImpl() {}

        public CoreseStringableImpl(String str) {
            setValue(str);
	}
          
        @Override
        public void setValue(String str){
            this.value = str;
        }
        
        @Override
        public String getLabel(){
            return this.value;
        }
        
	/**
	 * Cast a literal to a boolean may be allowed: when the value
	 * can be cast to a float, double, decimal or integer, if this value is 0, then return false, else return true
	 */
        @Override
	public IDatatype cast(String target) { 
		if (target.equals(RDF.xsdboolean)){
			try {
				Float f = new Float(getLabel());
				if (f == 0)      return CoreseBoolean.FALSE;
				else if (f == 1) return CoreseBoolean.TRUE;
				else return null;
			} catch (NumberFormatException e) {
				return super.cast(target); 
			}		   
		} else {
			return super.cast(target); 
		}
	}

        @Override
	public  int getCode(){
		return code;
	}

        
        @Override
	public String getLowerCaseLabel(){
		return getLabel().toLowerCase();
	}

        @Override
	public boolean isNumber() {
		return false;
	}

        @Override
	public boolean isTrue() throws CoreseDatatypeException {
		return booleanValue();
	}
        
        @Override
	public boolean booleanValue () {
		return getLabel().length() > 0;
	}

        @Override
	public boolean isTrueAble() {
		return true;
	}

        @Override
	public boolean contains(IDatatype iod){
		try{
			return getLowerCaseLabel().indexOf(iod.getLowerCaseLabel()) != -1;
		}
		catch(ClassCastException e){
			logger.fatal(e.getMessage());
			return false;
		}
	}


        @Override
	public boolean startsWith(IDatatype iod){
		try{
			return getLabel().startsWith(iod.getLabel());
		}
		catch(ClassCastException e){
			logger.fatal(e.getMessage());
			return false;
		}
	}

	//optimization
	public boolean contains(String label){
		return getLowerCaseLabel().indexOf(label.toLowerCase()) != -1;
	}


	public boolean startsWith(String label){
		return getLabel().startsWith(label);
	}

        @Override
	public String getNormalizedLabel(){
		return getLabel();
	}

	public static String getNormalizedLabel(String label){
		return label;
	}


	public boolean equals(String siod){
		return getLabel().equals(siod);
	}



	int intCompare(CoreseStringableImpl icod) {
		return getLabel().compareTo(icod.getLabel());
	}


//        @Override
//	public IDatatype plus(IDatatype iod) {
//		String str = getLabel() + iod.getLabel();
//		
//		if (this instanceof CoreseURI || iod instanceof CoreseURI) {
//			return new CoreseURI(str);
//		}
//		else {
//			return new CoreseString(str);
//		}
//	}

//        @Override
//	public IDatatype minus(IDatatype iod) {
//		int index = getLabel().indexOf(iod.getLabel());
//		String str=null;
//		
//		if (index == 0){
//			str = getLabel().substring(iod.getLabel().length());
//		}
//		else if (index > 0){
//			str = getLabel().substring(0, index - 1);
//		}
//		
//		if (str != null){
//			if (this instanceof CoreseURI || iod instanceof CoreseURI){
//				return new CoreseURI(str);
//			}
//			else {
//				return new CoreseString(str);
//			}
//		}
//		else
//			return iod;
//	}

}