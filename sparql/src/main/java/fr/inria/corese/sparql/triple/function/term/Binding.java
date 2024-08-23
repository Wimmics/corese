package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Binder;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.HashMapList;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.parser.VariableLocal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * Stack vor LDScript variable bindings Variable have a relative index stack
 * index = level + var index level is the level of current function call in the
 * stack
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Binding implements Binder {
    
    private static final String URI = "uri";
    private static final String MAP2 = "m2";
    private static final String MAP1 = "m1";
    private static final String EXP = "str";
    private static final String STMT = "stmt";
    private static final String LIST = "list";
    private static final String NUMBER = "number";
    
    static final String NL = System.getProperty("line.separator");
    public static final int UNBOUND = ExprType.UNBOUND;
    public static boolean DEBUG_DEFAULT = false;
    public static boolean DYNAMIC_CAPTURE_DEFAULT = false;
    public static final String SLICE_SERVICE  = "?slice_service";
    public static final String SERVICE_REPORT = "?_service_report";
    public static final String SERVICE_REPORT_FORMAT = "?_service_report_%s";
    public static final String SERVICE_REPORT_ZERO = "?_service_report_0";
    public static final String SERVICE_REPORT_ONE = "?_service_report_1";
    public static final String SERVICE_PARAM = "?service_param";
    
    ArrayList<Expr> varList;
    ArrayList<IDatatype> valList;
    // level of the stack before function call
    // every funcall add a level
    ArrayList<Integer> level;
    int currentLevel = 0, count = 0;
    
    HashMap<String, IDatatype> globalValue;
    HashMap<String, Variable>  globalVariable;
    private ProcessVisitor visitor;
    private AccessRight accessRight;
    
    private boolean debug = DEBUG_DEFAULT;

    private static Logger logger = LoggerFactory.getLogger(Binding.class);
    private boolean dynamicCapture = DYNAMIC_CAPTURE_DEFAULT;
    private boolean result;
    private boolean coalesce = false;
    private Access.Level accessLevel = Access.Level.USER_DEFAULT;
    private ContextLog contextLog;
    private Context context;
    // data shared back and forth when calling share()
    private Share share;
    // transformation Mappings with xt:mappings()
    private Mappings mappings;
    private IDatatype datatypeValue;
    // draft sparql evaluation report
    // record partial results of sparql statements
    private IDatatype report;
    
    private static Binding singleton;
    
    static {
        setSingleton(new Binding());
    }

    Binding() {
        varList = new ArrayList();
        valList = new ArrayList();
        level = new ArrayList();
        setReport(DatatypeMap.newServiceReport());
        setGlobalVariableValues(new HashMap<>());
        setGlobalVariableNames(new HashMap<>());
        setAccessRight(new AccessRight());
        getCreateLog();
        setShare(new Share());
    }
    
    public class Share {
        // Enables transformer to record data during processing
        // using function st:visit()
        // created by PluginTransform getVisitor()
        // Visitor is shared among every Binding during transformation 
        // processing because every Binding share the same Share object
        // when a sub transformer creates a Visitor, the calling transformer
        // get it through the same Share object. see function share()
        // transformer also records its Binding and hence its Visitor
        private Object transformerVisitor;

        // Enables function now() to return the same value during processing
        private Optional<IDatatype> savNowValue = Optional.empty();

        public Object getTransformerVisitor() {
            return transformerVisitor;
        }

        public void setTransformerVisitor(Object transformerVisitor) {
            this.transformerVisitor = transformerVisitor;
        }

        public Optional<IDatatype> getSavNowValue() {
            return savNowValue;
        }

        public void setNowValue(IDatatype nowValue) {
            this.savNowValue = Optional.ofNullable(nowValue);
        }

    }

    public static Binding create() {
        return new Binding();
    }

    @Override
    public void clear() {
        varList.clear();
        valList.clear();
        level.clear();
        currentLevel = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Level stack: ").append(level).append(NL);
        for (int i = index(); i >= 0; i--) {
            sb.append("(").append(i).append(") ");
            sb.append(varList.get(i)).append(" = ").append(valList.get(i));
            sb.append(NL);
        }
        if (! getGlobalVariableNames().isEmpty()) {
            sb.append("Global:").append(NL);
        }
        for (String name : getGlobalVariableNames().keySet()) {
            sb.append(name).append(" = ").append(getGlobalVariableValues().get(name)).append(NL);
        }
        sb.append("access level: ").append(getAccessLevel()).append(NL);
        if (getLog() !=null) {
            sb.append(getLog());
        }
        return sb.toString();
    }

    public int size() {
        return varList.size();
    }

    int index() {
        return varList.size() - 1;
    }

    /**
     * A function (possibly with no arg) or a let has been called
     *
     * @return
     */
    @Override
    public boolean hasBind() {
        return varList.size() > 0 || level.size() > 0;
    }
    
    public boolean isEmpty() {
        return size() == 0 && getGlobalVariableNames().isEmpty();
    }

    int getIndex(Expr var) {
        return currentLevel + var.getIndex();
    }

    public void set(Expr exp, Expr var, IDatatype val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                count++;
                allocate(exp);
                break;

            case ExprType.LET:               
            case ExprType.FOR:
                allocation(exp);
        }
        set(var, val);
    }
    
    // must perform allocation before
    public void setlet(Expr exp, Expr var, IDatatype val) {
        set(var, val);
    }
    
    // do it when desallocation return false
    public void unsetlet(Expr exp, Expr var, IDatatype val) {
        unset(var);
    }
    
    public void allocation(Expr exp) {
        if (exp.getNbVariable() > 0) {
            allocate(exp);
        }
    }
    
    public boolean desallocation(Expr exp) {
        if (exp.getNbVariable() > 0) {
            desallocate(exp);
            return true;
        }
        return false;
    }

    public void unset(Expr exp, Expr var, IDatatype val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                desallocate(exp);
                break;

            case ExprType.LET:
            case ExprType.FOR:
                if (desallocation(exp)) {
                    return;
                }

            default:
                unset(var);
        }
    }

     //special case: unary function
    public void set(Function exp, Expr var, IDatatype val) {
        pushLevel();
        push(var, val);
        // allocate additional variables
        for (int j = 1; j < exp.getNbVariable(); j++) {
            push(null, null);
        }
    }
    
    public void setTailRec(Function exp, Expr var, IDatatype val) {
        valList.set(currentLevel, val);
        // unset additional variables
        for (int j = 1; j < exp.getNbVariable(); j++) {
            valList.set(currentLevel+j, null);
        }
    }
    
     //special case: binary function
     public void set(Function exp, Expr var1, IDatatype val1, Expr var2, IDatatype val2) {
        pushLevel();
        push(var1, val1);
        push(var2, val2);
        // allocate additional variables
        for (int j = 2; j < exp.getNbVariable(); j++) {
            push(null, null);
        }
    }
    
    public void unset(Function exp) {
        desallocate(exp);
    }
    
    /**
     * Level of current function call in the stack Also level of filter for/let
     */
    void pushLevel() {
        currentLevel = varList.size();
        level.add(currentLevel);
    }

    void popLevel() {
        if (!level.isEmpty()) {
            level.remove(level.size() - 1);
        }
        if (level.isEmpty()) {
            currentLevel = 0;
        } else {
            currentLevel = level.get(level.size() - 1);
        }
    }

    void pop() {
        varList.remove(varList.size() - 1);
        valList.remove(valList.size() - 1);
    }
    
    public int getLevelSize() {
        return level.size();
    }
    
    public int getVariableSize() {
        return varList.size();
    }
    
    
    /**
     * catch exception: reset stack in previous state
     */
    public void pop(int varSize, int levelSize) {
        while (varList.size() > varSize) {
            pop();
        }
        while (level.size() > levelSize) {
            popLevel();
        }
    }

    /**
     * Allocate block in the stack for local variables of exp
     */
    public void allocate(Expr exp) {
        pushLevel();
        // allocate block for fun
        for (int i = 0; i < exp.getNbVariable(); i++) {
            varList.add(null);
            valList.add(null);
        }
    }

    /**
     * Desallocate block in the stack for local variables of exp
     */
    public void desallocate(Expr exp) {
        for (int i = 0; i < exp.getNbVariable(); i++) {
            pop();
        }
        popLevel();
    }

    /**
     * Function call
     */
    public void set(Expr exp, List<Expr> lvar, IDatatype[] value) {
        count++;
        pushLevel();
        int i = 0;
        for (Expr var : lvar) {
            push(var, value[i++]);
        } 
        // allocate additional variables:
        for (int j = i; j<exp.getNbVariable(); j++){
            push(null, null);
        }
    }

    void push(Expr var, IDatatype val) {
        varList.add(var);
        valList.add(val); 
    }
    
    void set(Expr var, IDatatype val) {
        int index = getIndex(var);
        varList.set(index, var);
        valList.set(index, val);
    }

    //@Override
    public void unset(Expr exp, List<Expr> lvar) {
        desallocate(exp);
    }

    void unset(Expr var) {
        valList.set(getIndex(var), null);
    }

    /**
     * Get variable value within current function call binding environment
     * between top of stack and level
     */
    @Override
    public IDatatype get(Expr var) {
        return getBasic(var, true);
    }
        
    public IDatatype getBasic(Expr var, boolean withStatic) {
        switch (var.getIndex()) {
            case UNBOUND: {
                if (isDynamicCapture()) {
                    for (int i = varList.size() - 1; i >= 0; i--) {
                        Expr vv = varList.get(i);
                        if (vv != null && vv.isDynamic() && vv.equals(var)) {
                            return valList.get(i);
                        }
                    }
                }

                IDatatype dt = (withStatic)?getGlobalVariable(var.getLabel()):getBasicGlobalVariable(var.getLabel());
                if (dt == null) {

                    if (isDebug()) {
                        logger.warn("Variable unbound: " + var);
                        System.out.println(this);
                    }
                }
                return dt;
            }
            default:
                IDatatype dt = valList.get(getIndex(var));
                if (dt == null && isDebug()) {
                    logger.warn("Variable unbound: " + var);
                }
                return dt;
        }
    }

    void trace(Expr exp, IDatatype[] value) {
        System.out.print(pretty(count) + " " + pretty(level.size()) + " ");
        System.out.print((exp.getFunction() == null) ? exp.getDefinition() : exp.getFunction() + " ");
        for (IDatatype dt : value) {
            System.out.print(" " + dt);
        }
        System.out.println();
    }

    String pretty(int n) {
        return "(" + ((n < 10) ? ("0" + n) : n) + ")";
    }

    void trace(Expr exp, IDatatype value) {
        IDatatype[] aa = new IDatatype[1];
        aa[0] = value;
        trace(exp, aa);
    }

    // todo:  why not level ???
    @Override
    public boolean isBound(String label) {
        for (int i = index(); i >= 0; i--) {
            if (varList.get(i).getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    int getLevel() {
        return level.get(level.size() - 1);
    }

    public int getCurrentLevel() {
        return (level.isEmpty()) ? 0 : getLevel();
    }
    
    public int getCurrentVariableLevel() {
        return varList.isEmpty() ? 0 : varList.size() - 1;
    }
    
    // global variable
    public IDatatype getVariable(String name) {
        return getGlobalVariableValues().get(name);
    }
    
    // global variable + static global variable
    @Override
    public IDatatype getGlobalVariable(String name) {
        IDatatype dt = getBasicGlobalVariable(name);
        if (dt == null) {
            return getStaticVariable(name);
        }
        return dt;
    }
    
    public IDatatype getBasicGlobalVariable(String name) {
        return getGlobalVariableValues().get(name);
    }
    
    public IDatatype popBasicGlobalVariable(String name) {
        IDatatype dt = getGlobalVariableValues().get(name);
        setGlobalVariable(name, null);
        return dt;
    }
    
    public IDatatype getGlobalVariableBasic(String name) {
        return getGlobalVariableValues().get(name);
    } 
    
    public Binding setVariable(String name, IDatatype val) {
        return bind(new VariableLocal(name), val);
    }
    
    public Binding setGlobalVariable(String name, IDatatype val) {        
        return bind(new VariableLocal(name(name)), val);
    }
    
    static String name(String name) {
        if (name.startsWith("?")) {
            return name;
        }
        return "?" + name;
    }
    
    public static Binding setStaticVariable(String name, IDatatype val) {        
        return getSingleton().setVariable(name(name), val);
    }
    
    public static IDatatype getStaticVariable(String name) {
        return getSingleton().getVariable(name);
    }
    
    public boolean hasVariable() {
        return ! getGlobalVariableValues().isEmpty();
    }
    
    // must be LocalVariable, i.e. LDScript Variable
    public Binding bind(Variable var, IDatatype val) {
        bind(null, var, val);
        return this;
    }

    /**
     * set(?x = exp) ?x is already bound, assign variable
     */
    public void bind(Expr exp, Variable var, IDatatype val) {
        switch (var.subtype()) {
            // global means SPARQL variable
            // local  means LDScript variable
            case ExprType.GLOBAL:
                break;
            default:
                switch (var.getIndex()) {
                    case UNBOUND:
                        
                        if (isDynamicCapture()) {
                            for (int i = varList.size() - 1; i >= 0; i--) {
                                Expr vv = varList.get(i);
                                if (vv != null && vv.isDynamic() && vv.equals(var)) {
                                     valList.set(i, val);
                                     return;
                                }
                            }
                        }
                        
                        // global variable
                        define(var, val);
                        break;
                    default:
                        valList.set(getIndex(var), val);
                }
        }
    }
    
    public void unbind(Expr exp, Variable var) {
        bind(exp, var, null);
    }
    
    void define(Variable var, IDatatype val) {
        getGlobalVariableValues().put(var.getLabel(), val);
        getGlobalVariableNames().put(var.getLabel(), var);
    }

    @Override
    public List<Expr> getVariables() {
        if (level.size() > 0) {
            // funcall: return variables of this funcall (including let var)
            return getVar();
        } else {
            // let variables
            return getLetVar();
        }
    }
    
    List<Expr> getLetVar() {
        ArrayList<Expr> list = new ArrayList();
        list.addAll(varList);
        addGlobalVariables(list);
        return list;
    }

    /**
     * Funcall has bound variables from level to top of stack Return these
     * variables (may be empty if function has no arg)
     *
     * @return
     */
    List<Expr> getVar() {
        int start = getLevel();
        int top = varList.size();
        ArrayList<Expr> list = new ArrayList();
        for (int i = start; i < top; i++) {
            if (varList.get(i) != null && valList.get(i) != null) {
                list.add(varList.get(i));
            }
        }
        addGlobalVariables(list);
        return list;
    }
    
    void addGlobalVariables(List<Expr> list) {
        for (Variable var : getGlobalVariableNames().values()) {
            if (!list.contains(var)) {
                list.add(var);
            }
        }
    }
    
    @Override
    public void share(Binder b) {
        share((Binding) b);
    }
    
    public void share(Binding b) {
        shareGlobalVariable(b);
        shareContext(b);
    }
    
    public void share(Binding b, Context c) {
       if (b!=null) {
           share(b);
       }
       if (c!=null) {
           share(c);
       }
    }
    
    void shareContext(Binding b) {
        setDebug(b.isDebug());
        setAccessLevel(b.getAccessLevel());
        setAccessRight(b.getAccessRight());
        if (b.getLog()!=null) {
            setLog(b.getLog());
        }
        if (b.getContext()!=null) {
            setContext(b.getContext());
        }
        if (b.getMappings() != null) {
            setMappings(b.getMappings());
        }
        if (b.getReport()!=null) {
            setReport(b.getReport());
        }
        if (b.getShare()!=null) {
            setShare(b.getShare());
        }
    }
    
    // use case: env inherit Log/Context from xt:sparql()
    // PluginImpl sparql() kgram()
    public void subShare(Binding b) {
        if (b != null) {
            if (getLog() == null) {
                setLog(b.getLog());
            }
            if (getContext() == null) {
                setContext(b.getContext());
            }
        }
    }
    
    void shareGlobalVariable(Binding b) {
        setGlobalVariableNames(b.getGlobalVariableNames());
        setGlobalVariableValues(b.getGlobalVariableValues());
    }

    public Optional<IDatatype> getNowValue() {
        return getShare().getSavNowValue();
    }

    public void setNowValue(IDatatype nowValue) {
        getShare().setNowValue(nowValue);
    }
    
    public HashMap<String, Variable> getGlobalVariableNames() {
        return globalVariable;
    }
    
    public HashMap<String, IDatatype> getGlobalVariableValues() {
        return globalValue;
    }
      
    void setGlobalVariableNames(HashMap<String, Variable> m) {
        globalVariable = m;
    }
    
    void setGlobalVariableValues(HashMap<String, IDatatype> m) {
        globalValue = m;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getCount() {
        return count;
    }

//    @Override
//    public void bind(Expr exp, Expr var, Node val) {
//        bind(exp, var, (IDatatype) val);
//    }

//    @Override
//    public void set(Expr exp, Expr var, Node val) {
//        set(exp, var, (IDatatype) val);
//    }
//
//    @Override
//    public void set(Expr exp, List<Expr> lvar, Node[] value) {
//        set(exp, lvar, (IDatatype[]) value);
//    }

//    @Override
//    public void unset(Expr exp, Expr var, Node value) {
//        unset(exp, var, (IDatatype) value);
//    }

    /**
     * return(dt) LDScript function set boolean result field to true
     *
     * @param dt
     * @return
     */
    public IDatatype result(IDatatype dt) {
        result = true;
        return dt;
    }

    /**
     * sequence and for loop LDScript statements check whether intermediate
     * evaluation is a return(dt); if true, evaluation resumes
     *
     * @return the result
     */
    public boolean isResult() {
        return result;
    }

    /**
     * LDScript function call return dt and set boolean result field to false
     *
     * @param dt
     * @return
     */
    public IDatatype resultValue(IDatatype dt) {
        result = false;
        return dt;
    }
    
    @Override
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    @Override
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    
    public boolean isDynamicCapture() {
        return dynamicCapture;
    }

    
    public void setDynamicCapture(boolean dynamicCapture) {
        this.dynamicCapture = dynamicCapture;
    }

    
    public boolean isCoalesce() {
        return coalesce;
    }

   
    public void setCoalesce(boolean coalesce) {
        this.coalesce = coalesce;
    }

   
    public static Binding getSingleton() {
        return singleton;
    }

    
    public static void setSingleton(Binding aSingleton) {
        singleton = aSingleton;
    }

    
    public Access.Level getAccessLevel() {
        return accessLevel;
    }

    
    public void setAccessLevel(Access.Level accessLevel) {
        this.accessLevel = accessLevel;
    }

    
    synchronized public ContextLog getLog() {
        return contextLog;
    }
    
    synchronized public ContextLog getCreateLog() {
        if (getLog() == null) {
            setLog(new ContextLog());
        }
        return getLog();
    }

   
    public void setLog(ContextLog context) {
        this.contextLog = context;
    }
    
    @Override
    public StringBuilder getTrace() {
        return getCreateLog().getCreateTrace();
    }

    
    public IDatatype getDatatypeValue() {
        return datatypeValue;
    }

    
    public void setDatatypeValue(IDatatype datatypeValue) {
        this.datatypeValue = datatypeValue;
    }
    
   
    public AccessRight getAccessRight() {
        return accessRight;
    }

    
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }
    
    
    public static int getDefaultValue(String name, int value) {
        IDatatype dt = getStaticVariable(name);
        return (dt == null) ? value: dt.intValue();
    }
    
    public Context getCreateContext() {
        if (getContext() == null) {
            setContext(new Context());
        }
        return getContext();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    
    public void set(Context c) {
        share(c);
    }
    
    public void share(Context c) {
        setContext(c);
        setAccessLevel(c.getLevel());
        if (c.getAccessRight() != null) {
            setAccessRight(c.getAccessRight());
        }
    }

    @Override
    public Mappings getMappings() {
        return mappings;
    }

    @Override
    public Binding setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }
    
    
    /**
     * ProcessVisitorDefault call 
     * Generate evaluation report for optional/minus/union graph/service
     */
    public void visit(Exp exp, Node g, Mappings m1, Mappings m2) {
        IDatatype dt = getReport(exp); 
        dt.set(NUMBER, exp.getNum());
        dt.set(EXP, exp.toString());
        dt.set(MAP1,  DatatypeMap.createObject(m1));
        if (m2!=null){
            dt.set(MAP2,  DatatypeMap.createObject(m2));
        }
        if (g!=null){
            dt.set(URI,  g.getDatatypeValue());
        }
    }
    
    /**
     * share same report for every call on same named graph pattern or service exp
     * exp report contains list(rep_1  rep_n) one rep_i for each uri
     */
    IDatatype getReport(Exp exp) {
        IDatatype dt  = DatatypeMap.newServiceReport();
        
        if (exp.isGraph() || exp.isService()) {
            // report -> rep(exp).list = (rep_1 .. rep_n)
            for (IDatatype pair : getReport()) {
                IDatatype rep = pair.get(1);
                
                if (rep.get(NUMBER).intValue() == exp.getNum()) {
                    rep.get(LIST).getList().add(dt);
                    return dt;
                }
            }
            // create new report rep for exp and report list in rep
            IDatatype rep  = DatatypeMap.newServiceReport();
            IDatatype list = DatatypeMap.newList(dt);
            getReport().set(STMT + getReport().size(), rep);
            rep.set(NUMBER, exp.getNum());
            rep.set(LIST, list);
        } else {
            getReport().set(STMT + getReport().size(), dt);
        }
        
        return dt;
    }

    public IDatatype getReport() {
        return report;
    }

    public void setReport(IDatatype report) {
        this.report = report;
    }

    public Object getTransformerVisitor() {
        return getShare().getTransformerVisitor();
    }

    public void setTransformerVisitor(Object transformerVisitor) {
        getShare().setTransformerVisitor(transformerVisitor);
    }

    public Share getShare() {
        return share;
    }

    public void setShare(Share share) {
        this.share = share;
    }
    
    // service parameter
    public void init(HashMapList<String> map) {
        IDatatype dt = DatatypeMap.cast(map);
        setGlobalVariable(SERVICE_PARAM, dt);
    }
    
}
