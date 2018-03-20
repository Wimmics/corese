package fr.inria.corese.core.approximate.algorithm.impl;

import fr.inria.corese.core.approximate.strategy.AlgType;
import fr.inria.corese.core.approximate.algorithm.Utils;
import java.util.HashMap;
import java.util.Map;

/**
 * N-Gram similarity measurement algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 27 août 2015
 */
public class NGram extends BaseAlgorithm {

    private final static int NG = 3;//default
    private int n;

    public NGram() {
        this(NG);
    }

    public NGram(int n) {
        super(AlgType.ng);
        this.n = n;
    }

    @Override
    public double calculate(String s1, String s2, String parameter) {
        double sim = this.calculate(s1, s2);
        Utils.msg("N-Gram" + NG, s1, s2, parameter, sim);
        return sim;
    }

    private double calculate(String s1, String s2) {
        double sim = MAX;
        if (!s1.equalsIgnoreCase(s2)) {
            Map<String, Integer> res1 = tokenize(s1, n);
            Map<String, Integer> res2 = tokenize(s2, n);

            int c = common(res1, res2);
            //int u = Math.max(s1.length(), s2.length()) - n + 1;
            int u = res1.size() + res2.size() - c;
            //u = u > 0 ? u : 1;
            sim = (double) c / (double) u;
        }
        return sim;
    }

    private int common(Map<String, Integer> tokens1, Map<String, Integer> tokens2) {
        int res = 0;

        for (String t1 : tokens1.keySet()) {
            if (tokens2.keySet().contains(t1)) {
                res++;
            }
        }

        return res;
    }

    private Map<String, Integer> tokenize(String c, int n) {
        Map<String, Integer> tokens = new HashMap<String, Integer>();

        String spacer = "";
        c = spacer + c + spacer;

        for (int i = 0; i < c.length(); i++) {
            if (i <= (c.length() - n)) {
                String t = c.substring(i, n + i).toLowerCase();
                if (tokens.containsKey(t)) {
                    tokens.put(t, tokens.get(t) + 1);
                } else {
                    tokens.put(t, 1);
                }
            }
        }
        return tokens;
    }
}
