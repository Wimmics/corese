package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Given query, endpoint URI and size
 * compute paths starting from query solution resources with path size leq length 
 * If a second endpoint URI is given, compute paths from first endpoint to second endpoint
 * 
 * For each path, return the path, the count of target resources and count of distinct target resources
 * With two endpoint URI, return two paths
 * Result is in Turtle format, may be written in a file
 * 
 * This program may also be used as a visitor with @ldpath annotation
 * path lengths are given with @length at the end of endpoint URI: http://dbpedia.org/sparql@2
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LinkedDataPath implements QueryVisitor {
    
    public static final String OPTION       = NSManager.USER;
    public static final String URI          = OPTION + "uri";
    public static final String BNODE        = OPTION + "bnode";
    public static final String LITERAL      = OPTION + "literal";
    public static final String SUBJECT      = OPTION + "subject";
    public static final String DISTINCT     = OPTION + "distinct";
    public static final String AGGREGATE    = OPTION + "aggregate";
    public static final String DATATYPE     = OPTION + "datatype";
    
    public static final String SPLIT        = "@split";
    public static final String SLICE        = "@slice";
    public static final String TIMEOUT      = "@timeout";
    public static final String GRAPH        = "@graph";
    
    Graph graph;
    QueryProcess exec;
    Result result;
    AST astq;
    // path length on first endpoint
    private int length = 1;
    // path length on remote endpoint
    private int endpointLength = 0;
    int maxQuery = Integer.MAX_VALUE;
    private int timeout = 10000;
    boolean trace = !true;

    // find links between local and endpoint
    private List<String> localList;
    // find links between local and endpoint
    private List<String> endpointList;
    List<Constant> serverList;
    ASTQuery ast;
    String file;
    private List<String> reject;
    private List<String> accept;
    private List<String> option;
    List<String> graphList;

    boolean parallel = true;
    boolean detail = false;
    boolean test = false;
    boolean datatype = true;
    boolean aggregate = true;

    public LinkedDataPath() {
        graph = Graph.create();
        astq = new AST(this);
        localList = new ArrayList<>();
        endpointList = new ArrayList<>();
        serverList = new ArrayList<>();
        accept = new ArrayList<>();
        reject = new ArrayList<>();
        option = new ArrayList<>();
    }
    
    public LinkedDataPath(String uri) {
        this();
        getLocalList().add(uri);
    }
    
    public LinkedDataPath(String uri1, String uri2) {
        this();
        getLocalList().add(uri1);
        if (uri2 != null) {
            getEndpointList().add(uri2);
        }
    }

    @Override
    public void visit(ASTQuery ast) {
        try {
            ast.getMetadata().remove(Metadata.VISITOR);
            ldp(ast);
            result.process();            
        } catch (IOException ex) {
            Logger.getLogger(LinkedDataPath.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(LinkedDataPath.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(LinkedDataPath.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(LinkedDataPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void visit(Query query) {
    }

    @Override
    public void visit(Query q, fr.inria.corese.sparql.api.Graph g) {
    }

    Result ldp(ASTQuery ast) throws IOException, EngineException, InterruptedException {
        return process(ast);
    }
    
    public void setFile(String name) {
        file = name;
    }
    
    public Result getResult() {
        return result;
    }
    
    ASTQuery getAST() {
        return ast;
    }
            

    void start(ASTQuery ast) throws EngineException {
        this.ast = ast;
        metadata(ast);
        result = new Result(ast);
        result.setLinkedDataPath(this);
        if (ast.isDebug()) {
            ast.setDebug(false);
            trace = true;
        }
        exec = QueryProcess.create(graph);
    }
    
    void finish(ASTQuery ast) {
         result.setFile(file);
    }
    
    void metadata(ASTQuery ast) {
        if (ast.getMetadata() != null && ast.getMetadata().hasValue(Metadata.LDPATH)) {
            List<String> list = ast.getMetadata().getValues(Metadata.LDPATH);
            ast.getMetadata().remove(Metadata.LDPATH);
            String uri = list.get(0);
            int n = getLength(uri);
            if (n != -1) {
                setPathLength(n);
                uri = clean(uri);
            }
            System.out.println("LDP first: " + getPathLength() + " " + uri);
            getLocalList().add(uri);
            if (list.size() > 1) {
                String uri2 = list.get(1);
                n = getLength(uri2);
                if (n != -1) {
                    setEndpointPathLength(n);
                    uri2 = clean(uri2);
                }
                System.out.println("LDP remote: " + getEndpointPathLength() + " " + uri2);
                getEndpointList().add(uri2);
            }
        }
        if (ast.hasMetadata(Metadata.ACCEPT)) {
            setAccept(ast.getMetadata().getValues(Metadata.ACCEPT));
            System.out.println("Accept: " + getAccept());
        }
        if (ast.hasMetadata(Metadata.REJECT)) {
            setReject(ast.getMetadata().getValues(Metadata.REJECT));
            System.out.println("Reject: " + getReject());
        }
        if (ast.hasMetadata(Metadata.OPTION)) {
            processOption(ast.getMetadata().getValues(Metadata.OPTION));
            System.out.println("Option: " + getOption());
        }
        if (ast.getMetadata().hasMetadata(TIMEOUT)) {
            setTimeout(ast.getMetadata().getDatatypeValue(TIMEOUT).intValue());
            System.out.println("Timeout: " + getTimeout());
        }
        if (ast.getMetadata().hasMetadata(SPLIT)) {
            setMaxQuery(ast.getMetadata().getDatatypeValue(SPLIT).intValue());
            System.out.println("Split: " + getMaxQuery());
        }
        if (ast.getMetadata().hasMetadata(SLICE)) {
            int slice = ast.getMetadata().getDatatypeValue(SLICE).intValue();
            ProcessVisitorDefault.SLICE_DEFAULT_VALUE = slice;
            System.out.println("Slice: " + slice);
        }
        if (ast.hasMetadata(GRAPH)) {
            graphList = ast.getMetadata().getValues(GRAPH);
            System.out.println("Graph: " + graphList);
        }
        if (ast.hasMetadata(Metadata.SKIP)) {
            skip(ast);
        }
        if (ast.hasMetadata(Metadata.DEBUG)) {
            ast.getMetadata().remove(Metadata.DEBUG);
        }
        if (ast.hasMetadata(Metadata.FILE)) {
            setFile(ast.getMetadata().getValue(Metadata.FILE));
        }
        if (ast.hasMetadata(Metadata.DETAIL)) {
            detail = true;
        }
        if (ast.hasMetadata(Metadata.NEW)) {
            test = true;
        }
    }
    
    void skip(ASTQuery ast) {
        for (String name : ast.getMetadata().getValues(Metadata.SKIP)) {
           switch (name) {
               case AGGREGATE: setAggregate(false); break;
               case DATATYPE:  setDatatype(false); break;
           }
        }
    }
    
    void setAggregate(boolean b) {
        aggregate = b;
    }
    
    boolean isAggregate() {
        return aggregate;
    }
    
    void setDatatype(boolean b) {
        datatype = b;
    }
    
    boolean isDatatype() {
        return datatype;
    }
    
    void processOption(List<String> list) {
         setOption(list);
    }
    
    boolean hasOption(String name) {
        return getOption().contains(name);
    }

    int getLength(String uri) {
        if (uri.contains("@")) {
            String str = uri.substring(uri.indexOf("@") + 1);
            return Integer.valueOf(str);
        }
        return -1;
    }
    
    String clean(String uri) {
        if (uri.contains("@")) {
            return uri.substring(0, uri.indexOf("@"));
        }
        return uri;
    }
    
    public Result process(String q) throws EngineException, InterruptedException, IOException {
        exec = QueryProcess.create(graph);
        return process(exec.ast(q));
    }
    
    public Result process(ASTQuery ast) throws EngineException, InterruptedException, IOException {
        return process(ast, 1);
    }
    
    public Result process(ASTQuery ast, int varIndex) throws EngineException, InterruptedException, IOException {
        Logger.getLogger(LinkedDataPath.class.getName()).info("Start Linked Data Path Finder");
        start(ast);
        federate(ast, getLocalList());
        if (getPathLength() == 0 && !getEndpointList().isEmpty()) {
            // @ldpath <uri1@0> @endpoint <uri2>
            // keep ast query as is on uri1 and join on remote endpoint uri2
            join(ast, astq.length(ast) + 1);
        } else 
        if (test) {
            step(ast, 1, varIndex);
        }
        else {
            process(ast, 1, varIndex);
        }
        finish(ast);
        return result;
    }
    
    
    void step(ASTQuery ast, int i, int varIndex) throws EngineException {
        if (trace) {
            System.out.println(ast);
        }
        ASTQuery ast2 = astq.step(ast, varIndex);
        Mappings map = exec.query(ast2);
        result.record(ast2, map);
        if (i < getPathLength()) {
            List<Constant> list = getPropertyList(map);
            for (Constant p : list) {
                if (acceptable(p)) {
                    ASTQuery ast3 = astq.property(ast2, p, varIndex);
                    step(ast3, i+1, varIndex+1);
                }
            }
        }
    }

    /**
     * a) @federate s1 select distinct ?p { EXP ?si ?p ?sj } b) for all p in ?p
     * : @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj } c) for
     * all p in ?p : select count(distinct ?sj) as ?count) where { service s1
     * {EXP ?si p ?sj} service s2 { ?sj ?p ?sk }} d) recursion with i = i+1
     *
     * Results recorded in table ASTQuery -> Mappings
     */
    void process(ASTQuery ast, int i, int varIndex) throws InterruptedException, EngineException {
        // add: ?s ?p ?v
        ASTQuery ast1 = astq.variable(ast, varIndex);
        if (trace) {
            System.out.println(ast1);
        }
        complete(ast1);
        Mappings map = exec.query(ast1);
        if (trace) {
            System.out.println(map);
        }
        List<Constant> list = getPropertyList(map);
        process(ast1, list, i, varIndex);
    }
    
    void complete(ASTQuery a) {
        if (getAST().getMetaValue(Metadata.LIMIT)!=null) {
            System.out.println("limit: " + getAST().getMetadata().getDatatypeValue(Metadata.LIMIT));
            a.getMetadata().add(Metadata.LIMIT, getAST().getMetadata().getDatatypeValue(Metadata.LIMIT));
            a.setLimit(getAST().getMetadata().getDatatypeValue(Metadata.LIMIT).intValue());
        }       
    }

    /**
     * ast has a path q1/q2/../qi consider additional predicates
     * path/p1..path/pn in pList
     */
    void process(ASTQuery ast, List<Constant> pList, int i, int varIndex) throws InterruptedException {
        // for all p in pList: @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj }
        property(ast, pList, varIndex);

        if (i < getPathLength()) {
            // for all p in pList: @federate s1 select distinct ?p { EXP ?si p ?sj ?sj ?p ?sk }
            List<Mappings> mapList = variable(ast, pList, varIndex);
            // mapList is, for each p,  the next predicate list to consider
            for (Mappings map : mapList) {
                // newList is the new predicate list (for p) to consider recursively 
                List<Constant> newList = getPropertyList(map);
                process(exec.getAST(map), newList, i + 1, varIndex + 1);
            }
        }
    }
    

    /**
     * for all p in list : consider path with additional triple pattern ?si p
     * ?sj a) @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj }
     * b) select (count(distinct ?sj) as ?count) where { service s1 {EXP ?si p
     * ?sj} service s2 { ?sj ?p ?sk }}
     */
    void property(ASTQuery ast1, List<Constant> list, int varIndex) throws InterruptedException {
        if (list.size() > maxQuery) {  
            ArrayList<ArrayList<Constant>> newList = split(list, maxQuery);
            for (List<Constant> subList : newList) {
                propertyBasic(ast1, subList, varIndex);
            }
        }
        else {
            propertyBasic(ast1, list, varIndex);
        }
    }
    
    ArrayList<ArrayList<Constant>> split(List<Constant> list, int max) {
        ArrayList<ArrayList<Constant>> newList = new ArrayList<>();
        ArrayList<Constant> subList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            subList.add(list.get(i));
            if (subList.size() == max) {
                newList.add(subList);
                subList = new ArrayList<>();
            }
        }
        if (! subList.isEmpty()) {
            newList.add(subList);
        }
        //System.out.println("split: " + newList);
        return newList;
    }

    
    void propertyBasic(ASTQuery ast1, List<Constant> list, int varIndex) throws InterruptedException {
        ArrayList<QueryProcessThread> plist = new ArrayList<>();
        for (Constant p : list) {
            if (! acceptable (p)) {
                continue;
            }
            
            ASTQuery ast2 = astq.property(ast1, p, varIndex);
            astq.filter(ast2, varIndex+1);
            
            //  named graph
            astq.complete(ast2);
            
            if (trace) {
                System.out.println("first endpoint");
                System.out.println(ast2);
            }

            // predicates are processed in parallel threads
            QueryProcessThread qp = new QueryProcessThread(graph, ast2, p);
            plist.add(qp);
            qp.start();

            if (!getEndpointList().isEmpty()) {
                // try link with second remote endpoint

                //ASTQuery serv = endpoint(ast2, varIndex+1);
                ASTQuery serv = endpoint(astq.property(ast1, p, varIndex), varIndex+1);
                if (trace) {
                    System.out.println("second endpoint");
                    System.out.println(serv);
                }
                //ProcessVisitorDefault.SLICE_DEFAULT_VALUE = 50;
                QueryProcessThread qpe = new QueryProcessThread(graph, serv, p);
                qpe.setJoin(true);
                plist.add(qpe);
                qpe.start();
            }
            else if (getGraph(1) != null) {
                ASTQuery aa = astq.graphPathObject(astq.property(ast1, p, varIndex), getGraph(0), getGraph(1), varIndex+1);
                if (trace) {
                    System.out.println("second graph");
                    System.out.println(aa);
                }
                QueryProcessThread qpe = new QueryProcessThread(graph, aa, p);
                qpe.setJoin(true);
                plist.add(qpe);
                qpe.start();
            }
        }
        int j = 1;
        for (QueryProcessThread qp : plist) {
            qp.join(timeout);
            Mappings mm = qp.getMappings();
            if (qp.isJoin() && mm != null) {
                if (trace) System.out.println("Endpoint result: \n" + mm.get(0));
                if (detail && hasCount(mm)) {
                    detail(ast1, qp.getPredicate(), varIndex);
                }
            }
            if (trace) {
                System.out.println(String.format("%s/%s: nb res: %s", j++, plist.size(), (mm == null) ? "" : mm.size()));
            }
            result.record(qp.getAST(), mm);
        }
    }
    
    
    
    /**
     * compute join of ast with remote endpoint
     */
    public void join(ASTQuery ast, int varIndex) {
        ASTQuery serv = endpoint(ast, varIndex);
        if (trace) {
            System.out.println(serv);
        }
        //if (trace) System.out.println(serv);
        //ProcessVisitorDefault.SLICE_DEFAULT_VALUE = 50;
        QueryProcessThread qpe = new QueryProcessThread(graph, serv, null);
        qpe.process();
        Mappings mm = qpe.getMappings();
        result.record(qpe.getAST(), mm);
    }
    
    public ASTQuery endpoint(ASTQuery ast, int varIndex) {
        return endpoint(ast, varIndex, true);
    }
    
    public ASTQuery endpoint(ASTQuery ast, int varIndex, boolean count) {
        if (!  count) {
            return astq.service(ast, getLocalList().get(0), getEndpointList().get(0), varIndex, false);
        }
        else if (getEndpointPathLength() == 0) {
            // total number of joins
            return astq.service(ast, getLocalList().get(0), getEndpointList().get(0), varIndex);
        } else {
            // number oj joins for each remote property 
            return astq.servicePath(ast, getLocalList().get(0), getEndpointList().get(0), varIndex);
        }
    }

    
    

    List<Constant> getPropertyList(Mappings map) {
        ArrayList<Constant> list = new ArrayList<>();
        for (Mapping m : map) {
            IDatatype dt =  m.getValue(AST.PROPERTY_VAR);
            if (dt != null) {
                Constant p = Constant.create(dt);
                list.add(p);
            }
        }
        return list;
    }

    
    boolean hasCount(Mappings map) {
        return map != null  
                && ((map.getValue(AST.COUNT_VAR) != null
                    && map.getValue(AST.COUNT_VAR).intValue() > 0)
                || (map.getValue(AST.DISTINCT_VAR) != null
                    && map.getValue(AST.DISTINCT_VAR).intValue() > 0)
                );
    }
    
    /**
     * Exec endpoint query without count and display result Mappings
     */
    void detail(ASTQuery ast1, Constant p, int i) {
        ASTQuery ast2 = astq.property(ast1, p, i);
        ASTQuery aa = endpoint(ast2, i + 1, false);
        System.out.println(aa);
        QueryProcessThread qpe = new QueryProcessThread(graph, aa, p);
        qpe.process();
        System.out.println(qpe.getMappings());
    }
    
    boolean acceptable(Constant p) {
        return acceptable(p.getLabel());
    }
    
     boolean acceptable(String p) {
        return accept(p) && ! reject(p);
    }
    
    boolean accept(String p) {        
        if (getAccept().isEmpty()) {
            return true;
        }
        for (String name : getAccept()) {
            if (p.startsWith(name)) {
                return true;
            }
        }
        return false;
    }
    
    boolean reject(String p) {
        for (String name : getReject()) {
            if (p.startsWith(name)) {
                return true;
            }
        }
        return false;
    }
    
    
    List<Mappings> variable(ASTQuery ast, List<Constant> list, int varIndex) throws InterruptedException {
        if (list.size() > maxQuery) {  
            ArrayList<Mappings> res = new ArrayList<>();
            ArrayList<ArrayList<Constant>> newList = split(list, maxQuery);
            for (List<Constant> subList : newList) {
                List<Mappings> sol = variableBasic(ast, subList, varIndex);
                res.addAll(sol);
            }
            return res;
        }
        else {
            return variableBasic(ast, list, varIndex);
        }
    }

    // in ast add ?sj ?p ?sk
    List<Mappings> variableBasic(ASTQuery ast, List<Constant> list, int varIndex) throws InterruptedException {
        ArrayList<Mappings> mapList = new ArrayList<>();
        ArrayList<QueryProcessThread> plist = new ArrayList<>();

        for (Constant p : list) {
            // replace former ?si ?p ?sj by ?si p ?sj
            // add ?sj ?p ?sk
            if (! acceptable (p)) {
                continue;
            }
            ASTQuery ast2 = astq.propertyVariable(ast, p, varIndex);
            if (trace) {
                System.out.println(ast2);
            }
            QueryProcessThread qp = new QueryProcessThread(graph, ast2, p);
            plist.add(qp);
            qp.start();
        }

        int j = 1;
        for (QueryProcessThread qp : plist) {
            qp.join(timeout);
            Mappings mm = qp.getMappings();
            if (trace) {
                System.out.println(String.format("%s/%s: %s", j++, plist.size(), mm));
            }
            if (mm != null && mm.size() > 0) {
                mapList.add(mm);
            }
        }

        return mapList;
    }

    /**
     * Create basic query
     */
    ASTQuery create() throws EngineException {

        String q = "prefix nobel: <http://data.nobelprize.org/terms/>"
                + "@ldpath <http://s-paths.lri.fr:8890/sparql> "
                //+ "@rest <http://dbpedia.org/sparql>"
                + "select * where {"
                + "?s1 rdf:type nobel:Laureate ."
                + "}";

        Query qq = exec.compile(q);
        ASTQuery ast = exec.getAST(qq);
        return ast;
    }

    ASTQuery federate(ASTQuery ast, List<String> list) {
        if (!list.isEmpty()) {
            Metadata meta = new Metadata().add(Metadata.FEDERATE, list.get(0));
            ast.addMetadata(meta);
        }
        return ast;
    }

    /**
     * @return the length
     */
    public int getPathLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setPathLength(int length) {
        this.length = length;
    }

    /**
     * @return the endpointLength
     */
    public int getEndpointPathLength() {
        return endpointLength;
    }

    /**
     * @param endpointLength the endpointLength to set
     */
    public void setEndpointPathLength(int endpointLength) {
        this.endpointLength = endpointLength;
    }

    /**
     * @return the endpointList
     */
    public List<String> getEndpointList() {
        return endpointList;
    }

    /**
     * @param endpointList the endpointList to set
     */
    public void setEndpointList(List<String> endpointList) {
        this.endpointList = endpointList;
    }

    /**
     * @return the localList
     */
    public List<String> getLocalList() {
        return localList;
    }

    /**
     * @param localList the localList to set
     */
    public void setLocalList(List<String> localList) {
        this.localList = localList;
    }
    
    public void setDebug(boolean b) {
        trace  = b;
    }
    
    /**
     * @return the accept
     */
    public List<String> getAccept() {
        return accept;
    }

    /**
     * @param accept the accept to set
     */
    public void setAccept(List<String> accept) {
        this.accept = accept;
    }
    
    public void setAccept(String uri) {
        accept.add(uri);
    }

    /**
     * @return the reject
     */
    public List<String> getReject() {
        return reject;
    }

    /**
     * @param reject the reject to set
     */
    public void setReject(List<String> reject) {
        this.reject = reject;
    }
    
    public void setReject(String uri) {
        reject.add(uri);
    }

    /**
     * @return the option
     */
    public List<String> getOption() {
        return option;
    }

    /**
     * @param option the option to set
     */
    public void setOption(List<String> option) {
        this.option = option;
    }
    
    public void setOption(String uri) {
        option.add(uri);
    }
    
    AST ast() {
        return astq;
    }
    
    public void setMaxQuery(int n) {
        maxQuery = n;
    }
    
    int getMaxQuery() {
        return maxQuery;
    }
    
    String getGraph(int n) {
        if (graphList == null || graphList.size() <= n) {
            return null;
        }
        return graphList.get(n);
    }
    
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
  

}
