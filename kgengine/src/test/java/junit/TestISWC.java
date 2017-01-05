/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package junit;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static junit.TestUnit.data;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestISWC {
    
    
      public static void main(String[] args) throws EngineException, LoadException{
          new TestISWC().testFib();
      }
      
       public void testFib() throws EngineException, LoadException{
             Graph g = Graph.create(); 
             QueryProcess exec = QueryProcess.create(g);
        
            
             
             QueryLoad ql = QueryLoad.create();
             String q1 = ql.readWE(data + "test/iswc/lds/fib.rq");
             String q2 = ql.readWE(data + "test/iswc/lds/sort.rq");
             String q3 = ql.readWE(data + "test/iswc/lds/sigma.rq");

             // 30 : 0.3487  JS : 0.0076
             // 35 : 3.6     JS : 0.0683
            Mappings map = null;
            
            int n = 10;
            int res = 0;
            double time = 0, total = 0;
            double dd = 0;
            
            
           ArrayList<Integer> ll = null;
           for (int i = 0; i < n; i++) {
//               ArrayList<Integer> list = new ArrayList<Integer>();
//               ll = list;
//               int size = 1000;
//               for (int j = 0; j < size; j++) {
//                   list.add(size - j);
//               }
                Date d1 = new Date();             
                map = exec.query(q2);
                //res = fib(35);
                //sort(list);
                 stat();
                Date d2 = new Date();
                time = d2.getTime() - d1.getTime() ;
                System.out.println(i + " : " + time);
                total += time;
            }
            
            System.out.println("Time : " + (total) / (n ));
            System.out.println("dd = " + dd);
            System.out.println(map);
           
            // System.out.println(map.getQuery().getExtension());
           System.out.println(Interpreter.count);
                              
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
       
       int fib(int n){
           if (n <= 2){ return 1; }
           else { return fib(n - 1) + fib(n - 2) ; }
       }
    
    

}
