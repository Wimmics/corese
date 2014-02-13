package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.PPrinter;

/**
 * Plugin for filter evaluator Compute semantic similarity of classes and
 * solutions for KGRAPH
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {

    static Logger logger = Logger.getLogger(PluginImpl.class);
    static String DEF_PPRINTER = PPrinter.PPRINTER;
    String PPRINTER = DEF_PPRINTER;
    // for storing Node setProperty() (cf Nicolas Marie store propagation values in nodes)
    // idem for setObject()
    static Table table;
    MatcherImpl match;
    Loader ld;

    PluginImpl(Matcher m) {
        if (table == null) {
            table = new Table();
        }
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
    }

    public static PluginImpl create(Matcher m) {
        return new PluginImpl(m);
    }

    public Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {

            case TURTLE:
                return turtle(env, p);

            case GRAPH:
                return getGraph(p);

            case LEVEL:
                return getLevel(env, p);

            case INDENT:
                return indent(env, p);

            case PROLOG:
                return prolog(env, p);

            case SIM:
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                // solution similarity
                return similarity(g, env);
        }

        return null;
    }

    public Object function(Expr exp, Environment env, Producer p, Object o) {
        IDatatype dt = datatype(o);

        switch (exp.oper()) {

            case KGRAM:
            case NODE:
            case LOAD:
            case DEPTH:
            case SKOLEM:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                
                switch (exp.oper()) {
                    case KGRAM:
                        return kgram(g, o);

                    case NODE:
                        return node(g, o);

                    case LOAD:
                        return load(g, o);

                    case DEPTH:
                        return depth(g, o);
                        
                     case SKOLEM:               
                        return g.skolem(dt);    
                }                
                
            case PPRINT:
            case PPRINTALL:
                return pprint(dt, null, null, null, exp, env, p);

            case TEMPLATE:
                return pprint(null, dt, exp, env, p);

            case PPRINTWITH:
                return pprint(dt, null, exp, env, p);

            case TURTLE:
                return turtle(dt, env, p);

            case PPURI:
            case URILITERAL:
                return uri(exp, dt, env, p);

            case INDENT:
                return indent(dt);

            case VISITED:
                return visited(dt, env, p);



            case GET:
                return getObject(o);

            case SET:
                return setObject(o, null);

            case QNAME:
                return qname(o, env);

           

        }
        return null;
    }

    private IDatatype visited(IDatatype dt, Environment env, Producer p) {
        PPrinter pp = getPPrinter(env, p);
        boolean b = pp.isVisited(dt);
        return getValue(b);
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1,
                dt2 = (IDatatype) o2;
        switch (exp.oper()) {

            case GETP:
                return getProperty(dt1, dt2.intValue());

            case SETP:
                return setProperty(dt1, dt2.intValue(), null);

            case SET:
                return setObject(dt1, dt2);

               
            case SIM:              
            case PSIM:               
            case ANCESTOR:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                switch (exp.oper()) {
                    case SIM:
                        // class similarity
                        return similarity(g, dt1, dt2);

                    case PSIM:
                        // prop similarity
                        return pSimilarity(g, dt1, dt2);


                    case ANCESTOR:
                        // common ancestor
                        return ancestor(g, dt1, dt2);
                }

             case WRITE:                
                return write(dt1, dt2);   
                
            case PPRINT:
            case PPRINTALL:
                // dt1: focus
                // dt2: arg
                return pprint(dt1, dt2, null, null, exp, env, p);

            case PPRINTWITH:
            case PPRINTALLWITH:
                // dt1: uri of pprinter
                // dt2: focus
                return pprint(dt2, null, dt1, null, exp, env, p);

            case TEMPLATE:
                // dt1: template name
                // dt2: focus
                return pprint(dt2, null, null, dt1, exp, env, p);

            case TEMPLATEWITH:
                // dt1: uri pprinter
                // dt2: template name
                return pprint(dt1, dt2, exp, env, p);

        }

        return null;
    }

    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {

        IDatatype dt1 = (IDatatype) args[0];
        IDatatype dt2 = (IDatatype) args[1];
        IDatatype dt3 = (IDatatype) args[2];


        switch (exp.oper()) {

            case SETP:
                return setProperty(dt1, dt2.intValue(), dt3);


            case TEMPLATE:
                // dt1: template name
                // dt2: focus
                // dt3: arg
                return pprint(dt2, dt3, null, dt1, exp, env, p);

            case TEMPLATEWITH:
                // dt1: uri pprinter
                // dt2: template name
                // dt3: focus
                return pprint(dt3, null, dt1, dt2, exp, env, p);

            case PPRINTWITH:
                // dt1: uri pprinter
                // dt2: focus
                // dt3: arg
                return pprint(dt2, dt3, dt1, null, exp, env, p);

        }

        return null;
    }

    IDatatype similarity(Graph g, IDatatype dt1, IDatatype dt2) {

        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    IDatatype ancestor(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        Node n = distance.ancestor(n1, n2);
        return (IDatatype) n.getValue();
    }

    IDatatype pSimilarity(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setPropertyDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    /**
     * Similarity of a solution with Corese method Sum distance of approximate
     * types Divide by number of nodes and edge
     *
     * TODO: cache distance in Environment during query proc
     */
    public IDatatype similarity(Graph g, Environment env) {
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
        Distance distance = g.setClassDistance();

        // number of node + edge in the answer
        int count = 0;
        float dd = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null) {
                Entity edge = memory.getEdge(qEdge);

                if (edge != null) {
                    count += 1;

                    for (int i = 0; i < edge.nbNode(); i++) {
                        // count nodes only once
                        Node n = edge.getNode(i);
                        if (!visit.containsKey(n)) {
                            count += 1;
                            visit.put(n, true);
                        }
                    }

                    if ((g.isType(qEdge) || env.getQuery().isRelax(qEdge))
                            && qEdge.getNode(1).isConstant()) {

                        Node qtype = g.getNode(qEdge.getNode(1).getLabel());
                        Node ttype = g.getNode(edge.getNode(1).getLabel());

                        if (qtype == null) {
                            // query type is undefined in ontology
                            qtype = qEdge.getNode(1);
                        }
                        if (ttype == null) {
                            // target type is undefined in ontology
                            ttype = edge.getNode(1);
                        }

                        if (!subClassOf(g, ttype, qtype, env)) {
                            dd += distance.distance(ttype, qtype);
                        }
                    }
                }
            }
        }

        if (dd == 0) {
            return getValue(1);
        }

        double sim = distance.similarity(dd, count);

        return getValue(sim);

    }

    boolean subClassOf(Graph g, Node n1, Node n2, Environment env) {
        if (match != null) {
            return match.isSubClassOf(n1, n2, env);
        }
        return g.isSubClassOf(n1, n2);
    }

    
    private IDatatype write(IDatatype dtfile, IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        ql.write(dtfile.getLabel(), dt.getLabel());
        return dt;
    }

    class Table extends Hashtable<Integer, PTable> {
    }

    class PTable extends Hashtable<Object, Object> {
    }

    PTable getPTable(Integer n) {
        PTable t = table.get(n);
        if (t == null) {
            t = new PTable();
            table.put(n, t);
        }
        return t;
    }

    Object getObject(Object o) {
        return getProperty(o, Node.OBJECT);
    }

    IDatatype setObject(Object o, Object v) {
        setProperty(o, Node.OBJECT, v);
        return TRUE;
    }

    IDatatype setProperty(Object o, Integer n, Object v) {
        PTable t = getPTable(n);
        t.put(o, v);
        return TRUE;
    }

    Object getProperty(Object o, Integer n) {
        PTable t = getPTable(n);
        return t.get(o);
    }

    Node node(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        Node n = g.getNode(dt, false, false);
        return n;
    }

    IDatatype depth(Graph g, Object o) {
        Node n = node(g, o);
        if (n == null || g.getClassDistance() == null) {
            return null;
        }
        Integer d = g.getClassDistance().getDepth(n);
        if (d == null) {
            return null;
        }
        return getValue(d);
    }

    IDatatype load(Graph g, Object o) {
        loader(g);
        IDatatype dt = (IDatatype) o;
        try {
            ld.loadWE(dt.getLabel());
        } catch (LoadException e) {
            logger.error(e);
            return FALSE;
        }
        return TRUE;
    }

    void loader(Graph g) {
        if (ld == null) {
            ld = ManagerImpl.getLoader();
            ld.init(g);
        }
    }

    Mappings kgram(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        String query = dt.getLabel();
        QueryProcess exec = QueryProcess.create(g, true);
        try {
            Mappings map = exec.sparqlQuery(query);
            return map;
        } catch (EngineException e) {
            return new Mappings();
        }
    }

    IDatatype qname(Object o, Environment env) {
        IDatatype dt = (IDatatype) o;
        if (!dt.isURI()) {
            return dt;
        }
        Query q = env.getQuery();
        if (q == null) {
            return dt;
        }
        ASTQuery ast = (ASTQuery) q.getAST();
        NSManager nsm = ast.getNSM();
        String qname = nsm.toPrefix(dt.getLabel(), true);
        if (qname.equals(dt.getLabel())) {
            return dt;
        }
        return getValue(qname);
    }

    IDatatype prolog(Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod);
        String pref = p.getNSM().toString();
        return getValue(pref);
    }

    IDatatype pprint(IDatatype tbase, IDatatype temp, Expr exp, Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod, getLabel(tbase), null);
        return p.pprint(getLabel(temp),
                exp.oper() == ExprType.PPRINTALL
                || exp.oper() == ExprType.PPRINTALLWITH,
                exp.getModality());
    }

    /**
     * exp is the calling expression: kg:pprint kg:pprintAll kg:template focus
     * is the node to be printed tbase is the path of the template base to be
     * used, may be null temp is the name of a named template, may be null
     * modality: kg:pprintAll(?x ; separator = "\n")
     */
    IDatatype pprint(IDatatype focus, IDatatype arg, IDatatype tbase, IDatatype temp, Expr exp, Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod, getLabel(tbase), focus);
        IDatatype dt = p.pprint(focus, arg,
                getLabel(temp),
                exp.oper() == ExprType.PPRINTALL
                || exp.oper() == ExprType.PPRINTALLWITH,
                exp.getModality(), exp, env.getQuery());
        return dt;
    }

    IDatatype turtle(IDatatype o, Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod);
        IDatatype dt = p.turtle(o);
        return dt;
    }

    IDatatype turtle(Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod);
        p.setTurtle(true);
        return EMPTY;
    }

    IDatatype uri(Expr exp, IDatatype dt, Environment env, Producer prod) {
        if (dt.isURI()) {
            return turtle(dt, env, prod);
        } else if (dt.isLiteral() && exp.oper() == ExprType.URILITERAL) {
            return turtle(dt, env, prod);
        } else {
            return pprint(dt, null, null, null, exp, env, prod);
        }
    }

    IDatatype getLevel(Environment env, Producer prod) {
        return getValue(level(env, prod));
    }

    int level(Environment env, Producer prod) {
        PPrinter p = getPPrinter(env, prod);
        return p.level();
    }

    IDatatype indent(IDatatype dt) {
        return indent(dt.intValue());
    }

    IDatatype indent(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(" ");
        }
        return DatatypeMap.newStringBuilder(sb);
    }

    IDatatype indent(Environment env, Producer prod) {
        return indent(level(env, prod));
    }

    /**
     *
     */
    PPrinter getPPrinter(Environment env, Producer p) {
        return getPPrinter(env, p, (String) null, null);
    }

    String getLabel(IDatatype dt) {
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return null;
    }

    String getPP(Producer prod, IDatatype dt) {
        Graph g = getGraph(prod);
        if (g == null) {
            return PPrinter.TURTLE;
        }
        IDatatype type = g.getValue(RDF.TYPE, dt);
        if (type != null) {
            String p = PPrinter.getPP(type.getLabel());
            if (p != null) {
                return p;
            }
        }
        return PPrinter.TURTLE;
    }

    PPrinter getPPrinter(Environment env, Producer prod, String t, IDatatype dt) {
        Query q = env.getQuery();
        String p = null;

        if (t != null) {
            p = t;
        } else if (q.hasPragma(Pragma.TEMPLATE)) {
            p = (String) q.getPragma(Pragma.TEMPLATE);
        } else if (!q.isPrinterTemplate() && dt != null) {
            // q is a single template query (not member of a pprinter template set)
            // and it has no pprinter name 
            // search pprinter according to type of resource dt
            p = getPP(prod, dt);
        }

        Object o = q.getPP(p);

        if (o != null) {
            return (PPrinter) o;
        } else {
            Graph g = getGraph(prod);
            if (g == null) {
                g = Graph.create();
            }
            PPrinter pp = PPrinter.create(g, p);
            ASTQuery ast = (ASTQuery) q.getAST();
            pp.setNSM(ast.getNSM());
            q.setPPrinter(p, pp);
            return pp;
        }
    }

    public void setPPrinter(String str) {
        PPRINTER = str;
    }
}
