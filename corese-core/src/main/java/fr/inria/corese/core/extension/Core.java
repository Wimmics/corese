package fr.inria.corese.core.extension;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.PluginImpl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.core.FunctionEvaluator;
import fr.inria.corese.sparql.triple.function.extension.IOFunction;
import fr.inria.corese.sparql.triple.function.proxy.GraphSpecificFunction;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Super class for Java extension function class 
 * public class Myclass extends Core 
 * Extension function called with prefix:
 * function://fr.inria.corese.core.extension.Myclass
 * See sparql.triple.function.core.Extern
 * 
 * Use case: JavaCompiler compiles SHACL Interpreter
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017-2019
 */
public class Core extends PluginImpl implements FunctionEvaluator {

    private static final String MSH = "http://ns.inria.fr/shacl/";
    static Class<IDatatype>[][] signature;
    static HashMap<String, String> prefix;
    HashMap <String, String> functionName;

    static {
        init();
    }

    static void init() {
        defSignature();
        defNamespace();
    }
    
    public Core() {
        functionName = new HashMap<>();
        defFunction();
    }
    
    static HashMap<String, String> getPrefix() {
        return prefix;
    }
    
    static void define(String pref, String ns) {
        prefix.put(pref, ns);
    }
    
    /**
     * Define prefix namespace for funcall(sh:fun)
     * because functions are defined as sh_fun()
     */
    static void defNamespace() {
        prefix = new HashMap<>();
        define("sh", NSManager.SHAPE);
        define("msh", MSH);
    }
    
    static void defSignature() {
        signature = new Class[20][];
        for (int i = 0; i < signature.length; i++) {
            Class[] sig = new Class[i];
            Arrays.fill(sig, IDatatype.class);
            signature[i] = sig;
        }
    }
    
    void defFunction() {
        functionName.put(NSManager.EXT+"member", "member");
    }

    @Override
    public void setProducer(Producer producer) {
        super.setProducer(producer);
    }
    
    @Override
    public void setEnvironment(Environment env) {
        super.setEnvironment(env);
    }
    
    
    
    
    
    

    /**
     * Mappings map Return value of var in first Mapping Use case: bind (exists
     * { } as ?b)
     */
    IDatatype mapget(IDatatype m, IDatatype var) {
        if (m.pointerType() != PointerType.MAPPINGS) {
            return null;
        }
        Mappings map = m.getPointerObject().getMappings();
        if (map.size() == 0) {
            return null;
        }
        return (IDatatype) map.getValue(var.getLabel());
    }

    IDatatype xt_print(IDatatype... dt) {
        for (IDatatype val : dt) {
            System.out.print(dt);
            System.out.println(" ");
        }
        System.out.println();
        return TRUE;
    }

    IDatatype xt_load(IDatatype... dt) {
        return new GraphSpecificFunction("load").load(this, dt);
    }

    IDatatype xt_graph() {
        return DatatypeMap.createObject(getGraph());
    }

    IDatatype xt_value(IDatatype g, IDatatype s, IDatatype p) {
        return value(null, getProducer(), g, s, p, 1);
    }

    IDatatype xt_value(IDatatype s, IDatatype p) {
        return value(null, getProducer(), null, s, p, 1);
    }

    IDatatype xt_objects(IDatatype s, IDatatype p) {
        return enumerate(s, p, null, 1);
    }

    IDatatype xt_subjects(IDatatype p, IDatatype o) {
        return enumerate(null, p, o, 0);
    }

