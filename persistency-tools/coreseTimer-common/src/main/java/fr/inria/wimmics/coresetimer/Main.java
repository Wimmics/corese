/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

/**
 *
 * @author edemairy
 */
public class Main {
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		System.setProperty("fr.inria.corese.factory", "fr.inria.corese.tinkerpop.Factory");
		CoreseTimer timer = new CoreseTimer( CoreseAdapter.class.getCanonicalName() , CoreseTimer.Profile.DB); 
		timer.run();


		System.setProperty("fr.inria.corese.factory", "");
		timer = new CoreseTimer( CoreseAdapter.class.getCanonicalName(), CoreseTimer.Profile.MEMORY); 
		timer.run();
	}	
}
