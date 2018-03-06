package fr.inria.corese.kgram.event;



/**
 * Event Listener to trace KGRAM execution
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface EventListener  {
	
	boolean send(Event e);
	
	boolean handle(int sort);
	
	boolean handle(int sort, boolean b);
	
	void setObject(Object obj);
	

}
