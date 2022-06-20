package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Binary;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Union;
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
    public static String[] OPER_LIST = {"=", "regex", "contains", "strstarts"};
    ASTQuery ast;
    private FederateVisitor visitor;
    ArrayList<BasicGraphPattern> res;
    // when join bgp fail, query should fail
    HashMap<BasicGraphPattern, Boolean>  bgpFail;
    
    static {
        init();
    }
    
    SelectorFilter(FederateVisitor vis, ASTQuery ast) {
        this.ast = ast;
        visitor = vis;
        res = new ArrayList<>();
        bgpFail = new HashMap<>();
    }
    
    static void init() {
        map = new HashMap<>();
        for (String ope : OPER_LIST) {
            defineOperator(ope, true);
        }
    }
    
    public static void defineOperator(String oper, boolean b)  {
        map.put(oper, b);
    }
    
    public static void rejectOperator(String oper, boolean b)  {
        map.remove(oper);
    }
    
    class JoinResult {
        // list of join connected pair {t1 . t2}
        private ArrayList<BasicGraphPattern> bgpList;
        // query may fail when bgp {t1 t2} has no join
        private HashMap<BasicGraphPattern, Boolean>  bgpFail;
        
        JoinResult(ArrayList<BasicGraphPattern> res, HashMap<BasicGraphPattern, Boolean>  fail) {
            bgpList = res;
            bgpFail = fail;
        }

        public ArrayList<BasicGraphPattern> getBgpList() {
            return bgpList;
        }

        public void setBgpList(ArrayList<BasicGraphPattern> bgpList) {
            this.bgpList = bgpList;
        }

        public HashMap<BasicGraphPattern, Boolean> getBgpFail() {
            return bgpFail;
        }

        public void setBgpFail(HashMap<BasicGraphPattern, Boolean> bgpFail) {
            this.bgpFail = bgpFail;
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
    
    JoinResult processJoin() {
        processJoin(ast); 
        JoinResult ares = new JoinResult(res, bgpFail);
        return ares;
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
       
    // By default, basic bgp fail when join fail
    void processJoin(ASTQuery ast) {
        processJoin(ast, true);
    }
    
    void processJoin(ASTQuery ast, boolean fail) {
        processJoin(ast.getBody(), fail);
    }
    
    void processJoin(Exp body, boolean fail) {
        if (body.isBGP()) {
            processBGPJoin(body, fail);
        }
        else if (body.isOptional()) {
           processJoin(body.getOptional(), fail);
        }
        else if (body.isMinus()) {
           processJoin(body.getMinus(), fail);
        }
        else if (body.isUnion()) {
           processJoin(body.getUnion(), fail);
        }
        else if (body.isQuery()) {
            processJoin(body.getAST(), fail);
        }
        else if (body.isFilter()) {
            processJoinFilter(body.getFilter());
        }
        else for (Exp exp : body) {
            processJoin(exp, fail);
        }
    }   
    
    // filter exists { bgp }
    // participate to bgp join selection in order to benefit
    // from bgp processing heuristics
    boolean processJoinFilter(Expression exp) {
        boolean exist = false;
        if (exp.isTerm()) {
            if (exp.getTerm().isTermExist()) {
                exist = true;
                processJoinExist(exp);
            } else {
                for (Expression arg : exp.getArgs()) {
                    if (processJoinFilter(arg)) {
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }
    
    // exp = exists { bgp }
    void processJoinExist(Expression exp) {
        Exp bgp = exp.getTerm().getExist().get(0);
        processJoin(bgp, false);
    }
    
    // optional minus
    void processJoin(Binary body, boolean fail) {
        processBGPJoin(body.get(0), fail);
        processBGPJoin(body.get(1), false);
        
        if ((body.isOptional() && getVisitor().isFederateOptional()) ||
            (body.isMinus()    && getVisitor().isFederateMinus())) {
            // test join(t1, t2) on triple of both arg of optional/minus
            // to enable simplification of body
            BasicGraphPattern bgp = BasicGraphPattern.create();
            addTriple(body.get(0), bgp);
            addTriple(body.get(1), bgp);
            processBGPJoin(bgp, false);
        }
    }
    
    // union
    void processJoin(Union body, boolean fail) {
        processBGPJoin(body.get(0), false);
        processBGPJoin(body.get(1), false);               
    }

    void addTriple(Exp exp, BasicGraphPattern bgp) {
        for (Exp e : exp) {
            if (e.isTriple() && !bgp.getBody().contains(e)) {
                // skip duplicate triples
                bgp.add(e);
            }
        }
    }

    // generate bgp for test of join {t1 t2}
    void processBGPJoin(Exp body, boolean fail) {
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
                            add(t1, t2, fail);
                        }
                    }
                }
            } else {
                processJoin(e1, fail);
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
    
    void add(Triple t1, Triple t2, boolean fail) {
        if (t1.getPredicate().isVariable() && t2.getPredicate().isConstant()) {
            basicAdd(t2, t1, fail);
        }
        else {
            basicAdd(t1, t2, fail);
        }
    }
    
    void basicAdd(Triple t1, Triple t2, boolean fail) {
        BasicGraphPattern bgp = BasicGraphPattern.create(t1, t2);
        BasicGraphPattern key = getKey(bgp);
        if (key==bgp) {
            res.add(bgp);            
        }
        if (fail) { 
           bgpFail.put(key, fail); 
        }
    }
    
    // return unique key bgp
    BasicGraphPattern getKey(BasicGraphPattern bgp) {
        for (BasicGraphPattern exp : res) {
            boolean sim = bgp.similarPair(exp);
            if (sim) {
                return exp;
            }
        }
        return bgp;
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
