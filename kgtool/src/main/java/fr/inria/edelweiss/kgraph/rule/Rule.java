package fr.inria.edelweiss.kgraph.rule;

import fr.inria.edelweiss.kgram.core.Query;

public class Rule {
	static int COUNT = 0;
	
	Query query;
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
	
	Query getQuery(){
		return query;
	}
	
	String getName(){
		return name;
	}
	
	int getNum(){
		return num;
	}

}
