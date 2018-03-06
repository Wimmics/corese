package fr.inria.corese.kgraph.approximate.ext;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Option;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.corese.kgram.core.Query;
import static fr.inria.corese.kgraph.approximate.algorithm.Utils.msg;
import fr.inria.corese.kgraph.approximate.algorithm.Parameters;
import static fr.inria.corese.kgraph.approximate.ext.ASTRewriter.S;
import fr.inria.corese.kgraph.approximate.strategy.ApproximateStrategy;
import fr.inria.corese.kgraph.approximate.strategy.StrategyType;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.CLASS_HIERARCHY;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.URI_EQUALITY;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.LITERAL_LEX;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.PROPERTY_EQUALITY;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.URI_LEX;
import static fr.inria.corese.kgraph.approximate.strategy.StrategyType.URI_WN;
import fr.inria.corese.kgraph.logic.OWL;
import fr.inria.corese.kgraph.logic.RDF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * AST rewriting: according to different rules, modify the original SPARQL by
 * adding triple patterns/filters, etc
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 oct. 2015
 */
public class ASTRewriter implements QueryVisitor {

    final static int S = 1, P = 2, O = 3;
    private final static String VAR = "?_var_";
    public final static String APPROXIMATE = Processor.APPROXIMATE;
    private int countVar = 0;
    private ASTQuery ast;
    ApproximateStrategy strategy;
    boolean relaxProperty = !true,
            relaxURI      = !true,
            relaxLiteral  = true;

    @Override
    public void visit(Query query) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void visit(ASTQuery ast) {
        if (!ast.isRelax()) {
            return;
        }
        this.ast = ast;
        init();     
        strategy = new ApproximateStrategy();
        this.initOptions(ast);
        visit(ast.getBody());
    }
    
    // @relax kg:uri_literal_property
    void init(){
        List<String> list = ast.getMetadata().getValues(Metadata.RELAX);
        if (list == null){
            return;
        }  
        if (! list.isEmpty()){
            // let user decide about literal
           relaxLiteral = !true; 
        }
        for (String str : list){
            String name = str.toLowerCase();
            if (name.equals(Metadata.RELAX_URI)  || name.contains("*")){
                relaxURI        = true;
            }
            if (name.equals(Metadata.RELAX_PROPERTY) || name.contains("*")){
                relaxProperty   = true;
            }
            if (name.equals(Metadata.RELAX_LITERAL) || name.contains("*")){
                relaxLiteral    = true;
            } 
        }
    }

    private void visit(Exp exp) {
        List<Exp> exTemp = new ArrayList<Exp>();
        exTemp.addAll(exp.getBody());

        for (Exp e : exTemp) {
            if (e.isTriple()){
                if (! e.isFilter()) {
                    process(exp, e.getTriple());
                }
            }
            else for (Exp ee : e) {
                    visit(ee);
            }
        }
    }

    private void process(Exp exp, Triple t) {
        //for(Exp exp: ast.gets)
        msg("------ BEFORE -----\n" + exp, false);

        //1 pre process, choose strategies for each atom
        Map<Integer, TripleWrapper> map = new HashMap<Integer, TripleWrapper>();

        init(t, t.getSubject(), S, map);
        init(t, t.getPredicate(), P, map);
        init(t, t.getObject(), O, map);

        msg("\n------ pre-process lsit-----");
        for (TripleWrapper value : map.values()) {
            msg(value.toString());
        }

        //2 rewrite triples in AST
        List<Triple> filters = new ArrayList<Triple>();
        List<Option> options = new ArrayList<Option>();

        rewrite(map.get(S), filters, options);
        if (relaxProperty){
            rewrite(map.get(P), filters, options);
        }
        rewrite(map.get(O), filters, options);

        for (Triple filter : filters) {
            exp.add(filter);
        }

        for (Option option : options) {
            exp.add(option);
        }

        msg("\n------ AFTER -----\n" + exp, false);
    }

