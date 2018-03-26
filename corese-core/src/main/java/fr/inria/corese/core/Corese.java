package fr.inria.corese.core;

import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author edemairy
 */
public class Corese {
    private static Logger logger = LoggerFactory.getLogger(Corese.class);
	public static void init() {
		logger.info("Corese, Inria: " + new Date());
	}	
}
