package fr.inria.corese.gui.event;

import fr.inria.corese.gui.core.MainFrame;
import fr.inria.edelweiss.kgram.event.EvalListener;


/**
 * 
 * KGRAM Eval Listener
 * Interact with GUI through a synchronized buffer
 *
 */
public class MyEvalListener extends EvalListener {
	MainFrame frame;
	
	public static MyEvalListener create(){
		MyEvalListener el = new MyEvalListener();
		return el;
	}
	
	public void setFrame(MainFrame mf){
		frame = mf;
	}
	
	public void log(Object obj){
		String str = obj.toString();
		//OC:
		super.log(obj);
		frame.appendMsg(str);
	}
	

	
}
