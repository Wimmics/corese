package fr.inria.corese.core.approximate.ext;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import static fr.inria.corese.core.approximate.ext.ASTRewriter.O;
import static fr.inria.corese.core.approximate.ext.ASTRewriter.P;
import static fr.inria.corese.core.approximate.ext.ASTRewriter.S;
import fr.inria.corese.core.approximate.strategy.StrategyType;
import java.util.ArrayList;
import java.util.List;

/**
 * TripleWrapper.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 30 nov. 2015
 */
class TripleWrapper {

    private final Triple triple;
    private final int position;
    private final List<StrategyType> strategy;

    public TripleWrapper(Triple triple, int position, List<StrategyType> strategy) {
        this.triple = triple;
        this.position = position;
        this.strategy = strategy;
    }

    public TripleWrapper(Triple triple, int position) {
        this(triple, position, new ArrayList<StrategyType>());
    }

    public Atom getAtom() {
        switch (position) {
            case S:
                return this.triple.getSubject();
            case P:
                return this.triple.getPredicate();
            case O:
                return this.triple.getObject();
            default:
                return null;
        }
    }

    public void setAtom(Variable var) {
        switch (position) {
            case S:
                triple.setSubject(var);
                break;
            case P:
                triple.setPredicate(var);
                break;
            case O:
                triple.setObject(var);
                break;
            default:
        }
    }

    public void addStrategy(StrategyType strategy) {
        this.strategy.add(strategy);
    }

    public int getPosition() {
        return position;
    }

    public Triple getTriple() {
        return triple;
    }

    public List<StrategyType> getStrategies() {
        return strategy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.triple).append("\t");
        sb.append(this.getAtom()).append(",\t");
        sb.append(this.getPosition()).append(",\t");
        sb.append("[");
        for (StrategyType st : strategy) {
            sb.append(st.name()).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
