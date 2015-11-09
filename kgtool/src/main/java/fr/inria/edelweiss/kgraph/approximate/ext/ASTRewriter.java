package fr.inria.edelweiss.kgraph.approximate.ext;

import static fr.inria.acacia.corese.cg.datatype.RDF.qrdfsLiteral;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Option;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.approximate.aggregation.ApproximateStrategy;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.ApproximateStrategy.check;
import fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.CLASS_HIERARCHY;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.URI_EQUALITY;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.LITERAL_LEX;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.LITERAL_SS;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.PROPERTY_DR;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.PROPERTY_EQUALITY;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.URI_COMMENT;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.URI_LABEL;
import static fr.inria.edelweiss.kgraph.approximate.aggregation.StrategyType.URI_NAME;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.BaseAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.NLPHelper;
import fr.inria.edelweiss.kgraph.logic.OWL;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * AST rewriting: according to different rules, modify the original SPARQL by adding
 * triple patterns/filters, etc
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 oct. 2015
 */
public class ASTRewriter implements QueryVisitor {

    final static int S = 1, P = 2, O = 3;
    private final static String VAR = "?_var_";
    public final static String MATCH = "approximate";

    private int count = 0;
    private ASTQuery ast;

    @Override
    public void visit(ASTQuery ast) {
        if (!ast.isMore()) {
            return;
        }

        this.ast = ast;
        this.initOptions();

        List<Triple> triplePatterns = new ArrayList<Triple>();
        //1. iterate each query pattern in SPARQL
        for (Exp exp : ast.getBody().getBody()) {
            if (exp.isTriple() && !exp.isFilter()) {
                triplePatterns.add(exp.getTriple());
            }
        }

        System.out.println("------ BEFORE -----\n" + ast);

        //1 pre process, choose strategies for each atom
        List<TripleWrapper> ss = this.process(triplePatterns);

        System.out.println("\n\n------ pre-process lsit-----");
        for (TripleWrapper s : ss) {
            System.out.println(s);
        }

        //2 rewrite triples in AST
        for (TripleWrapper s : ss) {
            this.rewrite(s);
        }

        System.out.println("\n\n------ AFTER -----\n" + ast);

        System.out.println("\n\n------ SIMILARITY MEASUREMENT -----\n" + ast);
    }

    @Override
    public void visit(Query query) {
        //throw new UnsupportedOperationException("Not supported yet."); 
    }

    //generate the list of algorithms that are applicable
    //take into account the user pre-defined algorithms
    private List<TripleWrapper> process(List<Triple> triplePatterns) {
        List<TripleWrapper> ltw = new LinkedList<TripleWrapper>();
        for (Triple triple : triplePatterns) {
            process(triple, triple.getSubject(), S, ltw);
            process(triple, triple.getPredicate(), P, ltw);
            process(triple, triple.getObject(), O, ltw);
        }
        return ltw;
    }

    //choose the Strategy for the URI and put them into a list
    private void process(Triple triple, Atom atom, int pos, List<TripleWrapper> atoms) {
        if (atom == null) {
            return;
        }

        List<StrategyType> lst = new ArrayList<StrategyType>();

        //*** URI ***
        if (atom.isConstant() && !atom.isLiteral()) {

            //S P O
            add(lst, URI_NAME);
            add(lst, URI_EQUALITY);

            //P
            if (pos == P && !atom.getName().equalsIgnoreCase(RDF.TYPE)) { //propery does not have rdfs:label & rdfs:comment
                add(lst, PROPERTY_EQUALITY);
                add(lst, PROPERTY_DR);

                //S&O
            } else {//property owl:equivalentProperty

                add(lst, URI_LABEL);
                add(lst, URI_COMMENT);

                //pattern A rdf:type B
                //to test
                if (pos == O && triple.getPredicate().getName().equalsIgnoreCase(RDF.TYPE)) {
                    add(lst, CLASS_HIERARCHY);
                }
            }
        }

        //*** LITERAL ***
        //datatype == xsd:string
        if (atom.isLiteral() && atom.getDatatype().equalsIgnoreCase("xsd:string")) {
            add(lst, LITERAL_LEX);

            //@lang=en ??
            add(lst, LITERAL_SS);
        }

        if (!lst.isEmpty()) {
            atoms.add(new TripleWrapper(triple, pos, lst));
        }
    }

