package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
 * This program may also be used as a visitor with @ldpath and @endpoint annotations
 * path lengths are given with @length at the end of endpoint URI: http://dbpedia.org/sparql@2
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LinkedDataPath implements QueryVisitor {

    static final String RDF_TYPE = RDF.RDF + "type";
    static final String RDFS_LABEL = RDF.RDFS + "label";
    static final String[] exclude = {}; //RDF_TYPE};

    Graph graph;
    QueryProcess exec;
    Result result;
    AST astq;
    // path length on first endpoint
    private int length = 1;
    // path length on remote endpoint
    private int endpointLength = 0;
    boolean trace = !true;

    // find links between local and endpoint
    private List<String> localList;
    // find links between local and endpoint
    private List<String> endpointList;
    List<Constant> serverList;
    ASTQuery ast;
    String file;

    boolean parallel = true;


    public LinkedDataPath() {
        graph = Graph.create();
        astq = new AST();
        localList = new ArrayList<>();
        endpointList = new ArrayList<>();
        serverList = new ArrayList<>();
    }
    
    public LinkedDataPath(String uri) {
        this();
        getLocalList().add(uri);
    }
    
    public LinkedDataPath(String uri1, String uri2) {
        this();
        getLocalList().add(uri1);
        getEndpointList().add(uri2);
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
        }
    }

    @Override
    public void visit(Query query) {
    }

    @Override
    public void visit(Query q, fr.inria.corese.sparql.api.Graph g) {
    }

    public Result ldp(ASTQuery ast) throws IOException, EngineException, InterruptedException {
        Logger.getLogger(LinkedDataPath.class.getName()).info("Start Linked Data Path");
        process(ast);
        return result;
    }
    
    public void setFile(String name) {
        file = name;
    }
    
    public HashMap<ASTQuery, Mappings> getResult() {
        return result.getResult();
    }

    void start(ASTQuery ast) throws EngineException {
        metadata(ast);
        result = new Result(ast);
        result.setLinkedDataPath(this);
        result.setFile(file);
        if (ast.isDebug()) {
            trace = true;
        }
        exec = QueryProcess.create(graph);
    }
    
    void metadata(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.LDPATH)) {
            String uri = ast.getMetadata().getValue(Metadata.LDPATH);
            ast.getMetadata().remove(Metadata.LDPATH);
            if (uri != null) {
                int n = getLength(uri);
                if (n != -1) {
                    setPathLength(n);
                    uri = clean(uri);
                }
                System.out.println("LDP first: " + getPathLength() + " " + uri);
                getLocalList().add(uri);
            }
        }
        if (ast.hasMetadata(Metadata.ENDPOINT)) {
            String uri = ast.getMetadata().getValue(Metadata.ENDPOINT);
            if (uri != null) {
                int n = getLength(uri);
                if (n != -1) {
                    setEndpointPathLength(n);
                    uri = clean(uri);
                }
                System.out.println("LDP remote: " + getEndpointPathLength() + " " + uri);
                getEndpointList().add(uri);
            }
        }
        if (ast.hasMetadata(Metadata.FILE)) {
            setFile(ast.getMetadata().getValue(Metadata.FILE));
        }
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
    
    
    void process(ASTQuery ast) throws EngineException, InterruptedException, IOException {
        start(ast);
        federate(ast, getLocalList());
        if (getPathLength() == 0 && ! getEndpointList().isEmpty()) {
            // @ldpath <uri1@0> @endpoint <uri2>
            // keep ast query as is on uri1 and join on remote endpoint uri2
            join(ast, astq.length(ast));
        } else {
            process(ast, 1);
        }
    }
    
    public void complete(ASTQuery ast, List<Constant> list, int i) throws EngineException, InterruptedException, IOException {
         ast = astq.complete(ast, list, i);
         process(ast);
    }

    /**
     * a) @federate s1 select distinct ?p { EXP ?si ?p ?sj } b) for all p in ?p
     * : @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj } c) for
     * all p in ?p : select count(distinct ?sj) as ?count) where { service s1
     * {EXP ?si p ?sj} service s2 { ?sj ?p ?sk }} d) recursion with i = i+1
     *
     * Results recorded in table ASTQuery -> Mappings
     */
    void process(ASTQuery ast, int i) throws InterruptedException {
        // add: ?s ?p ?v
        ASTQuery ast1 = astq.variable(ast, i);
        if (trace) {
            System.out.println(ast1);
        }
        Mappings map = exec.query(ast1);
        if (trace) {
            System.out.println(map);
        }
        List<Constant> list = getPropertyList(map);
        process(ast1, list, i);
    }

    /**
     * compute join of ast with remote endpoint
     */
    void join(ASTQuery ast, int i) {
        ASTQuery serv = endpoint(ast, i+1);
        if (trace) {
            System.out.println(serv);
        }
        //if (trace) System.out.println(serv);
        ProcessVisitorDefault.SLICE_DEFAULT_VALUE = 50;
        QueryProcessThread qpe = new QueryProcessThread(graph, serv, null);
        qpe.process();
        Mappings mm = qpe.getMappings();
        result.record(qpe.getAST(), mm);
    }
    
    ASTQuery endpoint(ASTQuery ast, int i) {
        if (getEndpointPathLength() == 0) {
            return astq.service(ast, getLocalList().get(0), getEndpointList().get(0), i);
        } else {
            return astq.servicePath(ast, getLocalList().get(0), getEndpointList().get(0), i);
        }
    }

    /**
     * ast has a path q1/q2/../qi consider additional predicates
     * path/p1..path/pn in pList
     */
    void process(ASTQuery ast, List<Constant> pList, int i) throws InterruptedException {
        // for all p in pList: @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj }
        property(ast, pList, i);

        if (i < getPathLength()) {
            // for all p in pList: @federate s1 select distinct ?p { EXP ?si p ?sj ?sj ?p ?sk }
            List<Mappings> mapList = variable(ast, pList, i);
            // mapList is, for each p,  the next predicate list to consider
            for (Mappings map : mapList) {
                // newList is the new predicate list (for p) to consider recursively 
                List<Constant> newList = getPropertyList(map);
                process(exec.getAST(map), newList, i + 1);
            }
        }
    }

    List<Constant> getPropertyList(Mappings map) {
        ArrayList<Constant> list = new ArrayList<>();
        for (Mapping m : map) {
            IDatatype dt = (IDatatype) m.getValue(AST.PROPERTY_VARIABLE);
            if (dt != null) {
                Constant p = Constant.create(dt);
                list.add(p);
            }
        }
        return list;
    }

    /**
     * for all p in list : consider path with additional triple pattern ?si p
     * ?sj a) @federate s1 select (count(*) as ?count) where { EXP ?si p ?sj }
     * b) select (count(distinct ?sj) as ?count) where { service s1 {EXP ?si p
     * ?sj} service s2 { ?sj ?p ?sk }}
     */
    void property(ASTQuery ast1, List<Constant> list, int i) throws InterruptedException {
        int timeout = 10000;
        ArrayList<QueryProcessThread> plist = new ArrayList<>();

        for (Constant p : list) {
            ASTQuery ast2 = astq.property(ast1, p, i);
            if (trace) {
                System.out.println(ast2);
            }

            // predicates are processed in parallel threads
            QueryProcessThread qp = new QueryProcessThread(graph, ast2, p);
            plist.add(qp);
            qp.start();

            if (!getEndpointList().isEmpty() && i >= 1 && accept(p.getLabel())) {
                // try link with second remote endpoint

                ASTQuery serv = endpoint(ast2, i+1);
                if (trace) {
                    System.out.println(serv);
                }
                ProcessVisitorDefault.SLICE_DEFAULT_VALUE = 50;
                QueryProcessThread qpe = new QueryProcessThread(graph, serv, p);
                qpe.setJoin(true);
                plist.add(qpe);
                qpe.start();
            }
        }
        int j = 1;
        for (QueryProcessThread qp : plist) {
            qp.join(timeout);
            Mappings mm = qp.getMappings();
//            if (qp.isJoin() && mm != null) {
//                if (trace) System.out.println("Res: " + mm.size());
//                //System.out.println(mm);
//            }
            if (trace) {
                System.out.println(String.format("%s/%s: nb res: %s", j++, plist.size(), (mm == null) ? "" : mm.size()));
            }
            result.record(qp.getAST(), mm);
        }
    }

    boolean accept(String uri) {
        for (String name : exclude) {
            if (uri.equals(name)) {
                return false;
            }
        }
        return true;
    }

    // in ast add ?sj ?p ?sk
    List<Mappings> variable(ASTQuery ast, List<Constant> list, int i) throws InterruptedException {
        ArrayList<Mappings> mapList = new ArrayList<>();
        ArrayList<QueryProcessThread> plist = new ArrayList<>();
        int timeout = 10000;

        for (Constant p : list) {
            // replace former ?si ?p ?sj by ?si p ?sj
            // add ?sj ?p ?sk
            ASTQuery ast2 = astq.propertyVariable(ast, p, i);
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

}
