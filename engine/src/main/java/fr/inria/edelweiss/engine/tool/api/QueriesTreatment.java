package fr.inria.edelweiss.engine.tool.api;

import java.util.List;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.edelweiss.engine.model.api.Query;

public interface QueriesTreatment {

	/**
	 * create a query with a set of clauses
	 */
	public List<Query> createQuery(IEngine server,List<String> queryFiles);
	
}
