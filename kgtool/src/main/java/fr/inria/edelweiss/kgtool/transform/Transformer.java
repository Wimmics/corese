package fr.inria.edelweiss.kgtool.transform;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Extension;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import org.apache.log4j.Logger;

/**
 * SPARQL Template Transformation Engine
 * 
 * Use case: translate SPIN RDF into SPARQL
 * concrete syntax pprint OWL 2 RDF in functional syntax Use a list of templates
 * : template { presentation } where { pattern } Templates are loaded from a
 * directory or from a file in .rul format (same as rules) 
 * st:apply-templates(?x) : execute one template on ?x 
 * st:apply-templates-with(st:owl, ?x) : execute one template on ?x 
 * st:apply-all-templates(?x) : execute all templates on ?x
 * st:call-template(uri, ?x) execute named template
 *
 * Olivier Corby, Wimmics INRIA I3S - 2012
 */
public class Transformer  {
    private static Logger logger = Logger.getLogger(Transformer.class);

    private static final String NULL = "";
    private static final String STL         = NSManager.STL;
    public static final String SQL          = STL + "sql";
    public static final String SPIN         = STL + "spin";
    public static final String TOSPIN       = STL + "tospin";
    public static final String OWL          = STL + "owl";
    public static final String OWLRL        = STL + "owlrl";   
    public static final String TURTLE       = STL + "turtle";
    public static final String RDFXML       = STL + "rdfxml";
    public static final String JSON         = STL + "json";
    public static final String TRIG         = STL + "trig";
    public static final String TABLE        = STL + "table";
    public static final String HTML         = STL + "html";
    public static final String SPARQL       = STL + "sparql";
    public static final String RDFRESULT    = STL + "result";
    public static final String NAVLAB       = STL + "navlab";
    public static final String RDFTYPECHECK = STL + "rdftypecheck";
    public static final String SPINTYPECHECK= STL + "spintypecheck";
    public static final String STL_PROFILE  = STL + "profile";
    public static final String STL_START    = STL + "start";
    public static final String STL_TRACE    = STL + "trace";
    public static final String STL_DEFAULT  = Processor.STL_DEFAULT;   
    //private static final String STL_TURTLE  = STL + "turtle";   
    public static final String STL_IMPORT   = STL + "import";   
    public static final String STL_PROCESS  = Processor.STL_PROCESS;   
    public static final String STL_AGGREGATE  = Processor.STL_AGGREGATE;   
    public static final String STL_TRANSFORM = Context.STL_TRANSFORM;
    // default
    public static final String PPRINTER = TURTLE;
    private static final String OUT = ASTQuery.OUT;
    public static final String IN = ASTQuery.IN;
    private static final String IN2 = ASTQuery.IN2;
    private static String NL = System.getProperty("line.separator");
    private static boolean isOptimizeDefault = false;
    private static boolean isExplainDefault = false;

    private TemplateVisitor visitor;
    Graph graph, fake;
    QueryEngine qe;
    Query query;
    NSManager nsm;
    QueryProcess exec;
    Processor proc;
    private Dataset ds;
    Stack stack;
    static Table table;
    String pp = PPRINTER;
    // separator of results of several templates st:apply-all-templates()
    String sepTemplate = NL;
    // separator of several results of one template
    String sepResult = " ";
    boolean isDebug = false;
    private boolean isTrace = false;
    private boolean isDetail = false;
    private IDatatype EMPTY;
    boolean isTurtle = false;
    int nbt = 0, max = 0, levelMax = Integer.MAX_VALUE, level = 0;
   
    String start = STL_START;
    HashMap<Query, Integer> tcount;
    HashMap<String, String> loaded, imported;
    private Context context;
    private boolean isHide = false;
    public boolean stat = !true;
    private boolean isAllResult = true;
    private boolean isCheck = false;
    // index and run templates according to focus node type
    // no subsumption (exact match on type)
    private boolean isOptimize = isOptimizeDefault;
    