    //approximate the name of URI
    //ex, kg:john, kg:Johnny
    //applicable to: subject, predicate and object
    public void rewrite(TripleWrapper tw) {
        Variable var = variable();

        //1. get strategies in group G1 and merge them in one filter
        List<StrategyType> merge = ApproximateStrategy.getMergableStrategies(tw.getStrategies());
        if (!merge.isEmpty()) {
            //2.2 generate filters with functions
            ast.getBody().add(filter(var, tw.getAtom(), ApproximateStrategy.getAlgrithmString(merge)));
        }

        //2. iterate other strategies
        for (StrategyType st : tw.getStrategies()) {
            if (merge.contains(st)) {
                continue;
            }

            String label;
            Triple t1, t2;
            Option opt = Option.create(BasicGraphPattern.create());
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
                    ast.getBody().add(opt);
                    break;
                case URI_LABEL:
                case URI_COMMENT:
                    label = (st == URI_COMMENT) ? RDFS.COMMENT : RDFS.LABEL;
                    Variable comment1 = variable();
                    Variable comment2 = variable();

                    //create two addional triple pattern: {x rdfs:label y} or {x rdfs:comment y}
                    t1 = ast.createTriple(var, Constant.create(label), comment1);
                    t2 = ast.createTriple(tw.getAtom(), Constant.create(label), comment2);
                    BasicGraphPattern bgp = BasicGraphPattern.create(t1, t2);

                    //create a filter
                    Exp filter = filter(comment1, comment2, ApproximateStrategy.getAlgrithmString(st), var, tw.getAtom());
                    //optional {t1. t2. filter}
                    opt.add(BasicGraphPattern.create(bgp, filter));
                    ast.getBody().add(opt);
                    break;
            }
        }

        //2.3 replace uri with vairable
        tw.setAtom(var);
    }

    //add a filter with a specific function and parameters
    private Triple filter(Variable var, Atom atom, String algs, Expression ... more) {
        Term function = ast.createFunction(ast.createQName(MATCH));
        function.add(var);
        function.add(atom);
        function.add(Constant.create(algs, qrdfsLiteral));
        if(more!=null && more.length>0){
            for (Expression para : more) {
                function.add(para);
            }
        }
        return Triple.create(function);
    }

    private void initOptions() {
        // ** option: strategies used **
        List<String> strategyOption = ast.getApproximateSearchOptions(Pragma.STRATEGY);//get option kg:strategy from Pragma
        // ** option: strategy priortiy **
        List<String> priorityStrategyOption = ast.getApproximateSearchOptions(Pragma.PRIORITY_STRATEGY);//get priority kg:priority_s from pragma
        // ** option: algorithms used **
        List<String> algorithmOption = ast.getApproximateSearchOptions(Pragma.ALGORITHM);
        // ** option: algorithm priortiy **
        List<String> priorityAlgorithmOption = ast.getApproximateSearchOptions(Pragma.PRIORITY_ALGORITHM);

        ApproximateStrategy.init(strategyOption, priorityStrategyOption, algorithmOption, priorityAlgorithmOption);

        //WordNet and POS tagger
        //WordNet dict path
        List<String> wnp = ast.getApproximateSearchOptions(Pragma.WN_PATH);
        if (wnp != null && !wnp.isEmpty()) {
            NLPHelper.WN_PATH = wnp.get(0);
        }

        //wordnet version
        List<String> wnv = ast.getApproximateSearchOptions(Pragma.WN_VERSION);
        if (wnv != null && !wnv.isEmpty()) {
            NLPHelper.WN_VER = wnv.get(0);
        }

        //pos tagger
        List<String> pos = ast.getApproximateSearchOptions(Pragma.POS_TAGGER);
        if (pos != null && !pos.isEmpty()) {
            NLPHelper.POS_TAGGER = pos.get(0);
        }

        //String metric
        List<String> metric = ast.getApproximateSearchOptions(Pragma.STRING_METRIC);
        if (metric != null && !metric.isEmpty()) {
            NLPHelper.STRING_METRIC = metric.get(0);
        }

        //String metric
        List<String> threshold = ast.getApproximateSearchOptions(Pragma.THRESHOLD);
        if (threshold != null && !threshold.isEmpty()) {
            BaseAlgorithm.THRESHOLD = Double.valueOf(threshold.get(0));
        }
    }

    private void add(List<StrategyType> list, StrategyType st) {
        if (check(st)) {
            list.add(st);
        }
    }

    private Variable variable() {
        return new Variable(VAR + count++);
    }
}
