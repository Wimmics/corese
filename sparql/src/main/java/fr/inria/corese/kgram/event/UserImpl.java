package fr.inria.corese.kgram.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.swing.JOptionPane;


/**
 * Model user interaction with debugger
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class UserImpl implements User {
	
	BufferedReader read;
	Hashtable<String, Integer> table;
	Hashtable<String, String> tableButton;
	
	UserImpl(){
		read = new BufferedReader(new InputStreamReader(System.in));
		table = new Hashtable<String, Integer>();
		tableButton = new Hashtable<String, String>();
		init();
	}
	
	void init(){
		// complete current exp (until Event.FINISH for this exp)
		table.put("c", Event.COMPLETE);
		// next exp in stack (or back to previous exp)
		table.put("f", Event.FORWARD);
		// display an help about user events
		table.put("h", Event.HELP);
		// display current Mapping
		table.put("m", Event.MAP);
		// next candidate
		table.put("n", Event.NEXT);
		// terminates query execution
		table.put("q", Event.QUIT);
		// next elementary step
		table.put("s", Event.STEP);
		// compute until current exp succeeds
		table.put("t", Event.SUCCESS);
		// display (or not) events that are skipped
		table.put("v", Event.VERBOSE);
		// display an help about user events
		table.put("?", Event.HELP);

	}
	
	public String help(){
		String str = "";
		for (String key : table.keySet()){
			str += key + " : " + EventImpl.getTitle(table.get(key)) + "\n";
 		}
		JOptionPane.showMessageDialog(null, str);
		return str;
	}

	public int get() {
		try {
			String str = read.readLine();
			Integer event = table.get(str);
			if (event == null){
				event = Event.STEP;
			}
			return event;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Event.STEP;

	}

}
