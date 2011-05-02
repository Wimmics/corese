package fr.inria.edelweiss.kgram.event;

import fr.inria.edelweiss.kgram.api.core.ExpType;

/**
 * Event Listener to trace KGRAM execution with statistics
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class StatListener extends EvalListener {
	
	int enumTrue = 0, enumFalse = 0, edge = 0,
	filterTrue = 0, filterFalse = 0,
	step = 0, bind = 0, graph = 0,
	filter = 0, eval = 0,
	result = 0, query = 0, total = 0;
	
	int[] stat = new int[Event.END + 1];
	int[] statExp = new int[ExpType.TITLE.length]; 
	
	StatListener(){
		super();
		start();
	}
	
	void start(){
		for (int i=0; i<stat.length; i++){
			stat[i] = 0;
		}
		for (int i=0; i<statExp.length; i++){
			statExp[i] = 0;
		}
	}
	
	public static StatListener create(){
		StatListener el = new StatListener();
		return el;
	}
	
	public int getStat(int type){
		return stat[type];
	}
	
	public boolean send(Event e){
		total++;
		stat[e.getSort()]++;

		switch (e.getSort()){
		
		case Event.START:
			statExp[e.getExp().type()]++;
			break;
		
		case Event.ENUM:
			if (e.getExp().isEdge()){
				edge ++;
				if (e.isSuccess()) enumTrue++;
				else enumFalse++;
			}
			break;
			
			
		case Event.FILTER:
			filter++;
			if (e.isSuccess()) filterTrue++;
			else filterFalse++;
			break;
			
		}
		
		return true;
		
	}
	
	public String toString(){
		String str = "";
		
		for (int i=Event.BEGIN; i<=Event.END; i++){
			String title = EventImpl.getTitle(i);
			if (title != null){
				str += title + ": " + stat[i] + "\n";
			}
		}
		
		for (int i=ExpType.EMPTY; i<=ExpType.EVAL; i++){
			String title = ExpType.TITLE[i];
			if (title != null){
				str += title + ": " + statExp[i] + "\n";
			}
		}

		if (edge>0){
			str += "Edge true:  " + enumTrue +"\n";
			str += "Edge false: " + enumFalse +"\n";
			str += "true/total: " + 100 * enumTrue/(edge) +"%\n";
		}

		if (filter>0){
			str += "Filter true:  " + filterTrue +"\n";
			str += "Filter false: " + filterFalse +"\n";
			str += "true/total: " + 100 * filterTrue/(filter) +"%\n";
		}
		str += "total: " + total;
		return str;
		
	}

}
