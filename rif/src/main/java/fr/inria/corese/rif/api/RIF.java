package fr.inria.corese.rif.api;

public interface RIF {
	
	static final String XML   =  "http://www.w3.org/XML/1998/namespace";
	static final String RDF   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDFS  =  "http://www.w3.org/2000/01/rdf-schema#";
	static final String XSD   =  "http://www.w3.org/2001/XMLSchema#";
	static final String OWL   =  "http://www.w3.org/2002/07/owl#";

	static final String XSDPrefix  = "xsd";
	static final String RDFPrefix =  "rdf";
	static final String RDFSPrefix =  "rdfs";
	static final String XMLPrefix =  "xml";
	static final String OWLPrefix =  "owl";

	static final String RIFNS = "http://www.w3.org/2007/rif" ;
	static final String RIFPrefix = "rif" ;

	static final String RIF = RIFNS + "#" ;
	static final String CRIF = RIFPrefix + ":" ;

	static final String IRI = RIF + "iri" ;
	static final String LOCAL = RIF + "local" ;

	static final String CIRI = CRIF + "iri" ;
	static final String CLOC = CRIF + "local" ;

	static final String RDF_PLAIN_LITERAL = RDF + "PlainLiteral" ;
	
	static final String xsdboolean 	= XSD+"boolean";
	static final String xsdinteger 	= XSD+"integer";
	static final String xsddecimal 	= XSD+"decimal";
	static final String xsdfloat 	= XSD+"float";
	static final String xsddouble 	= XSD+"double";
	static final String xsdstring 	= XSD+"string";

}
