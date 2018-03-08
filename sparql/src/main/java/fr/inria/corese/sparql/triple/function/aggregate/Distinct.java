/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.IDatatype;
import java.util.Comparator;
import java.util.TreeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class Distinct {

    static boolean compareIndex = false;
    static Distinct singleton = new Distinct();
    
    static TreeData create(){
        return singleton.createTreeData();
    }
    
    TreeData createTreeData(){
        return new TreeData(); 
    }

    class TreeData extends TreeMap<IDatatype, IDatatype> {

        boolean hasNull = false;

        TreeData() {
            super(new Compare());
        }

        boolean add(IDatatype dt) {

            if (dt == null) {
                if (hasNull) {
                    return false;
                } else {
                    hasNull = true;
                    return true;
                }
            }

            if (containsKey(dt)) {
                return false;
            }
            put(dt, dt);
            return true;
        }
    }

    class Compare implements Comparator<IDatatype> {

        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {
            if (compareIndex && dt1.getCode() != dt2.getCode()) {
                // same value with different datatype considered different
                return Integer.compare(dt1.getCode(), dt2.getCode());
            } else {
                return dt1.compareTo(dt2);
            }
        }
    }
}
