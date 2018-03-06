package sna;

import java.util.ArrayList;
import java.util.Collections;

import fr.inria.corese.kgram.api.core.Node;

/**
 * 
 * Olivier Corby, Edelweiss, INRIA, 2011
 *
 */
public class SSNAHandlerImpl extends SSNAHandler {
	
	Node source, target;
	int length = 0, maxCount = 0, countResult = 0;
		
	ArrayList<Record> list, list2;
	
	
	SSNAHandlerImpl(){
		list = new ArrayList<Record>();
		list2 = new ArrayList<Record>();
	}
	
	public static SSNAHandlerImpl create(){
		return new SSNAHandlerImpl();
	}
	
	class Record implements Comparable<Record> {
		Node source, target, node;
		Integer length, count;
		
		Record(Node source, Node target, Node node, int count, int length){
			this.length = length;
			this.source = source;
			this.target = target;
			this.node = node;
			this.count  = count;
		}
		
		public String toString(){
			String str =  count + " " + length + " " + source + " " + target;
			if (node != null){
				str += " " + node;
			}
			return str;
		}
		
		public int compareTo(Record rec){
			return rec.count.compareTo(count);
		}
	}
	
	public void display(){
		Collections.sort(list);
		Collections.sort(list2);
		System.out.println("** NB pairs: " + countResult);
		Node node = null;
		int cc = 0;
		for (int i = 0; i<50 && i<list.size(); i++){
			Record rec = list.get(i);
			System.out.println(rec);
		}
		System.out.println("__");
		for (int i = 0; i<50 && i<list2.size(); i++){
			Record rec = list2.get(i);
			System.out.println(rec);
		}
	}
		
	public void geodesic(Node source, Node target, int length, int count){
		if (match(source) || match(target)){
			System.out.println(source + " " + target + " " + count  + " " + length);
		}
		
		countResult++;
		if (count > maxCount){
			maxCount = count;
			
		}
//		Record rec = new Record(source, target, null, count, length);
//		list.add(rec);
	}	
	
	public void geodesic(Node source, Node target, Node node, int length, int count){
		if (match(source) || match(target) || match(node)){
			System.out.println(source + " " + target + " " + node + " " + count  + " " + length);
		}
//		Record rec = new Record(source, target, node, count, length);
//		list2.add(rec);
		}
	
	public void degree(Node node, float degree){
		if (match(node)){
			System.out.println(node + " " + degree);
		}
	}
	
	boolean match(Node node){
		return node.getLabel().contains("orby");
	}

}
