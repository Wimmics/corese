package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 * SPARQL-based RDF AST Pretty Printer Use case: pprint SPIN RDF in SPARQL
 * concrete syntax pprint OWL 2 RDF in functional syntax Use a list of templates
 * : template { presentation } where { pattern } Templates are loaded from a
 * directory or from a file in .rul format (same as rules) Templates can also be
 * defined using defTemplate() If called with kg:pprint(?x) : execute one
 * template on ?x If called with kg:pprintAll(?x) : execute all templates on ?x
 *
 * Olivier Corby, Wimmics INRIA I3S - 2012
 */
public class PPrinter {

    private static final String NULL = "";
    public static final String PPLIB = "/template/";
    public static final String PPNS = NSManager.PPN;
    public static final String SQL = PPNS + "sql";
    public static final String SPIN = PPNS + "spin";
    public static final String OWL = PPNS + "owl";
    public static final String TURTLE = PPNS + "turtle";
    public static final String TYPECHECK = PPNS + "typecheck";
    // default
    public static final String PPRINTER = TURTLE;
    private static final String OUT = ASTQuery.OUT;
    private static final String IN = ASTQuery.IN;
    private static final String IN2 = ASTQuery.IN2;
    private static String NL = System.getProperty("line.separator");
    private static boolean isOptimizeDefault = false;
    private static boolean isExplainDefault = false;

    
    Graph graph, fake;
    QueryEngine qe;
    Query query;
    NSManager nsm;
    QueryProcess exec;
    PPrinterStack stack;
    static PPrinterTable table;
    String pp = PPRINTER;
    // separator of results of several templates kg:templateAll()
    String sepTemplate = NL;
    // separator of several results of one template
    String sepResult = " ";
    boolean isDebug = false;
    private boolean isDetail = false;
    private IDatatype EMPTY;
    boolean isTurtle = false;
    int nbt = 0, max = 0;
    private static final String START = Exp.KGRAM + "start";
    String start = START;
    HashMap<Query, Integer> tcount;
    private boolean isHide = false;
    public boolean stat = !true;
    private boolean isAllResult = true;
    private static Logger logger = Logger.getLogger(PPrinter.class);
    private boolean isCheck = false;
    // index and run templates according to focus node type
    // no subsumption (exact match on type)
    private boolean isOptimize = isOptimizeDefault;

    static {
        table = new PPrinterTable();
    }
    private boolean isExplain = isExplainDefault();

    /**
     * @return the isCheck
     */
    public boolean isCheck() {
        return isCheck;
    }

    /**
     * @param isCheck the isCheck to set
     */
    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    /**
     * @return the isDetail
     */
    public boolean isDetail() {
        return isDetail;
    }

    /**
     * @param isDetail the isDetail to set
     */
    public void setDetail(boolean isDetail) {
        this.isDetail = isDetail;
    }

    /**
     * @return the isOptimizeDefault
     */
    public static boolean isOptimizeDefault() {
        return isOptimizeDefault;
    }

    /**
     * @param aIsOptimizeDefault the isOptimizeDefault to set
     */
    public static void setOptimizeDefault(boolean aIsOptimizeDefault) {
        isOptimizeDefault = aIsOptimizeDefault;
    }
    
    /**
     * @return the isExplainDefault
     */
    public static boolean isExplainDefault() {
        return isExplainDefault;
    }

    /**
     * @param aIsExplainDefault the isExplainDefault to set
     */
    public static void setExplainDefault(boolean aIsExplainDefault) {
        isExplainDefault = aIsExplainDefault;
    }

    /**
     * @return the isOptimize
     */
    public boolean isOptimize() {
        return isOptimize;
    }

    /**
     * @param isOptimize the isOptimize to set
     */
    public void setOptimize(boolean isOptimize) {
        this.isOptimize = isOptimize;
    }

    PPrinter(Graph g, String p) {
        this(QueryProcess.create(g, true), p);
    }

