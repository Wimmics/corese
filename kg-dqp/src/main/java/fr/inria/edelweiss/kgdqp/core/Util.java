/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gaignard
 */
public class Util {

    public static List<Filter> getApplicableFilter(Environment env, Edge edge) {
        // KGRAM exp for current edge
        Exp exp = env.getExp();;
        List<Filter> lFilters = new ArrayList<Filter>();
        for (Filter f : exp.getFilters()) {
            // filters attached to current edge
            if (f.getExp().isExist()) {
                // skip exists { PAT }
                continue;
            }

            if (exp.bind(f)) {
                lFilters.add(f);
            }
        }

//         workaround for non gathered filters
        if (lFilters.isEmpty()) {
            for (Exp e : env.getQuery().getBody()) {
                if (e.isFilter()) {
                    Filter kgFilter = e.getFilter();
//                    System.out.println("\t\tFilter " + kgFilter.toString() + " -> " + edge);
                    if (bound(edge, kgFilter)) {
//                        System.out.println("\t\t\t BOUND");
                        lFilters.add(kgFilter);
                    }
                }
            }
        }

        return lFilters;
    }

    /*
     * ?x p ?y FILTER ((?x > 10) && (?z > 10))
     * 
     * !!!!!!
     * ENV :: ?z = {0,2}
     * TODO ?x p ?y FILTER ((?x > ?z) && (?z > 10))
     * !!!!!!
     */
    public static boolean bound(Edge edge, Filter filter) {
        List<String> vars = new ArrayList<String>();
        if (edge.getNode(0).isVariable()) {
            vars.add(edge.getNode(0).toString());
        }
        if (edge.getNode(1).isVariable()) {
            vars.add(edge.getNode(1).toString());
        }
        
        List<String> varsFilter = filter.getVariables();
        for (String var : varsFilter) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }
}
