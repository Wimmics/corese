package fr.inria.corese.test.dev.shex;

import fr.inria.lille.shexjava.schema.concrsynt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author corby
 */
public class ShexConstraint {
    
    private static final String SH_DATATYPE = "sh:datatype";
    private static final String SH_IN = "sh:in";
    private static final String SH_HAS_VALUE = "sh:hasValue";
    private static final String SH_MIN_EXCLUSIVE = "sh:minExclusive";
    private static final String SH_MIN_INCLUSIVE = "sh:minInclusive";
    private static final String SH_MAX_EXCLUSIVE = "sh:maxExclusive";
    private static final String SH_MAX_INCLUSIVE = "sh:maxInclusive";
    private static final String SH_NODE_KIND = "sh:nodeKind";
    private static final String SH_UNDEF = "sh:Undef";
    private static final String SH_BLANK = "sh:BlankNode";
    private static final String SH_IRI_OR_BLANK = "sh:BlankNodeOrIRI";
    private static final String SH_LITERAL = "sh:Literal";
    private static final String SH_IRI = "sh:IRI"; 
    private static final String SH_PATTERN = "sh:pattern";
    private static final String SH_MAXLENGTH = "sh:maxLength";
    private static final String SH_MINLENGTH = "sh:minLength";
    private static final String SH_OR = "sh:or";
    private static final String SH_AND = "sh:and";
    private static final String SH_NOT = "sh:not";
    
    Shex shex;
    private ShexShacl shacl;

    ShexConstraint(Shex shex) {
        this.shex = shex;
        setShacl(shex.getShacl());
    }

    void process(Constraint cst) {
        //trace(cst);
        if (cst instanceof DatatypeConstraint) {
            process((DatatypeConstraint) cst);
        } else if (cst instanceof ValueSetValueConstraint) {
            process((ValueSetValueConstraint) cst);
        } else if (cst instanceof FacetNumericConstraint) {
            process((FacetNumericConstraint) cst);
        } else if (cst instanceof FacetStringConstraint) {
            process((FacetStringConstraint) cst);
        } else if (cst instanceof IRIStemConstraint) {
            process((IRIStemConstraint) cst);
        } else if (cst instanceof LanguageConstraint) {
            process((LanguageConstraint) cst);
        } else if (cst instanceof LanguageStemConstraint) {
            process((LanguageStemConstraint) cst);
        } else if (cst instanceof LiteralStemConstraint) {
            process((LiteralStemConstraint) cst);
        } else if (cst instanceof NodeKindConstraint) {
            process((NodeKindConstraint) cst);
        } else {
            trace("undef cst: " + cst.getClass().getName() + " " + cst);
        }
    }

    void trace(Object obj) {
        System.out.println(obj.getClass().getName() + " " + obj);
    }

    String getValue(String val) {
        return shex.getValue(val);
    }

    String getPrefixURI(String val) {
        return shex.getPrefixURI(val);
    }
    
    void define(String name, String value) {
        shex.define(name, value);
    }
    void define(String name, List<String> value) {
        shex.define(name, value);
    }   
    void process(DatatypeConstraint cst) {
        shex.define(SH_DATATYPE, shex.getPrefixURI(cst.getDatatypeIri().toString()));
    }
    void define(String name, BigDecimal value) {
        shex.define(name, value);
    }

    // sh:or(value, regex)
    void process(ValueSetValueConstraint cst) {
        ShexShacl res =  processBasic(cst);  
        getShacl().append(res);
    }
        
