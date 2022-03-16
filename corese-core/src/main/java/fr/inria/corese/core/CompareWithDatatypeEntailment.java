package fr.inria.corese.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.XSD;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import java.util.Comparator;

/**
 * TreeMap Node index comparator with D-entailment
 * Assign Node index for:
 * 1) graph match node join with D-entailment
 * 2) sorting graph Index edge list
 * Assign same node index when compatible datatypes (same datatypes or datatypes
 * both in (integer, long, decimal)) and same value (and possibly different
 * labels) 1 = 01 = 1.0 = '1'^^xsd:long != 1e0 integer|decimal have same index
 * integer|decimal and float|double have different index float and double have
 * different index '1'^^xsd:boolean and true have same index (they unify in
 * graph match)
 */
class CompareWithDatatypeEntailment implements Comparator<IDatatype> {

    CompareWithDatatypeEntailment() {
    }

    @Override
    public int compare(IDatatype dt1, IDatatype dt2) {
        int res;
        try {
            // value comparison with = on values only
            res = dt1.compare(dt2);
        } catch (CoreseDatatypeException ex) {
            return compareWhenException(dt1, dt2);
        }

        if (res == 0) {
            // equal by value
            if ((dt1.isDecimalInteger() && dt2.isDecimalInteger())
                    || dt1.getCode() == dt2.getCode()) {
                // compatible datatypes, same value: same index
                return 0;
            } else {
                // integer|decimal vs float|double
                // float vs double
                return generalizedDatatype(dt1).compareTo(generalizedDatatype(dt2));
            }
        } else {
            return res;
        }
    }

    /**
     * boolean vs number incomparable dates
     *
     */
    int compareWhenException(IDatatype dt1, IDatatype dt2) {
        if (dt1.isDate() && dt2.isDate()) {
            // some dates are incomparable, compare string date
            // as they are not equal, we just need to return -1 or +1 
            // in a deterministic way
            return dt1.getLabel().compareTo(dt2.getLabel());
        }
        return generalizedDatatype(dt1).compareTo(generalizedDatatype(dt2));
    }

    /*
         * return same datatype URI for decimal/integer/long
         * to secure the walk into the table
         * 
     */
    String generalizedDatatype(IDatatype dt) {
        if (dt.isDecimalInteger()) {
            return XSD.xsddecimal;
        }
        return dt.getDatatypeURI();
    }

}
