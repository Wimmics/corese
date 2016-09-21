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
public interface CoreseAdapterInterface {
	/** @param param  */
	public void preProcessing(String param);
	public void execQuery(String query);
	public void postProcessing();

	public void saveResults(String resultsFileName);

}
