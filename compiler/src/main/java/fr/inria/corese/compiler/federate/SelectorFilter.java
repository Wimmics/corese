package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
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
    ArrayList<BasicGraphPattern> res;
    
    static {
        init();
    }
    
    SelectorFilter(ASTQuery ast) {
        this.ast = ast;
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
    
    void processJoin(ASTQuery ast) {
        processJoin(ast.getBody());
    }
    
    void processJoin(Exp body) {
        if (body.isBGP()) {
            processBGPJoin(body);
        }
        else if (body.isQuery()) {
            processJoin(body.getAST());
        }
        else for (Exp exp : body) {
            processJoin(exp);
        }
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
    
    // generate test of join {t1 t2}
    void processBGPJoin(Exp body) {
        int i = 0;
        for (Exp exp : body) {
            if (exp.isTriple()) {
                for (int j = i+1; j<body.size(); j++) {
                    Exp ee = body.get(j);
                    if (ee.isTriple()) {
                        Triple t1 = exp.getTriple();
                        Triple t2 = ee.getTriple();
                        if (t1.isConnected(t2)) {
                            add(t1, t2);
                        }
                    }
                }
            }
            i++;
        }
    }
    
    void add(Triple t1, Triple t2) {
        BasicGraphPattern bgp = BasicGraphPattern.create(t1, t2);
        res.add(bgp);
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
        res.add(bgp);
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
    }
    
    boolean accept(Expression exp) {
        Boolean b = map.get(exp.getName().toLowerCase());
        return b!= null && b;
    }
    
    
}
