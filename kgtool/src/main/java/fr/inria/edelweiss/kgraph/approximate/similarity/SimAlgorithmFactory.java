package fr.inria.edelweiss.kgraph.approximate.similarity;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.aggregation.ApproximateStrategy;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.CombinedAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.JaroWinkler;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.NGram;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.NLPHelper;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.TextSimilarity;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate instance of similarity measurement algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 sept. 2015
 */
public class SimAlgorithmFactory {

    public static ISimAlgorithm create(String name) {
        AlgType alg = ApproximateStrategy.valueOf(name);
        return alg == null ? null : create(alg);
    }

    /**
     * Create an instance of algorithm using the given type of algorithm
     *
     * @param type
     * @return
     */
    public static ISimAlgorithm create(AlgType type) {
        switch (type) {

            //**N-Gram
            case ng:
                return new NGram();
            //case eq:
            //    return new Equality();
            case jw:
                return new JaroWinkler();
            case wn:
                try {
                    return new TextSimilarity(NLPHelper.createInstance());
                } catch (Exception ex) {
                    Logger.getLogger(SimAlgorithmFactory.class.getName()).log(Level.WARNING, "Cannot initialize NLP helper!", ex);
                }
                return null;
            case ch:
            //integrate the old algorithm
            //return new ClassHieararchy(alg);
            //case dr:
            //return new DomainRange(alg);
            //case mult:
            default:
                //return new BaseAlgorithm(alg);
                return null;
        }
    }

    /**
     * Generate a combined similarity measurement algorithm
     *
     * @param algs
     * @return
     */
    public static ISimAlgorithm createCombined(String algs) {
        return createCombined(ApproximateStrategy.getAlgrithmList(algs));
    }

    /**
     * Create a combined similarity measurement algorithm
     *
     * @param algs
     * @return
     */
    public static ISimAlgorithm createCombined(List<AlgType> algs) {
        List<ISimAlgorithm> algList = new LinkedList<ISimAlgorithm>();

        for (AlgType at : algs) {
            ISimAlgorithm alg = create(at);
            if (alg != null) {
                algList.add(alg);
            }
        }

        return new CombinedAlgorithm(algList);
    }
}
