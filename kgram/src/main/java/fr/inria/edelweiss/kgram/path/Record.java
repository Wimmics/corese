package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;

import fr.inria.edelweiss.kgram.api.core.Regex;

public class Record extends ArrayList<Regex> {
	
	Record push(Regex exp){
		if (exp != null){
			add(exp);
		}
		return this;
	}
	
	Record set(Regex exp){
		if (exp != null){
			set(size()-1, exp);
		}
		return this;
	}
	
	Regex pop(){
		if (size() == 0) return null;
		Regex exp = get(size()-1);
		remove(size()-1);
		return exp;
	}
	
	

}
