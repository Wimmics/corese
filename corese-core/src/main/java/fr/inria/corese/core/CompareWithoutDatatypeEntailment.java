package fr.inria.corese.core;

import fr.inria.corese.sparql.api.IDatatype;
import java.util.Comparator;


/**
 * TreeMap Node index comparator without D-entailment
 * Assign Node index for:
 * 1) graph match node join without D-entailment
 * 2) sorting graph Index edge list 
 * Assign same node index when same datatype and same value (and possibly
 * different labels) 
 * 1 and 01  have same index
 * integer|decimal and double|float have different index
 *'1'^^xsd:boolean and true have same index
 */
class CompareWithoutDatatypeEntailment implements Comparator<IDatatype> {

    CompareWithoutDatatypeEntailment() {
    }

    @Override
    public int compare(IDatatype dt1, IDatatype dt2) {

        if (dt1.getCode() == dt2.getCode()
                && dt1.getDatatypeURI().equals(dt2.getDatatypeURI())) {
            if (dt1.equals(dt2)) {
                // same datatype, same value: same index (even if labels are different)
                // 1 = 01 ; 1 != 1.0
                return 0;
            }
        }

        // compare with sameTerm instead of equal value
        // 1 != 1.0 ; they have different index
        return dt1.compareTo(dt2);
    }
}
