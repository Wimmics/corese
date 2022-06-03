package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;
import java.util.List;

/**
 * Rewrite one triple into one service with several URI 
 * from, from named and named graph have two rewrite solutions
 * 1- service s { select from g where exp }
 * 2- service s { graph g exp }
 * solution 1 does not conform to SPARQL standard because 
 * dataset in subquery is forbidden
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class RewriteTriple {
      
    FederateVisitor vis;
    // true:  graph g exp; false: select from g where exp
    boolean withGraph = true;
    
    RewriteTriple(FederateVisitor vis) {
        this.vis = vis;
    }

    /**
     * Rewrite Triple t as: 
     * -- name == null
     * service <Si> { t }  
     * -- name == null && query = select from g1 gn 
     * service <Si> { graph g1 { t } union graph gn { t } }
     * -- name == g
     * service <Si> { graph g { t } }  
     * Add filters of body bound by t in the BGP, except exists filters.
     */
    Service rewriteTripleWithSeveralURI(Atom name, Triple t, Exp body, List<Exp> list) {
        BasicGraphPattern bgp = BasicGraphPattern.create(t);
        filter(body, t, bgp, list);
        List<Atom> alist = vis.getServiceList(t);
        if (alist.isEmpty()) {
            vis.error(t, "rewrite triple");
        }
        return rewrite(name, bgp, alist);
    }

    /**
     * Two cases:
     * 1- One triple and several service URI
     * 2- A BGP and one service URI.
     */
    Service rewrite(Atom namedGraph, BasicGraphPattern bgp, List<Atom> serviceList) {
        Exp exp;
        if (namedGraph == null) {
            if (vis.isFederateBGP()) {
                exp = bgp;
            }
            else if (getAST().getDataset().hasFrom()) {
                // select from gi where bgp
                exp = from(bgp);
            } else {
                exp = bgp;
            }
        } else {
            // graph name { bgp }
            exp = named(namedGraph, bgp);
        }        
        Service s = vis.service(serviceList, bgp(exp));
        return s;
    }
             
    Exp bgp(Exp exp) {
        if (exp.isBGP()) {
            return exp;
        }
        return BasicGraphPattern.create(exp);
    }
    
    // graph name { bgp }
    Exp named(Atom name, BasicGraphPattern bgp) {
        return graphNamed(name, bgp);
    }   
     
    // select from where bgp
    Exp from(BasicGraphPattern bgp) {
        if (getAST().getFrom().size() == 1) {
            return basicGraphFrom(bgp);
        }
        return graphFrom(bgp.copy());
    }
    
    
    
    Exp graphNamed(Atom name, BasicGraphPattern bgp) {
        return Source.create(name, bgp);
    }
    
    /**
     * for all t in bgp, for all g in from : 
     * graph g1 { t1 } union .. graph gn { t1 }
     * graph g1 { tm } union .. graph gn { tm }
     */       
    Exp graphFrom(Exp bgp) {
        int i = 0;
        for (Exp exp : bgp) {
            if (! exp.isFilter()) {
                Exp union = graphUnion(BasicGraphPattern.create(exp), getAST().getFrom());               
                bgp.set(i, bgpSelectDistinct(union));
            }
            i++;
        }
        return bgp;
    }
    
    Exp basicGraphFrom(Exp bgp) {
        Source src = Source.create(getAST().getFrom().get(0), bgp);
        return src;
    }
    
    Exp bgpSelectDistinct(Exp exp) {
        if (exp.isGraph()) {
            return exp;
        }
        return BasicGraphPattern.create(distinct(exp));
    }
    
    Exp distinct(Exp exp) {
        Query q = query(BasicGraphPattern.create(exp));
        q.getAST().setDistinct(true);
        return q;
    }
    
    Exp graphUnion(Exp exp, List<Constant> from) {
        Source src = Source.create(from.get(0), exp);
        if (from.size() == 1) {
            return src;
        }
        Exp union = src;
        int i = 0;
        for (Constant cst : from) {
            if (i++ > 0) {
                src = Source.create(cst, exp);
                union = Union.create(bgp(union), bgp(src));
            }
        }
        return union;
    }
        

    Query query(Exp exp) {
        ASTQuery as = getAST().subCreate();
        as.setBody(exp);
        as.setSelectAll(true);
        return Query.create(as);
    }
    
    
    /**
     * Find filters bound by t in body, except exists {} Add them to bgp
     */
    void filter(Exp body, Triple t, Exp bgp, List<Exp> list) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (vis.isRecExist(exp)) {
                    // skip
                }
                else {
                    if (t.bind(exp.getFilter()) && ! bgp.getBody().contains(exp)) {
                        bgp.add(exp);
                        if (! list.contains(exp)) {
                            list.add(exp);
                        }
                    }
                }
            }
        }
    }
    
    
     /**
     * @return the ast
     */
    public ASTQuery getAST() {
        return vis.getAST();
    }

    Exp selectNamed(Atom name, BasicGraphPattern bgp) {
        Query q = query(bgp);
        q.getAST().getDataset().addFrom(name.getConstant());
        return BasicGraphPattern.create(q);
    }
    
        // select from uri { bgp }
    Exp selectFrom(BasicGraphPattern bgp) {
        Query q = query(bgp);
        q.getAST().getDataset().setFrom(getAST().getFrom());
        return BasicGraphPattern.create(q);
    }

}
