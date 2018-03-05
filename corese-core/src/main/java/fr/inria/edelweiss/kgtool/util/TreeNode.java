
package fr.inria.edelweiss.kgtool.util;

import fr.inria.acacia.corese.api.IDatatype;
import java.util.Comparator;
import java.util.TreeMap;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
  public class TreeNode extends TreeMap<IDatatype, IDatatype> {

         TreeNode(Comparator<IDatatype> c) {
            super(c);
        }
        
        public TreeNode(){
 //           this(new Compare());
        }

    /**
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node in graph It (may) represent (1
     * integer) and (1.0 float) as two different Nodes Current implementation of
     * EdgeIndex sorted by values ensure join (by dichotomy ...)
     */
    class Compare implements Comparator<IDatatype> {

        public int compare(IDatatype dt1, IDatatype dt2) {

            // xsd:integer differ from xsd:decimal 
            // same node for same datatype 
            if (dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null) {
                int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
                if (cmp != 0) {
                    return cmp;
                }
            }

            int res = dt1.compareTo(dt2);
            return res;
        }
    }
    
  }
