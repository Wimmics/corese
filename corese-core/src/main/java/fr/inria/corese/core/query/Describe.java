package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.tool.MetaProducer;
import fr.inria.corese.core.api.Log;
import fr.inria.corese.core.Graph;

/**
 * Implement pragma status
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Describe {
	
	static final String NL = System.getProperty("line.separator");

	static final String nbClass = 
		"# Class" + NL +
		"select (count(distinct ?c) as ?nb) where {?c a rdfs:Class}";	
	
	static final String nbClassDirectInst = 
		"# Class having direct instances" + NL +
		"select (count(distinct ?c) as ?nb) where {?x a ?c}";
	
	static final String nbClassInst = 
		"# Class having instances" + NL +
		"select (count(distinct ?c) as ?nb) where {?x rdf:type/rdfs:subClassOf* ?c}";
	
	static final String nbClassNoDirectInst = 
		"# Class having no direct instances" + NL +
		"select (count(distinct ?c) as ?nb) where {?c a rdfs:Class . filter not exists {?x a ?c}}";	
	
	static final String nbClassNoInst = 
		"# Class having no instances" + NL +
		"select (count(distinct ?c) as ?nb) where {?c a rdfs:Class . filter not exists {?x rdf:type/rdfs:subClassOf* ?c}}";

	static final String nbPropNoDirectInst = 
		"# Property having no direct instances" + NL +
		"select (count(distinct ?p) as ?nb) where {?p a rdf:Property . filter not exists {?x ?p ?y}}";	
	
	static final String nbPropNoInst = 
		"# Property having no instances" + NL +
		"select (count(distinct ?q) as ?nb) where {?q a rdf:Property . filter not exists {?p rdfs:subPropertyOf* ?q . ?x ?p ?y}}";		
		
	static final String nbEntail = 
		"# RDFS entailment" + NL +
		"select (count(*) as ?nb) where {graph kg:entailment {?x ?p ?y}}";
	
	static final String nbEntail2 = 
		"# RDFS entailment" + NL +
		"select ?p (count(*) as ?nb) from kg:entailment where {?x ?p ?y} " +
		"group by ?p order by desc(?nb) limit 5";
	
	static final String nbNotEntail = 
		"# Edge not entailed" + NL +
		"select ?p (count(*) as ?nb)  where {graph ?g {?x ?p ?y filter(?g != kg:entailment)}} " +
		"group by ?p order by desc(?nb) limit 5";
	
	
	static final String nbRule = 
		"# Rule inference" + NL +
		"select (count(*) as ?nb) where {graph kg:rule {?x ?p ?y}}";
	
	static final String maxSource = 
		"# Source degree" + NL +
		"select ?x (count(?y) as ?nb)  where {?x ?p ?y} " +
		"group by ?x order by desc(?nb) limit 5" ;
	
	static final String maxTarget = 
		"# Target degree" + NL +
		"select ?y (count(?x) as ?nb)  where {?x !rdf:type ?y} " +
		"group by ?y order by desc(?nb) limit 5" ;
	
	static final String maxInst = 
		"# Class instance" + NL +
		"select ?y (count(?x) as ?nb)  where {?x rdf:type ?y} " +
		"group by ?y order by desc(?nb) limit 5" ;
	
	static final String maxDepth = 
		"# Ontology depth" + NL +
		"select (kg:depth(?c) as ?d) ?c where {" +
		"select (kg:similarity() as ?sim) ?c where {?c rdf:type rdfs:Class}} order by desc(?d) limit 1" ;
	
	static final String duplicate = 
		"# Duplicate entailment" + NL +
		"select (count(*) as ?nb) from kg:entailment where {?x ?p ?y " +
		"filter exists {graph ?g {?x ?p ?y . filter(?g != kg:entailment)}}}" ;
	
	static final String duplicate2 = 
		"# Duplicate entailment" + NL +
		"select ?p (count(*) as ?nb) from kg:entailment where {" +
		"?x ?p ?y . filter exists {graph ?g {?x ?p ?y . filter(?g != kg:entailment)}}} " +
		"group by ?p order by desc(?nb) limit 5" ;
	
	static final String[] queries = 
	{nbClass, nbClassDirectInst, nbClassInst, nbClassNoDirectInst, nbClassNoInst, 
		nbPropNoDirectInst, nbPropNoInst, nbEntail, nbEntail2, nbNotEntail, duplicate, duplicate2, nbRule, maxSource, maxTarget, maxInst, maxDepth};
	
	
	QueryProcess exec;
	Query query;
	
	
	Describe(QueryProcess e, Query q){
		exec = e;
		query = q;
	}
	

	void describe(boolean detail){
		Producer p = exec.getProducer();
		if (p instanceof MetaProducer){
			int i = 1;
			MetaProducer mp = (MetaProducer) p;
			for (Producer pp : mp.getProducers()){
				describe(pp, i++, detail);
			}
		}
		else {
			describe(p, 1, detail);
		}
	}
	
	
	void describe(Producer p, int n, boolean detail){
		if (p instanceof ProducerImpl){
			ProducerImpl pi = (ProducerImpl) p;
			Graph g = pi.getGraph();
			info("Dataset " + n + ":\n" , g);
			info("", g.getIndex());

			if (detail){
				log(g);
				query(g);
			}
		}
	}

	
	void log(Graph g){
		Log man = g.getLog();
		if (g.isLog() && man.get(Log.LOAD).size()>0){
			StringBuffer sb = new StringBuffer();
			for (Object name : man.get(Log.LOAD)){
				sb.append(name + "\n");
			}
			info("Load: \n", sb.toString());
		}
	}
	
	void query(Graph g){
		Mappings map;
		QueryProcess exec = QueryProcess.create(g);
		try {
			for (String q : queries){
				System.out.println(q);
				map = exec.query(q);
				info(q + NL, map);
			}
			
		} catch (EngineException e) {
		}
	}
	
	
	
	
	int getValue(Mappings m, String n){
		IDatatype dt =  m.getValue(n);
		if (dt == null) return 0;
		return dt.intValue();
	}
	
	void info(String str, Object obj){
		query.addInfo(str, obj);
	}

}
