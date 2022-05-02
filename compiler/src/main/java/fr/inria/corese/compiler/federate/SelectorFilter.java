package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Subset of filter to be considered in source selection and in 
 * sort bgp
 */
public class SelectorFilter {    
    static HashMap<String, Boolean> map;
    static String[] ope = {"=", "regex", "contains", "strstarts"};
    ASTQuery ast;
    private FederateVisitor visitor;
    ArrayList<BasicGraphPattern> res;
    
    static {
        init();
    }
    
    SelectorFilter(FederateVisitor vis, ASTQuery ast) {
        this.ast = ast;
        visitor = vis;
        res = new ArrayList<>();
    }
    
    static void init() {
        map = new HashMap<>();
        for (String ope : ope) {
            map.put(ope, true);
        }
    }
    
    /**
     * Return list of all triples with possibly filters bound by triples
     * One BGP per triple/filters
     * Focus on filter (exp = exp)
     */
    List<BasicGraphPattern> process() {
        process(ast);       
        return res;
    }
    
    List<BasicGraphPattern> processJoin() {
        processJoin(ast);       
        return res;
    }
    
    void process(ASTQuery ast) {
        for (Expression exp : ast.getModifierExpressions()) {
            processFilter(exp);
        }
        process(ast.getBody());
    }
           
    void process(Exp body) {
        if (body.isBGP()) {
            processBGP(body);
        }
        else if (body.isQuery()) {
            process(body.getAST());
        }
        else for (Exp exp : body) {
            process(exp);
        }
    }
    
    void processBGP(Exp body) {
        processBGPTriple(body);
    }
    
    
    
    
    void processJoin(ASTQuery ast) {
        processJoin(ast.getBody());
    }
    
    void processJoin(Exp body) {
        if (body.isBGP()) {
            processBGPJoin(body);
        }
        else if (body.isOptional()) {
           processJoin(body.getOptional());
        }
        else if (body.isQuery()) {
            processJoin(body.getAST());
        }
        else for (Exp exp : body) {
            processJoin(exp);
        }
    }
    
    void processJoin(Optional body) {
        processBGPJoin(body.get(0));
        processBGPJoin(body.get(1));
        if (getVisitor().isFederateOptional()) {
            // test join(t1, t2) on triple of both arg of optional
            BasicGraphPattern bgp = BasicGraphPattern.create();
            addTriple(body.get(0), bgp);
            addTriple(body.get(1), bgp);
            processBGPJoin(bgp);
        }
    }

    void addTriple(Exp exp, BasicGraphPattern bgp) {
        for (Exp e : exp) {
            if (e.isTriple()) {
                bgp.add(e);
            }
        }
    }

    // generate bgp for test of join {t1 t2}
    void processBGPJoin(Exp body) {
        int i = 0;
        for (Exp e1 : body) {
            if (e1.isTriple() && accept(e1.getTriple())) {
                Triple t1 = e1.getTriple();
                for (int j = i + 1; j < body.size(); j++) {
                    Exp e2 = body.get(j);
                    if (e2.isTriple()) {
                        Triple t2 = e2.getTriple();
                        if (accept(t2)
                                //&& accept(t1, t2)
                                && t1.isConnected(t2)) {
                            add(t1, t2);
                        }
                    }
                }
            } else {
                processJoin(e1);
            }
            i++;
        }
    }
    
    boolean accept(Triple t1, Triple t2) {
        if (t1.getPredicate().isVariable() && t2.getPredicate().isVariable()) {
        
        }
        return true;
    }
    
    // accept for join test
    boolean accept(Triple t) {
        return getVisitor().createJoinTest(t);            
    }
    
    void add(Triple t1, Triple t2) {
        if (t1.getPredicate().isVariable() && t2.getPredicate().isConstant()) {
            basicAdd(t2, t1);
        }
        else {
            basicAdd(t1, t2);
        }
    }
    
    void basicAdd(Triple t1, Triple t2) {
        BasicGraphPattern bgp = BasicGraphPattern.create(t1, t2);
        if (accept(bgp)) {
            res.add(bgp);
        }
    }
    
    // do not generate "duplicate" pair of triple
    boolean accept(BasicGraphPattern bgp) {
        for (BasicGraphPattern exp : res) {
            boolean sim = bgp.similarPair(exp);
            if (sim) {
                return false;
            }
        }
        return true;
    }
    
    
    void processBGPTriple(Exp body) {
        for (Exp exp : body) {
            if (exp.isFilter() || exp.isBind()) {
                processFilter(exp.getFilter());
            }
            else if (exp.isTriple()) {
                process(exp.getTriple(), body);
            } else {
                process(exp);
            }
        }
    }
    
    void processFilter (Expression exp) {
        if (exp.isTermExist()) {
            process(exp.getTerm().getExistBGP());
        }
        else if (exp.isTerm()) {
            for (Expression e : exp.getArgs()) {
                processFilter(e);
            }
        }
    }
        
    // for each triple:
    // collect relevant filter or values for triple variables
    // create a candidate bgp with filter, add bgp in res
    void process(Triple t, Exp body) {
        BasicGraphPattern bgp = ast.bgp(t);
        List<Variable> list = t.getVariables();
        
        if (!list.isEmpty()) {
            for (Exp exp : body) {
                if (exp.isFilter() && accept(exp.getFilter())) {
                    if (exp.getFilter().isBound(list)) {
                        bgp.add(exp);
                    }
                }
                else if (exp.isValues()) {
                    if (exp.getValuesExp().isBound(list)) {
                        bgp.add(exp);
                    }
                }
            }
        }
        
        res.add(bgp);
    }
    
    boolean accept(Expression exp) {
        Boolean b = map.get(exp.getName().toLowerCase());
        return b!= null && b;
    }

    public FederateVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(FederateVisitor visitor) {
        this.visitor = visitor;
    }
    
    
}
