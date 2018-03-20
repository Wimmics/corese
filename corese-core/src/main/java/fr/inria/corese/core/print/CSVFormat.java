package fr.inria.corese.core.print;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

public class CSVFormat {
	// The end-of-line in CSV is CRLF i.e. Unicode codepoints 13 (0x0D) and 10 (0x0A).
	static final String EOL = "\n";
	static final String SEP = ", ";
	static final String QUOTE = "\"";

	static final String[] SPECIAL = {",", QUOTE, "\n"};

	Mappings lm;
	Query query;
	List<String> select;

	CSVFormat(Mappings m){
		lm = m;
		setQuery(m.getQuery());
	}
	
	public static CSVFormat create(Mappings m){
		return new CSVFormat(m);
	}
	
	String eol(){
		return EOL;
	}
	
	String sep(){
		return SEP;
	}
	
	void setQuery(Query q){
		query = q;
		select = new ArrayList<String> ();
		for (Node node : q.getSelect()){
			select.add(node.getLabel());
		}
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder(variables() + eol());
		str.append(values());
		return str.toString();
	}
	
	String variables(){
		String str = "";
		Query q = lm.getQuery();
		boolean first = true;
		for (String var : select){
			if (first) {
				first = false;
			}
			else {
				str += sep();
			}
			str += getVariable(var);
		}
		return str;
	}
	
	String getVariable(String var){
		return var.substring(1);
	}
	
	StringBuilder values(){
		StringBuilder str = new StringBuilder("");
		
		for (Mapping map : lm){
			boolean first = true;
			
			for (String var : select){
				if (first) {
					first = false;
				}
				else {
					str.append(sep());
				}
				
				Node node = map.getNode(var);
				if (node != null){
					str.append(getLabel(node));
				}
			}
			
			str.append(eol());
		}
		
		return str;
		
	}
	
	
	String getLabel(Node node){
		String label = node.getLabel();
		if (isSpecial(label)){
			label = escape(label);
			label = QUOTE + label + QUOTE;
		}
		return label;
	}
	
	String escape(String str){
		if (str.contains(QUOTE)){
			int index = str.indexOf(QUOTE);
			str = str.substring(0, index) + QUOTE + QUOTE + escape(str.substring(index+1));
		}
		return str;
	}
	
	boolean isSpecial(String str){
		for (String pat : SPECIAL){
			if (str.contains(pat)){
				return true;
			}
		}
		return false;
	}
	

}
