package fr.inria.edelweiss.kgraph.rule;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.printer.SPIN;
import fr.inria.edelweiss.kgram.api.core.Edge;
import java.util.List;

import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Closure;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rule {

    static String TRANS_QUERY = "";
    static String TRANS_PSEUDO_QUERY = "";
    static final String TPVAR = "?t";
    static final int UNDEF = -1;
    static final int DEFAULT = 0;
    static final int TRANSITIVE = 1;
    static final int GENERIC_TRANSITIVE = 2;
    static final int PSEUDO_TRANSITIVE = 3;
    static int COUNT = 0;
    Query query;
    List<Node> predicates;
    private Closure closure;
    private Record record;
    String name;
    int num;
    private double time = 0.0;
    int rtype = UNDEF;
    private boolean isGeneric = false;
    private boolean isClosure = false;
    private Node provenance;

    static {
        try {
            QueryLoad ql = QueryLoad.create();
            TRANS_QUERY = ql.readWE(Rule.class.getResourceAsStream("/query/transitive.rq"));
            TRANS_PSEUDO_QUERY = ql.readWE(Rule.class.getResourceAsStream("/query/transitivepseudo.rq"));
        } catch (LoadException ex) {
            Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Rule(String n, Query q) {
        query = q;
        name = n;
    }

    public static Rule create(String n, Query q) {
        Rule r = new Rule(n, q);
        return r;
    }

    public static Rule create(Query q) {
        Rule r = new Rule("rule", q);
        return r;
    }
    
    String toGraph(){
        ASTQuery ast = (ASTQuery) getQuery().getAST();
        SPIN sp = SPIN.create();
        sp.visit(ast, "kg:r" + getIndex());
        return sp.toString();
    }

    void set(List<Node> list) {
        predicates = list;
    }

    List<Node> getPredicates() {
        return predicates;
    }

    public Query getQuery() {
        return query;
    }

    public Object getAST() {
        return query.getAST();
    }

    String getName() {
        return name;
    }

    public int getIndex() {
        return num;
    }
    
    public void setIndex(int n){
        num = n;
    }

    /**
     * Index of edge with predicate p If there is only one occurrence of p
     */
    public int getIndex(Node p) {

        return -1;
    }

    void setGeneric(boolean b) {
        isGeneric = true;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    public int type() {
        if (rtype == UNDEF) {
            rtype = getType();
        }
        return rtype;
    }

    /**
     * Detect if rule is transitive
     */
    int getType() {
        try {
            SPINProcess sp = SPINProcess.create();
            Graph g = sp.toSpinGraph((ASTQuery) getAST());
            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(TRANS_QUERY);

            int res = DEFAULT;
            if (map.size() > 0) {
                if (map.getNode(TPVAR) == null) {
                    res = TRANSITIVE;
                } else {
                    res = GENERIC_TRANSITIVE;
                }
            } 
            else {
                map = exec.query(TRANS_PSEUDO_QUERY);
                if (map.size() > 0) {
                    res = PSEUDO_TRANSITIVE;
                }
            }
            return res;

        } catch (EngineException ex) {
            System.out.println("R: error:  " + getAST());
            return DEFAULT;
        }

    }

    public int getEdgeIndex() {
        switch (type()) {
            case TRANSITIVE:
            case PSEUDO_TRANSITIVE:
                return 0;
            case GENERIC_TRANSITIVE:
                return 1;
            default:
                return -1;
        }
    }

    public boolean isAnyTransitive() {
        return (type() == TRANSITIVE
                || type() == GENERIC_TRANSITIVE);
    }

    public boolean isTransitive() {
        return (type() == TRANSITIVE);
    }

    public boolean isGTransitive() {
        return type() == GENERIC_TRANSITIVE;
    }

    public boolean isPseudoTransitive() {
        return (type() == PSEUDO_TRANSITIVE); // || type() == TRANSITIVE);
    }
    
    /**
     * this = x type c2        :- x type c1        & c1 subclassof c2
     * r    = c1 subclassof c3 :- c1 subclassof c2 & c2 subclassof c3
     */
    public boolean isPseudoTransitive(Rule r) {
        if (isPseudoTransitive() && r.isTransitive()){            
            Node p  = r.getUniquePredicate();
            Node pp = getQuery().getBody().get(1).getEdge().getEdgeNode();                      
            return p.equals(pp);
        }
        return false;
    }
    
    /**
     * 
     * TODO: 
     * only if pseudo rdf:type is after it's transitive rdfs:subClassOf
     */
//    public boolean isLoop() {
//        return isClosure() || isPseudoTransitive();
//    }

    public boolean isClosure() {
        return isClosure;
    }

    public void setClosure(boolean b) {
        isClosure = b;
    }

    public Node getPredicate(int i) {
        return query.getBody().get(i).getEdge().getEdgeNode();
    }

    public Node getUniquePredicate() {
        Exp cons = query.getConstruct();
        if (cons.size() == 1 && cons.get(0).isEdge()) {
            Edge edge = cons.get(0).getEdge();
            if (edge.getEdgeVariable() == null) {
                return edge.getEdgeNode();
            }
        }
        return null;
    }

    /**
     * @return the closure
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * @param closure the closure to set
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public void clean() {
        closure = null;
        record = null;
    }

    /**
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     * @return the provenance
     */
    public Node getProvenance() {
        return provenance;
    }

    /**
     * @param provenance the provenance to set
     */
    public void setProvenance(Node provenance) {
        this.provenance = provenance;
    }

    /**
     * @return the record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(Record record) {
        this.record = record;
    }
}
