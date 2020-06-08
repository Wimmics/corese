package fr.inria.corese.shex;

import fr.inria.lille.shexjava.schema.abstrsynt.EachOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Group several occurrences of same property in order to generate qualifiedValueShape
 * Identify Shacl properties such as sh:targetClass that may be considered as shacl statement
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Qualified extends HashMap<String, List<TripleExpr>> {

    Shex shex;

    Qualified(Shex shex) {
        this.shex = shex;
    }

    // create map: property -> list of Expr
    Qualified create(EachOf exp, boolean forward) {
        for (TripleExpr te : exp.getSubExpressions()) {
            TripleExpr ee = te;

            if (ee instanceof RepeatedTripleExpression) {
                RepeatedTripleExpression re = (RepeatedTripleExpression) ee;
                if (re.getSubExpression() instanceof TripleConstraint) {
                    ee = re.getSubExpression();
                }
            }

            if (ee instanceof TripleConstraint) {
                TripleConstraint tc = (TripleConstraint) ee;
                if (forward == tc.getProperty().isForward()) {
                    String uri = tc.getProperty().getIri().toString();
                    List<TripleExpr> list = get(uri);
                    if (list == null) {
                        list = new ArrayList<>();
                        put(uri, list);
                    }
                    // store repeated if any
                    list.add(te);
                }
            }
        }
        return this;
    }

    List<TripleExpr> process() {
        return process(false);
    }

    List<TripleExpr> process(boolean shacl) {
        ArrayList<TripleExpr> done = new ArrayList<>();

        for (String uri : keySet()) {
            // two occurrences of same property p result in 
            // qualifiedValueShape for each occurrence
            // to conform to shex semantics
            // use case: p [a b] ; p [c d]
            // -> p qualifiedValueShape [ sh:in (a b)] qualifiedMinCount 1
            // -> p qualifiedValueShape [ sh:in (c d)] qualifiedMinCount 1 
            List<TripleExpr> list = get(uri);

            if (shacl || list.size() > 1) {
                for (TripleExpr te : list) {
                    if (te instanceof TripleConstraint) {
                        TripleConstraint tc = (TripleConstraint) te;
                        if (shacl) {
                            // special case: sh:targetClass considered as shacl statement
                            if (shex.isShacl(tc.getProperty())) {
                                shex.processShacl(tc);
                                done.add(tc);
                            }
                        } else {
                            shex.qualify(tc);
                            done.add(tc);
                        }
                        
                    } else if (te instanceof RepeatedTripleExpression && !shacl) {
                        RepeatedTripleExpression re = (RepeatedTripleExpression) te;
                        if (re.getSubExpression() instanceof TripleConstraint) {
                            TripleConstraint tc = (TripleConstraint) re.getSubExpression();
                            shex.qualify(tc, new Context(re));
                            done.add(te);
                        }
                    }

                }
            }
        }
        return done;
    }

}