    //choose the Strategy for the URI and put them into a list
    private void init(Triple triple, Atom atom, int pos, Map<Integer, TripleWrapper> map) {
        if (atom == null) {
            return;
        }

        List<StrategyType> lst = new ArrayList<StrategyType>();
        IDatatype dt = atom.getDatatypeValue();
        
        if (dt.isURI()) {
            if (! relaxURI && pos != P ){
                return;
            }
            //S P O
            add(lst, URI_WN);
            add(lst, URI_LEX);
            add(lst, URI_EQUALITY);
            
            if (pos == P && !atom.getName().equalsIgnoreCase(RDF.TYPE)) { //property does not have rdfs:label & rdfs:comment
                add(lst, PROPERTY_EQUALITY);              
            } 
            else if (pos == O && triple.isType()) {
                    add(lst, CLASS_HIERARCHY);              
            }
        }
        else if (dt.isLiteral() && relaxLiteral){ 
                if (dt.getCode() == IDatatype.STRING ||
                    dt.getCode() == IDatatype.LITERAL ){ //(dt.getDatatypeURI().equals(xsdstring) ) {       
                    add(lst, LITERAL_LEX);

                //@lang=en ??
//                if (atom.getLang() == null || 
//                        (atom.getLang() != null && atom.getLang().equalsIgnoreCase("en"))) {
//                    add(lst, LITERAL_WN);
//                }
            }
        }

        if (!lst.isEmpty()) {
            map.put(pos, new TripleWrapper(triple, pos, lst));
        }
    }

    //approximate the name of URI
    //ex, kg:john, kg:Johnny
    //applicable to: subject, predicate and object
    private void rewrite(TripleWrapper tw, List<Triple> filters, List<Option> options) {
        if (tw == null) {
            return;
        }

        Variable var = new Variable(VAR + countVar++);

        //1. get strategies in group G1 and merge them in one filter
        List<StrategyType> merge = strategy.getMergableStrategies(tw.getStrategies());
        if (!merge.isEmpty()) {
            //2.2 generate filters with functions
            filters.add(createFilter(var, tw.getAtom(), strategy.getAlgorithmString(merge)));
        }

        //2. iterate other strategies
        for (StrategyType st : tw.getStrategies()) {
            if (merge.contains(st)) {
                continue;
            }

            String label;
            Triple t1, t2;
            Option opt = new Option();
            switch (st) {
                case PROPERTY_EQUALITY:
                case URI_EQUALITY:
                    label = (st == URI_EQUALITY) ? OWL.SAMEAS : OWL.EQUIVALENTPROPERTY;
                    //create two addional triple pattern {x eq y}
                    t1 = (ast.createTriple(var, Constant.create(label), tw.getAtom()));
                    t2 = (ast.createTriple(tw.getAtom(), Constant.create(label), var));

                    //the filter can be omitted, because the similarity (equality =1)
                    //create optional {t1, t2}
                    opt.add(BasicGraphPattern.create(t1, t2));
                    options.add(opt);
                    break;
//                case URI_LABEL:
//                case URI_COMMENT:
//                    label = (st == URI_COMMENT) ? RDFS.COMMENT : RDFS.LABEL;
//                    Variable text1 = variable(false);
//                    Variable text2 = variable(false);
//
//                    //create two addional triple pattern: {x rdfs:label y} or {x rdfs:comment y}
//                    t1 = ast.createTriple(var, Constant.create(label), text1);
//                    t2 = ast.createTriple(tw.getAtom(), Constant.create(label), text2);
//                    BasicGraphPattern bgp = BasicGraphPattern.create(t1, t2);
//
//                    //create a filter
//                    bgp.add(Triple.create(ast.createOperator("!=", var, tw.getAtom())));
//                    Exp filter = filter(text1, text2, ApproximateStrategy.getAlgrithmString(st), var, tw.getAtom());
//                    bgp.add(filter);
//                    opt.add(bgp);
//                    ast.getBody().add(opt);
//                    break;
            }
        }

        //2.3 replace uri with vairable
        tw.setAtom(var);
    }

    //add a filter with a specific function and parameters
    private Triple createFilter(Variable var, Atom atom, String algs) {
        Term function = Term.function(APPROXIMATE);
        function.add(var);
        function.add(atom);
        function.add(Constant.createString(algs)); //, qrdfsLiteral));
        function.add(Constant.create(Parameters.THRESHOLD));
        return Triple.create(function);
    }

    private void initOptions(ASTQuery ast) {
        strategy.init(ast);
        Parameters.init(ast);
    }

    private void add(List<StrategyType> list, StrategyType st) {
        if (strategy.check(st)) {
            list.add(st);
        }
    }
}
