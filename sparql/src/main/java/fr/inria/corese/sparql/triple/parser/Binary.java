package fr.inria.corese.sparql.triple.parser;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class Binary extends Exp {

    @Override
    public void set(int n, Exp exp) {
        super.set(n, (exp.isBGP()) ? exp : BasicGraphPattern.create(exp));
    }
    
    @Override
    public Exp add(Exp exp) {
        return super.add((exp.isBGP()) ? exp : BasicGraphPattern.create(exp));
    }
    
    @Override
    public boolean isBinaryExp() {
        return true;
    }

}
