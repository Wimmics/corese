package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * 
 * Draft access rights with pragma
 * Not used
 *
 */
public class Access {
	
	ASTQuery ast;
	TTable ttable;

	Access(ASTQuery a){
		ast = a;
	}
	
	
	  
    // from -> NSTable
    class TTable extends Hashtable <String, NSTable>{}
    
    // subject from {ns}
    class NSTable extends Hashtable<String, SVector>{}
    
    class SVector extends ArrayList<String> {}
    
	
	 /**
     * Store access pragma tables
     */
    void pragma(Exp pragma){
    	if (pragma == null) return;

    	NSManager nsm = ast.getNSM();
    	for (Exp exp : pragma.getBody()){
    		if (exp.isRelation()){
    			Triple t = (Triple) exp;
    			//sem:root sem:type wiki:Page ;
    			String subject =  nsm.toNamespace(t.getSubject().getName());
    			String property = nsm.toNamespace(t.getProperty().getName());
    			String object =   nsm.toNamespace(t.getObject().getName());
    			//logger.debug("** PRAGMA AST: " + t);
    			if (property.equals(RDFS.ACCEPT)){
    				property =  RDFS.FROM;
    			}
    			if (subject.equals(RDFS.RDFRESOURCE)){
    				def(RDFS.RDFSUBJECT, property, object);
    				def(RDFS.RDFOBJECT, property, object);
    			}
    			else {
    				def(subject, property, object);
    			}
    		}
    	}
    	if (ttable!=null)
    		cleanPragma();

    }
    
    
    
  	// replace xxx from _:b . _:b type yyy by xxx type yyy
	// xxx from [type yyy] by xxx type yyy
    void cleanPragma(){
    	if (getSubject(RDFS.FROM)!=null)
    	for (String subject : getSubject(RDFS.FROM)){
    		SVector from = getValue(RDFS.FROM, subject);
    		if (from!=null){
    			for (int i=0; i<from.size(); i++){
    				// from.get(i) = _:b
    				SVector types = getValue(RDFS.RDFTYPE, from.get(i));
    				if (types != null){
    					// there exist _:b type yyy
    					// remove : xxx cos:from _:b
    					from.remove(i);
    					for (String type : types){
    						// add xxx rdf:type yyy
    						def(subject, RDFS.RDFTYPE, type);
    					}
    				}
    			}
    		}
    	}
    }
    
    
    public boolean hasNamespace(){
    	return ttable != null;
    }
    
    public boolean hasProperty(String name){
    	return ttable.get(name) != null;
    }
    
    // return from/deny
    public Iterable<String> getProperty(){
    	return ttable.keySet();
    }
    
    // prop : from/deny
    // return subjects of prop
    public Iterable<String> getSubject(String prop){
    	NSTable table =  ttable.get(prop);
    	if (table == null) return null;
    	return table.keySet();
    }
    
    public Iterable<String> getValues(String prop, String name){
    	NSTable table =  ttable.get(prop);
    	if (table == null) return null;
    	return table.get(name);
    }
    
    public SVector getValue(String prop, String name){
    	NSTable table =  ttable.get(prop);
    	if (table == null) return null;
    	return table.get(name);
    }
    
    void def(String name, String prop, String ns){
    	if (ttable == null) ttable = new TTable();
    	NSTable table = ttable.get(prop);
    	if (table == null){
    		table = new NSTable();
    		ttable.put(prop, table);
    	}
    	access(table, name, ns);
    }

    void access(NSTable table, String name, String ns){
    	SVector vec = table.get(name);
    	if (vec == null){
    		vec = new SVector();
    		table.put(name, vec);
    	}
    	if (! vec.contains(ns))
    		vec.add(ns);
    }

	

}
