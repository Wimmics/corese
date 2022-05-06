package fr.inria.corese.compiler.federate;

import fr.inria.corese.compiler.federate.util.ResourceReader;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class SelectorIndex {
    public static boolean SELECT_ENDPOINT = false;
    public static double NBSUCCESS = 0.5;
    private static final String FILTER_EXISTS = 
    "filter exists {?s void:propertyPartition/void:property <%s>}\n";
    private static final String OPTIONAL = 
"optional {?s void:propertyPartition/void:property <%s> bind (1 as ?b_%s)}\n";
    // default source discovery query pattern
    private static final String DEFAULT_QUERY_PATTERN = 
            "/query/indexpatternendpoint.rq";
    // path set by Property FEDERATE_INDEX_PATTERN
    public static String QUERY_PATTERN = null;
    
    Selector selector;
    ASTQuery ast;
    String uri;
    List<Constant> uriList;
    
    SelectorIndex(Selector s, ASTQuery ast) {
        selector = s;
        this.ast = ast;
    }
    
    SelectorIndex(Selector s, ASTQuery ast, String uriIndex) {
        selector = s;
        this.ast = ast;
        this.uri = uriIndex;
    }
                   
    // generate query for endpoint URL source discovery
    // with federate query ast as input      
    ASTQuery process() {
        try {
            ASTQuery a = getQuery();
            return a;
        } catch (EngineException ex) {
            selector.getVisitor().logger.error(ex.getMessage());
            return ASTQuery.create();
        }
    }
    


    // generate query for endpoint URL source discovery    
    // for ast federate query
    ASTQuery getQuery() throws EngineException {
        // get query pattern with %s
        String pattern = getPattern(QUERY_PATTERN, DEFAULT_QUERY_PATTERN);
        // generate test part to find federate query predicates
        String test = predicateTest(ast);
        String queryString = String.format(pattern, test);
        //System.out.println("pattern:\n"+pattern);
        //System.out.println("test:\n"+test);
        System.out.println("query:\n"+queryString);
        Query q = selector.getQuerySolver().compile(queryString);
        return q.getAST();
    }
    
    // generate test part to find federate query predicates
    String predicateTest(ASTQuery ast) {   
        StringBuilder sb = new StringBuilder();
        int i = 0;
        
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {
                // skip variable predicate 
            } 
            else {
                // generate test for predicate
                sb.append(String.format(OPTIONAL, p.getLongName(), i++)); 
            }
        }        
        // count number of predicates present in endpoint url
        count(sb, i);               
        return sb.toString();
    }
    
    // count number of predicates present in endpoint url
    // endpoint succeed when: 
    // endpoint nb predicates >= query nb predicates * rate
    // e.g. it has 0.5 * query nb predicates
    // NBSUCCESS can be set by 
    // property FEDERATE_INDEX_SUCCESS 0.75
    // annotation @fedSuccess 0.75
    void count(StringBuilder sb, int i) {
        sb.append("bind (");
        for (int j = 0; j < i; j++) {
            if (j > 0) {
                sb.append(" + ");
            }
            sb.append("coalesce(?b_").append(j).append(", 0)");
        }
        sb.append(" as ?c)\n");
        sb.append(String.format("filter (?c >= %s)", i * NBSUCCESS));
    }
        
       
//    ASTQuery process(Variable serv, ASTQuery aa) {
//        try {
//            BasicGraphPattern bgp = compile(serv, aa);
//            ASTQuery a = getQuery2(serv, bgp);
//            return a;
//        } catch (EngineException ex) {
//            selector.getVisitor().logger.error(ex.getMessage());
//            return aa;
//        }
//    }
    
    
//    ASTQuery getQuery2(Variable serv, BasicGraphPattern bgp) throws EngineException {
//        Values values = Values.create(serv, uriList);
//        String pattern = getPattern("/query/indexpattern.rq");
//        String str = String.format(pattern, (SELECT_ENDPOINT?"":values), bgp.toStringBasic());
//        System.out.println("pattern:\n"+str);
//        Query q = selector.getQuerySolver().compile(str);
//        return q.getAST();
//    }
    
    
    String generate2(ASTQuery ast) {   
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } 
            else {
                sb.append(String.format(FILTER_EXISTS,p.getLongName())); 
            }
        }
        
        return sb.toString();
    }
        
    BasicGraphPattern compile(Variable serv, ASTQuery aa) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        int i = 0;
        
        for (Constant p : ast.getPredicateList()) {
            if (p.getLabel().equals(ASTQuery.getRootPropertyURI())) {

            } else {
                // ?meta dcterms:modified ?modif .
                // ?meta kgi:curated ?s .
                // ?s void:sparqlEndpoint ?serv
                
                // bind (exists {?s void:propertyPartition/void:property $pred} as ?b)
                Constant part = aa.createQName("void:propertyPartition");
                Constant prop = aa.createQName("void:property");
                
                Variable s  = Variable.create("?s");
                Variable x  = Variable.create("?x");
                
                Triple t1 = aa.triple(s, part, x);
                Triple t2 = aa.triple(x, prop, Constant.create(p.getLongName()));
                
                // exists { ?serv idx:namespace/idx:data/idx:predicate predicate }
                Variable var = selector.exist(aa, bgp, aa.bgp(t1, t2), i);
                selector.declare(p, var);
                                
                i++;
            }                      
        }
        
                
        return bgp;
    }
    
    
    
    String getPattern(String path, String defaut) {
        String pattern = "select * where {%s}";
        try {
            if (path == null) {
                return new ResourceReader().getResource(defaut);
            } else {
                return new ResourceReader().readWE(path);
            }
        } catch (IOException ex) {
            selector.getVisitor().logger.error(ex.getMessage());
        }
        return pattern;
    }
    
    
    
}
