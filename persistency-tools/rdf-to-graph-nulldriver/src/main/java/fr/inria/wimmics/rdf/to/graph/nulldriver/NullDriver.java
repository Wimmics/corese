/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.rdf.to.graph.nulldriver;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import java.util.Map;
import java.util.logging.Logger;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class NullDriver extends GdbDriver {
	static final Logger logger = Logger.getLogger(NullDriver.class.getName());
	@Override
	public void openDb(String string) {
		logger.fine("Opening db "+string);
	}

	@Override
	public void closeDb() {
		logger.fine("Closing db");
	}

	@Override
	public void createNode(Value value) {
		logger.fine("Creating node "+value.toString());
	}

	@Override
	public Object createRelationship(Object o, Object o1, String string, Map<String, Object> map) {
		logger.fine("Creating relationship bw "+o.toString()+" and "+o1.toString());
		return null;
	}

	@Override
	public void commit() {
		logger.fine("Commiting");
	}

	@Override
	public Object getNode(Value v) {
		logger.fine("returning "+v.toString());
		return v;
	}
}
