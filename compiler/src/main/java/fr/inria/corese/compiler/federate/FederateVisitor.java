package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.federate.util.RewriteError;
import fr.inria.corese.compiler.federate.util.RewriteErrorManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTSelector;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_BGP;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_COMPLETE;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_JOIN;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_OPTIONAL;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_MINUS;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_PARTITION;
import static fr.inria.corese.sparql.triple.parser.Metadata.FED_UNDEFINED;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.visitor.ASTParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Prototype for federated query
 *
 * @federate <s1> <s2> 
 * select * where { } 
 * Recursively rewrite every triple t as:
 * service <s1> <s2> { t } Generalized service statement with several URI
 * Returns the union of Mappings  
 * PRAGMA:
 * Property Path evaluated in each of the services but not on the union 
 * (hence PP is not federated)
 * graph ?g { } by default is evaluated as federated onto servers
 * @skip kg:distributeNamed : 
 * named graph as a whole on each server 
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class FederateVisitor implements QueryVisitor, URLParam {


    public static Logger logger = LoggerFactory.getLogger(FederateVisitor.class);
    //static final String UNDEF = "?undef_serv";
    
    public static final String PROXY = "_proxy_";
    // Federation definitions: URL -> list URL
    private static HashMap<String, List<Atom>> federation;
    // draft
    public static boolean TEST_FEDERATE = true;
    // generate partition of connnected bgp
    public static boolean FEDERATE_BGP = true;
    // if we find a complete partition, do not split it in subparts
    public static boolean PARTITION = true;
    // test and use join between left and right bgp of minus/optional
    public static boolean MINUS = true;
    public static boolean OPTIONAL = true;
    // skip undefined arg of union optional minus
    public static boolean UNDEFINED = true;   
    // complete partition with list of all triple alone
    public static boolean COMPLETE_BGP = false;
    // source selection generate bind (exists {t1 . t2} as ?b)
    // for each pair of connected triple
    public static boolean SELECT_JOIN = true;
    public static boolean SELECT_JOIN_PATH = true;
    // take filter into account in source selection
    public static boolean SELECT_FILTER = true;
    // use source selection join to generate connected bgp with join
    public static boolean USE_JOIN = true;
    public static boolean TRACE_FEDERATE = false;
    // specific processing for rdf list and bnode variable
    public static boolean PROCESS_LIST = true;
    public static int    NB_ENDPOINT = 20;
    public static double NB_SUCCESS  = 0.5;
    
    // false: evaluate named graph pattern as a whole on each server 
    // true:  evaluate the triples of the named graph pattern on the merge of 
    // the named graphs of the servers
    boolean distributeNamed = true;
    // same in case of select from where
    boolean distributeDefault = false;
    // service selection for triples
    private boolean select = true;
    // consider filter in source selection
    private boolean selectFilter = SELECT_FILTER;
    // group connected triples with same service into connected service s { BGP }
    boolean group = true;
    // in optional/minus/union: group every triples with same service into one service s { BGP }
    private boolean merge = true;
    // factorize unique service in optional/minus/union
    boolean simplify  = true;
    // Heuristics: Group and restrict federated services on the intersection of their URL list
    private boolean mergeService = false;
    // rewrite filter exist {}
    boolean exist = true;
    // insert a service inside a service
    private boolean bounce = false;
    boolean verbose = false;
    // rewrite service uri {} as values var {uri} . service var {}
    boolean variable = false;
    // generate (count(*) as ?c)
    boolean aggregate = false;
    boolean provenance = false;
    // draft query graph index
    private boolean index = false;
    // generate one service for the whole body  (no recursive rewrite)
    private boolean sparql = false;
    private boolean processList = PROCESS_LIST;
    private boolean traceFederate = TRACE_FEDERATE;
    private boolean testFederate = TEST_FEDERATE;
    // generate partition of connected bgp:
    private boolean federateBGP = FEDERATE_BGP;
    private boolean federateJoin = SELECT_JOIN;
    private boolean federatePartition = PARTITION;
    private boolean federateOptional = OPTIONAL;
    private boolean federateMinus = MINUS;
    private boolean federateComplete = COMPLETE_BGP;
    private boolean federateIndex = false;
    private boolean federateClass = true;
    // in union/optional/minus, skip arg with undefined service
    private boolean federateUndefine = UNDEFINED;
    public static List<String> BLACKLIST;
    public static List<String> BLACKLIST_EXCEPT;
    // predicate such as owl:sameAs that split bgp connectivity
    public static List<String> DEFAULT_SPLIT;
    private List<String> include;
    private List<String> exclude;
    // predicate such as owl:sameAs that split bgp connectivity
    public List<String> split;
    private List<String> indexURLList;
    private int nbEndpoint   = NB_ENDPOINT;
    private double nbSuccess = NB_SUCCESS;

    ASTQuery ast;
    Stack stack;
    // generate and evaluate triple selection query
    private Selector selector;
    // Record triple selection result
    private ASTSelector astSelector;
    QuerySolver exec;
    // Group triple patterns that share a unique service URI into one service URI
    private PrepareBGP groupBGP;
    // Rewrite BGP as service {}
    private RewriteTriple rewriteTriple;
    // Rewrite service (uri) { } as values var { (uri) } service var { }
    RewriteService rs;
    RewriteList rewriteList;
    // Group several services with same URI into one service
    RewriteNamedGraph rewriteNamed;
    private RewriteErrorManager errorManager;
    private Simplify sim;
    List<Atom> empty;
    // Federation URL
    private URLServer url;
    // use case: reuse federated visitor source selection
    private Mappings mappings;
    private Mappings selection;
    private Mappings discovery;
    
    static {
        federation = new HashMap<>();
        DEFAULT_SPLIT = new ArrayList<>();
        // can be removed with @split us:test or with FEDERATE_SPLIT = <<empty string>>
        DEFAULT_SPLIT.add(RDF.OWL_SAME_AS);
        BLACKLIST = new ArrayList<>();    
        BLACKLIST_EXCEPT = new ArrayList<>();    
    }
    
    public FederateVisitor(QuerySolver e){
        stack = new Stack();
        exec = e;
        groupBGP = new PrepareBGP(this);
        rewriteTriple = new RewriteTriple(this);
        rewriteNamed = new RewriteNamedGraph(this);
        rewriteList = new RewriteList(this);
        sim = new Simplify(this);
        empty = new ArrayList<>(0);
        errorManager = new RewriteErrorManager();
        include = new ArrayList<>();
        exclude = new ArrayList<>();
        split = DEFAULT_SPLIT;
    }
    
    /**
     * Query Visitor just before compiling AST
     */
    @Override
    public void visit(ASTQuery ast) {        
        process(ast);
        ast.setFederateVisit(true);
        report(ast);
    }
    
    @Override
    public void visit(fr.inria.corese.kgram.core.Query query) {
        query.setFederate(true);
        ASTQuery ast =  query.getAST();
        ast.getLog().setAST(ast);
        exec.getLog().setAST(ast);
        query.setSelection(getMappings());
        query.setDiscorevy(getDiscovery());
    }
    
    // before solver query exec
    @Override
    public void before(fr.inria.corese.kgram.core.Query q) {
       
    }
    
    @Override
    public void after(Mappings map) {
        if (provenance) {
            Provenance prov = getProvenance(map);
            System.out.println(prov);        
        }
    }
    
    // start function
    void process(ASTQuery ast) {
        this.ast = ast;
        option();
        if (! init()) {
            return;
        }
        getGroupBGP().setDebug(ast.isDebug());
        
        if (isSparql()) {
            // select where { BGP } -> select where { service URLs { BGP } }
            verbose("\nbefore:\n%s", ast.getBody());
            sparql(ast);
        }
        else {       
            if (ast.getContext() != null) {
                // tune service URL with Context
                ast.setServiceList(tune(ast.getContext(), ast.getServiceList()));
            }  
            boolean suc = true;
            if (isSelect()) {
                // triple selection
                suc = sourceSelection(ast);
            }
            verbose("\nbefore:\n%s", ast.getBody());
            
            if (suc) {
                rewrite(ast);
            }
            else {
                logger.info("Source selection fail");
            }
        }
        after(ast);
    }
    
    // return false when two connected triple that are in only one endpoint
    // do not join according to source selection
    boolean sourceSelection(ASTQuery ast) {
        boolean suc = true;
        try {
            if (ast.getContext() != null
                    && ast.getContext().getAST() != null
                    && ast.getContext().getAST().getAstSelector() != null) {
                // use case: gui -> QueryProcess modifier has recorded former ast query in context
                // and there is an AstSelector available
                // copy it for current ast
                ASTSelector sel = ast.getContext().getAST().getAstSelector();
                setAstSelector(sel.copy(ast));
            } else {
                setSelector(new Selector(this, exec, ast)
                        .setMappings(getMappings())
                        .setNbEndpoint(getNbEndpoint())
                        .setNbSuccess(getNbSuccess()));
                suc = getSelector().process();
                setAstSelector(getSelector().getAstSelector());
            }
            ast.setAstSelector(getAstSelector());
        } catch (EngineException ex) {
            logger.error("Selection error");
            logger.error(ex.getMessage());
            suc = false;
        }
        return suc;
    }
    
    void after(ASTQuery ast) {
        error(ast);
        verbose(ast);
    }
    
    void error(ASTQuery ast) {
        if (!getErrorManager().getErrorList().isEmpty()) {
            for (RewriteError err : getErrorManager().getErrorList()) {
                logger.error(err.toString());
            }
        }
        if (ast.hasUndefinedService()) {
            logger.error("Query rewrite fail due to undefined triple");
            ast.setFail(true);
        }
    }
    
    void verbose(ASTQuery ast) {
        if (verbose) {
            System.out.println("\nafter:");
            System.out.println(ast.getMetadata());
            System.out.println(ast.getBody());
            System.out.println();
        }
    }
    
    // possibly process @report: generate variable ?_service_report
    void report(ASTQuery ast) {
        ASTParser walk = new ASTParser(ast).report();
        ast.process(walk);
    }
       
    // Federation definition
    boolean init()  {
        if (ast.hasMetadata(Metadata.FEDERATION)) {
            List<String> list = ast.getMetadata().getValues(Metadata.FEDERATION);
            List<Atom> serviceList;
            boolean define = true;
            
            if (list == null) {
                define = false;
                // source discovery
                // discover relevant endpoints in kg graph index
                Selector sel = new Selector(this, exec, ast)
                        .setNbEndpoint(getNbEndpoint())
                        .setNbSuccess(getNbSuccess());
                try {
                    list = sel.getIndexURIList(getIndexURLList());
                    ast.getCreateContext().setFederateIndex(true);
                    setFederateIndex(true);
                } catch (EngineException ex) {
                    logger.error(ex.getMessage());
                    return false;
                }
            }
            
            if (list.isEmpty() && getInclude().isEmpty()) {
                logger.info("Candidate and include endpoint empty");
                return false;
            }
            else if (list.size() == 1 && define) {
                // refer to federation
                serviceList = getFederation().get(list.get(0));
                setURL(new URLServer(list.get(0)));
                
                if (serviceList == null) {
                    logger.info("Undefined federation: " + list.get(0));
                    //return false;
                    serviceList = new ArrayList<>();
                    serviceList.add(Constant.createResource(list.get(0)));
                }
                
            } else {
                // define federation
                serviceList = new ArrayList<>();
                int start = (define)?1:0;
                
                for (int i = start; i < list.size(); i++) {
                    String uri = list.get(i);
                    if (accept(uri)){
                        serviceList.add(Constant.createResource(uri));
                    }
                }
                
                for (String uri : getInclude()) {
                    if (! list.contains(uri) && accept(uri)) {
                        serviceList.add(Constant.createResource(uri));
                    }
                }
                
                if (define) {
                    defFederation(list.get(0), serviceList);
                }
            }
            ast.setServiceList(serviceList);
            // same as Transformer federate()
            // use case: come here from corese server federate mode
            ast.defService((String)null);
        }

        return true;
    }
    
    boolean accept(String uri) {
        boolean b = !BLACKLIST.contains(uri) && !getExclude().contains(uri);
        if (!b) {
            logger.info("Candidate endpoint is blacklisted: " + uri);
        }
        return b;
    }

    public  static synchronized boolean blacklist(String uri) {
        if (! BLACKLIST.contains(uri) && 
            ! BLACKLIST_EXCEPT.contains(uri)) {
            BLACKLIST.add(uri);
            return true;
        }
        return false;
    } 
    
    public static boolean isBlackListed(String uri) {
        return BLACKLIST.contains(uri);
    }
    
    public static List<String> getBlacklist() {
        return BLACKLIST;
    }
    
    public Provenance getProvenance(Mappings map) {
        Provenance prov = new Provenance(rs.getServiceList(), map);
        map.setProvenance(prov);
        return prov;
    }
    
    
    /**
     * Metadata: 
     * default is true:
     * @skip kg:select kg:group kg:simplify kg:distributeNamed
     * default is false:
     * @type kg:exist kg:verbose
     */
    void option()  {
        logger.info(ast.getMetadata().toString());
        if (ast.hasMetadataValue(Metadata.TYPE, Metadata.VERBOSE)) {
            verbose = true;
        }
        if (skip(Metadata.DISTRIBUTE_NAMED)) {
            distributeNamed = false;
        }
        if (skip(Metadata.SELECT_SOURCE)) {
            setSelect(false);
        }
        if (skip(Metadata.SELECT_FILTER)) {
            setSelectFilter(false);
        }
        if (skip(Metadata.GROUP)) {
            group = false;
        }
        if (skip(Metadata.MERGE)) {
            setMerge(false);
        }
        if (skip(Metadata.SIMPLIFY)) {
            simplify = false;
        }
        if (skip(Metadata.EXIST)) {
            exist = false;
        }
        if (ast.hasMetadata(Metadata.MERGE_SERVICE)) {
            setMergeService(true);
        }
        if (ast.hasMetadata(Metadata.BOUNCE)) {
            bounce = true;
        }
        if (ast.hasMetadata(Metadata.VARIABLE) || ast.hasMetadata(Metadata.PUBLIC)) {
            variable = true;
        }
        if (ast.hasMetadata(Metadata.SERVER)) {
            variable = true;
            aggregate = true;
        }
        if (ast.hasMetadata(Metadata.PROVENANCE)) {
            variable = true;
            provenance = true;
        }
        if (ast.hasMetadata(Metadata.SPARQL)) {
            setSparql(true);
        }
        
        setFederateBGP(getValue(FED_BGP, isFederateBGP()));
        setFederateJoin(getValue(FED_JOIN, isFederateJoin()));
        setFederatePartition(getValue(FED_PARTITION, isFederatePartition()));
        setFederateComplete(getValue(FED_COMPLETE, isFederateComplete()));
        setFederateOptional(getValue(FED_OPTIONAL, isFederateOptional()));
        setFederateMinus(getValue(FED_MINUS, isFederateMinus()));
        setFederateUndefined(getValue(FED_UNDEFINED, isFederateUndefined()));
        
        if (ast.getMetaValue(Metadata.FED_CLASS) !=null) {            
            setFederateClass(ast.getMetaValue(Metadata.FED_CLASS).booleanValue());
        }
        if (ast.getMetaValue(Metadata.FED_SUCCESS) !=null) {
            // success rate for source discovery
            // 0.5 means half of the predicates are required
            // to consider an endpoint
            setNbSuccess(ast.getMetaValue(Metadata.FED_SUCCESS).doubleValue());
        }
        if (ast.getMetaValue(Metadata.FED_LENGTH) !=null) {
            // number of endpoint url considered from source discovery
            setNbEndpoint(ast.getMetaValue(Metadata.FED_LENGTH).intValue());
        }
        if (ast.hasMetadata(Metadata.FED_INCLUDE)) { 
            // add endpoint uri to result of index source discovery
            setInclude(ast.getMetadata().getValues(Metadata.FED_INCLUDE));
            logger.info("Include: " + getInclude());
        }
        if (ast.hasMetadata(Metadata.FED_EXCLUDE)) {            
            // remove endpoint uri from result of index source discovery
            setExclude(ast.getMetadata().getValues(Metadata.FED_EXCLUDE));
        }
        if (ast.hasMetadata(Metadata.INDEX)) {
            // federate query with index for source discovery
            // @index <http://index.org/sparql>
            setIndexURLList(ast.getMetadata().getValues(Metadata.INDEX));
            logger.info("Index URL: "+ getIndexURLList());
        }
        
        if (ast.hasMetadata(Metadata.FED_WHITELIST)) {
             if (ast.hasMetadataValue(Metadata.FED_WHITELIST)) {
                 for (String uri : ast.getMetadata().getValues(Metadata.FED_WHITELIST)) {
                     getBlacklist().remove(uri);
                 }
             }
             else {
                 getBlacklist().clear();
             }
        }
        
        if (ast.hasMetadataValue(Metadata.SPLIT)) {
            // @split owl:sameAs
            setSplit(ast.getMetadata().getValues(Metadata.SPLIT));
            logger.info("Split: "+ getSplit());
        }
        
        option(ast.getDataset());
        
        if (ast.getContext()!=null) {
            option(ast.getContext());
        }
    }
    
    void option(Dataset ds) {
        if (!ds.getIndex().isEmpty()) {
            // federate query with index for source discovery
            // select * from <index:http://index.org/sparql>
            setIndexURLList(ds.getIndex());
            logger.info("Index URL: " + getIndexURLList());
        }
    }
    
    // option from service parameter sv:federateLength=20
    // service parameter prefixed with sv: are available in ast context
    void option(Context ct) {
        IDatatype dt = ct.getFirst(FED_LENGTH);
        if (dt != null) {
            setNbEndpoint(Integer.valueOf(dt.stringValue()));
        }
        dt = ct.getFirst(FED_SUCCESS);
        if (dt != null) {
            setNbSuccess(Double.valueOf(dt.stringValue()));
        }
        dt = ct.get(FED_INCLUDE);
        
        if (dt != null) {
            for (IDatatype uri : dt) {
                if (! getInclude().contains(uri.getLabel())) {
                    getInclude().add(uri.getLabel());
                }
            }
        }
    }
    
    boolean getValue(String meta, boolean b) {
        if (ast.getMetaValue(meta)!=null) {
            return ast.getMetaValue(meta).booleanValue();
        }
        return b;
    }
    
    boolean skip(String name) {
        return ast.hasMetadataValue(Metadata.SKIP, name);
    }
    
    boolean isExist() {
        return exist;
    }
    
    /**
     * Rewrite query body with one service clause with federation URLs
     */
    void sparql(ASTQuery ast) {
        Exp body = ast.getBody();
        List<Atom> list = ast.getServiceList();
        if (ast.getContext() != null) {
            list = tune(ast.getContext(), list);
        }
        Service serv = Service.create(list, body);
        ast.setBody(ast.bgp(serv));
        // TODO: check inherit limit ??? offset ???
        complete(ast);        
        // include external values clause inside body
        prepare(ast);
        variable(ast);
        finish(ast);       
    }
    
    /**
     * Complete URL of SPARQL endpoints of federation with information from context
     * For example: mode=share&mode=debug
     */
    List<Atom> tune(Context c, List<Atom> list) {
        if (isShareable(c)) {
            List<Atom> alist = new ArrayList<>();
            for (Atom at : list) {
                String uri = at.getConstant().getLongName();
                if (c.accept(uri)) {
                    uri = c.tune(uri);
                    System.out.println("Fed tune: " + uri);
                    alist.add(Constant.createResource(uri));
                }
            }
            return alist;
        }
        return list;
    }
    
    boolean isShareable(Context c) {
        return (c.hasValue(MODE) && c.hasValue(SHARE))
                || c.hasValue(ACCEPT) || c.hasValue(REJECT) || c.hasValue(EXPORT);
    }
       
    /**
     * Main rewrite function 
     */
    void rewrite(ASTQuery ast) {
        prepare(ast);
        rewrite(null, ast);
        graph(ast);
        complete(ast);
        variable(ast);
        finish(ast);
    }
    
    void finish(ASTQuery ast) {
        ast.getVisitorList().add(this);
    }
    
    void prepare(ASTQuery ast) {
        if (ast.getValues() != null) {
            ast.where().add(0, ast.getValues());
            ast.setValues(null);
        }
    }
    
    void complete(ASTQuery ast) {
        setLimit(ast);
    }
    
    void graph(ASTQuery ast) {
        new RewriteServiceGraph(this).process(ast);
    }
    
    /**
     * Unique service inherits query limit if any
     */
    void setLimit(ASTQuery ast) {
        if (ast.hasLimit()) {
            Exp body = ast.getBody();
            if (body.size() == 1 && body.get(0).isService()) {
                Service s = body.get(0).getService();
                ASTQuery aa = ast.getSetSubQuery(s);
                if (!aa.hasLimit()) {
                    aa.setLimit(ast.getLimit());
                }
            }
        }
    }
    
    // @variable
    // rewrite service (uri) {} as values ?serv { (uri) } service ?serv {}
    void variable(ASTQuery ast) {
        if (variable) {
            rs = new RewriteService(this);
            rs.process(ast);
            if (aggregate) {
                aggregate(ast, rs.getVarList());
            }
        }
    }
    
    /**
     * select ?serv_1  ?serv_n (count(*) as ?count)
     * where {}
     * group by ?serv_1  ?serv_n.
     */
    void aggregate(ASTQuery ast, List<Variable> valList) {
        ast.setGroup(valList);
        ast.cleanSelect();
        ast.setSelect(valList);
        Term fun = Term.function(Processor.COUNT);
        Variable var = Variable.create("?count");
        ast.defSelect(var, fun);
    }
    
    /**
     * ast is global or subquery
     * name is embedding named graph if any
     */
    void rewrite(Atom name, ASTQuery ast) {       
        for (Expression exp : ast.getModifierExpressions()) {
            rewriteFilter(name, exp);
        }       
        rewrite(name, ast.getBody());
        
    }
    
    /**
     * Core rewrite function
     * Recursively rewrite triple t as: service <s1> <s2> { t } Add
     * filters that are bound by the triple (except some exists {} which must stay
     * local)
     */
    Exp rewrite(Atom namedGraph, Exp body) {
        return rewrite(namedGraph, body, body);
    }
    
    Exp rewrite(Atom namedGraph, Exp main, Exp body) {
        ArrayList<Exp> filterList = new ArrayList<>();
        
        if (group && body.isBGP()) {
            // BGP body may be modified
            // rewrite triples into service URI { t1 t2 }
            // possibly several service with same URI where each bgp is connected  
            // these service clauses will not be rewritten afterward
            // triple with several URI not rewritten yet
            // filterList is list of filter/bind that have been copied in service
            // @todo: filter
            boolean suc = rewriteList.process(body);
            if (! suc) {
                // @todo: fail
            }
            // if isFederateBGP(), compute uri2bgp 
            // and do not rewrite anything yet (effective)
            // else rewrite triple as service  (deprecated)
            URI2BGPList uri2bgp = 
                 getGroupBGP().process(namedGraph, main, body, filterList); 
            
            if (isTraceFederate()) {
                trace("body first phase:\n%s", body);
                uri2bgp.trace();
            }     
            
            if (isFederateBGP()) {
                // rewrite connected bgp with several uri as service
                // filter that have been copied in specific bgp service
                // have been removed from body and copied into filterList
                Exp exp = new RewriteBGPList(this, uri2bgp)
                        .process(namedGraph, body, filterList);
                
                if (exp != null) {
                    // rewritten bgp inserted in body
                    // triples that have been rewritten will be removed below
                    if (exp.isUnion()) {
                        body.add(exp);
                    }
                    else {
                        body.addAll(exp);
                    }
                }
            } 
        }
        
        ArrayList<Exp> expandList = new ArrayList<> ();
        
        for (int i = 0; i < body.size(); i++) {
            // rewrite remaining triples into service with several URI
            Exp exp = body.get(i);
            if (exp.isQuery()) {
                // TODO: graph name ??
                rewrite(namedGraph, exp.getAST());
            } else if (exp.isService() || exp.isValues()) {
                // keep it
            } else if (exp.isFilter() || exp.isBind()) {
                // rewrite exists { }
                if (! filterList.contains(exp)){
                    // not already processed
                    // rewrite filter exists BGP and bind (exists BGP as var) as service
                    rewriteFilter(namedGraph, exp.getFilter());
                }
            } else if (exp.isTriple()) {
                if (isFederateBGP()) {
                    // triple already processed by RewriteBGPList
                    // remove it
                    body.getBody().remove(exp);
                    i--;
                } else {
                    // remaining triple with several services
                    // triple t -> service (<Si>) { t }
                    // copy relevant filters in service
                    Exp res = getRewriteTriple()
                            .rewriteTripleWithSeveralURI(namedGraph, exp.getTriple(), body, filterList);
                    body.set(i, res);
                }
            } else if (exp.isGraph()) {
                Exp res = rewrite(exp.getNamedGraph());
                if (distributeNamed) {
                    expandList.add(res);
                } 
                body.set(i, res);
            }  
            else if (exp.isBinaryExp()) {
                // recursively rewrite arguments
                exp = rewrite(namedGraph, exp);
                if (simplify) {
                    exp = getSimplify().simplify(exp);
                } 
                i = insert(body, exp, i);
                
            } else {
                // BGP
                rewrite(namedGraph, body, exp);
            }
        }              
        
        // remove filters that have been copied into services
        for (Exp filter : filterList) {
            body.getBody().remove(filter);
        }
                
        if (!expandList.isEmpty()) {
            // named graph expansion
            rewriteNamed.expand(body, expandList);
        }
        
        if (body.isBGP()) {
            // merge different service and graph with same url
            getSimplify().process(body);
            // move bind in appropriate service
            bind(body);
            // move filter in appropriate service
            moveFilter(body);
            sort(body);           

            if (isMergeService()) {
                // draft not default behavior
                body = new SimplifyService().simplify(body);
                sort(body);           
            }           
        }
                
        return body;
    }

    void verbose(String mes, Object... obj) {
        if (verbose)  {
            trace(mes, obj);
        }
    }
    
    void trace(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }
    
    int insert(Exp body, Exp exp, int i) {
        if (exp.isBGP()) {
            // optional|minus rewritten
            // in some case:
            //     service s1 {A} service s2 {B} optional/minus {service s2 C}
            // -> exp = {service s1 {A} . service s2 {B optional/minus C}}
            body.set(i, exp.get(0));     // s1
            if (exp.size()==2) {
                body.add(i + 1, exp.get(1)); // s2
                i++;
            }
        } else {
            body.set(i, exp);
        }
        return i;
    }
    
   /**
     * Move bind (exp as var) into appropriate service uri { } if any
     */
    void bind(Exp body) {
        ArrayList<Exp> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isBind() && ! exp.getFilter().isTermExistRec()) {
                boolean b = getGroupBGP().move(exp, body);
                if (b) {
                    list.add(exp);
                }
            }
        }
        for (Exp exp : list) {
            body.getBody().remove(exp);
        }
    }
    
    
    /**
     * Move filter into appropriate service 
     */
    boolean moveFilter(Exp body) {
        return getGroupBGP().moveFilter(body);
    }
    
    void filter(Exp body, Exp bgp) {
        filter(body, bgp, new ArrayList<>());
    }

     // copy relevant filter from body into bgp
    void filter(Exp body, Exp bgp, List<Exp> list) {
        List<Variable> varList = bgp.getInscopeVariables();
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (exp.getFilter().isTermExistRec()) {
                    // skip
                }
                else if (exp.getFilter().isBound(varList) && 
                        !bgp.getBody().contains(exp)) {
                    bgp.add(exp);
                    
                    if (!list.contains(exp)) {
                        list.add(exp);
                    }
                }               
            }
        }
    }
    
    void sort(Exp exp) {
        new Sorter(this).process(exp);
    }
    
    
     ASTQuery getAST() {
         return ast;
     }
       
   
    // exp = graph name { bgp }
    Exp rewrite(Source exp) {
        if (isFederateBGP()) {
            // 1) bgp partition { e_i } computed without named graph
            // 2) generate service S { graph name { e_i } }
            Exp res = rewrite(exp.getSource(), exp.getBodyExp());
            return res;
        }
        /**
         * former case:
         * graph ?g EXP when from named gi is provided: rewrite every triple t
         * in EXP as UNION(i) graph gi t otherwise graph ?g EXP is left as is
         * and is evaluated as is on each endpoint. graph URI EXP is rewritten
         * as graph URI t for all t in EXP
         */
        else if (distributeNamed) {
            return rewriteNamed.rewriteNamed(ast, exp);
        } else {
            return rewriteNamed.simpleNamed(ast, exp);
        }
    }
    
    // no endpoint URL for triple t
    void error(Triple t, String mes) {
        logger.error(mes);
        error(t);
    }
    
    void error(Triple t) {
        logger.error("Undefined triple: " + t);
        //ast.setFail(true);
    }
    
    List<Atom> getAtomList(List<String> list) {
        ArrayList<Atom> alist = new ArrayList<>();
        for (String str : list) {
            alist.add(Constant.create(str));
        }
        return alist;
    }
         
    List<Atom> getServiceList(Triple t) {
        if (t.isPath()){
            return getServiceListPath(t);
        }
        return getServiceListTriple(t);
    }
    
    List<Atom> getServiceListTriple(Triple t) {     
        if (isSelect()) {
            List<Atom> list = getPredicateService(t); 
            if (list != null && ! list.isEmpty()) {
                return list;
            }
        }
        return getDefaultServiceList();
    }
    
    List<Atom> getPredicateService(Triple t) {
        List<Atom> list = getAstSelector().getPredicateService(t);
        if (list == null) {
            if (t.getPredicate().isVariable()) {
                return ast.getServiceList();
            }
        }
        return list;
    }

    List<Atom> getServiceListPath(Triple t) {
        List<Atom> serviceList = getAstSelector().getPredicateService(t);
        if (serviceList==null) {
            // path t was not tested because it has no constant
            return getServiceListPathPredicate(t);
        }
        if (serviceList.isEmpty()) {
            return getDefaultServiceList();
        }
        return serviceList;
    }
    
    List<Atom> getServiceListPathPredicate(Triple t) {
        List<Atom> serviceList = new ArrayList<>();
        
        for (Constant p : t.getRegex().getPredicateList()) {
            for (Atom serv : getServiceListBasic(p)) {
                add(serviceList, serv);
            }
        }
        if (serviceList.isEmpty()) {
            return getDefaultServiceList();
        }
        return serviceList;
    }
    
    void add(List<Atom> list, Atom at) {
        if (! list.contains(at)) {
            list.add(at);
        }
    }
    
    List<Atom> getServiceListBasic(Constant p) {          
        if (isSelect()) {
            List<Atom> list = getAstSelector().getPredicateService(p); //getSelector().getPredicateService(p);
            if (list == null) {
                return new ArrayList<>(0);
            }
            return list;
        }
        return getDefaultServiceList();
    }
       
    List<Atom> getServiceList(Constant p) {          
        if (isSelect()) {
            List<Atom> list = getAstSelector().getPredicateService(p); 
            if (list != null && ! list.isEmpty()) {
                return list;
            }
        }
        return getDefaultServiceList();
    }
    
    
    // when there is no service for a triple
    List<Atom> getDefaultServiceList() {
        if (isSelect()) {
            return undefinedService();
        }
        return ast.getServiceList();
    }
    
    List<Atom> undefinedService() {
        ArrayList<Atom> list = new ArrayList<>();
        //list.add(Variable.create(UNDEF));
        return list;      
    }
    
    Service service(List<Atom> list, Exp exp) {
        return Service.create(list, exp, false);
    }
 
    // accept for create join test
    boolean createJoinTest(Triple t) {
        if (t.isPath()) {
            return SELECT_JOIN_PATH;            
        }
        return true;
    }
       
    boolean acceptWithoutJoinTest(Triple t) {
        if (t.isPath() && !SELECT_JOIN_PATH) {
            // there is no join test for path: accept it
           return true;            
        }
        return false;
    }
    
    boolean rewriteFilter(Atom name, Expression exp) {
        boolean exist = false;
        if (exp.isTerm()) {
            if (exp.getTerm().isTermExist()) {
                exist = true;
                rewriteExist(name, exp);
            } else {
                for (Expression arg : exp.getArgs()) {
                    if (rewriteFilter(name, arg)) {
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }
    
     /*
     * Rewrite filter exists { t }
     * as:
     * filter exists { service <Si> { t } }
     * PRAGMA: it returns all Mappings whereas in this case
     * it could return only one. However, in the general case: 
     * exists { t1 t2 } it must return all Mappings.
     */
    void rewriteExist(Atom name, Expression exp) {
        Exp body = exp.getTerm().getExist().get(0);
        rewrite(name, body);
        getSimplify().simplifyFilterExist(body);
    }
    
    

    /**
     * Find filters bound by t in body, except exists {} Add them to bgp
     */
    void filter(Exp body, Triple t, Exp bgp, List<Exp> list) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
                if (! isRecExist(exp)) {
                    Expression f = exp.getFilter();
                    if (t.bind(f) && ! bgp.getBody().contains(exp)) {
                        bgp.add(exp);
                        if (! list.contains(exp)) {
                            list.add(exp);
                        }
                    }
                }
            }
        }
    }
    
    boolean isRecExist(Exp f) {
        return f.getFilter().isTermExistRec();
    }
    
    boolean isExist(Exp f) {
        return f.getFilter().isTermExist();
    }

    boolean isNotExist(Exp f) {
        return f.getFilter().isNotTermExist() ;
    }
  
    public boolean isBounce() {
        return bounce;
    }
       
    public static void defineFederation(String name, List<String> list) {
        List<Atom> serviceList = new ArrayList<>();
        for (String url : list) {
            serviceList.add(Constant.createResource(url));
        }
        defFederation(name, serviceList);
    }
    
    public static void declareFederation(String name, List<IDatatype> list) {
        List<Atom> serviceList = new ArrayList<>();
        for (IDatatype url : list) {
            serviceList.add(Constant.create(url));
        }
        defFederation(name, serviceList);
    }
    
    public static void defFederation(String name, List<Atom> list) {
        getFederation().put(name, list);
    }
    
    /**
     * @return the federation
     */
    public static HashMap<String, List<Atom>> getFederation() {
        return federation;
    }
    
    public static List<Atom> getFederation(String name) {
        return getFederation().get(name);
    }
    
    public List<Atom> getFederationFilter(String name) {
        List<Atom> list = getFederation().get(name);
        if (list == null) {
            return null;
        }
        return list;
    }

  
    public static void setFederation(HashMap<String, List<Atom>> aFederation) {
        federation = aFederation;
    }

    public boolean isMerge() {
        return merge;
    }

   
    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public boolean isSelectFilter() {
        return selectFilter;
    }

    
    public void setSelectFilter(boolean selectFilter) {
        this.selectFilter = selectFilter;
    }

    
    public boolean isIndex() {
        return index;
    }

   
    public void setIndex(boolean index) {
        this.index = index;
    }

   
    public boolean isSparql() {
        return sparql;
    }

   
    public void setSparql(boolean sparql) {
        this.sparql = sparql;
    }

    
    public URLServer getURL() {
        return url;
    }

    
    public void setURL(URLServer url) {
        this.url = url;
    }

    public boolean isMergeService() {
        return mergeService;
    }

    public void setMergeService(boolean mergeService) {
        this.mergeService = mergeService;
    }

    public ASTSelector getAstSelector() {
        return astSelector;
    }

    public void setAstSelector(ASTSelector astSelector) {
        this.astSelector = astSelector;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
    
    public PrepareBGP getGroupBGP() {
        return groupBGP;
    }

    public void setGroupBGP(PrepareBGP rew) {
        this.groupBGP = rew;
    }
    
    RewriteTriple getRewriteTriple() {
         return rewriteTriple;
    }

    public void setRewriteTriple(RewriteTriple rwt) {
        this.rewriteTriple = rwt;
    }

    public boolean isProcessList() {
        return processList;
    }

    public void setProcessList(boolean processList) {
        this.processList = processList;
    }

    public boolean isTestFederate() {
        return testFederate;
    }

    public void setTestFederate(boolean testFederate) {
        this.testFederate = testFederate;
    }

    @Override
    public Mappings getMappings() {
        return mappings;
    }

    public FederateVisitor setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }

    public boolean isTraceFederate() {
        return traceFederate;
    }

    public void setTraceFederate(boolean traceFederate) {
        this.traceFederate = traceFederate;
    }

    public boolean isFederateBGP() {
        return federateBGP;
    }

    public void setFederateBGP(boolean federateBGP) {
        this.federateBGP = federateBGP;
    }

    public boolean isFederateJoin() {
        return federateJoin;
    }

    public void setFederateJoin(boolean federateJoin) {
        this.federateJoin = federateJoin;
    }

    public boolean isFederatePartition() {
        return federatePartition;
    }

    public void setFederatePartition(boolean federatePartition) {
        this.federatePartition = federatePartition;
    }

    public boolean isFederateOptional() {
        return federateOptional;
    }

    public void setFederateOptional(boolean federateOptional) {
        this.federateOptional = federateOptional;
    }

    public boolean isFederateComplete() {
        return federateComplete;
    }

    public void setFederateComplete(boolean federateComplete) {
        this.federateComplete = federateComplete;
    }

    public RewriteErrorManager getErrorManager() {
        return errorManager;
    }

    public void setErrorManager(RewriteErrorManager errorManager) {
        this.errorManager = errorManager;
    }
    
    public Simplify getSimplify() {
        return sim;
    }

    public void setSimplify(Simplify sim) {
        this.sim = sim;
    }

    public List<String> getInclude() {
        return include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public boolean isFederateIndex() {
        return federateIndex;
    }

    public void setFederateIndex(boolean federateIndex) {
        this.federateIndex = federateIndex;
    }

    public int getNbEndpoint() {
        return nbEndpoint;
    }

    public void setNbEndpoint(int nbEndpoint) {
        this.nbEndpoint = nbEndpoint;
    }

    public double getNbSuccess() {
        return nbSuccess;
    }

    public void setNbSuccess(double nbSuccess) {
        this.nbSuccess = nbSuccess;
    }

    public List<String> getIndexURLList() {
        return indexURLList;
    }

    public void setIndexURLList(List<String> indexURLList) {
        this.indexURLList = indexURLList;
    }
    
    public boolean isSplit(Triple t) {
        return getSplit().contains(t.getProperty().getLabel());
    }

    public List<String> getSplit() {
        return split;
    }

    public void setSplit(List<String> list) {
        split = list;
    }

    public boolean isFederateClass() {
        return federateClass;
    }

    public void setFederateClass(boolean federateClass) {
        this.federateClass = federateClass;
    }

    public Mappings getSelection() {
        return selection;
    }

    public void setSelection(Mappings selection) {
        this.selection = selection;
    }

    public Mappings getDiscovery() {
        return discovery;
    }

    public void setDiscovery(Mappings discovery) {
        this.discovery = discovery;
    }

    public boolean isFederateUndefined() {
        return federateUndefine;
    }

    public void setFederateUndefined(boolean federateUndefine) {
        this.federateUndefine = federateUndefine;
    }

    public boolean isFederateMinus() {
        return federateMinus;
    }

    public void setFederateMinus(boolean federateMinus) {
        this.federateMinus = federateMinus;
    }

   
}
