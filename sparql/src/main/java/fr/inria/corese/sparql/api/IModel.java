package fr.inria.corese.sparql.api;


/**
 * This class is a model for given parameters; it allows us to to parameterize a query.<br />
 * In an application, one can need to get a value in a parameter. To achieve this, it is 
 * possible to use the IModel interface.<br />
 * 1. Write an implementation of IModel: IModelImpl.<br />
 * 2. Create an instance of this class: <code>IModelImpl mod = new IModelImpl();</code><br />
 * 3. Put the needed parameter in it: <code>mod.put('param_name', param_value);</code><br />
 * 4. Call it in the query: <br />
 * <code><pre>
 * PREFIX get: &lt;http://www.inria.fr/acacia/corese/eval#&gt; 
 * SELECT * WHERE { ?x ?p get:param_name } 
 * </pre></code><br />
 * <br />
 * @author Olivier Corby & Olivier Savoie
 */
@Deprecated
public interface IModel {

	/**
     * Return the given parameter's value.
     * @param name a parameter.
     * @return the parameter's value.
     */
	public String getParameter(String name);
	
	/**
     * Return the given parameter's values.
     * @param name a parameter.
     * @return the parameter's values.
     */
	public String[] getParameterValues(String name);

	
}