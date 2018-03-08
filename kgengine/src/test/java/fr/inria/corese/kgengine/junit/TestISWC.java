/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgengine.junit;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static fr.inria.corese.kgengine.junit.TestUnit.data;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestISWC {

    public static void main(String[] args) throws EngineException, LoadException {
        new TestISWC().testFib();
    }

    public void testFib() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);



        QueryLoad ql = QueryLoad.create();
        // old = 3650
        // new = 1840
        String q1 = ql.readWE(data + "test/iswc/lds/fib.rq");
        String q1j = ql.readWE(data + "test/iswc/lds/fibjava.rq");

        String q2 = ql.readWE(data + "test/iswc/lds/sort.rq");
        String q3 = ql.readWE(data + "test/iswc/lds/sigma.rq");

        // 30 : 0.3487  JS : 0.0076
        // 35 : 3.6     JS : 0.0683
        Mappings map = null;

        int n = 10;
        int res = 0;
        IDatatype dt = null;
        double time = 0, total = 0;
        double dd = 0;
        //System.out.println(q2);
        IDatatype list = null;
        
        for (int i = 0; i < n; i++) {
            Date d1 = new Date();
            // with bind: 1802 1155.0 1114.0 1082.0 1069.0  878.0 722.0
            // with bind and IDatatype[]:  2394
            map = exec.query(q1);
            Date d2 = new Date();
            time = d2.getTime() - d1.getTime();
            System.out.println(i + " : " + time);
            total += time;
        }
        System.out.println("Time : " + (total) / (n));
        System.out.println("dd = " + dd);
        System.out.println(map);
        System.out.println("dt = " + dt);
        
        total = 0;
        for (int i = 0; i < n; i++) {
            Date d1 = new Date();
            // 130 (was 700 before opt)
            dt = fib(DatatypeMap.newInstance(35));
            Date d2 = new Date();
            time = d2.getTime() - d1.getTime();
            System.out.println(i + " : " + time);
            total += time;
        }
        System.out.println("Time : " + (total) / (n));
        System.out.println("dd = " + dd);
        System.out.println(map);
        System.out.println("dt = " + dt);
    }

    void stat() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i < 100000; i++) {
            list.add(i);
        }

        double sig = sigma(list);
        int med = median(list);
        double avg = avg(list);

    }

    int median(List<Integer> list) {
        Collections.sort(list);
        return list.get((int) (list.size() - 1) / 2);
    }

    double sigma(List<Integer> list) {
        double avg = avg(list);
        double tmp = 0;
        for (int i = 0; i < list.size(); i++) {
            tmp += Math.pow(list.get(i) - avg, 2);
        }
        double pow = Math.pow(tmp / list.size(), 0.5);
        return pow;
    }

    double avg(List<Integer> list) {
        double avg = 0;
        for (int i = 0; i < list.size(); i++) {
            avg += list.get(i);
        }
        return avg / list.size();
    }

    IDatatype list(int n) {
        ArrayList<IDatatype> list = new ArrayList<>(n);
        for (int j = 0; j < n; j++) {
            list.add(DatatypeMap.newInstance(Math.random()));
        }
        return DatatypeMap.newInstance(list);
    }

    void sort(ArrayList<Integer> l) {
        for (int i = l.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (l.get(j + 1) < l.get(j)) {
                    int tmp = l.get(j + 1);
                    l.set(j + 1, l.get(j));
                    l.set(j, tmp);
                }
            }
        }
    }

    void sort(IDatatype l) {
        //for (int i = l.size() - 1; i > 0; i--) {
        for (IDatatype i : DatatypeMap.newIterate(l.size() - 1, 0, -1)) {
            for (IDatatype j : DatatypeMap.newIterate(0, i.intValue() - 1)) { //(int j = 0; j < i; j++) {
                IDatatype jp = DatatypeMap.newInstance(j.intValue() + 1);
                if (l.get(jp.intValue()).lt(l.get(j.intValue())).booleanValue()) {
                    IDatatype tmp = l.get(jp.intValue());
                    DatatypeMap.set(l, jp, l.get(j.intValue()));
                    DatatypeMap.set(l, j, tmp);
                }
            }
        }
    }

    int fib(int n) {
        if (n <= 2) {
            return 1;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    public IDatatype fib(IDatatype dt) {
        if (dt.le(DatatypeMap.TWO).booleanValue()) {
            return DatatypeMap.ONE;
        } else {
            return fib(dt.minus(DatatypeMap.ONE)).plus(fib(dt.minus(DatatypeMap.TWO)));
        }
    }
}
