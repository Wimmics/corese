package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

class Env {
	Vector<String> vars;
	Vector<String> states;
	Vector<String> leaves;
	boolean state = false;
	String name;
	int vcount = 0;
	
	Env(){
		vars =    new Vector<String>();
		states =  new Vector<String>();
		leaves =  new Vector<String>();
	}
	
	Env(boolean b){
		this();
		state = b;
	}
	
	Env fork(){
		Env env = new Env(state);
		env.setName(name);
		return env;
	}
	
	void setName(String str){
		name = str;
	}
	
	String getName(){
		return name;
	}
	
	String newVar(String name){
		return name +  vcount++;
	}
	
	
}
