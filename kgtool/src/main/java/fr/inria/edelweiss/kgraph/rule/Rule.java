package fr.inria.edelweiss.kgraph.rule;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Edge;
import java.util.List;

import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.OWL;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rule {

    static String TRANS_QUERY = "";
        
    static final String TPVAR = "?t";

    static final int UNDEF = -1;
    static final int DEFAULT = 0;
    static final int TRANSITIVE = 1;
    static final int GENERIC_TRANSITIVE = 2;
    static int COUNT = 0;
    Query query;
    List<Node> predicates;
    String name;
    int num;
    int rtype = UNDEF;
    private boolean isGeneric = false;
    
    static {
        try {
            QueryLoad ql = QueryLoad.create();
            TRANS_QUERY = ql.read(Rule.class.getResourceAsStream("/query/transitive.rq"));
        } catch (IOException ex) {
            Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    Rule(String n, Query q) {
        query = q;
        name = n;
        num = COUNT++;
    }

    public static Rule create(String n, Query q) {
        Rule r = new Rule(n, q);
        return r;
    }

    public static Rule create(Query q) {
        Rule r = new Rule("rule", q);
        return r;
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

    int getIndex() {
        return num;
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
            return res;

        } catch (EngineException ex) {
            System.out.println("R: error:  " + getAST());
            return DEFAULT;
        }

    }

    int getType2() {
        int res = DEFAULT;
        Exp exp = getQuery().getBody();

        if (!(exp.type() == Exp.AND
                && (getPredicates().size() == 1 || getPredicates().size() == 2)
                && (exp.size() == 2 || exp.size() == 3))) {
            return DEFAULT;
        }

        int i = 0;
        for (Exp ee : exp) {
            if (!ee.isEdge()) {
                return DEFAULT;
            }
            Edge edge = ee.getEdge();
            if (exp.size() == 2 || i > 0) {
                if (!(edge.getNode(0).isVariable() && edge.getNode(1).isVariable())) {
                    return DEFAULT;
                }
            }

            i++;
        }

        Edge e1 = exp.get(0).getEdge();
        Edge e2 = exp.get(1).getEdge();

        if (exp.size() == 3) {
            res = GENERIC_TRANSITIVE;
            e1 = exp.get(1).getEdge();
            e2 = exp.get(2).getEdge();

            Edge e0 = exp.get(0).getEdge();
            if (!e0.getNode(1).getLabel().equals(OWL.TRANSITIVE)
                    || !e0.getEdgeNode().getLabel().equals(RDF.TYPE)) {
                return DEFAULT;
            }
            Node pred = e0.getNode(0);
            if (!pred.isVariable()) {
                return DEFAULT;
            }
            if (e1.getEdgeVariable() == null || !e1.getEdgeVariable().equals(pred)
                    || e2.getEdgeVariable() == null || !e2.getEdgeVariable().equals(pred)) {
                return DEFAULT;
            }

        } else {
            if (!e1.getEdgeNode().equals(e2.getEdgeNode())) {
                return DEFAULT;
            }
            res = TRANSITIVE;
        }


        boolean correct =
                !e1.getNode(0).equals(e1.getNode(1))
                && e1.getNode(1).equals(e2.getNode(0))
                && !e2.getNode(0).equals(e2.getNode(1))
                && !e1.getNode(0).equals(e2.getNode(1));

        if (!correct) {
            return DEFAULT;
        }

        Exp cons = getQuery().getConstruct();
        if (cons.size() == 1 && cons.get(0).isEdge()) {
            Edge edge = cons.get(0).getEdge();

            if (edge.getEdgeVariable() == null
                    && (e1.getEdgeVariable() != null
                    || !edge.getEdgeNode().equals(e1.getEdgeNode()))) {
                return DEFAULT;
            }

            if (edge.getEdgeVariable() != null
                    && (e1.getEdgeVariable() == null
                    || !edge.getEdgeVariable().equals(e1.getEdgeVariable()))) {
                return DEFAULT;
            }

            if (!(edge.getNode(0).equals(e1.getNode(0))
                    && edge.getNode(1).equals(e2.getNode(1)))) {
                return DEFAULT;
            }


        }


        return res;
    }

    public int getEdgeIndex() {
        switch (type()) {
            case TRANSITIVE:
                return 0;
            case GENERIC_TRANSITIVE:
                return 1;
            default:
                return -1;
        }
    }

    public boolean isTransitive() {
        return (type() == TRANSITIVE);
    }

    public boolean isGTransitive() {
        return type() == GENERIC_TRANSITIVE;
    }

    public boolean isTransitive2() {
        Exp exp = getQuery().getBody();
        if (exp.type() == Exp.AND
                && getPredicates().size() == 1
                && exp.size() == 2) {

            for (Exp ee : exp) {
                if (!ee.isEdge()) {
                    return false;
                }
                Edge edge = ee.getEdge();
                if (edge.getEdgeVariable() != null) {
                    return false;
                }
                if (!(edge.getNode(0).isVariable() && edge.getNode(1).isVariable())) {
                    return false;
                }
            }

            Edge e1 = exp.get(0).getEdge();
            Edge e2 = exp.get(1).getEdge();

            boolean correct =
                    !e1.getNode(0).equals(e1.getNode(1))
                    && e1.getNode(1).equals(e2.getNode(0))
                    && !e2.getNode(0).equals(e2.getNode(1))
                    && !e1.getNode(0).equals(e2.getNode(1));

            if (!correct) {
                return false;
            }

            Exp cons = getQuery().getConstruct();
            if (cons.size() == 1 && cons.get(0).isEdge()) {
                Edge edge = cons.get(0).getEdge();
                if (edge.getEdgeVariable() == null
                        && edge.getEdgeNode().equals(e1.getEdgeNode())
                        && edge.getNode(0).equals(e1.getNode(0))
                        && edge.getNode(1).equals(e2.getNode(1))) {
                    return true;
                }
            }
        }

        return false;
    }
}
