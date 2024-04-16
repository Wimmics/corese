package fr.inria.corese.core.extension;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.print.LogManager;
import fr.inria.corese.core.query.Construct;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.Cleaner;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.IDatatypeList;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic Java Extension function public class
 * prefix fun: <function://fr.inria.corese.core.extension.Extension>
 * fun:test(xt:graph())
 * 
 * Provide access to query execution environment
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class Extension extends Core {
    private static Logger logger = LoggerFactory.getLogger(Extension.class);
    
    static Extension singleton;
    
    static {
        singleton = new Extension();
    }
    
//    public static Extension singleton() {
//        return singleton;
//    }
    
    Binding getBinding() {
        if (getEnvironment() == null) {
            return null;
        }
        return  getEnvironment().getBind();
    }
    
    // inherit access level from Binding
    Context getCreateContext() {
        Binding b = getBinding();
        if (b == null) {
            return new Context(Level.DEFAULT);
        }
        return new Context(b.getAccessLevel());
    }
    
    ContextLog getLog() {
        return getBinding().getLog();
    }
    
    public IDatatype getContextLog() {
        if (getLog() == null) {
            return null;
        }
        return DatatypeMap.createObject(getLog());
    }
    
    public IDatatype getMessage() {
        String url = getLog().getMessage();
        if (url == null) {
            return null;
        }
        String text = new Service().getString(url);
        if (text == null || text.isEmpty()) {
            return null;
        }
        return DatatypeMap.json(text);
    }
    
    public IDatatype parallel(IDatatype name) {
        return DatatypeMap.newResource(String.format(ProviderService.LOCAL_SERVICE_NS, name.getLabel()));
    }
    

    // return list of variables bound in environment
    public IDatatype variables() {
        ArrayList<IDatatype> list = new ArrayList<>();
        
        for (Node node : getEnvironment().getQueryNodes()) {
            if (node!=null && node.isVariable() && ! node.isBlank()) {
                Node report = getEnvironment().getNode(node);
                if (report !=null) {
                    list.add(report.getDatatypeValue());
                }
            }
        }
        return DatatypeMap.newList(list);
    }
           
    /**
     * Service evaluation report graph recorded in ContextLog 
     */
    public IDatatype getLogGraph() {
        if (getLog() == null) {
            return null;
        }
        LogManager man = new LogManager(getLog());
        try {
            Graph g = man.parse();
            return DatatypeMap.createObject(g);
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }
    
    public IDatatype getLogURL() {
        if (getLog() == null || getLog().getLink() == null) {
            return null;
        }
        return DatatypeMap.newResource(getLog().getLink());
    }   
       
    public IDatatype parse(IDatatype dt) throws EngineException {
        Context c = getCreateContext();
        if (Access.reject(Feature.LDSCRIPT_SPARQL, c.getLevel())) {
            throw new SafetyException(TermEval.SPARQL_MESS);
        }
        ASTQuery ast = parseQuery(dt.getLabel(), c);
        if (ast == null) { 
            return null;
        }
        return DatatypeMap.createObject(ast);
    }
    
    ASTQuery parseQuery(String str, Context c) throws EngineException {
        QueryProcess exec = QueryProcess.create();
        Query q = exec.compile(str, c);
        return  q.getAST();
    }
           
    
    public IDatatype list(IDatatype dt) {
        if (dt.isExtension()) { 
            switch (dt.getDatatypeURI()) {
                case IDatatype.MAPPINGS_DATATYPE: return list(dt.getPointerObject().getMappings());
                case IDatatype.MAPPING_DATATYPE:  return list(dt.getPointerObject().getMapping());
            }
        }
        if (dt.isTripleWithEdge()) {
            return list(dt.getEdge());
        }
        if (dt.getNodeObject() != null && dt.getNodeObject() instanceof Enumeration) {
            return DatatypeMap.newList((Enumeration) dt.getNodeObject());
        }
        if (dt.getNodeObject() != null && dt.getNodeObject() instanceof Object[]) {
            return DatatypeMap.newList((Object[]) dt.getNodeObject());
        }
        return DatatypeMap.list();
    }
    
    IDatatype list(Edge e) {
        IDatatypeList list = DatatypeMap.newList(
                e.getSubjectValue(), e.getPredicateValue(), e.getObjectValue());
        return DatatypeMap.newList(list);
    }
    
    // Mappings as list(list(var, val))
    IDatatype list(Mappings map) {
        IDatatypeList list = DatatypeMap.newList();
        for (Mapping m : map) {
            list.addAll(m.getDatatypeList());
        }
        return list;
    }
    
    // Mapping as list(list(var, val))
    IDatatype list(Mapping m) {
        return m.getDatatypeList();
    }
          
    public IDatatype allEntailment(IDatatype dt) {
        Construct.setAllEntailment(dt.booleanValue());
        return dt;
    }
    
    
     public IDatatype distance(IDatatype dt1, IDatatype dt2) { 
         return distance(dt1, dt2, null);
     }

    
     public IDatatype distance(IDatatype dt1, IDatatype dt2, IDatatype dt) {
        Graph g = getGraph();
        Node n1 = g.getNode(dt1);
        Node n2 = g.getNode(dt2);
        if (n1 == null || n2 == null) {
            return getValue(Integer.MAX_VALUE);
        }

        Distance distance = g.getClassDistance();
        if (distance == null || (dt != null && ! distance.getSubClassOf().equals(dt.getLabel()))) {
            distance = new Distance(g);
            distance.setStep(1);
            if (dt != null) {
                distance.setSubClassOf(dt.getLabel());
            }            
            distance.start();
            g.setClassDistance(distance);
        }
        double dd = distance.distance(n1, n2);
        return getValue(dd);
    }
    
    
    
    
    // clean OWL ontology: remove duplicate statements
    public IDatatype clean() {
        Cleaner clean = new Cleaner(getGraph());
        clean.setVisitor(new QuerySolverVisitorRule(new RuleEngine(), getEval()));
        try {
            clean.process();
        } catch (IOException | LoadException | EngineException ex) {
            logger.error(ex.getMessage());
        } 
        return DatatypeMap.TRUE;
    }
    
    
    
    
    // example    
    IDatatype test(IDatatype dt) {
        getEnvironment();
        getProducer();
        getGraph();
        getEval();
        return DatatypeMap.TRUE;
    }
    
   ProcessVisitor getVisitor() {
       return getEval().getVisitor();
   } 
    
   public IDatatype closeVisitor() {
       getEval().setVisitor(new ProcessVisitorDefault());
       getVisitor().setDefaultValue(TRUE);
       return visitor();
   } 
   
   public IDatatype openVisitor() {
       getEval().setVisitor(new QuerySolverVisitor(getEval()));
       getVisitor().init(getEnvironment().getQuery());
       return visitor();
   } 
    
    /**
     * Accessor
     * fun:visitor()
     */
    public IDatatype visitor() {
        return cast(getVisitor());
    }
    
    public IDatatype query() {
        return cast(getQuery());
    }
    
    public IDatatype ast() {
        return cast(getAST());
    }
    
    public IDatatype edge(IDatatype n) {
        Edge[] edges = getEnvironment().getEdges();
        if (edges == null || edges.length < n.intValue()) {
            return null;
        }
        return cast(edges[n.intValue()]);
    }
    
    Query getQuery() {
        return getEnvironment().getQuery();
    }
    
    ASTQuery getAST() {
        return  getEnvironment().getQuery().getAST();
    }
    
    IDatatype cast(Object obj) {
        return DatatypeMap.getValue(obj);
    }
    
    
    
    public IDatatype fib(IDatatype n) {
        switch (n.intValue()) {
            case 0: 
            case 1: return n;
            default: return fib(n.minus(DatatypeMap.ONE)).plus(fib(n.minus(DatatypeMap.TWO)));
        }
    }
    
    public IDatatype fibJava(IDatatype n) {
        return DatatypeMap.newInstance(fib(n.intValue()));
    }

    
    int fib(int n) {
        switch (n) {
            case 0: 
            case 1: return n;
            default: return fib(n-1) + fib(n-2);
        }
    }
    
    /**
     * @return the result of exp(dt), dt is a number
     */
    public IDatatype exponential(IDatatype dt) {
        return DatatypeMap.newInstance(Math.exp(dt.doubleValue()));
    }

    /**
     * @return PI value
     */
    public IDatatype pi() {
        return DatatypeMap.newInstance(Math.PI);
    }
    
}
