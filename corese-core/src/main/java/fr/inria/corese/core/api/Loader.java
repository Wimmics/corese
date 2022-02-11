package fr.inria.corese.core.api;

import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.LoadException;
import java.io.InputStream;

public interface Loader {
    static final int RDFXML_FORMAT = 0;
    static final int RDFA_FORMAT   = 1;
    static final int TURTLE_FORMAT = 2;
    static final int NT_FORMAT     = 3;
    static final int JSONLD_FORMAT = 4;
    static final int RULE_FORMAT   = 5;
    static final int QUERY_FORMAT  = 6;
    static final int UNDEF_FORMAT  = 7;
    static final int TRIG_FORMAT   = 8;
    static final int NQUADS_FORMAT = 9;
    static final int WORKFLOW_FORMAT = 10;
    static final int OWL_FORMAT     = 11;
    static final int XML_FORMAT     = 12;
    static final int JSON_FORMAT    = 13;
    
    static final String ACCEPT = "Accept";
    static final String JSONLD_FORMAT_STR = "application/ld+json";
    static final String JSON_FORMAT_STR = "application/json";
    static final String RDFXML_FORMAT_STR = "application/rdf+xml";
    static final String NQUADS_FORMAT_STR = "text/n-quads";
    static final String TRIG_FORMAT_STR = "text/trig";
    static final String NT_FORMAT_STR = "text/n3";
    static final String TURTLE_FORMAT_STR = "text/turtle";
    static final String HTML_FORMAT_STR = "text/html";
    static final String ALL_FORMAT_STR =
            "text/turtle;q=1.0, application/rdf+xml;q=0.9, application/ld+json;q=0.7, application/json;q=0.6";
    
	void init(Object o);
	
	boolean isRule(String path);
                  
        void parse(String path) throws LoadException;
       
        void parse(String path, String source) throws LoadException;
       
        void parse(String path, String source, String base, int format) throws LoadException;
		        
	
	@Deprecated
        void load(String path);
	
	@Deprecated
        void load(String path, String source);
        
	@Deprecated
        void load(String path, String base, String source, int format) throws LoadException;

	@Deprecated
        void load(InputStream stream, String str) throws LoadException;
	
	@Deprecated
	void loadWE(String path) throws LoadException;
	
	@Deprecated
	void loadWE(String path, String source) throws LoadException;
	
	RuleEngine getRuleEngine();
        
        int getFormat(String path);


}
