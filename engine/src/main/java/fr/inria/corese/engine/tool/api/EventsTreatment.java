package fr.inria.corese.engine.tool.api;

import java.util.List;

import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.Clause;
import fr.inria.corese.engine.model.api.ExpFilter;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.engine.model.api.Query;

public interface EventsTreatment {

	/**
	 * search triples matching with clause in the base of events
	 */
	public LBind searchTriples(Query query, Clause clause, Bind bind);

	public LBind searchTriples(Query query, List<Clause> clause, Bind bind);
	
	public boolean test(Query query, ExpFilter filter, Bind b, int level, int n);

}
