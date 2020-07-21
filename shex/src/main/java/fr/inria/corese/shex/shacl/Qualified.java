package fr.inria.corese.shex.shacl;

import fr.inria.lille.shexjava.schema.abstrsynt.AbstractNaryShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.AbstractNaryTripleExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.EachOf;
import fr.inria.lille.shexjava.schema.abstrsynt.OneOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Detect  several occurrences of same property in order to generate
 * sh:qualifiedValueShape 
 * Store information in Context as TripleExpr -> Boolean
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Qualified extends HashMap<String, List<TripleExpr>> {

    Qualified() {
    }
    
    void traceClass(Object obj) {
        System.out.println(obj.getClass().getName());
    }
    
    // test ex:p and ^ex:p 
    void create(AbstractNaryShapeExpr exp, Context ct) {
        create(exp, ct, true);
        clear();
        create(exp, ct, false);
    }
    
    void create(AbstractNaryTripleExpr exp, Context ct) {
        create(exp, ct, true);
        clear();
        create(exp, ct, false);
    }

    // AND [OR]
    void create(AbstractNaryShapeExpr exp, Context ct, boolean forward) {
        for (ShapeExpr ee : exp.getSubExpressions()) {
            if (ee instanceof Shape) {
                Shape sh = (Shape) ee;
                create(sh.getTripleExpression(), ct, forward);
            }
        }
        count(ct);
    }

    // oneOf eachOf
    void create(AbstractNaryTripleExpr exp, Context ct, boolean forward) {
        for (TripleExpr ee : exp.getSubExpressions()) {
            create(ee, ct, forward);
        }
        count(ct);
    }
    
    void count(Context ct) {
        for (String uri : keySet()) {
            List<TripleExpr> list = get(uri);
            // several occurrences of same property => sh:qualifiedValueShape
            if (list.size() > 1) {
                for (TripleExpr texp : list) {
                    ct.qualify(texp);
                }
            }
        }
    }
    
    void create(TripleExpr ee, Context ct, boolean forward) {
        if (ee instanceof RepeatedTripleExpression) {
            RepeatedTripleExpression re = (RepeatedTripleExpression) ee;
            create(re.getSubExpression(), ct, forward);
        } else if (ee instanceof TripleConstraint) {
            TripleConstraint tc = (TripleConstraint) ee;
            create(tc, ct, forward);
        } 
        else if (ee instanceof EachOf) {
            AbstractNaryTripleExpr abs = (AbstractNaryTripleExpr) ee;
            create(abs, ct, forward);
        }
//        else if (ee instanceof AbstractNaryTripleExpr) {
//            AbstractNaryTripleExpr abs = (AbstractNaryTripleExpr) ee;
//            create(abs, ct, forward);
//        }
    }
    
    // record TripleConstraint
    void create(TripleConstraint tc, Context ct, boolean forward) {
        if (forward == tc.getProperty().isForward()) {
            String uri = tc.getProperty().getIri().toString();
            getList(uri).add(tc);
        }
    }
    
    List<TripleExpr> getList(String uri) {
        List<TripleExpr> list = get(uri);
        if (list == null) {
            list = new ArrayList<>();
            put(uri, list);
        }
        return list;
    }

}
