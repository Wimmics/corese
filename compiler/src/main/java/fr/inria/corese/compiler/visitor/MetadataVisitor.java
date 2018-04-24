package fr.inria.corese.compiler.visitor;

import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Binding;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata Visitor
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MetadataVisitor implements QueryVisitor {
    
    private static final String META_VARIABLE   = "?meta";
    private static final String META_LIST       = "munc:metaList";
    private static final String UNCERTAINTY     = "munc:hasUncertainty";
    private static final String VALUE           = "xt:value";
    private static final String NAME            = "xt:name";
    
    ASTQuery ast;
    int count = 0;
    
    public MetadataVisitor() {}

    @Override
    public void visit(ASTQuery ast) {
        init(ast);
        process(null, ast.getBody());
    }

    @Override
    public void visit(Query query) {
        
    }
    
    void init(ASTQuery ast) {
         this.ast = ast;
         if (ast.getNSM().getNamespace("munc") == null) {
             ast.defNSNamespace("munc", "http://ns.inria.fr/metauncertainty/v1/");
         }
    }
    
    /**
     *  prefix munc:   <http://ns.inria.fr/metauncertainty/v1>

    select ?g ?s ?p ?o ?meta where {
      graph ?g {triple(?s ?p ?o ?Tm)}
      triple(?g munc:hasUncertainty [] ?Gm)
      bind(us:metaList(?Tm,?Gm) as ?meta)
    }

    function us:metaList(?Tm,?Gm) {
     */
    
    /**
     * name is a named graph
     */
    void process(Atom name, Exp body) {
        ArrayList<Exp> list = new ArrayList<>();
        
        for (Exp exp : body) {
            if (exp.isNamedGraph()) {
                Source g = exp.getNamedGraph();
                process(g.getSource(), g.getBodyExp());
            } 
            else if (exp.isFilter()) {}
            else if (exp.isTriple()) {
                if (name != null) {                  
                    process(exp.getTriple(), list, name);
                }
            }
            else {
               for (Exp ee : exp) {
                   process(name, ee);
               } 
            }
        }
        
        for (Exp exp : list) {
            body.add(exp);
        }       
    }
    
    /**
      graph ?g {triple(?s ?p ?o ?Tm)}
      * ->
      bind(us:metaList(?Tm, xt:value(xt:name(), munc:hasUncertainty, 2)) as ?meta)
      * 
      * xt:name() -> current named graph, that is ?g
      * xt:value get the value of subject property
      * 2 is the node index of metadata value (1 is node index of object value)
     */
    void process(Triple t, List<Exp> list, Atom name) {
        if (t.getArgs() != null && !t.getArgs().isEmpty()) {
            Variable tmeta = t.getArgs().get(0).getVariable();
            Term gname = ast.createFunction(ast.createQName(NAME));
            Term gmeta = ast.createFunction(ast.createQName(VALUE),
                    gname, ast.createQName(UNCERTAINTY), Constant.create(2));
            Term fun = ast.createFunction(ast.createQName(META_LIST), tmeta, gmeta);
            Variable meta = Variable.create(META_VARIABLE + count++);
            Binding b = Binding.create(fun, meta);
            list.add(b);
        }
    }
   
      

}
