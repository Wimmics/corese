package fr.inria.edelweiss.kgraph.rule;

import java.util.List;

import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.api.core.Node;

public class Rule {
	static int COUNT = 0;
	
	Query query;
	List<Node> predicates;
	String name;
	int num;
	
	Rule(String n, Query q){
		query = q;
		name = n;
		num = COUNT++;
	}
	
	public static Rule create(String n, Query q){
		Rule r = new Rule(n, q);
		return r;
	}
	
	public static Rule create(Query q){
		Rule r = new Rule("rule", q);
		return r;
	}
	
	void set(List<Node> list){
		predicates = list;
	}
	
	List<Node> getPredicates(){
		return predicates;
	}
	
	public Query getQuery(){
		return query;
	}
	
	String getName(){
		return name;
	}
	
	int getIndex(){
		return num;
	}

}
