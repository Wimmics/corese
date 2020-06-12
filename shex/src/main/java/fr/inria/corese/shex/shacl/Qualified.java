package fr.inria.corese.shex.shacl;

import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.abstrsynt.EachOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Group several occurrences of same property in order to generate
 * qualifiedValueShape Identify Shacl properties such as sh:targetClass that may
 * be considered as shacl statement
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
            return process(null);
    }
    
    List<TripleExpr> process(Shape sh) {
        ArrayList<TripleExpr> done = new ArrayList<>();

        for (String uri : keySet()) {
            // two occurrences of same property p result in 
            // qualifiedValueShape for each occurrence
            // to conform to shex semantics
            // use case: p [a b] ; p [c d]
            // -> p qualifiedValueShape [ sh:in (a b)] qualifiedMinCount 1
            // -> p qualifiedValueShape [ sh:in (c d)] qualifiedMinCount 1 
            List<TripleExpr> list = get(uri);

            if (list.size() > 1) {
                for (TripleExpr te : list) {
                    if (te instanceof TripleConstraint) {
                        TripleConstraint tc = (TripleConstraint) te;
                        shex.processQualify(tc);
                        done.add(tc);
                    } else if (te instanceof RepeatedTripleExpression) {
                        RepeatedTripleExpression re = (RepeatedTripleExpression) te;
                        if (re.getSubExpression() instanceof TripleConstraint) {
                            TripleConstraint tc = (TripleConstraint) re.getSubExpression();
                            shex.processQualify(tc, new Context(re));
                            done.add(te);
                        }
                    }

                }
            }
        }
        return done;
    }
    
   

}
