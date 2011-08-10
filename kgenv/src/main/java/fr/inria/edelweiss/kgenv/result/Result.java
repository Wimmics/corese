package fr.inria.edelweiss.kgenv.result;

import java.util.Enumeration;
import java.util.Hashtable;



class Result extends Hashtable<String,Value> {
	
	Result(){
		
	}
	
	public Value get(String key){
		if (key.startsWith("?") || key.startsWith("$")){
			key = key.substring(1);
		}
		return super.get(key);
	}
	
	public String toString(){
		String str = "";
		
		for (Enumeration<String> en = keys(); en.hasMoreElements();){
			String var = en.nextElement();
			str += var + " = " + get(var) + "\n";
		}
		
		return str;
	}
}



