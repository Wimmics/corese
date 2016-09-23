/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author edemairy
 */
public class Corese {
    private static Logger logger = LogManager.getLogger(Corese.class);
	public static void init() {
		logger.info("static initialisation when Graph class is loaded.");
	}	
}
