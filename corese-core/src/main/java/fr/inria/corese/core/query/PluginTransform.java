package fr.inria.corese.core.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.transform.DefaultVisitor;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.PointerType.GRAPH;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.ComputerProxy;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer LDScript functions.
 */
public class PluginTransform implements ComputerProxy {

    private static final String FORMAT_LIB = "/webapp/data/format/";
    static Logger logger = LoggerFactory.getLogger(PluginTransform.class);

    PluginImpl plugin;

    public PluginTransform() {
    }

    PluginTransform(PluginImpl p) {
        plugin = p;
    }

    /**
     * Return or create current transformer (create in case of different graph)
     */
    @Override
    public Transformer getTransformer(Binding b, Environment env, Producer p) throws EngineException {
        return getTransformer(b, env, p, null, (IDatatype) null, null);
    }

    @Override
    public Transformer getTransformer(Binding b, Environment env, Producer prod, Expr exp, IDatatype uri, IDatatype dtgname) throws EngineException {
        return getTransformer(b, env, prod, uri, dtgname, false, isWith(exp));
    }

    @Override
    public GraphProcessor getGraphProcessor() {
        return plugin;
    }

    @Override
    public TemplateVisitor getVisitor(Binding b, Environment env, Producer p) {        
        return getVisitorNew(b, env, p);
    }
           
    // TemplateVisitor is shared among every Binding of every subtransformation
    public TemplateVisitor getVisitorNew(Binding b, Environment env, Producer p) {        
        TemplateVisitor vis = (TemplateVisitor) b.getTransformerVisitor();
        if (vis == null) {            
            vis = new DefaultVisitor((Graph) p.getGraph());
            b.setTransformerVisitor(vis);
        }        
        return vis;
    }

    /**
     * Transformation templates share Transformer Context Query and Template
     * alone have own Context
     */
    @Override
    public Context getContext(Binding b, Environment env, Producer p) {
        Context c = getQueryContext(b, env, p);
        if (c == null) {
            try {
                c = getTransformerContext(b, env, p);
            } catch (EngineException ex) {
                c = new Context();
            }
            env.getQuery().setContext(c);
            if (b.getContext() == null) {
                b.setContext(c);
            }
        }
        return c;
    }

    public Context getContext() {
        return getContext(getEnvironment().getBind(), getEnvironment(), getProducer());
    }

    Environment getEnvironment() {
        return plugin.getEnvironment();
    }

    Producer getProducer() {
        return plugin.getProducer();
    }

    Context getQueryContext(Binding b, Environment env, Producer p) {
        Query q = env.getQuery().getGlobalQuery();
        Context c =  q.getContext();
        
        if (c == null && !q.isTransformationTemplate()) {
            //  std Query or Template alone
            
            if (b.getContext() != null) {
                // use case: xt:sparql(query) create Context using st:set
                return b.getContext();
            }
            else {
                c = new Context();
                q.setContext(c);
                b.setContext(c);
            }
        }
        return c;
    }

    /**
     * Context of current Transformer
     */
    Context getTransformerContext(Binding b, Environment env, Producer p) throws EngineException {
        Transformer t = getTransformerCurrent(b, env, p);
        return t.getContext();
    }

    /**
     * Return current transformer (do not create in case of different graph)
     */
    Transformer getTransformerCurrent(Binding b, Environment env, Producer p) throws EngineException {
        return getTransformer(b, env, p, (IDatatype) null, (IDatatype) null, true, false);
    }

    /**
     * uri: transformation URI gname: named graph If uri == null, get current
     * transformer current = true: return current transformer even if not same
     * graph use case: graph ?shape { st:cget(sh:def, ?name) } TODO: cache for
     * named graph
     */
    Transformer getTransformer(Binding b, Environment env, Producer prod, IDatatype uri, IDatatype dtgname, boolean current, boolean isGraph) 
            throws EngineException {
        try {
            Query q = env.getQuery();
            ASTQuery ast =  q.getAST();
            String transform = getTrans(uri);
            Transformer t = (Transformer) q.getTransformer(transform);

            if (transform == null && t != null) {
                transform = t.getTransformation();
            }

            if (dtgname != null) {
                // transform named graph
                if (dtgname.isPointer() && dtgname.pointerType() == GRAPH) {
                    // dtgname contains a Graph
                    // use case: let (?g = construct {} where {}){ 
                    // st:apply-templates-with-graph(st:navlab, ?g) }
                    t = Transformer.createWE((Graph) dtgname.getPointerObject(), transform, b.getAccessLevel());
                    complete(q, t, uri);
                } else {
                    String gname = dtgname.getLabel();
                    t = Transformer.createWE((Graph) prod.getGraph(), transform, gname, isGraph, b.getAccessLevel());
                    complete(q, t, uri);
                }
            } else if (t == null) {
                t = Transformer.createWE(prod, transform, b.getAccessLevel());
                complete(q, t, uri);
                q.setTransformer(transform, t);
            } else if (!current) {
                Graph g = t.getGraph();
                if (g != prod.getGraph()) {
                    // Transformer exist but with another graph
                    // create a new one
                    t = Transformer.createWE(prod, transform, b.getAccessLevel());
                    complete(q, t, uri);
                }
            }
            return t;
        } catch (LoadException ex) {
            throw ex.getCreateEngineException();
        }

    }

    String getTrans(IDatatype trans) {
        return getLabel(trans);
    }

    String getLabel(IDatatype dt) {
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    boolean isWith(Expr exp) {
        return (exp == null) ? true
                : exp.oper() == ExprType.APPLY_TEMPLATES_GRAPH
                || exp.oper() == ExprType.APPLY_TEMPLATES_WITH_GRAPH;
    }

    void complete(Query q, Transformer t, IDatatype uri) {
        t.complete(q, (Transformer) q.getTransformer());
        if (uri != null) {
            t.getContext().set(Transformer.STL_TRANSFORM, uri);
        }
    }

    @Override
    public NSManager getNSM(Binding b, Environment env, Producer prod) {
        Transformer p;
        try {
            p = getTransformer(b, env, prod);
            return p.getNSM();
        } catch (EngineException ex) {
            logger.error("getTransformer fails in getNSM");
            return NSManager.create();
        }
    }

    public IDatatype format(IDatatype... par) {
        String f = getFormat(par[0]);
        Object[] arr = new Object[par.length - 1];
        for (int i = 0; i < arr.length; i++) {
            IDatatype dt = par[i + 1];
            arr[i] = dt.objectValue();
        }
        String res = String.format(f, arr);
        return plugin.getValue(res);
    }

    String getFormat(IDatatype dt) {
        if (dt.isURI()) {
            return getFormatURI(dt.stringValue());
        }
        return dt.stringValue();
    }
    
    String getFormatURI(String uri) {
        try {
            QueryLoad ql = QueryLoad.create();
            if (uri.startsWith(NSManager.STL_FORMAT)) {
                // st:format/loc/myformat.html -> /webapp/format/loc/myformat.html
                String name = FORMAT_LIB + uri.substring(NSManager.STL_FORMAT.length());
                return ql.getResource(name);
            }
            return ql.readProtect(uri);
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

        return "";
    }
}
