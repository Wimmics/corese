package fr.inria.edelweiss.rif.ast;

import fr.inria.edelweiss.rif.api.IConclusion;
import fr.inria.edelweiss.rif.api.IConnectible;

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
