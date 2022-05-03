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
    
    void toString(Exp exp, ASTBuffer sb) {
        if (exp.isBGP()) {
            if (exp.size() > 0 && exp.get(0).isQuery()) {
                exp.toString(sb);
            } else {
                // skip { } around first arg of optional
                exp.display(sb);
            }
        } else {
            exp.toString(sb);
        }
    }

}
