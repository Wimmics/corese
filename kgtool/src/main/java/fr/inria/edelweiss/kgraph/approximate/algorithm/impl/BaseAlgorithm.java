package fr.inria.edelweiss.kgraph.approximate.algorithm.impl;

import fr.inria.edelweiss.kgraph.approximate.strategy.AlgType;
import fr.inria.edelweiss.kgraph.approximate.algorithm.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.algorithm.Utils;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Utils.empty;

/**
 * Base Algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 24 sept. 2015
 */
public class BaseAlgorithm implements ISimAlgorithm {

    private final AlgType type;
    public final static String OPTION_URI = "uri";
    private final String[] options = new String[]{OPTION_URI};

    /**
     * Constructor with default type 'empty'
     */
    BaseAlgorithm() {
        this(AlgType.empty);
    }

    /**
     * Constructor with given type
     *
     * @param type
     */
    public BaseAlgorithm(AlgType type) {
        this.type = type;
    }

    /**
     * Return type of algorithm
     *
     * @return
     */
    public AlgType getType() {
        return this.type;
    }

    //@Override
//    public double calculate(String s1, String s2) {
//        return this.calculate(s1, s2, null);
//    }
    @Override
    public double calculate(String s1, String s2, String parameters) {
        Utils.msg("Base-" + this.getType().name(), s1, s2, parameters, MIN);
        return MIN;
    }

    public boolean isValid(String option) {
        if (empty(option)) {
            return false;
        }

        for (String opt : options) {
            if (opt.equalsIgnoreCase(option)) {
                return true;
            }
        }

        return false;
    }
}
