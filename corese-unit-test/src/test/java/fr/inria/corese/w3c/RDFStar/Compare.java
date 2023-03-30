package fr.inria.corese.w3c.RDFStar;

import java.util.HashMap;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 */
public class Compare {
    boolean strict = true;
    Mappings kgram, w3c;

    public Compare(Mappings corese, Mappings w3c) {
        kgram = corese;
        this.w3c = w3c;
    }

    /**
     * KGRAM vs W3C result
     */
    public boolean validate() {
        if (kgram.size() != w3c.size()) {
            return false;
        }
        return check(kgram, w3c);
    }

    public boolean check(Mappings kgram, Mappings w3c) {
        boolean result = true, printed = false;
        HashMap<Mapping, Mapping> table = new HashMap<>();

        for (Mapping w3cres : w3c) {
            // for each w3c result
            boolean ok = false;

            for (Mapping kres : kgram) {
                // find a new kgram result that is equal to w3c
                if (table.containsKey(kres)) {
                    continue;
                }

                ok = compare(kgram, w3c, kres, w3cres);

                if (ok) {
                    table.put(kres, w3cres);
                    break;
                }
            }

            if (!ok) {
                result = false;

                System.out.println("** Failure");
                if (printed == false) {
                    System.out.println("corese:");
                    System.out.println(kgram);
                    System.out.println("w3c:");
                    System.out.println(w3c);
                    printed = true;
                }
                for (Node var : w3cres.getQueryNodes()) {
                    // for each w3c variable/value
                    Node val = w3cres.getNode(var);
                    System.out.println(var + " [" + val + "]");
                }
                System.out.println("--");
            }

        }
        return result;
    }

    // compare two results
    boolean compare(Mappings kgmap, Mappings w3map, Mapping kgres, Mapping w3res) {
        TBN tbn = new TBN();
        boolean ok = true;

        for (Node var : w3res.getQueryNodes()) {
            if (!ok) {
                break;
            }

            // for each w3c variable/value
            IDatatype w3val = datatype(w3res.getNode(var));
            // find same value in kgram
            if (w3val != null) {
                String cvar = var.getLabel();
                Node kNode = kgres.getNode(var);
                if (kNode == null) {
                    ok = false;
                } else {
                    IDatatype kdt = datatype(kNode);
                    IDatatype wdt = w3val;
                    ok = compare(kdt, wdt, tbn);
                }
            }
        }

        if (ok && kgmap.getSelect() != null) {
            // kgram result has additional data
            for (Node node : kgmap.getSelect()) {
                if (kgres.getNodeValue(node) != null && w3res.getNode(node) == null) {
                    ok = false;
                    // if (w3res.getQueryNodes().length > 0) {
                    // System.out.println("kg: "+ node + " = " + kgres.getNodeValue(node));
                    // System.out.println();
                    // System.out.println("w3c: " + w3res);
                    // System.out.println("kgr: " + kgres);
                    // }
                    break;
                }
            }
        }

        return ok;
    }

    // target value of a Node
    IDatatype datatype(Node n) {
        if (n == null) {
            return null;
        }
        return n.getValue();
    }

    boolean compare(Edge e1, Edge e2, TBN tbn) {
        return compare(e1.getSubjectValue(), e2.getSubjectValue(), tbn) &&
                compare(e1.getObjectValue(), e2.getObjectValue(), tbn) &&
                compare(e1.getPredicateValue(), e2.getPredicateValue(), tbn);
    }

    // compare kgram vs w3c values
    boolean compare(IDatatype kdt, IDatatype wdt, TBN tbn) {
        boolean ok = true;

        if (kdt.isTriple()) {
            if (wdt.isTriple()) {
                ok = compare(kdt.getEdge(), wdt.getEdge(), tbn);
            } else {
                ok = false;
            }
        } else if (kdt.isBlank()) {
            if (wdt.isBlank()) {
                // blanks may not have same ID but
                // if repeated they should both be the same
                ok = tbn.same(kdt, wdt);
            } else {
                ok = false;
            }
        } else if (wdt.isBlank()) {
            ok = false;
        } else if (kdt.isNumber() && wdt.isNumber()) {
            ok = kdt.sameTerm(wdt);

            if (DatatypeMap.isLong(kdt) && DatatypeMap.isLong(wdt)) {
                // ok
            } else {
                if (!ok) {
                    // compare them at 10^-10
                    ok = Math.abs((kdt.doubleValue() - wdt.doubleValue())) < 10e-10;
                    if (ok) {
                    }
                }
            }

        } else {
            ok = kdt.sameTerm(wdt);
            if (!ok) {
                if (matchDatatype(kdt, wdt)) {
                    ok = kdt.equals(wdt);
                }
            }
        }

        if (ok && strict && wdt.isLiteral()) {
            // check same datatypes
            if (kdt.getDatatype() != null && wdt.getDatatype() != null) {
                ok = kdt.getDatatype().sameTerm(wdt.getDatatype());
            } else if (kdt.getDatatype() != wdt.getDatatype()) {
                ok = false;
            }
            if (!ok) {
            }
        }

        return ok;

    }

    boolean matchDatatype(IDatatype dt1, IDatatype dt2) {
        return (dt1.getCode() == IDatatype.LITERAL) && (dt2.getCode() == IDatatype.STRING)
                || (dt1.getCode() == IDatatype.STRING) && (dt2.getCode() == IDatatype.LITERAL);
    }

    /**
     * Blanks may have different ID in test case and in kgram but same ID should
     * remain the same Hence store ID in hashtable to compare
     *
     */
    class TBN extends HashMap<IDatatype, IDatatype> {

        boolean same(IDatatype dt1, IDatatype dt2) {
            if (containsKey(dt1)) {
                return get(dt1).sameTerm(dt2);
            } else {
                put(dt1, dt2);
                return true;
            }
        }
    }

}
