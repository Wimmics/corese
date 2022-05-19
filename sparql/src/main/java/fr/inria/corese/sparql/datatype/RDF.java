package fr.inria.corese.sparql.datatype;

public interface RDF {
    
	 static final String XML   =  "http://www.w3.org/XML/1998/namespace";
	 static final String RDF   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	 static final String RDFS  =  "http://www.w3.org/2000/01/rdf-schema#";
	 static final String XSD   =  "http://www.w3.org/2001/XMLSchema#";
	 static final String OWL   =  "http://www.w3.org/2002/07/owl#";
	 static final String RDF_HTML   =  RDF + "HTML";
         
         static final String FIRST = RDF+"first";
         static final String REST  = RDF+"rest";           
         static final String OWL_SAME_AS = OWL+"sameAs";
	 
	 static final String XSDPrefix  = "xsd";
	 static final String RDFPrefix =  "rdf";
	 static final String RDFSPrefix =  "rdfs";
	 static final String XMLPrefix =  "xml";
	 static final String OWLPrefix =  "owl";
	 
	static final String RDFSRESOURCE= RDFS+"Resource";
	static final String RDFSLITERAL 	= RDFS+"Literal";
	static final String XMLLITERAL  	= RDF+"XMLLiteral";
	static final String HTML  	= RDF+"HTML";
	static final String LANGSTRING  = RDF+"langString";
	static final String BLANKSEED   = "_:";
	
	static final String qxsdString 	 = "xsd:string";
	static final String qxsdInteger  = "xsd:integer";
	static final String qxsdBoolean  = "xsd:boolean";
	static final String qxsdlangString= "rdf:langString";
	static final String qrdfsLiteral = "rdfs:Literal";

	static final String xsdboolean 	= XSD+"boolean";
	static final String xsdinteger 	= XSD+"integer";
	static final String xsdlong 	= XSD+"long";
	static final String xsdint 	= XSD+"int";
	static final String xsdshort 	= XSD+"short";
	static final String xsdbyte	= XSD+"byte";
	static final String xsddecimal 	= XSD+"decimal";
	static final String xsdfloat 	= XSD+"float";
	static final String xsddouble 	= XSD+"double";

	static final String xsdnonNegativeInteger = XSD+"nonNegativeInteger";
	static final String xsdnonPositiveInteger = XSD+"nonPositiveInteger";
	static final String xsdpositiveInteger 	  = XSD+"positiveInteger";
	static final String xsdnegativeInteger    = XSD+"negativeInteger";

	static final String xsdunsignedLong    = XSD+"unsignedLong";
	static final String xsdunsignedInt     = XSD+"unsignedInt";
	static final String xsdunsignedShort   = XSD+"unsignedShort";
	static final String xsdunsignedByte    = XSD+"unsignedByte";

	static final String rdflangString 	= RDF+"langString";
	static final String xsdstring 	= XSD+"string";
	static final String xsdnormalizedString 	= XSD+"normalizedString";
	static final String xsdtoken 	= XSD+"token";
	static final String xsdnmtoken 	= XSD+"NMTOKEN";
	static final String xsdanyURI 	= XSD+"anyURI";
	static final String xsdname 	= XSD+"Name";
	static final String xsdncname 	= XSD+"NCName";
	static final String xsdlanguage = XSD+"language";

	static final String xsdduration = XSD+"duration";
	static final String xsddaytimeduration = XSD+"dayTimeDuration";
	static final String xsddate 	= XSD+"date";
	static final String xsddateTime = XSD+"dateTime";
	static final String xsdday 		= XSD+"gDay";
	static final String xsdmonth 	= XSD+"gMonth";
	static final String xsdyear 	= XSD+"gYear";


}
