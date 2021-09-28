package fr.inria.corese.kgram.event;

import java.util.Hashtable;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;

/**
 * Event Listener to trace KGRAM execution
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class EvalListener implements EventListener, User {
	
	Hashtable<Integer, Boolean> table;
	User user;
	
	Object object;
	Exp exp;
	Eval kgram;
	
	boolean 
		debug = true,
		complete = false,
		skip = false,
		verbose = false,
		go = true;
	
	int state = Event.STEP;
	
	int index = -1, nbResult = 0, nbPathStep = 0;
	
	public EvalListener(){
		init(true);
		user = new UserImpl();
	}
	
	
	public static EvalListener create(){
		EvalListener el =  new EvalListener();
		return el;
	}
	
	public static EvalListener create(int n){
		EvalListener el = create();
		el.index = n;
		return el;
	}
	
	void init(boolean b){
		table = new Hashtable<Integer, Boolean>();
		handle(Event.ALL, b);
	}

	public String help(){
		return null;
	}
	
	public int get(){
		return Event.STEP;
	}

	@Override
	public boolean handle(int sort) {
		// TODO Auto-generated method stub
		return table.get(sort);
	}
	
	public boolean handle(int sort, boolean b){
		if (sort == Event.ALL){
			for (int e = Event.BEGIN; e <= Event.END; e++){
				table.put(e, b);
			}
		}
		else {
			table.put(sort, b);
		}
		return b;
	}
	
	
	/**
	 * Proxy to User Interaction:
	 * return next, skip, quit, etc.
	 */
	public void setUser(User b){
		user = b;
	}

	
	public boolean send(Event e) {
		
		if (!handle(e.getSort())){
			return true;
		}
		else if (! debug){ 
			if (verbose){
				log(e);
			}
			return true;
		} 
		
		switch (e.getSort()){
			case Event.LIMIT: state = Event.STEP; exp = null; break;
			case Event.BEGIN: nbResult = 0; nbPathStep = 0; break;
			case Event.RESULT: nbResult++; break;
			case Event.PATHSTEP: nbPathStep++; break;

		}
		
		
		
		switch (state){
		
			case Event.COMPLETE:
				// eval silently until end of exp
				if (! (e.getSort() == Event.FINISH && e.getExp() == exp)){
					if (verbose) log(e);
					return true;
				}
				else {
					state = Event.STEP;
					exp = null;
				}
				break;
			
				
			case Event.NEXT:
				// next candidate
				if (e.getExp() == exp){
					state = Event.STEP;
					exp = null;
					break;
				}
				else {
					if (verbose) {
						log(e);
					}
					return true;
				}
				
				
			case Event.SUCCESS:
				// wait success of a filter or enumeration
				if ((e.getSort() == Event.FILTER || e.getSort() == Event.ENUM) && 
					e.getExp() == exp &&  e.isSuccess()){
					state = Event.STEP;
					exp = null;
					break;
				}
				else {
					if (verbose) log(e);
					return true;
				}	
				
			case Event.FORWARD:
				// try to get higher in stack:
				if (e.getExp() != exp){
					state = Event.STEP;
					exp = null;
					break;
				}
				else {
					if (verbose) log(e);
					return true;
				}
							
		}
		
		
		
		
		switch (e.getSort()){
		
			case Event.ENUM: 
				// skip enumeration that fail !!!
				// stop only when success !!!
				log(e);
				if (! e.isSuccess()) return true;
				break;
				
			case Event.START:
			case Event.FINISH:
				if (e.getExp().isFilter()) return true;
				log(e);
				break;
				
			case Event.PATHSTEP:  break;

			default: 		
				switch (e.getSort()){
				
				case Event.RESULT:
					log("** Results: " + nbResult);
					log("** Path Step: " + nbPathStep);
				}
				
				log(e);

		}	
		
		return process(e);
		
	}
	
	
	/**
	 * What does the user want ?
	 */
	boolean process(Event e){
		boolean wait = true;
		while (wait){
			
			int res = user.get();

			wait = false;

			switch (res){

			case Event.STEP: 
				// next elementary step
				break;
		
				
			case Event.FORWARD: 
				// next exp in stack
			case Event.COMPLETE: 
				// eval until end of exp
				state = res; exp = e.getExp(); 
				break;
				
				
			case Event.NEXT: 
				// skip until next candidate of exp
 				state = res; exp = e.getExp(); 
 				break;
				
			case Event.SUCCESS: 
				// eval until success of filter or success of enum
				state = res; exp = e.getExp(); 
				break;

				
				
			case Event.QUIT:
				// complete query
				debug = false; break;
			
			
			
			case Event.VERBOSE: 
				verbose = true;
				log("Mode verbose ok");
				wait = true;
				break;

			case Event.NONVERBOSE: 
				verbose = false;
				log("Fin du mode verbose");
				wait = true;
				break;
				
			case Event.MAP: 
				log(getKGRAM().getEnvironment()); 
				wait = true; 
				break;
				
			case Event.HELP:
				log(user.help());
				wait = true; 
				break;
			}
		}
		return true;
	}

	
	Node getNode(Node qNode){
		return kgram.getEnvironment().getNode(qNode);
	}

	@Override
	public void setObject(Object obj) {
		if (obj instanceof Eval){
			kgram = (Eval) obj;
		}
		else {
			object = obj;
		}
	}
	
	public Eval getKGRAM(){
		return kgram;
	}
	
	public void log(Object obj){
		System.out.println(obj);
	}
	
	
}
