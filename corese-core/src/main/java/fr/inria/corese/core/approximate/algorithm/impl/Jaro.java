package fr.inria.corese.core.approximate.algorithm.impl;

import fr.inria.corese.core.approximate.strategy.AlgType;

/**
 * Jaro distance metric (Jaro, 1989, 1995)
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 oct. 2015
 */
public class Jaro extends BaseAlgorithm {

    public Jaro() {
        super(AlgType.jw);
    }

    /**
     * gets the similarity of the two strings using Jaro distance.
     *
     * @param s1 the first input string
     * @param s2 the second input string
     * @param parameter
     * @return a value between 0-1 of the similarity
     */
    @Override
    public double calculate(String s1, String s2, String parameter) {
        return calculate(s1, s2);
    }

    private double calculate(final String s1, final String s2) {

        //get half the length of the string rounded up - (this is the distance used for acceptable transpositions)
        final int halflen = ((Math.min(s1.length(), s2.length())) / 2) + ((Math.min(s1.length(), s2.length())) % 2);

        //get common characters
        final StringBuffer common1 = getCommonCharacters(s1, s2, halflen);
        final StringBuffer common2 = getCommonCharacters(s2, s1, halflen);

        //check for zero in common
        if (common1.length() == 0 || common2.length() == 0) {
            return 0.0f;
        }

        //get the number of transpositions
        int transpositions = 0;
        int n = Math.min(common1.length(), common2.length());
        for (int i = 0; i < n; i++) {
            if (common1.charAt(i) != common2.charAt(i)) {
                transpositions++;
            }
        }
        transpositions /= 2.0f;

        //calculate jaro metric
        return (common1.length() / ((float) s1.length())
                + common2.length() / ((float) s2.length())
                + (common1.length() - transpositions) / ((float) common1.length())) / 3.0f;
    }

    /**
     * returns a string buffer of characters from string1 within string2 if they
     * are of a given distance seperation from the position in string1.
     *
     * @param string1
     * @param string2
     * @param distanceSep
     * @return a string buffer of characters from string1 within string2 if they
     * are of a given distance seperation from the position in string1
     */
    private StringBuffer getCommonCharacters(final String string1, final String string2, final int distanceSep) {
        //create a return buffer of characters
        final StringBuffer returnCommons = new StringBuffer();
        //create a copy of string2 for processing
        final StringBuffer copy = new StringBuffer(string2);
        //iterate over string1
        int n = string1.length();
        int m = string2.length();
        for (int i = 0; i < n; i++) {
            final char ch = string1.charAt(i);
            //set boolean for quick loop exit if found
            boolean foundIt = false;
            //compare char with range of characters to either side

            for (int j = Math.max(0, i - distanceSep); !foundIt && j < Math.min(i + distanceSep, m - 1); j++) {
                //check if found
                if (String.valueOf(copy.charAt(j)).equalsIgnoreCase(String.valueOf(ch))) {
                    foundIt = true;
                    //append character found
                    returnCommons.append(ch);
                    //alter copied string2 for processing
                    copy.setCharAt(j, (char) 0);
                }
            }
        }
        return returnCommons;
    }
}
