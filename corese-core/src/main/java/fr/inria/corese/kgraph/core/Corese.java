package fr.inria.corese.kgraph.core;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author edemairy
 */
public class Corese {
    private static Logger logger = LogManager.getLogger(Corese.class);
	public static void init() {
		logger.info("Corese, Inria: " + new Date());
	}	
}