    IDatatype enumerate(IDatatype s, IDatatype p, IDatatype o, int n) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (IDatatype dt : edge(null, getProducer(), s, p, o)) {
            Edge edge = dt.getPointerObject().getEdge();
            list.add((IDatatype) edge.getNode(n).getDatatypeValue());
        }
        return DatatypeMap.newList(list);
    }

    IDatatype xt_insert(IDatatype... ldt) {
        return insert(null, getProducer(), ldt);
    }

    // PRAGMA: it does not evaluate e in g
    // hence focus does not work when compiling LDS in Java
    IDatatype xt_focus(IDatatype g, IDatatype e) {
        return e;
    }

    Graph getGraph(IDatatype dt) {
        return (Graph) dt.getPointerObject();
    }

    IDatatype xt_turtle(IDatatype x) {
        if (x.isLiteral() && x.getDatatypeURI().equals(IDatatype.GRAPH_DATATYPE)) {
            Transformer t = Transformer.create(getGraph(x), Transformer.TURTLE);
            return t.process();
        } else {
            Transformer t = Transformer.create(getGraph(), Transformer.TURTLE);
            return t.process(x);
        }
    }

    IDatatype xt_turtle(IDatatype g, IDatatype x) {
        Transformer t = Transformer.create(getGraph(g), Transformer.TURTLE);
        return t.process(x);
    }
    
    @Override
    public IDatatype strdt(IDatatype dt, IDatatype type) {
        return DatatypeMap.newInstance(dt.getLabel(), type.getLabel());
    }

    void trace(Object... lobj) {
        for (Object obj : lobj) {
            System.out.print(obj);
            System.out.print(" ");
        }
        System.out.println("");
    }
    
    /**
     * funcall(sh:shacl)
     * ->
     * funcall(sh_shacl)
     */
    String javaName(IDatatype fun) {
        String str = fun.getLabel();
        String name = functionName.get(str);
        if (name == null) {
            for (String key : getPrefix().keySet()) {
                String ns = getPrefix().get(key);
                if (str.startsWith(ns)) {
                    name = key.concat("_").concat(str.substring(ns.length()));
                    functionName.put(str, name);
                    return name;
                }
            }
        }
        else {
            return name;
        }
        //functionName.put(str, str);
        return str;
    }
    
    public HashMap<String, String> getFunctionName() {
        return functionName;
    }
    
   public void trace() {
        HashMap<String, String> map = getFunctionName();
        int i = 0;
        for (String key : map.keySet()) {
            System.out.println(i++ + " " + map.get(key));
        }
    }
   
   public IDatatype safe(IDatatype dt) {
       return (dt == null) ? FALSE : TRUE;
   }

   // rq:gt -> gt
   String datatypeName(String name) {
       if (name.startsWith(NSManager.SPARQL)) {
           return name.substring(NSManager.SPARQL.length());
       }
       return null;
   }
   
    @Override
    public IDatatype funcall(IDatatype fun, IDatatype... ldt) {
        String name = datatypeName(fun.getLabel());
        if (ldt.length>0 && name != null) {
            try {
                IDatatype res = funcalldt(name, ldt);
                return res;
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                // continue
            }
        }
        name = javaName(fun);
        try {
            Method m = this.getClass().getMethod(name, signature(ldt.length));
            return (IDatatype) m.invoke(this, ldt);
        } catch (SecurityException e) {

        } catch (InvocationTargetException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
            trace(e, "funcall", name, ldt);
            trace(ldt);
        }
        return null;
    }
    
    /**
     * funcall(rq:gt, x, y) -> x.gt(y)
     */
    public IDatatype funcalldt(String name, IDatatype... ldt) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IDatatype[] param = new IDatatype[ldt.length-1];
        for (int i = 0; i<param.length; i++) {
            param[i] = ldt[i+1];
        }
        Method m = ldt[0].getClass().getMethod(name, signature(param.length));
        return (IDatatype) m.invoke(ldt[0], param);
    }
    
    public IDatatype apply(IDatatype fun, IDatatype dt) {
        return funcall(fun, DatatypeMap.toArray(dt));
    }

    Class[] signature(int n) {
        return signature[n];
    }

    Class[] signature1(int n) {
        Class<IDatatype>[] aclasses = new Class[n];
        Arrays.fill(aclasses, IDatatype.class);
        return aclasses;
    }

    void prepare(IDatatype[] ldt) {
        for (int i = 1; i < ldt.length; i++) {
            // additional list of length one considered as one value
            if (ldt[i] != null && ldt[i].isList() && ldt[i].size() == 1) {
                ldt[i] = ldt[i].get(0);
            }
        }
    }

    @Override
    public IDatatype map(IDatatype fun, IDatatype... ldt) {
        return map(fun, false, ldt);
    }

    public IDatatype mapany(IDatatype fun, IDatatype... ldt) {
        return map(fun, true, ldt);
    }

    public IDatatype map(IDatatype fun, boolean mapany, IDatatype... ldt) {
        String name = javaName(fun);
        try {
            prepare(ldt);
            Method m = this.getClass().getMethod(name, signature(ldt.length));
            IDatatype list = ldt[0];

            for (IDatatype dt : list.getValues()) {
                ldt[0] = dt;
                IDatatype obj = (IDatatype) m.invoke(this, ldt);

                if (mapany) {
                    if (obj != null && obj.booleanValue()) {
                        return TRUE;
                    }
                }
            }
            return (mapany) ? FALSE : TRUE;

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            trace(e, "map", name, ldt);
            trace(ldt);
        }
        return null;
    }

    IDatatype mapfindlist(IDatatype fun, IDatatype... ldt) {
        return maplist(fun, true, ldt);
    }

    @Override
    public IDatatype maplist(IDatatype fun, IDatatype... ldt) {
        return maplist(fun, false, ldt);
    }

    public IDatatype maplist(IDatatype fun, boolean find, IDatatype... ldt) {
        String name = javaName(fun);
        try {
            prepare(ldt);
            Method m = this.getClass().getMethod(name, signature(ldt.length));
            IDatatype list = ldt[0];
            ArrayList<IDatatype> res = new ArrayList<>();

            for (IDatatype dt : list.getValues()) {
                ldt[0] = dt;
                IDatatype obj = (IDatatype) m.invoke(this, ldt);

                if (obj != null) {
                    if (find) {
                        if (obj.booleanValue()) {
                            res.add(dt);
                        }
                    } else {
                        res.add(obj);
                    }
                }
            }

            return DatatypeMap.newInstance(res);

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            trace(e, "maplist", name, ldt);
            trace(ldt);
        }
        return null;
    }

    IDatatype dt_list(IDatatype dt) {
        if (dt.isList()) {
            return dt;
        }
        return DatatypeMap.newList(dt);
    }

    IDatatype xt_strip(IDatatype dt) {
        String str = NSManager.nstrip(dt.getLabel());
        if (str == null) {
            return null;
        }
        if (dt.getLabel().equals(str)) {
            return dt;
        }
        return DatatypeMap.newInstance(str);
    }

    IDatatype xt_validURI(IDatatype dt) {
        return new IOFunction("validURI").validURI(dt);
    }

    IDatatype xt_sparql(IDatatype q, IDatatype... dt) {
        return kgram(q, dt);
    }

    IDatatype xt_replace(IDatatype str, IDatatype x, IDatatype y) {
        return DatatypeMap.newInstance(str.getLabel().replace(x.getLabel(), y.getLabel()));
    }

    IDatatype st_visit(IDatatype... dt) {
        return null;
    }

    IDatatype st_apply_templates_with_graph(IDatatype... dt) {
        return null;
    }

    IDatatype st_turtle(IDatatype dt) {
        return xt_turtle(dt);
    }

    IDatatype rq_strstarts(IDatatype a, IDatatype b) {
        return strstarts(a, b);
    }

}
