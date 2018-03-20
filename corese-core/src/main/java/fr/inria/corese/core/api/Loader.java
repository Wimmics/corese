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
