package fr.inria.edelweiss.rif.ast;

import fr.inria.edelweiss.rif.api.IClause;
import fr.inria.edelweiss.rif.api.IConclusion;
import fr.inria.edelweiss.rif.api.IConnectible;
import fr.inria.edelweiss.rif.api.IFormula;


/** An atomic term can be either :
 * <ul><li>a rule's clause (existentially quantified or not), it's usually called a <i>universal fact</i></li>
 * <li>an item of a condition formula</li></ul> */
public abstract class Atomic extends Statement implements IFormula, IClause, IConclusion, IConnectible {
}