    // st:process() of template variable, may be overloaded
    private int process   = ExprType.TURTLE;
    private int aggregate = ExprType.STL_GROUPCONCAT;
    private int defAggregate = ExprType.STL_GROUPCONCAT;
    // st:default() process of template variable, may be overloaded
    // used when all templates fail
    // default is: return RDF term as is (effect is like xsd:string)
    private int defaut  = ExprType.TURTLE;

    static {
        table = new Table();
    }
    private boolean isExplain = isExplainDefault();
    private boolean hasDefault = false;
    private Expr processExp;

  

    Transformer(Graph g, String p) {
        this(QueryProcess.create(g, true), p);
    }
    
    Transformer(Producer prod, String p) {
        this(QueryProcess.create(prod), p);
    }

    Transformer(QueryProcess qp, String p) {
        context = new Context();
        setTransformation(p);
        fake = Graph.create();
        set(qp);
        nsm = NSManager.create();
        init();
        stack = new Stack(true);
        EMPTY = DatatypeMap.createLiteral(NULL);
        tcount = new HashMap<Query, Integer>();
        proc = Processor.create();
        loaded = new HashMap();
        imported = new HashMap();
    }
    
    
    public static Transformer create(Graph g) {
        return new Transformer(g, null);
    }

    public static Transformer create(Graph g, String p) {
        return new Transformer(g, p);
    }

    public static Transformer create(QueryProcess qp, String p) {
        return new Transformer(qp, p);
    }
    
      public static Transformer create(Producer prod, String p) {
        return new Transformer(prod, p);
    }
    
    public static Transformer create(String p) {
        Graph g = Graph.create();
        return new Transformer(g, p);
    }
    
   /**
     * Create Transformer for named graph
     * system named graph, 
     * std named grapĥ: use Dataset from name 
     * loaded graph
     */
    public static Transformer create(Graph g, String trans, String name) throws LoadException {
        return create(g, trans, name, true);
    }
    
    public static Transformer create(Graph g, String trans, String name, boolean with) throws LoadException {
        Dataset ds = null;
        Graph gg = g.getNamedGraph(name);
        if (gg == null) {
            Node n = g.getGraphNode(name);
            if (n == null) {
                gg = Graph.create();
                Load load = Load.create(gg);
                //load.load(name, Load.TURTLE_FORMAT);
                load.parse(name, Load.TURTLE_FORMAT);                
            } 
            else {
                gg = g;
                if (with){
                    ds = Dataset.create();
                    ds.addFrom(name);
                    ds.addNamed(name);
                }
                else {
                    ds = g.getDataset();
                    ds.remFrom(name);
                    ds.remNamed(name);
                }
            }
        }

        Transformer t = Transformer.create(gg);
        t.setDataset(ds);
        t.setTemplates(trans);
        return t;
    }
    
    
     public String transform(){
        IDatatype dt = process();
        if (dt == null){
            return null;
        }
        return dt.getLabel();
    }
     
     public String stransform(){
         String s = transform();
         if (s == null){
             return "";
         }
         return s;
     }
     
