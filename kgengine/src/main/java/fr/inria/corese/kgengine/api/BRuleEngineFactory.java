package fr.inria.corese.kgengine.api;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author corby
 *
 */


public class BRuleEngineFactory {
	public static String ENGINE = "fr.inria.corese.engine.core.Engine";
	private static Logger logger = LogManager.getLogger(BRuleEngineFactory.class);

	public static BRuleEngineFactory newInstance(){
		return new BRuleEngineFactory();
	}
	
	public IBRuleEngine create(){
		try {
			Class aclass =   Class.forName(ENGINE);
			if (aclass != null){ 
				return (IBRuleEngine) aclass.newInstance();
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.warn("Backward Rule Engine not available"); //e.printStackTrace();
		}
		return null;
	}

}
