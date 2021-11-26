package fr.inria.corese.sparql.datatype;

public interface Cst {
	/**
	 * implementation Class name for datatypes
	 */
	static final String pack      = "fr.inria.corese.sparql.datatype.";
	static final String extension = "fr.inria.corese.sparql.datatype.extension.";
	
	static final String jDatatype		= pack + "CoreseDatatype";
	static final String jTypeString		= pack + "CoreseString";
	static final String jTypeBoolean        = pack + "CoreseBoolean";
	static final String jTypeXMLString 	= pack + "CoreseXMLLiteral";
	static final String jTypeDouble		= pack + "CoreseDouble";
	static final String jTypeFloat		= pack + "CoreseFloat";
	static final String jTypeDecimal    	= pack + "CoreseDecimal";
	static final String jTypeInteger	= pack + "CoreseInteger";
	static final String jTypeInt            = pack + "CoreseInt";
        
	static final String jTypeGenericInteger	= pack + "CoreseGenericInteger";
	static final String jTypeLong       	= pack + "CoreseLong";
	static final String jTypeLiteral       	= pack + "CoreseLiteral";
	static final String jTypeUndef  	= pack + "CoreseUndefLiteral";
	static final String jTypeDate       	= pack + "CoreseDate";
	static final String jTypeDateTime       = pack + "CoreseDateTime";
	static final String jTypeDay    	= pack + "CoreseDay";
	static final String jTypeMonth          = pack + "CoreseMonth";
	static final String jTypeYear   	= pack + "CoreseYear";
	static final String jTypeGeneric        = pack + "CoreseGeneric";

	static final String jTypeURI		= pack + "CoreseURI";
	static final String jTypeURILiteral     = pack + "CoreseURILiteral";
	static final String jTypeBlank		= pack + "CoreseBlankNode";
	static final String jTypeArray		= pack + "CoreseArray";
        
        static final String jTypeJSON		= extension + "CoreseJSON";


}