    PPrinter(QueryProcess qp, String p) {
        //table = new PPrinterTable();
        graph = qp.getGraph();
        fake = Graph.create();
        pp = p;
        exec = qp;
        tune(exec);
        nsm = NSManager.create();
        init();
        stack = new PPrinterStack(true);
        EMPTY = DatatypeMap.createLiteral(NULL);
        tcount = new HashMap<Query, Integer>();
    }

    public void setTemplates(String p) {
        pp = p;
        init();
    }

    private void tune(QueryProcess exec) {
        // do not use Thread in Property Path
        // compute all path nodes and put them in a list
        // it is faster
        exec.setListPath(true);
        Producer prod = exec.getProducer();
        if (prod instanceof ProducerImpl) {
            // return value as is for kg:pprint()
            // no need to create a graph node in Producer
            ProducerImpl pi = (ProducerImpl) prod;
            pi.setSelfValue(true);
        }
    }

    public static PPrinter create(Graph g) {
        return new PPrinter(g, null);
    }

    public static PPrinter create(Graph g, String p) {
        return new PPrinter(g, p);
    }

    public static PPrinter create(QueryProcess qp, String p) {
        return new PPrinter(qp, p);
    }

    public static void define(String ns, String pp) {
        table.put(ns, pp);
    }
    
     public static void define(String ns, boolean isOptimize) {
        table.setOptimize(ns, isOptimize);
    }

    public void setNSM(NSManager n) {
        nsm = n;
    }