    /**
     * URI of the RDF graph to transform
     */
    public String transform(String uri) throws LoadException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(uri);
        set(g);
        return transform();
    }
 
    public void transform(InputStream in, OutputStream out) throws LoadException, IOException{
        transform(in, out, Load.TURTLE_FORMAT);
    }
    
    public void transform(InputStream in, OutputStream out, int format) throws LoadException, IOException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(in, format);
        set(g);
        String str = transform();
        if (str != null){
            out.write(str.getBytes("UTF-8"));
        }
    }
 
     
    public void write(String name) throws IOException {
        FileWriter fw = new FileWriter(name);
        String str = toString();
        fw.write(str);
        fw.flush();
        fw.close();
    }
     

 
    public void definePrefix(String p, String ns){
        nsm.definePrefix(p, ns);
    }
    
    public void setNSM(NSManager n) {
        nsm = n;
    }

    public NSManager getNSM() {
       return nsm;
    }
    
    public QueryEngine getQueryEngine(){
        return qe;
    }
    
    
    /** _________________________________________________________________ **/
    
    void set(QueryProcess qp) {
        graph = qp.getGraph();
        exec = qp;
        tune(exec);
    }
    
    void set(Graph g){
        set(QueryProcess.create(g, true));
    }
    
    
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
    
    
    public void setTemplates(String p) {
        setTransformation(p);
        init();
    }
    
    void setTransformation(String p){
        pp = p;
        setStarter(p);
    }
    
    /**
     * p = http://ns.inria.fr/name#core
     * fragment #core means start transformation with st:core
     * otherwise st:start
     */
    void setStarter(String uri){   
        String name = getName(uri);
        if (name != null){
             setStart(STL + name);
        }
//        if (uri != null && uri.contains("#")){
//            String name = uri.substring(1 + uri.indexOf("#"));
//            setStart(STL + name);
//        }  
    }
    
    /**
     * uri#name
     * @return name
     */    
    static String getName(String uri){
        if (uri != null && uri.contains("#")){
            return uri.substring(1 + uri.indexOf("#"));            
        } 
        return null;
    }
    
    public static String getStartName(String uri){
        String name = getName(uri);
        if (name == null){
            return null;
        }
        return STL + name; 
    }
    
    /**
     * uri#name
     * @return uri
     */
    public static String getURI(String uri) {
        if (uri != null && uri.contains("#")) {
            return uri.substring(0, uri.indexOf("#"));
        }
        return uri;
    }
    
    private void tune(QueryProcess exec) {
        // do not use Thread in Property Path
        // compute all path nodes and put them in a list
        // it is faster
        exec.setListPath(true);
        Producer prod = exec.getProducer();
        if (prod instanceof ProducerImpl) {
            // return value as is for st:apply-templates()
            // no need to create a graph node in Producer
            ProducerImpl pi = (ProducerImpl) prod;
            pi.setSelfValue(true);
        }
    }

    
    /**
     * 
     * @deprecated
     */
    public static void define(String type, String pp) {
        table.put(type, pp);
    }
    
     public static void define(String ns, boolean isOptimize) {
        table.setOptimize(ns, isOptimize);
    }

   
    
   

    public void setDebug(boolean b) {
        isDebug = b;
    }
    
    void setLevelMax(int n){
        levelMax = n;
    }
    
    public void setProcess(int type){
        process = type;
    }
    
    public void setDefault(int type){
        defaut = type;
    }

    public void setTurtle(boolean b) {
        isTurtle = b;
    }

    // when several templates st:apply-all-templates()
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
        return transform();
    }

    public StringBuilder toStringBuilder() {
        IDatatype dt = process();
        return dt.getStringBuilder();
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
    
    public int getProcess(){
        return process;
    }
    
     public int getAggregate(){
        return aggregate;
    }
    
    public Expr getProcessExp(){
        return processExp;
    }
    
    void setProcessExp(Expr e){
         processExp = e;
    }

    /**
     * Transform the whole graph (no focus node) 
     * Apply template st:start, if any
     * Otherwise, apply the first template that matches without
     * bindings.
     */
   
    
    public IDatatype process() {
        return process(null, false, null);
    }

    public IDatatype process(String temp) {
        return process(temp, false, null);
    }
    
    public IDatatype process(String temp, boolean all, String sep) {
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
            
            if (! nsm.isUserDefine()){
                // PPrinter NSM is empty : borrow template NSM
                setNSM(((ASTQuery) qq.getAST()).getNSM());
            }
            
            if (isDebug) {
                qq.setDebug(true);
            }
            //qq.setPPrinter(pp, this);
            // remember start with qq for function pprint below
            query = qq;
            if (query.getName() != null){
                context.setURI(STL_START, qq.getName());
            }
            else {
                context.set(STL_START, (String)null);
            }
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
    
    public int getLevel(){
        return level;
    }
    
    public void setLevel(int n){
         level = n;
    }
    
    public boolean isStart(){        
        return query != null && query.getName()!=null && query.getName().equals(STL_START);
    }
    
    public IDatatype process(Node node) {
        return process((IDatatype) node.getValue());
    }

    public IDatatype process(IDatatype dt) {
        return process(null, dt, null, null, false, null, null, null);
    }
    
    public IDatatype process(IDatatype[] a){
        return process(a, (a.length>0)?a[0]:null, null, null, false, null, null, null);
    }


    public IDatatype template(String temp, IDatatype dt) {
        return process(null, dt, null, temp, false, null, null, null);
    }

    /**
     * exp: the fun call, eg st:apply-templates(?x) 
     * dt1: focus node 
     * dt2: arg
     * args: list of args in case of st:call-template
     * temp: name of a template (may be null) 
     * allTemplates: execute all templates on focus and concat results 
     * sep: separator in case of allTemplates
     * use case: template {  ?w  } where {  ?w  } 
     * Search a template that matches ?w By convention, ?w is bound to ?in, all templates use
     * variable ?in as focus node in where clause and ?out as output node 
     * Execute the first template that matches ?w (all templates if allTemplates = true) 
     * Templates are sorted more "specific" first  using a 
     * pragma {st:template st:priority n } 
     * A template is applied only once on one node,
     * hence we store in a stack : node -> template 
     * context of evaluation: it is an extension function of a SPARQL query
     * select (st:apply-templates(?x) as ?px) (concat (?px ...) as ?out) where {}.
     */
    public IDatatype process(IDatatype[] args, IDatatype dt1, IDatatype dt2, String temp,
            boolean allTemplates, String sep, Expr exp, Query q) {      
        if (dt1 == null) {
            return EMPTY;
        }
        
        if (level() >= levelMax){
            return display(dt1, q);
        }

        ArrayList<IDatatype> result = null;
        if (allTemplates) {
            result = new ArrayList<IDatatype>();
        }
        boolean start = false;

        if (query != null && stack.size() == 0) {
            // just started with query in pprint() above
            // without focus node at that time
            // push dt -> query in the stack
            // query is the first template that started pprint (see function above)
            // and at that time ?in was not bound
            // use case:
            // template {"subClassOf(" ?in " " ?y ")"} where {?in rdfs:subClassOf ?y}
            start = true;
            stack.push(dt1, query);
       }

        if (isDebug || isTrace) {
            System.out.println("pprint: " + level() + " " + exp + " " + dt1 + " " + dt2);
        }

        Graph g = graph;
        QueryProcess exec = this.exec;
        
        int count = 0, n = 0;

        IDatatype type = null;
        if (isOptimize) {
            type = graph.getValue(RDF.TYPE, dt1);
        }
        
        List<Query> templateList = getTemplates(temp, type);
        Query tq = null;
        if (temp != null && templateList.size() == 1){
            // named template may have specific arguments
            tq = templateList.get(0);
        }
        Mapping m = getMapping(args, dt1, dt2, tq);

        for (Query qq : templateList) {
            
            Mapping bm = m;
            
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
                if (qq != tq && qq.getArgList() != null){
                    // std template has arg list: create appropriate Mapping
                    // TODO: we may want to check that card(arg) = card(param)
                    bm = getMapping(args, dt1, dt2, qq);
                }
                
                Mappings map = exec.query(qq, bm);
                stack.visit(dt1);

                if (!allTemplates) {
                    // if execute all templates, keep them in the stack 
                    // to prevent loop same template on same focus node
                    stack.pop();
                }

                IDatatype res = getResult(map);

                if (res != null) {
                    if (isTrace){
                        System.out.println(qq.getAST());
                    }

                    if (allTemplates) {
                        result.add(res);
                    } else {
                        if (start) {
                            stack.pop();
                        }
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
        
        
        // **** no template match dt ****


        if (start) {
            stack.pop();
        }

        if (temp != null) {
            // named template fail
            return EMPTY;
        }
        else if (isHasDefault()) {
            // apply st:default named template
            IDatatype res = process(args, dt1, dt2, STL_DEFAULT, allTemplates, sep, exp, q);
            if (res != EMPTY) {
                return res;
            }
        }

        // use a default display (may be dt1 as is or st:turtle)
        return display(dt1, q);
    }
 
    /**
     * when st:call-template(name, v1, ... vn)
     * ldt = [name, v1, ... vn]
     * otherwise ldt = null
     */
    Mapping getMapping(IDatatype[] ldt, IDatatype dt1, IDatatype dt2, Query q) {
        if (ldt != null && q != null && ! q.getArgList().isEmpty()){
            return getMapping(ldt, q.getArgList());
        }
        
        Node qn1 = NodeImpl.createVariable(getArg(q, 0));
        Node n1 = getNode(dt1);
        if (dt2 == null) {
            return Mapping.create(qn1, n1);
        } else {
            Node qn2 = NodeImpl.createVariable(getArg(q, 1));
            Node n2 = getNode(dt2);
            return Mapping.create(qn1, n1, qn2, n2);
        }
    }
    
    /**
     * ldt = [name, v1, ... vn]
     * first arg is the name of the template, skip it here
     */
    Mapping getMapping(IDatatype[] ldt, List<Node> args){
        int size = Math.min(ldt.length, args.size());
        Node[] qn = new Node[size];
        Node[] tn = new Node[size];
        for (int i = 0; i<size; i++){
            // i+1 because we skip name = ldt[0]
           qn[i] = NodeImpl.createVariable(args.get(i).getLabel());
           tn[i] = getNode(ldt[i]);
        }
        return Mapping.create(qn, tn);
    }
    
    String getArg(Query q, int n){
        if (q == null || q.getArgList().isEmpty()){
            return getArg(n);
        }
        List<Node> list = q.getArgList();
        if (n < list.size()){
           return list.get(n).getLabel();
        }
        else {
           return getArg(n);
        }
 }
    
    String getArg(int n){
        switch (n){
            case 0:  return IN;
            default: return IN2;
        }
    }

    Node getNode(IDatatype dt) {
        Node n = graph.getNode(dt, false, false);
        if (n == null) {
            // use case: st:apply-templates("header")
            n = fake.getNode(dt, true, true);
        }
        return n;
    }

    public IDatatype getResult(Mappings map) {
        Node node = map.getTemplateResult();
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
     * st:apply-all-templates(?x ; separator = sep)
     */
    IDatatype result(List<IDatatype> result, String sep) {
        if (defAggregate == ExprType.AGGAND){
            return booleanResult(result);
        }
        else {
             return stringResult(result, sep);
        }
    }
        
        
     IDatatype stringResult(List<IDatatype> result, String sep) {
         if (result.size() == 1){
             return result.get(0);
         }
       StringBuilder sb = new StringBuilder();
        sep = getTab(sep);
        
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
    
    /**
     * AND aggregate for boolean result
     */
    IDatatype booleanResult(List<IDatatype> result) {
        boolean isError = false, and = true;
        for (IDatatype dt : result) {
            if (dt == null) {
                isError = true;
            } else {
                try {
                    boolean b = dt.isTrue();
                    and &= b;

                } catch (CoreseDatatypeException ex) {
                    isError = true;
                }
            }
        }
        
        if (isError){
            return DatatypeMap.FALSE;
        }
               
        return (and) ? DatatypeMap.TRUE  :DatatypeMap.FALSE;
    }
    
    /**
     * Separator of st:apply-all-templates
     */
    String getTab(String sep){
        if (sep.equals("\n") || sep.equals("\n\n")){
            String str = tab().toString();
            if (sep.equals("\n\n")){
                str = NL + str;
            }
            sep = str;
        }
        return sep;       
    }
    
    public IDatatype tabulate(){
        int n = getLevel();
        return DatatypeMap.newStringBuilder(tab(n));   
    }
    
    public StringBuilder tab(){
          return tab(getLevel());   
    }

     public StringBuilder tab(int n){
        StringBuilder sb = new StringBuilder();
        sb.append(NL);
        for (int i=0; i<2*n; i++){
            sb.append(" ");
        }
        return sb;
    }

    /**
     * Default display when all templates fail
     */
    IDatatype display(IDatatype dt, Query q) {
        //exec.getEvaluator().eval
        int ope = defaut;
        if (q != null) {
           // Expr exp = q.getProfile(STL_DEFAULT);
            Extension ext = q.getExtension();
            if (ext != null){
                Expr exp = ext.get(STL_DEFAULT);
                if (exp != null){
                    ope = exp.getBody().oper(); //getExp(1).oper();               
                }
            } 
        }
        return display(dt, ope);
    }

    /**
     * template [st:turtle]
     * a template may overload the default display
     * @deprecated
     */
    IDatatype profile(IDatatype dt, String profile){
        return display(dt, proc.getOper(profile));
    }
    
   /**
     * Display when all templates fail
     * Default is to return IDatatype as is, 
     * final result will be the string value (when used in a concat())
     * TODO: implement st:default
     */
    IDatatype display(IDatatype dt, int oper){
    
        switch (oper){
            
            case ExprType.TURTLE:
                return turtle(dt);
        }
        
        // implements str()
        return dt;
    }

    /**
     * display RDF Node in its Turtle syntax
     */
    public IDatatype turtle(IDatatype dt) {
        return turtle(dt, false);
    }
    
    /**
     * force = true: if no prefix generate prefix
     */
    public IDatatype turtle(IDatatype dt, boolean force) {

        if (dt.isURI()) {
            String uri = nsm.toPrefixURI(dt.getLabel(), ! force);
            dt = DatatypeMap.newStringBuilder(uri);          
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
    
    /**
     * if prefix exists, return qname, else return URI as is (without <>)
     */
    public IDatatype qnameURI(IDatatype dt) {
        String uri = nsm.toPrefix(dt.getLabel(), true);
        return DatatypeMap.newStringBuilder(uri);                 
    }
    
    /**
     * Display a Literal with its ^^xsd:datatype
     * Use case: OWL 2 functional syntax
     */
     public IDatatype xsdLiteral(IDatatype dt) {
        return DatatypeMap.newStringBuilder(dt.toSparql(true, true));
    }    

     /**
      * @deprecated
      * */
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
    
    public static Table getTable(){
        return table;
    }

    /**
     * Load templates from directory (.rq) or from a file (.rul)
     */
   void init(){
       setOptimize(table.isOptimize(pp));
       Loader load = new Loader(this);
       load.setDataset(ds);
       qe = load.load(pp);
       if (isCheck()) {
            check();
       }
       load.profile(qe, qe);
       setHasDefault(qe.getTemplate(STL_DEFAULT) != null);
       
       Query profile = qe.getTemplate(STL_PROFILE);
       if (profile != null && profile.getExtension() != null){
           Expr exp = profile.getExtension().get(STL_AGGREGATE);
           if (exp != null){
               defAggregate = exp.getBody().oper(); //exp.getExp(1).oper();
           }                  
       }
       qe.sort();
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

    public Graph getGraph() {
        return graph;
    }
    
    /**
     * @return the isTrace
     */
    public boolean isTrace() {
        return isTrace;
    }

    /**
     * @param isTrace the isTrace to set
     */
    public void setTrace(boolean isTrace) {
        this.isTrace = isTrace;
    }
      
    public void load(String uri) {
        if (loaded.containsKey(uri)){
            return;
        }
        else {
            loaded.put(uri, uri);
        }
        Graph g = Graph.create();
        Load load = Load.create(g);
        try {
            //load.load(uri, Load.TURTLE_FORMAT);
            load.parse(uri, Load.TURTLE_FORMAT);             
            g.init();
            exec.add(g);
        } catch (LoadException ex) {
            logger.error(ex);
        }
    }

    /**
     * @return the hasDefault
     */
    public boolean isHasDefault() {
        return hasDefault;
    }

    /**
     * @param hasDefault the hasDefault to set
     */
    public void setHasDefault(boolean hasDefault) {
        this.hasDefault = hasDefault;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return ds;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.ds = dataset;
    }

    public String getTransformation() {
        return pp;
    }

    public IDatatype get(String name) {
        return getContext().get(name);
    }
    
    public void set(String name, IDatatype dt2){
        getContext().set(name, dt2);
    }
    
    public void export(String name, IDatatype dt2){
        getContext().export(name, dt2);
    }
    
    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }
    
    /**
     * Inherit NSManager & context of query that called this Transformer
     * Use case:
     * query = template { st:atw(st:cdn) } where {}
     * query is run by service
     * context contains service URI
     * 
     */
//    public void init(ASTQuery ast){
////        Context c = ast.getContext();
////        if (c != null){
////            setContext(c);
////        }
//        setNSM(ast.getNSM());
//    }
    
    
   /**
   * this new Transformer  inherit information from query and current transformer (if any)
   */
    public void complete(Query q, Transformer ct) {             
        if (ct == null) {
            ASTQuery ast = (ASTQuery) q.getAST();
            Context ctx = ast.getContext();          
            if (ctx != null){
                // inherit query context
                setContext(ctx);
            }
        }
        else {
            setNSM(ct.getNSM());            
            complete(ct.getContext());
            if (ct.getVisitor() != null){
                setVisitor(ct.getVisitor());
            }
        }
        
        
        // query prefix overload ct transformer prefix
        // because query call this new transformer
        ASTQuery ast = (ASTQuery) q.getAST();
        complete(ast.getNSM());
    }
    
    void complete(Context ctx) {
        getContext().complete(ctx);
    }
    
    /**
     * Inherit prefix from Query
     */
    void complete(NSManager nsm){
        if (nsm.isUserDefine()){
             getNSM().complete(nsm);
        }
    }

    /**
     * @return the visitor
     */
    public TemplateVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(TemplateVisitor visitor) {
        this.visitor = visitor;
        if (visitor != null){
            visitor.setGraph(graph);
        }
    }
    
    public void visit(IDatatype name, IDatatype obj, IDatatype arg){
        if (visitor == null){
            initVisit(name, obj, arg);
        }
        visitor.visit(name, obj, arg);        
   }
    
    TemplateVisitor defVisitor(){
        if (visitor == null){
            initVisit();
        }
        return visitor;
    }
    
    public IDatatype vset(IDatatype obj, IDatatype prop, IDatatype arg){
        return defVisitor().set(obj, prop, arg);
    }
    
    public IDatatype vget(IDatatype obj, IDatatype prop){
        return defVisitor().get(obj, prop);
    }
    
    public Collection<IDatatype> visited(){
        if (visitor != null){
           return visitor.visited();
        }
        return new ArrayList<IDatatype>();
    }
    
    public boolean visited(IDatatype dt){
        if (visitor != null){
            return visitor.isVisited(dt);
        }
        return false;
    }
    
    public IDatatype visitedGraph(){
        if (visitor == null){
            return null;
        }
        return visitor.visitedGraph();
    } 
    
    void initVisit(IDatatype name, IDatatype obj, IDatatype arg){
        initVisit();
    }
    
    void initVisit(){
        setVisitor(new DefaultVisitor());
    }
        
    void initVisit2(IDatatype name, IDatatype obj, IDatatype arg){
        if (name.getLabel().equals(STL_START)){
            if (obj.getLabel().equals(STL_TRACE)){
                setVisitor(new DefaultVisitor());
                visitor.visit(name, obj, arg);
            }
        }
    }
       
}
