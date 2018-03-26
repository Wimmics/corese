package fr.inria.corese.core.approximate.strategy;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.parser.Pragma;
import static fr.inria.corese.core.approximate.strategy.AlgType.ch;
import static fr.inria.corese.core.approximate.strategy.AlgType.eq;
import static fr.inria.corese.core.approximate.strategy.AlgType.jw;
import static fr.inria.corese.core.approximate.strategy.AlgType.ng;
import static fr.inria.corese.core.approximate.strategy.AlgType.wn;
import static fr.inria.corese.core.approximate.strategy.StrategyType.CLASS_HIERARCHY;
import static fr.inria.corese.core.approximate.strategy.StrategyType.LITERAL_LEX;
import static fr.inria.corese.core.approximate.strategy.StrategyType.LITERAL_WN;
import static fr.inria.corese.core.approximate.strategy.StrategyType.PROPERTY_EQUALITY;
import static fr.inria.corese.core.approximate.strategy.StrategyType.URI_LEX;
import static fr.inria.corese.core.approximate.strategy.StrategyType.URI_EQUALITY;
import static fr.inria.corese.core.approximate.strategy.StrategyType.URI_WN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Define the strategies
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 5 oct. 2015
 */
public class ApproximateStrategy {

    private final static Logger logger = LoggerFactory.getLogger(ApproximateStrategy.class);
    public final static String SEPARATOR = "-";
    
    //default strategy-algorithm map
    private final static Map<StrategyType, List<AlgType>> defaultStrategyMap = new EnumMap<StrategyType, List<AlgType>>(StrategyType.class);
    //real strategy-algorithm map applied
    private  Map<StrategyType, List<AlgType>> strategyMap = new EnumMap<StrategyType, List<AlgType>>(StrategyType.class);

    private final static List<StrategyType> mergableStrategy; //the strateiges that can be merged into filter
    private final static List<StrategyType> defaultStrategy;
    private  List<StrategyType> strategyList = null; //strategies to be applied
    private  List<AlgType> algorithmList = null; //algorithm list to be applied

    //private static Priority priority = new Priority();
    static {
        //*** DEFAULT MAP STRATEGY - ALGORITHMS
        defaultStrategyMap.put(URI_LEX, Arrays.asList(new AlgType[]{ng, jw}));//S P O
        defaultStrategyMap.put(URI_WN, Arrays.asList(new AlgType[]{wn}));//S P O
        defaultStrategyMap.put(URI_EQUALITY, Arrays.asList(new AlgType[]{eq}));//S P O

        defaultStrategyMap.put(PROPERTY_EQUALITY, Arrays.asList(new AlgType[]{eq}));//P
        defaultStrategyMap.put(CLASS_HIERARCHY, Arrays.asList(new AlgType[]{ch}));//A rdf:type B

        defaultStrategyMap.put(LITERAL_WN, Arrays.asList(new AlgType[]{wn}));//O@literal@xsd:string@en
        defaultStrategyMap.put(LITERAL_LEX, Arrays.asList(new AlgType[]{ng, jw}));//O@literal@xsd:string
        
       // strategyMap = defaultStrategyMap;

        mergableStrategy = Arrays.asList(new StrategyType[]{URI_LEX, URI_WN, CLASS_HIERARCHY, LITERAL_WN, LITERAL_LEX});
        
        defaultStrategy = new ArrayList<StrategyType>();
        defaultStrategy.add(URI_LEX);
        defaultStrategy.add(LITERAL_LEX);
    }

    /**
     * Initialize the strategy and algorithms using Pragma from AST
     * @param ast 
     */
    public  void init(ASTQuery ast) {
        //kg:strategy, option: strategies used
        List<String> strategyOption = ast.getApproximateSearchOptions(Pragma.STRATEGY);
        strategyList = parse(strategyOption, StrategyType.class);

        // kg:algorithm, option: algorithms to use
        List<String> algorithmOption = ast.getApproximateSearchOptions(Pragma.ALGORITHM);
        algorithmList = parse(algorithmOption, AlgType.class);

        // kg:priority, option: algorithm priortiy **
        List<String> priorityAlgorithmOption = ast.getApproximateSearchOptions(Pragma.PRIORITY_ALGORITHM);
        List<Double> algorithmPriorities = parse(priorityAlgorithmOption, Double.class);
        Priority.init(algorithmPriorities, algorithmList);

        // filter algorithms according to the settings above
        filter(strategyList, algorithmList);
    }

