package fr.inria.corese.core.transform;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.core.Query;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Keep track of nodes already printed to prevent loop Variant: check the pair
 * (dt, template) do not to loop on the same template ; may use several
 * templates on same node dt
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class Stack {
public static int count = 0;
    ArrayList<IDatatype> list;
    HashMap<IDatatype, ArrayList<Query>> map;
    HashMap<IDatatype, ArrayList<IDatatype[]>> arg;
    HashMap<IDatatype, IDatatype> visit;
    Transformer transformer;
    boolean multi = true;

    Stack(Transformer t, boolean b) {
        list  = new ArrayList<IDatatype>();
        map   = new HashMap<IDatatype, ArrayList<Query>>();
        arg   = new HashMap<IDatatype, ArrayList<IDatatype[]>>();
        visit = new HashMap<IDatatype, IDatatype>();
        multi = b;
        transformer = t;
    }

    int size() {
        return list.size();
    }

    @Deprecated
    void push(IDatatype dt) {
        list.add(dt);
    }

    /**
     * dt is the focus node (the first argument)
     * push template q    in dt  template stack 
     * push argument args in dt argument stack
     * args may be null. 
     */
    void push(IDatatype dt, IDatatype[] args, Query q) {
        list.add(dt);

        ArrayList<Query> qlist = map.get(dt);
        if (qlist == null) {
            qlist = new ArrayList<Query>();
            map.put(dt, qlist);
        }
        qlist.add(q);

        ArrayList<IDatatype[]> alist = arg.get(dt);
        if (alist == null) {
            alist = new ArrayList<IDatatype[]>();
            arg.put(dt, alist);
        }
        alist.add(args);
    }

    /**
     * Pop template and argument stack.
     */
    IDatatype pop() {
        if (list.size() > 0) {

            IDatatype dt = list.remove(list.size() - 1);

            ArrayList<Query> qlist = map.get(dt);
            qlist.remove(qlist.size() - 1);

            ArrayList<IDatatype[]> alist = arg.get(dt);
            alist.remove(alist.size() - 1);

            return dt;
        }
        return null;
    }
    
    
     /**
     * Check whether template q already applied on dt focus and possibly args
     */
    boolean contains(IDatatype dt, IDatatype[] args, Query q) {
        ArrayList<Query> qlist = map.get(dt);
        if (qlist == null) {
            return false;
        }
        ArrayList<IDatatype[]> alist = arg.get(dt);
        
        for (int i = 0; i < qlist.size(); i++) {
            if (qlist.get(i) == q) {
                // q is in dt stack
                if (q.getArgList().size() <= 1) {
                    // 0 or 1 argument: ?in or (?x)
                    // q in stack of dt
                    return true;
                } 
                else if (same(alist.get(i), args)) {
                    return true;
                }
            }
        }

        return false;
    }
          
    
   /**
    * Check whether two argument list are the same
    */
   boolean same(IDatatype[] a1, IDatatype[] a2){
       if (a1 != null && a2 != null){
           if (a1.length != a2.length){
               return false;
           }
           else {
               for (int i = 0; i<a1.length; i++){
                   if (! a1[i].equals(a2[i])){
                       return false;
                   }
               }
              return true;
           }
       }
       else {
           return a1 == a2;
       }
   }
   
   
   

    /**
     * Check whether template q already applied on dt focus and possibly args
     */
   @Deprecated
   boolean contains2(IDatatype dt, IDatatype[] args, Query q) {
        ArrayList<Query> qlist = map.get(dt);
        if (qlist == null || ! qlist.contains(q)){
            return false;
        }
        // q is in dt stack
        if (q.getArgList().size() <= 1){
            // 0 or 1 argument: ?in or (?x)
            return true;
        }
        // template q has several arguments
        ArrayList<IDatatype[]> alist = arg.get(dt);
        return contains(alist, args, qlist, q);
    }
   
   /**
    * Check whether q(args) already happened in alist arguments stack of dt focus node 
    */
   @Deprecated
   boolean contains(ArrayList<IDatatype[]> alist, IDatatype[] args, ArrayList<Query> qlist, Query q){
       for (int i = 0; i<qlist.size(); i++){
           if  (qlist.get(i) == q && same(alist.get(i), args)){
               return true;
           }
       }
       return false;
   }
   
   
   @Deprecated
   boolean contains(IDatatype dt) {
        return list.contains(dt);
    }

    boolean isVisited(IDatatype dt) {
        if (visit.containsKey(dt)) {
            return true;
        }
        ArrayList<Query> qlist = map.get(dt);
        if (qlist == null) {
            return false;
        }
        return qlist.size() > 1;
    }

    void visit(IDatatype dt) {
        visit.put(dt, dt);
    }

@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (IDatatype dt : list) {
            sb.append(i++);
            sb.append(" ");
            sb.append(dt);
            sb.append(": ");
            for (Query q : map.get(dt)) {
                sb.append(q.getAST());
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}