    ShexShacl processBasic(ValueSetValueConstraint cst) {
        ArrayList<String> valueList = new ArrayList<>();
       
        ArrayList<ShexShacl> cstList = new ArrayList<>();
        
        for (Value val : cst.getExplicitValues()) {
            valueList.add(getValue(val.stringValue()));
        }
        
        if (!valueList.isEmpty()) {
            ShexShacl fst = new ShexShacl();    
            fst.define(SH_IN, valueList);
            cstList.add(fst);
        }
        
        for (Constraint cc : cst.getConstraintsValue()) {
            if (cc instanceof IRIStemConstraint) {
                // ei = regex()
                cstList.add(process((IRIStemConstraint) cc));
            } else if (cc instanceof IRIStemRangeConstraint) {
                // ei = (and(regex(), not(ej))
                cstList.add(process((IRIStemRangeConstraint) cc));
            } else {
                System.out.println("Undef Value Set Cst: " + cc);
            }
        }
        
        if (cstList.size() == 1) {
            return cstList.get(0);
        }
        ShexShacl res = new ShexShacl();
        res.insert(SH_OR, cstList);
        return res;
    }
    
    ShexShacl process(IRIStemConstraint cst) {
        ShexShacl shacl = new ShexShacl();
        shacl.define(SH_PATTERN, String.format("\"^%s\"", cst.getIriStem()));
        return shacl;
    }
    
    /**
     * ex:~ - ex:unassigned - ex:assigned
     * and(regex(), not(valueIn()))
     * ex:~ - <http://aux.example/terms#med_>~ 
     * and(regex(), not((regex()), not(regex())))
     */
    ShexShacl process(IRIStemRangeConstraint cst) {
        ShexShacl stem = new ShexShacl()
                .define(SH_PATTERN, String.format("\"^%s\"", getStem(cst.getStem())));        
        ShexShacl constraint = processBasic(cst.getExclusions());
        ShexShacl exclude = new ShexShacl().insert(SH_NOT, constraint);                        
        ShexShacl res = new ShexShacl().insert(SH_AND, Arrays.asList(stem, exclude));        
        return res;
    }
       
    String getStem(Constraint cst) {
        // remove "IRIstem="
        if (cst instanceof IRIStemConstraint) {
            return ((IRIStemConstraint) cst).getIriStem();
        }
        return cst.toString();
    }

    void process(FacetNumericConstraint cst) {
        if (cst.getMaxincl() != null) {
            define(SH_MAX_INCLUSIVE, cst.getMaxincl());
        }
        if (cst.getMaxexcl() != null) {
            define(SH_MAX_EXCLUSIVE, cst.getMaxexcl());
        }
        if (cst.getMinexcl() != null) {
            define(SH_MIN_INCLUSIVE, cst.getMinincl());
        }
        if (cst.getMinexcl() != null) {
            define(SH_MIN_EXCLUSIVE, cst.getMinexcl());
        }
    }

    void process(FacetStringConstraint cst) {
        cst.getFlags();

        if (cst.getPatternString() != null) {
            define(SH_PATTERN, String.format("\"%s\"", cst.getPatternString()));
        }
        if (cst.getMinlength() != null) {
            define(SH_MINLENGTH, cst.getMinlength().toString());
        }
        if (cst.getMaxlength() != null) {
            define(SH_MAXLENGTH, cst.getMaxlength().toString());
        }
        if (cst.getLength() != null) {
            define(SH_MINLENGTH, cst.getLength().toString());
            define(SH_MAXLENGTH, cst.getLength().toString());
        }
    }

    void process(LanguageConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLangTag();
    }

    void process(LanguageStemConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLangStem();
    }

    void process(LiteralStemConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLitStem();
    }

    void process(NodeKindConstraint cst) {
        define(SH_NODE_KIND, getKind(cst));
    }

    String getKind(NodeKindConstraint cst) {
        if (cst == NodeKindConstraint.AllIRI) {
            return SH_IRI;
        }
        if (cst == NodeKindConstraint.AllLiteral) {
            return SH_LITERAL;
        }
        if (cst == NodeKindConstraint.AllNonLiteral) {
            return SH_IRI_OR_BLANK;
        }
        if (cst == NodeKindConstraint.Blank) {
            return SH_BLANK;
        }
        return SH_UNDEF;
    }

    /**
     * @return the shacl
     */
    public ShexShacl getShacl() {
        return shacl;
    }

    /**
     * @param shacl the shacl to set
     */
    public void setShacl(ShexShacl shacl) {
        this.shacl = shacl;
    }

}
