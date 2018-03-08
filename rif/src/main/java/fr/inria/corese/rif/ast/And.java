package fr.inria.corese.rif.ast;

import fr.inria.corese.rif.api.IConclusion;
import fr.inria.corese.rif.api.IConnectible;

/** Conjunction of 
 * <ul><li>atomic terms,</li>
 * <li>formulas,</li>
 * <li>frames (annotations).</li></ul>*/
public class And<C extends IConnectible> extends Connective<C> implements IConclusion {
	
	private And() { }

	public static <C extends IConnectible> And<C> create() {
		return new And<C>() ;
	}
	
}
