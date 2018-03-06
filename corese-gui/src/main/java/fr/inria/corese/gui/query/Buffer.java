package fr.inria.corese.gui.query;

import java.util.Hashtable;

import javax.swing.JOptionPane;

import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.kgram.event.EventImpl;
import fr.inria.corese.kgram.event.User;

/**
 * Synchronized buffer to interact with GUI & Listener
 * 
 * @author Olivier Corby
 * C INRIA 2010
 *
 */
public class Buffer implements User {
	
	private boolean available = false; 
	int result = Event.STEP;
	Hashtable<String, Integer> table;


	
	public String help(){
		table = new Hashtable<String, Integer>();
		table.put("Complete current expression ", Event.COMPLETE);
		table.put("Next expression in stack (or back to a previous expression) ", Event.FORWARD);
		table.put("Display current Mapping", Event.MAP);
		table.put("Next candidate ", Event.NEXT);
		table.put("Terminates query execution ", Event.QUIT);
		table.put("Compute until current expression succeeds ", Event.SUCCESS);
		table.put("Display (or not) events that are skipped ", Event.VERBOSE);
		table.put("Display an help about user events ", Event.HELP);
		String str = "";
		for (String key : table.keySet()){
			str +="--> "+ EventImpl.getTitle(table.get(key)) + " : " +key+  "\n";
 		}	
		JOptionPane.showMessageDialog(null, str);
		return str;
	}
	
	
	public synchronized int get(){
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		available = false;
		notify();
		return result;
	}
	

	
	public synchronized void set(int n){
		while (available == true) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		available = true;
		result = n;
		notify();
	}
	
	
}
