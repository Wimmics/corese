package fr.inria.acacia.corese.gui.query;

import java.util.Date;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.gui.core.MainFrame;
import fr.inria.acacia.corese.gui.event.MyEvalListener;
//import fr.inria.acacia.corese.util.Time;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
//import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.query.QueryProcess;


/**
 * Exec KGRAM Query in a // thread to enable interacting with EvalListener through the GUI
 */
public class Exec extends Thread {
	private static Logger logger = Logger.getLogger(Exec.class);
	MainFrame frame;
	String query;
	Buffer buffer;
	MyJPanelQuery panel;
	boolean debug = false;
	
	
	public Exec(MainFrame f,  String q, boolean b){
		frame = f;
		query = q;
		debug = b;
	}
	
	/**
	 * run the thread in //
	 * the buffer is used by listener to wait for user interaction 
	 * with buttons: next, quit, etc.
	 */
	public void process(){
		buffer = new Buffer();
		start();
	}
	
	/**
	 * run the thread in //
	 */
	public void run(){
		IResults res = query();
		frame.setBuffer(null);
		frame.getPanel().display(res,frame);
	}
	
	
	IResults query(){
		QueryExec exec =  QueryExec.create(frame.getMyCorese());
		if (debug) debug(exec);
		Date d1 = new Date();
		try {
			IResults l_Results = exec.SPARQLQuery(query);
			Date d2 = new Date();
			//logger.info("** Results: " + l_Results.size()); // + " ; Time: " + d2.get);
			return l_Results;
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			frame.getPanel().getTextArea().setText(e.toString());
		} 
		return null;
	}
	
	/**
	 * Create EvalListener
	 */
	void debug(QueryExec exec){
		MyEvalListener el = MyEvalListener.create();
		el.handle(Event.ALL, true);

		el.setFrame(frame);
		el.setUser(buffer);
		
		frame.setEvalListener(el);
		frame.setBuffer(buffer);
		
		exec.addEventListener(el);
	}

}
