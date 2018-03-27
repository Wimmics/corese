package fr.inria.corese.core.approximate.strategy;

import fr.inria.corese.core.approximate.algorithm.ISimAlgorithm;
import fr.inria.corese.core.approximate.algorithm.impl.BaseAlgorithm;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating the weights of each algorithm based on their priorities
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 12 oct. 2015
 */
public class Priority {

    private final static Logger logger = LoggerFactory.getLogger(Priority.class);
    // private static final Map<StrategyType, Double> strategyMap = new EnumMap<StrategyType, Double>(StrategyType.class);
    private static final Map<AlgType, Double> algorithmMap = new EnumMap<AlgType, Double>(AlgType.class);

    public static void init(List<Double> values, List<AlgType> target) {
        if (values != null && values.size() != target.size()) {
            logger.warn("Warning: size of 'kg:priority' does not correspond to the number of its target!");
            values = null;
        }

        for (int i = 0; i < target.size(); i++) {
            double value = (values == null) ? 1 : values.get(i);
            algorithmMap.put(target.get(i), value);
        }
    }

    public static double[] getWeightByAlgorithmType(List<AlgType> types) {
        return normalize(priority(types));
    }

    //"ng-ss-ch-eq" -> [0.1, 0.2, 0.3, 0.4]
    public static double[] getWeightByAlgorithm(List<ISimAlgorithm> algs) {
        List<AlgType> types = new ArrayList<AlgType>();
        for (ISimAlgorithm alg : algs) {
            types.add(((BaseAlgorithm) alg).getType());
        }

        return getWeightByAlgorithmType(types);
    }

    //{ng, ss, ch} -> [1, 3, 4]
    private static double[] priority(List<AlgType> algs) {
        double[] p = new double[algs.size()];
        for (int i = 0; i < algs.size(); i++) {
            p[i] = algorithmMap.get(algs.get(i));
        }
        return p;
    }

    //[2, 4, 6, 8] -> 20
    public static double sum(double[] nums) {
        double sum = 0;
        for (double num : nums) {
            sum += num;
        }
        return sum;
    }

    //[2, 4, 6, 8] x [0.1, 0.2, 0.3, 0.4] -> sum([0.2, 0.8, 1.8, 3.2]) -> 6d
    public static double sum(double[] nums, double[] weights) {
        double sum = 0;
        if (nums.length != weights.length) {
            return sum;
        }

        for (int i = 0; i < nums.length; i++) {
            sum += nums[i] * weights[i];
        }
        return sum;
    }

    //[2, 4, 6, 8] -> [0.1, 0.2, 0.3, 0.4]
    public static double[] normalize(double[] nums) {
        double sum = sum(nums);
        if (sum <= 0) {
            return null;
        }

        double[] norm = new double[nums.length];
        for (int i = 0; i < nums.length; i++) {
            norm[i] = nums[i] / sum;
        }
        return norm;
    }

    public static double[] getDefaultWeights(int size) {
        if (size < 1) {
            return null;
        }

        double[] num = new double[size];
        for (int i = 0; i < size; i++) {
            num[i] = 1.0 / size;
        }
        return num;
    }
}
