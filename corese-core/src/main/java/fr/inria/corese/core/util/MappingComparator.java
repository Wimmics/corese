package fr.inria.corese.core.util;

import java.util.Hashtable;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;

/**
 *
 * Compare Mappings (e.g. W3C vs KGRAM)
 *
 * Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class MappingComparator {

    public static MappingComparator create() {
        return new MappingComparator();
    }

    /**
     * Blanks may have different ID in test case and in kgram but same ID should
     * remain the same Hence store ID in hashtable to compare
     *
     */
    class TBN extends Hashtable<IDatatype, IDatatype> {

        boolean same(IDatatype dt1, IDatatype dt2) {
            if (containsKey(dt1)) {
                return get(dt1).sameTerm(dt2);
            } else {
                put(dt1, dt2);
                return true;
            }
        }
    }

    // target value of a Node
    IDatatype datatype(Node n) {
        return n.getValue();
    }

    public boolean validate(Mappings kgram, Mappings w3c) {
        boolean result = true, printed = false;
        Hashtable<Mapping, Mapping> table = new Hashtable<Mapping, Mapping>();

        for (Mapping w3cres : w3c) {
            // for each w3c result
            boolean ok = false;

            for (Mapping kres : kgram) {
                // find a new kgram result that is equal to w3c
                if (table.contains(kres)) {
                    continue;
                }

                ok = compare(kres, w3cres);

                if (ok) {
                    //if (kgram.getSelect().size() != w3cres.size()) ok = false;

                    for (Node qNode : kgram.getSelect()) {
                        // check that kgram has no additional binding 
                        if (kres.getNode(qNode) != null) {
                            if (w3cres.getNode(qNode) == null) {
                                ok = false;
                            }
                        }
                    }
                }

                if (ok) {
                    table.put(kres, w3cres);
                    break;
                }
            }

            if (!ok) {
                result = false;

                System.out.println("** Failure");
                if (printed == false) {
                    System.out.println(kgram);
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
    boolean compare(Mapping kres, Mapping w3cres) {
        TBN tbn = new TBN();
        boolean ok = true;

        for (Node var : w3cres.getQueryNodes()) {
            if (!ok) {
                break;
            }

            // for each w3c variable/value
            IDatatype w3cval = datatype(w3cres.getNode(var));
            // find same value in kgram
            if (w3cval != null) {
                String cvar = var.getLabel();
                Node kNode = kres.getNode(var);
                if (kNode == null) {
                    ok = false;
                } else {
                    IDatatype kdt = datatype(kNode);
                    IDatatype wdt = w3cval;
                    ok = compare(kdt, wdt, tbn);
                }
            }
        }

        return ok;
    }

    // compare kgram vs w3c values
    boolean compare(IDatatype kdt, IDatatype wdt, TBN tbn) {
        boolean ok = true;
        if (kdt.isBlank()) {
            if (wdt.isBlank()) {
                // blanks may not have same ID but 
                // if repeated they should  both be the same
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
                    ok
                            = Math.abs((kdt.doubleValue() - wdt.doubleValue())) < 10e-10;
                    if (ok) {
                        System.out.println("** Consider as equal: " + kdt.toSparql() + " = " + wdt.toSparql());
                    }
                }
            }

        } else {
            ok = kdt.sameTerm(wdt);
        }

        if (ok && wdt.isLiteral()) {
            // check same datatypes
            if (kdt.getDatatype() != null && wdt.getDatatype() != null) {
                ok = kdt.getDatatype().sameTerm(wdt.getDatatype());
            } else if (kdt.getDatatype() != wdt.getDatatype()){
                ok = false;
            }
            if (!ok) {
                System.out.println("** Datatype differ: " + kdt.toSparql() + " " + wdt.toSparql());
            }
        }

        return ok;

    }

}
