package fr.inria.corese.core.query;

import fr.inria.corese.core.logic.OWL;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.RDFS;
import java.util.HashMap;

/**
 * In OWL RL rules, OWL edge join may be "fuzzy" on one or two args
 * use case: if two bnodes represent the same OWL expression, they must be 
 * fuzzy matched, as EXP below
 * C equivalentClass EXP
 * xxx a EXP
 * =>
 * xxx a C
 * Producer and Matcher use it
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class FuzzyMatch {

    Table fuzzy;

    class Table extends HashMap<String, Integer> {
    }

    FuzzyMatch(){
        fuzzy = new Table();
    }
    
    public int fuzzy(String s) {
        Integer i = fuzzy.get(s);
        if (i == null) {
            return -1;
        }
        return i;
    }
    
    public void define(String s, int i){
        fuzzy.put(s, i);
    }

    public FuzzyMatch owl() {
        // fuzzy on left (right is a list)
        define(OWL.INTERSECTIONOF, 0);
        define(OWL.UNIONOF, 0);

        // fuzzy on right arg
        define(RDF.FIRST, 1);
        define(RDF.TYPE, 1);

        define(OWL.SOMEVALUESFROM, 1);
        define(OWL.ALLVALUESFROM, 1);
        define(OWL.ONCLASS, 1);

        // fuzzy on both arg
        define(RDFS.SUBCLASSOF, 2);
        define(OWL.EQUIVALENTCLASS, 2);
        define(OWL.COMPLEMENTOF, 2);
        define(OWL.DISJOINTWITH, 2);
        
        return this;
    }

}
