package fr.inria.corese.sparql.benchmark.dbpsb;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.kgram.core.Query;

/**
 * DBPSB.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 12 mars 2015
 */
public class DBPSB {

    public final static String data = "/Users/fsong/NetBeansProjects/dbsb/input/dbpedia200m.nt";
    public final static int[] plans = {Query.QP_T0};
    //Query.QP_T3,  Query.QP_T0, Query.QP_HEURISTICS_BASED
    public final static int[] group_filter = {};

    public final static int REPEAT = 10;
    
    public static void main(String args[]) throws EngineException {

        long[][] r = new long[plans.length][Queries.q.length];

        for (int i = 0; i < plans.length; i++) {
            Graph g = Graph.create();
            Load ld = Load.create(g);
            System.out.println("Loading data " + data + " ...");
            ld.load(data);
            QueryProcess exec = QueryProcess.create(g);
            
            exec.setPlanProfile(plans[i]);
            System.out.println("Testing plan " + plans[i] + " ...");
            for (int j = 0; j < Queries.q.length; j++) {
                long start = System.currentTimeMillis();
                for (int k = 0; k < REPEAT; k++) {
                    exec.query(Queries.q[j]);
                }
                long end = System.currentTimeMillis();
                r[i][j] = (end - start)/REPEAT;
                System.out.println("Q" + (j+1) + ",  " + r[i][j] + " ms");
            }
            g.clean();
        }

        print(r, null);
    }

    public static void print(long[][] r, int[] group) {
        //all
        if (group == null) {
            group = new int[Queries.q.length];
            for (int j = 0; j < Queries.q.length; j++) {
                group[j] = j + 1;
            }
        }
        //title
        StringBuilder sb = new StringBuilder("query\t");
        for (int j = 0; j < group.length; j++) {
            sb.append("Q" + (group[j]) + "\t");
        }
        sb.append("Avg\n");

        //data
        for (int i = 0; i < plans.length; i++) {
            sb.append(plans[i] + "\t");
            long sum = 0;
            for (int j = 0; j < group.length; j++) {
                sb.append(r[i][group[j] - 1] + "\t");
                sum += r[i][group[j] - 1];
            }
            sb.append(sum / group.length + "\n");
        }

        System.out.println(sb.toString());
    }
}
