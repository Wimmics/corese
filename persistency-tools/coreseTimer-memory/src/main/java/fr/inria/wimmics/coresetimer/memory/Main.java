/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer.memory;

import fr.inria.wimmics.coresetimer.CoreseTimer;

/**
 *
 * @author edemairy
 */
public class Main {
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		System.setProperty("CORESE_FACTORY", "fr.inria.corese.tinkerpop.Factory");
		CoreseTimer timer = new CoreseTimer( CoreseAdapter.class.getCanonicalName() , "db"); 
		timer.run();


		System.setProperty("CORESE_FACTORY", "");
		timer = new CoreseTimer( CoreseAdapter.class.getCanonicalName(), "memory"); 
		timer.run();
	}	
}
