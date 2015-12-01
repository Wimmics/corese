package fr.inria.edelweiss.kgraph.approximate.algorithm.impl;

import fr.inria.edelweiss.kgraph.approximate.algorithm.Utils;

/**
 * Jaro–Winkler distance (Winkler, 1990) based on Jaro
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 oct. 2015
 */
public class JaroWinkler extends Jaro {

    @Override
    public double calculate(String s1, String s2) {
        double sim = MAX;
        if (!s1.equalsIgnoreCase(s2)) {
            //jaro algorithm
            double jaroSim = super.calculate(s1, s2);

            int prefix = getCommonPrefix(s1, s2);
            sim = jaroSim + prefix * (0.1 * (1.0 - jaroSim));
        }

        Utils.msg("Jaro-Winkler", s1, s2, sim);
        return sim;
    }

    private int getCommonPrefix(String s1, String s2) {
        int cp = 0;
        for (int i = 0; i < 4 & i < s1.length() & i < s2.length(); i++) {
            if (String.valueOf(s1.charAt(i)).equalsIgnoreCase(String.valueOf(s2.charAt(i)))) {
                cp++;
            } else {
                break;
            }
        }
        return cp;
    }
}