    public NSManager getNSM() {
        return nsm;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public void setTurtle(boolean b) {
        isTurtle = b;
    }

    // when several templates kg:templateAll()
    public void setTemplateSeparator(String s) {
        sepTemplate = s;
    }

    // when several results for one template
    public void setResultSeparator(String s) {
        sepResult = s;
    }

    public void setStart(String s) {
        start = s;
    }

    public int nbTemplates() {
        return nbt;
    }

    public String toString() {
        IDatatype dt = pprint();
        return dt.getLabel();
    }

    public StringBuilder toStringBuilder() {
        IDatatype dt = pprint();
        return dt.getStringBuilder();
    }

    public void write(String name) throws IOException {
        FileWriter fw = new FileWriter(name);
        String str = toString();
        fw.write(str);
        fw.flush();
        fw.close();
    }

    public void defTemplate(String t) {
        try {
            qe.defQuery(t);
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public boolean isVisited(IDatatype dt) {
        return stack.isVisited(dt);
    }

    /**
     * Pretty print the graph. Apply the first query that matches without
     * bindings. Hence it may pprint a subpart of the graph, the subpart that
     * matches the first query
     */
    public IDatatype pprint() {
        return pprint(null, false, null);
    }

    public IDatatype pprint(String temp) {
        return pprint(temp, false, null);
    }

    public IDatatype pprint(String temp, boolean all, String sep) {
        query = null;
        ArrayList<IDatatype> result = new ArrayList<IDatatype>();
        if (temp == null) {
            temp = start;
        }
        List<Query> list = getTemplate(temp);
        if (list.size() == 0) {
            list = qe.getTemplates();
        }
        if (list.size() == 0) {
            logger.error("No templates");
        }

        for (Query qq : list) {

            if (isDebug) {
                qq.setDebug(true);
            }
            //qq.setPPrinter(pp, this);
            // remember start with qq for function pprint below
            query = qq;
            Mappings map = exec.query(qq);

            query = null;
            IDatatype res = getResult(map);

            if (res != null) {
                if (all) {
                    result.add(res);
                } else {
                    return res;
                }
            }
        }

        query = null;

        if (all) {
            IDatatype dt = result(result, separator(sep));
            return dt;
        }

        return EMPTY;
    }

    public int level() {
        return stack.size();
    }

    public int maxLevel() {
        return max;
    }

    public IDatatype pprint(Node node) {
        return pprint((IDatatype) node.getValue());
    }

    public IDatatype pprint(IDatatype dt) {
        return pprint(dt, null, null, false, null, null, null);
    }

    public IDatatype template(String temp, IDatatype dt) {
        return pprint(dt, null, temp, false, null, null, null);
    }

    /**
     * exp : the fun call, e.g. kg:pprint(?x) dt : focus node to be printed temp
     * : name of a template (may be null) allTemplates : execute all templates
     * on focus and concat results sep : separator in case of allTemplates
     *
     * use case: template { ... ?w ... } where { ... ?w ... } Search a template
     * that matches ?w By convention, ?w is bound to ?in, all templates use
     * variable ?in as focus node and ?out as output node Execute the first
     * template that matches ?w (all templates if allTemplates = true) Templates
     * are sorted more "specific" first They are sorted using a pragma {
     * kg:query kg:priority n } A template is applied only once on one node,
     * hence we store in a stack : node -> template context of evaluation:
     * select (kg:pprint(?x) as ?px) (concat (?px ...) as ?out) where {}
     */
    public IDatatype pprint(IDatatype dt1, IDatatype dt2, String temp,
            boolean allTemplates, String sep, Expr exp, Query q) {
        if (dt1 == null) {
            return EMPTY;
        }

        if (pp == null) {
            String name = getPP(dt1);
            if (name != null) {
                pp = name;
                init();
            }
        }

        ArrayList<IDatatype> result = null;
        if (allTemplates) {
            result = new ArrayList<IDatatype>();
        }
        boolean start = false;

        if (query != null && stack.size() == 0) {
            // just started with query in pprint() above
            // without focus node at that time
            //if (exp != null){ 
            // push dt -> query in the stack
            // query is the first template that started pprint (see function above)
            // and at that time ?in was not bound
            // use case:
            // template {"subClassOf(" ?in " " ?y ")"} where {?in rdfs:subClassOf ?y}
            start = true;
            stack.push(dt1, query);
            //}
        }

        if (isDebug) {
            System.out.println("pprint: " + level() + " " + exp + " " + dt1);
        }

        Graph g = graph;
        QueryProcess exec = this.exec;
        Mapping m = getMapping(dt1, dt2);

        int count = 0, n = 0;

        IDatatype type = null;
        if (isOptimize) {
            type = graph.getValue(RDF.TYPE, dt1);
        }

        if (isExplain && ! pp.equals(TURTLE)){
            PPrinter p = PPrinter.create(g, TURTLE);
            System.out.println();
            System.out.println("PP focus node: (" + level() + ") type: " + type + " nbt: " + getTemplates(temp, type).size());
            System.out.println(p.pprint(dt1).getLabel());
            System.out.println();
       }
        
        for (Query qq : getTemplates(temp, type)) {

            if (isDetail) {
                qq.setDebug(true);
            }

            if (!qq.isFail() && !stack.contains(dt1, qq)) {

                nbt++;

                if (allTemplates) {
                    count++;
                }
                stack.push(dt1, qq);
                if (stack.size() > max) {
                    max = stack.size();
                }

                if (stat) {
                    incr(qq);
                }

                n++;
                
                if (isExplain && ! pp.equals(TURTLE)){
                    System.out.println("PP try: " + level() + "\n" + qq.getAST());
                    System.out.println();
                    qq.setDebug(true);
                }
                
                Mappings map = exec.query(qq, m);

                stack.visit(dt1);

                if (!allTemplates) {
                    // if execute all templates, keep them in the stack 
                    // to prevent loop same template on same focus node
                    stack.pop();
                }

                IDatatype res = getResult(map);

                if (res != null) {

                    if (stat) {
                        succ(qq);
                    }

                    if (isDebug) {
                        trace(qq, res);
                    }


                    if (allTemplates) {
                        result.add(res);
                    } else {
                        if (start) {
                            stack.pop();
                        }
//                        if (pp.equals(SPIN)) {
//                            PPrinter p = PPrinter.create(g, TURTLE);
//                            System.out.println(n + " " + p.pprint(dt1).getLabel());
//                        }
                        return res;
                    }
                }
            }
        }

        if (allTemplates) {
            // gather results of several templates

            for (int i = 0; i < count; i++) {
                // pop the templates that have been executed
                stack.pop();
            }

            if (result.size() > 0) {

                if (start) {
                    stack.pop();
                }

                IDatatype res = result(result, separator(sep));
                return res;
            }
        }

        if (start) {
            stack.pop();
        }

        // no template match dt

        if (temp != null) {
            // named template fail
            return EMPTY;
        }

        // default: display dt as is
        return display(dt1, q);
    }

    Mapping getMapping(IDatatype dt1, IDatatype dt2) {
        Node qn1 = NodeImpl.createVariable(IN);
        Node n1 = getNode(dt1);
        if (dt2 == null) {
            return Mapping.create(qn1, n1);
        } else {
            Node qn2 = NodeImpl.createVariable(IN2);
            Node n2 = getNode(dt2);
            return Mapping.create(qn1, n1, qn2, n2);
        }
    }

    Node getNode(IDatatype dt) {
        Node n = graph.getNode(dt, false, false);
        if (n == null) {
            // use case: kg:pprint("header")
            n = fake.getNode(dt, true, true);
        }
        return n;
    }

    public IDatatype getResult(Mappings map) {
        IDatatype dt;
        if (isAllResult) {
            // group_concat(?out)
            dt = getAllResult(map);
        } else {
            // ?out
            dt = getSimpleResult(map);
        }
        return dt;
    }

    public IDatatype getAllResult(Mappings map) {
        Node node = map.getTemplateResult();
        if (node == null) {
            return null;
        }
        return datatype(node);
    }

    public IDatatype getSimpleResult(Mappings map) {
        Node node = map.getNode(OUT);
        if (node == null) {
            return null;
        }
        return datatype(node);
    }

    String separator(String sep) {
        if (sep == null) {
            return sepTemplate;
        }
        return sep;
    }

    String separator(Query q) {
        if (q.hasPragma(Pragma.SEPARATOR)) {
            return q.getStringPragma(Pragma.SEPARATOR);
        }
        return sepResult;
    }

    IDatatype datatype(Node n) {
        return (IDatatype) n.getValue();
    }

    private List<Query> getTemplates(String temp, IDatatype dt) {
        if (temp == null) {
            if (isOptimize) {
                return qe.getTemplates(dt);
            } else {
                return qe.getTemplates();
            }
        }
        return getTemplate(temp);
    }

    private List<Query> getTemplate(String temp) {
        Query q = qe.getTemplate(temp);
        ArrayList<Query> l = new ArrayList<Query>(1);
        if (q != null) {
            l.add(q);
        }
        return l;
    }

    /**
     * Concat results of several templates executed on same focus node
     * kg:pprintAll(?x)
     */
    IDatatype result(List<IDatatype> result, String sep) {
        StringBuilder sb = new StringBuilder();

        for (IDatatype d : result) {
            StringBuilder b = d.getStringBuilder();

            if (b != null) {
                if (b.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(sep);
                    }
                    sb.append(b);
                }
            } else if (d.getLabel().length() > 0) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(d.getLabel());
            }
        }

        IDatatype res = DatatypeMap.newStringBuilder(sb);
        return res;
    }

    IDatatype display(IDatatype dt, Query q) {
        if (isTurtle || (q != null && q.hasPragma(Pragma.TURTLE))) {
            return turtle(dt);
        } else if (isHide) {
            return EMPTY;
        } else {
            // the label of dt as in SPARQL concat()
            return dt;
        }
    }

    /**
     * pprint a URI, Literal, blank node in its Turtle syntax
     */
    public IDatatype turtle(IDatatype dt) {

        if (dt.isURI()) {
            String qname = nsm.toPrefix(dt.getLabel(), true);
            if (dt.getLabel().equals(qname)) {
                // no namespace, return <uri>
                dt = DatatypeMap.newStringBuilder(dt.toString());
            } else {
                // return qname
                dt = DatatypeMap.newStringBuilder(qname);
            }
        } else if (dt.isLiteral()) {
            if (dt.getCode() == IDatatype.INTEGER || dt.getCode() == IDatatype.BOOLEAN) {
                // print as is
            } else {
                // add quotes around string, add lang tag if any
                dt = DatatypeMap.newStringBuilder(dt.toString());
            }
        }
        return dt;
    }

    String getPP(IDatatype dt) {
        IDatatype type = graph.getValue(RDF.TYPE, dt);
        if (type != null) {
            String p = getPP(type.getLabel());
            if (p != null) {
                return p;
            }
        }
        return TURTLE;
    }

    public static String getPP(String type) {
        String ns = NSManager.namespace(type);
        return table.get(ns);
    }
    
    public static PPrinterTable getTable(){
        return table;
    }

    /**
     * Load templates from directory (.rq) or from a file (.rul)
     */
    void init() {
        if (pp == null) {
            qe = QueryEngine.create(graph);
        } else {
            setOptimize(table.isOptimize(pp));
            Load ld = Load.create(graph);
            try {
                //ld.loadWE(pp);
                load(ld, pp);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                logger.error("PPrint Load Error: " + pp);
            }

            qe = ld.getQueryEngine();

            if (qe == null) {
                qe = QueryEngine.create(graph);

                if (ld.getRuleEngine() != null) {

                    RuleEngine re = ld.getRuleEngine();
                    qe = QueryEngine.create(graph);
                    for (Rule r : re.getRules()) {
                        Query q = r.getQuery();
                        qe.defQuery(q);
                    }
                }
            }

            for (Query q : qe.getTemplates()) {
                q.setPPrinter(pp, this);
            }
            for (Query q : qe.getNamedTemplates()) {
                q.setPPrinter(pp, this);
            }

            qe.sort();
        }

        //qe.trace();

        if (isDebug) {
            trace();
        }

        if (isCheck()) {
            check();
            // trace();
        }
        //trace();

    }

    void load(Load ld, String pp) throws LoadException {
        if (nsm.inNamespace(pp, NSManager.PPN)) {
            // predefined pprinter: pp:owl pp:spin
            // loaded from Corese resource
            String name = nsm.strip(pp, NSManager.PPN);
            String src = PPLIB + name;

            if (!ld.isRule(src)) {
                src = src + Load.RULE;
            }
            InputStream stream = getClass().getResourceAsStream(src);
            if (stream == null) {
                throw LoadException.create(new IOException(pp));
            }
            // use non synchronized load method because we may be inside a query 
            // with a read lock
            ld.loadRule(new InputStreamReader(stream), src);
        } else {
            ld.loadWE(pp);
        }
    }

    /**
     * *************************************************************
     *
     * Check templates that would never succeed
     *
     **************************************************************
     */
    /**
     * Check if a template edges not exist in graph remove those templates from
     * the list to speed up PRAGMA: does not take RDFS entailments into account
     */
    public void check() {
        for (Query q : qe.getQueries()) {
            boolean b = graph.check(q);
            if (!b) {
                q.setFail(true);
            }
//			if (name(q).equals("specialproperty.rq") ){
//				q.setFail(true);
//			}
        }
        qe.clean();
        if (stat) {
            trace();
        }
    }

    public void trace() {
        System.out.println("PP nb templates: " + qe.getQueries().size());
        for (Query q : qe.getQueries()) {
            if (q.hasPragma(Pragma.FILE)) {
                System.out.println(name(q));
            }
            ASTQuery ast = (ASTQuery) q.getAST();
            System.out.println(ast);
        }
    }

    String name(Query qq) {
        String f = qq.getStringPragma(Pragma.FILE);
        if (f != null) {
            int index = f.lastIndexOf("/");
            if (index != -1) {
                f = f.substring(index + 1);
            }
        }
        return f;
    }

    void trace(Query qq, Node res) {
        System.out.println();
        System.out.println("query:  " + name(qq));
        System.out.println("result: " + res);
    }

    public void nbcall() {
        for (Query q : qe.getQueries()) {
            System.out.println(q.getNumber() + " " + name(q) + " " + tcount.get(q));
        }
    }

    private void succ(Query q) {
        Integer c = tcount.get(q);
        if (c == null) {
            tcount.put(q, 1);
        } else {
            tcount.put(q, c + 1);
        }
    }

    private void incr(Query qq) {
        qq.setNumber(qq.getNumber() + 1);
    }

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean isHide) {
        this.isHide = isHide;
    }

    private boolean isAllResult() {
        return isAllResult;
    }

    public void setAllResult(boolean isAllResult) {
        this.isAllResult = isAllResult;
    }
}