    //setup the real strategy-algorithm to be applied, according to the default
    //strategy-algorithm list and options from Pragma@AST
    private  void filter(List<StrategyType> ls, List<AlgType> la) {
        strategyMap = new EnumMap<StrategyType, List<AlgType>>(StrategyType.class);

        for (StrategyType st : ls) {
            if (!defaultStrategyMap.containsKey(st)) {
                continue;
            }
            List<AlgType> algs = new ArrayList<AlgType>();
            for (AlgType alg : defaultStrategyMap.get(st)) {
                if (la.contains(alg)) {
                    algs.add(alg);
                }
            }
            if (!algs.isEmpty()) {
                strategyMap.put(st, algs);
            }
        }
    }

    /**
     * Get strategies by group and only return the strategies appeared in the
     * given list
     *
     * @param filter
     * @return
     */
    public  List<StrategyType> getMergableStrategies(List<StrategyType> filter) {
        List<StrategyType> lst = new ArrayList<StrategyType>();
        for (StrategyType st : mergableStrategy) {
            if (filter.contains(st) && check(st)) {
                lst.add(st);
            }
        }
        return lst;
    }

    /**
     * Get list of algorithms that can be used by one strategy
     *
     * @param strategy
     * @return
     */
    public  List<AlgType> getAlgorithmTypes(StrategyType strategy) {
        return strategyMap.containsKey(strategy) ? strategyMap.get(strategy) : new ArrayList<AlgType>();
    }

    public  String getAlgorithmString(List<StrategyType> lst) {

        List<AlgType> types = new ArrayList<AlgType>();
        for (StrategyType st : lst) {
            types.addAll(strategyMap.get(st));
        }

        String algs = "";
        for (int i = 0; i < types.size(); i++) {
            algs += types.get(i).name();
            if (i < types.size() - 1) {
                algs += SEPARATOR;
            }
        }

        return algs;
    }

    /**
     * Convert a string to a list of algorithm instance
     * @param algs
     * @return 
     */
    public static List<AlgType> getAlgorithmList(String algs) {
        List<AlgType> list = new ArrayList<AlgType>();

        if (algs == null || algs.isEmpty()) {
            return list;
        }

        String[] algsArray = algs.split(SEPARATOR);
        for (String aa : algsArray) {
            AlgType at = valueOf(aa);
            if (at != null) {
                list.add(at);
            }
        }

        return list;
    }

    /**
     * Convert a string name to an instance of algorithm
     * @param alg
     * @return 
     */
    public static AlgType valueOf(String alg) {
        try {
            return AlgType.valueOf(alg);
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal algorithm name '" + alg + "'. \n" + e.getMessage());
        }
        return null;
    }

    /**
     * parse a list of strings to the specified type T
     *
     * @param <T>
     * @param options
     * @param type
     * @return
     */
    private  <T> List<T> parse(List<String> options, Class<T> type) {
        List<T> list;

        if (options == null || options.isEmpty()) {
            if (type.getName().equals(StrategyType.class.getName())) {
                //list = (List<T>) StrategyType.allValues();
                list = (List<T>) defaultStrategy;
            } else if (type.getName().equals(AlgType.class.getName())) {
                list = (List<T>) AlgType.allValues();
            } else {
                list = null;
            }

            return list;
        }

        list = new ArrayList<T>();
        for (String aa : options) {
            T t;
            try {
                if (type.getName().equals(StrategyType.class.getName())) {
                    t = (T) StrategyType.valueOf(aa);
                } else if (type.getName().equals(AlgType.class.getName())) {
                    t = (T) AlgType.valueOf(aa);
                } else {
                    t = (T) Double.valueOf(aa);
                }
                list.add(t);
            } catch (IllegalArgumentException e) {
                logger.warn("Approximate search: '" + aa + "' is not defined");
            }
        }
        return list;
    }

    /**
     * Check whether a given strategy is authroized to use
     * @param strategy
     * @return 
     */
    public  boolean check(StrategyType strategy) {
        if (!strategyList.contains(strategy)) {
            return false;
        }

        for (AlgType at : getAlgorithmTypes(strategy)) {
            if (!algorithmList.contains(at)) {
                return false;
            }
        }
        return true;
    }
}
