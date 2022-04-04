package fr.inria.corese.core.util;

import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRuleUser;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformerUser;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorUser;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessNamespace;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import static fr.inria.corese.sparql.triple.parser.NSManager.COS;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage Turtle Property file to tune corese behavior Authorized namespace for
 * @import and LinkedFunction
 *
 * @author Olivier Corby, INRIA, I3S 2020
 */
public class Parameter {
    
    private static final String LINKED_FUNCTION_ACCEPT = "?accept";
    private static final String LINKED_FUNCTION_REJECT = "?reject";
    private static final String LINKED_FUNCTION = "?lf";
    private static final String ACCESS_RIGHT = "?access";
    private static final String EVENT = "?event";
    private static final String PROFILE = "?profile";
    private static final String PARAM = "?param";
    private static final String FUNCTION_IMPORT = "?function";
    private static final String DATA_IMPORT = "?data";
    private static final String NOTIFY = "?notify";
    private static final String PARAM_GRAPH = "?paramGraph";
    private static final String EVENT_RULE = "?rule";
    private static final String EVENT_TRANS = "?trans";
    private static final String EVENT_SOLVER = "?solver";
    private static final String EVENT_SERVER = "?server";
    private static final String LEVEL = "?level";
    
    
    private static final String SOLVER_USER = COS+"user";
    private static final String SUPER_USER = COS+"super";
    private static final String SOLVER_SERVER_USER = "fr.inria.corese.server.webservice.QuerySolverVisitorServerUser";
    
    
    public static boolean PARAM_EVENT = false;
    public static boolean PROFILE_EVENT = false;
    
    String q = 
            "select ?event ?profile ?param ?solver ?rule ?trans ?server ?level "
            + "(aggregate(distinct ?acc) as ?accept) "
            + "(aggregate(distinct ?rej) as ?reject) "
            + "(aggregate(distinct ?fun) as ?function) "
            + "where {"
            + "optional { [] cos:level ?level }"
            + "optional { [] cos:accept ?acc ; cos:reject ?rej }"
            + "optional { [] cos:load ?fun }"
            + "optional { [] cos:event ?ev "
            + "optional { ?ev cos:active  ?event }"
            + "optional { ?ev cos:profile ?profile }"
            + "optional { ?ev cos:param   ?param }"
            // class name for Visitor
            + "optional { ?ev cos:solver  ?solver }"
            + "optional { ?ev cos:server  ?server }"
            + "optional { ?ev cos:rule    ?rule }"
            + "optional { ?ev cos:transformer ?trans }"
            + "}"
            + "}";
    
    Graph graph;
    QueryProcess exec;
    
    public Parameter () {
        graph = Graph.create();
        // LDScript static variable contains the Parameter graph
        Binding.setStaticVariable(PARAM_GRAPH, DatatypeMap.createObject(graph));
        exec = QueryProcess.create(graph);
    }
    
    public Parameter load(String path) throws LoadException {
        Load ld = Load.create(graph);
        ld.setEvent(false);
        ld.parse(path);
        return this;
    }
    
    public void process() {
        init();
        Mappings map = getMap(q);
        System.out.println(map);
        process(map);
        start();
    }
    
    // call @init event function
    void start() {
        try {
            QuerySolverVisitor vis = new QuerySolverVisitor(exec.getCreateEval());
            vis.initParam();
        } catch (EngineException ex) {
            Logger.getLogger(Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void process(Mappings map) {
        
        for (Mapping m : map) {
            for (Node var : map.getSelect()) {
                IDatatype value =  map.getValue(var);
                if (value != null) {
                    switch (var.getLabel()) {
                        case FUNCTION_IMPORT:
                            System.out.println("param: " + var + " " + value);
                            importFunction(value);
                            break;
                    }
                }
            }
        }
        
        //test(map);

        for (Mapping m : map) {
            for (Node var : map.getSelect()) {
                IDatatype value =  map.getValue(var);
                if (value != null) {
                    String label = value.getLabel();
                    
                    switch (var.getLabel()) {
                        case FUNCTION_IMPORT: // already processed above: skip
                            break;
                        default: System.out.println("param: " + var + " " + value);
                    }
                    
                    switch (var.getLabel()) {
                        case LINKED_FUNCTION_ACCEPT:
                            namespace(value, true);
                            break;
                        case LINKED_FUNCTION_REJECT:
                            namespace(value, false);
                            break;
                        case LINKED_FUNCTION:
                            Access.setLinkedFunction(value.booleanValue());
                            break;
                        case ACCESS_RIGHT:
                            Property.set(Property.Value.ACCESS_RIGHT, value.booleanValue());
                            break;
                        case EVENT:
                            Property.set(Property.Value.EVENT, value.booleanValue());
                            break;
                        case PROFILE:
                            PROFILE_EVENT = value.booleanValue();
                            break;
                        case PARAM:
                            PARAM_EVENT = value.booleanValue();
                            break; 
                        case EVENT_RULE:
                            QuerySolverVisitorRule.setVisitorName(label.equals(SOLVER_USER)
                                    ?QuerySolverVisitorRuleUser.class.getName():label);
                            break;
                        case EVENT_SOLVER:
                            QueryProcess.setVisitorName(label.equals(SOLVER_USER)
                                    ?QuerySolverVisitorUser.class.getName():label);
                            break;
                        case EVENT_SERVER:
                            QueryProcess.setServerVisitorName(label.equals(SOLVER_USER)
                                    ?SOLVER_SERVER_USER:label);
                            break;                            
                        case EVENT_TRANS:
                            QuerySolverVisitorTransformer.setVisitorName(label.equals(SOLVER_USER)
                                    ?QuerySolverVisitorTransformerUser.class.getName():label);
                            break;
                        case LEVEL: 
                            // ?level = cos:user -> Level.USER
                            Access.Level.USER_DEFAULT = level(label);
                    }
                }
            }
        }
    }
    
    /**
    * Assign the level of user action
    * default is PRIVATE
    */
    Access.Level level(String level) {
        switch (level) {
            // restrict action level to user
            case SOLVER_USER: return Access.Level.USER;
            // default level
            default: return Access.Level.DEFAULT;
        }
    }
    
    void namespace(IDatatype dt, boolean b) {
        for (IDatatype ns : dt.getList()) {
            System.out.println("lf: " + ns + " " + b);
            AccessNamespace.define(ns.getLabel(), b);
        }
    }
    
    void importFunction(IDatatype dt) {
        for (IDatatype path : dt.getList()) {
            try {
                exec.imports(path.getLabel());
            } catch (EngineException ex) {
                Logger.getLogger(Parameter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    Mappings getMap(String q) {
        
        Mappings map;
        try {
            map = exec.query(q);
        } catch (EngineException ex) {
            map = new Mappings();
            Logger.getLogger(Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }


    public void init() {
        AccessNamespace.clean();
    }
    
}
