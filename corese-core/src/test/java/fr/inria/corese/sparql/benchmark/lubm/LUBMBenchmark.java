package fr.inria.corese.sparql.benchmark.lubm;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.kgram.core.Mappings;

/**
 * LUBM benchmark 
 * the Lehigh University Benchmark (LUBM)
 * http://swat.cse.lehigh.edu/projects/lubm/
 * 
 * LUBM.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 avr. 2014
 */
public final class LUBMBenchmark {

    static final String lubm = LUBMBenchmark.class.getClassLoader().getResource("data").getPath() + "/lubm/";

    private static final long FAIL = -1;
    private Graph graph;
    private QueryProcess exec;
    String name;
    String[] q;

    long[][] results;
    long loadtime= 0;
    int repeat = 1;
    int timeout = 60 * 1000;//1 minute

    public static void main(String[] args) {
        LUBMBenchmark bench = new LUBMBenchmark("LUBM Benchmarking[http://swat.cse.lehigh.edu/projects/lubm/]", LUBMSparqlQuery.Q);
        bench.load(lubm+"/u0/");
        bench.load(lubm+"/u1/");
        bench.load(lubm+"/u2/");
        bench.run(10);
        System.out.println(bench.report());
    }

    public void load(String data) {
        Load ld = Load.create(graph);
        long start = System.currentTimeMillis();
        ld.load(data);
        loadtime += System.currentTimeMillis()-start;
    }

    public LUBMBenchmark(String name, String[] query) {
        graph = Graph.create();
        exec = QueryProcess.create(graph);
        this.q = query;
        this.name = name;
    }

    public void run() {
        run(repeat);
    }

    public void run(int repeat) {
        repeat = repeat > 1 ? repeat : 1;
        this.repeat = repeat;
        results = new long[q.length][this.repeat];

        for (int j = 0; j < q.length; j++) {
            //long ms = 0;
            System.out.println(q[j]);
            for (int i = 0; i < repeat; i++) {
                long t = execute(q[j]);
                results[j][i] = t;
                if (t == FAIL) {//query engine exception
                    break;
                }
                //ms += t/q.length;
            }
        }
    }

    private long execute(String q) {

        long start, end;
        try {
            start = System.currentTimeMillis();
            Mappings m= exec.query(q);
            System.out.println(m.size());
            end = System.currentTimeMillis();
        } catch (EngineException ex) {
            return FAIL;
        }
        return end - start;
    }

    public String report() {
        StringBuilder r = new StringBuilder();
        r.append("====" + name + "=====\n");
        r.append("[" + q.length + " queries, repeat " + repeat + " times, " + graph.size() + " triples, load time "+loadtime+" ms]\n");
        for (int i = 0; i < q.length; i++) {
            long avg = 0;
            r.append("[" + (i + 1) + "]\t");
            for (int j = 0; j < repeat; j++) {
                r.append(results[i][j] + "\t");
                avg += results[i][j];
            }
            r.append( avg/repeat + "\n");
        }

        return r.toString();
    }
}
