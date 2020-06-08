package fr.inria.corese.shex;

import fr.inria.lille.shexjava.schema.concrsynt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
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
    static final String SH_PATTERN = "sh:pattern";
    private static final String SH_MAXLENGTH = "sh:maxLength";
    private static final String SH_MINLENGTH = "sh:minLength";
    private static final String SH_OR = "sh:or";
    private static final String SH_AND = "sh:and";
    private static final String SH_NOT = "sh:not";
    static final String SH_LANGUAGE_IN = "sh:languageIn";
    
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
            // sh:in (value list)
            ShexShacl fst = new ShexShacl();    
            fst.define(SH_IN, valueList);
            cstList.add(fst);
        }
        
        ArrayList<LanguageConstraint> langList = new ArrayList<>();
        
        for (Constraint cc : cst.getConstraintsValue()) {
            //trace(cc);
            if (cc instanceof IRIStemConstraint) {
                // ei = regex()
                cstList.add(process((IRIStemConstraint) cc));
            } 
            else if (cc instanceof LiteralStemConstraint) {
                // ei = regex()
                cstList.add(process((LiteralStemConstraint) cc));
            } 
            else if (cc instanceof StemRangeConstraint) {
                // ei = (and(regex(), not(ej))
                cstList.add(process((StemRangeConstraint) cc));
            } 
            else if (cc instanceof LanguageConstraint) {
                langList.add((LanguageConstraint) cc);
            } 
            else if (cc instanceof LanguageStemConstraint) {
                cstList.add(process((LanguageStemConstraint) cc));
            } 
            else {
                System.out.println("Undef Value Set Cst: " + cc);
            }
        }
        
        if (! langList.isEmpty()) {
            ShexShacl lang = processLanguage(langList);
            cstList.add(lang);
        }
        
        if (cstList.size() == 1) {
            return cstList.get(0);
        }
        ShexShacl res = new ShexShacl()
                .insert(SH_OR, cstList).append(";").nl();
        return res;
    }
    
    ShexShacl process(IRIStemConstraint cst) {
        ShexShacl shacl = new ShexShacl();
        shacl.pattern(cst.getIriStem()).nl();
        return shacl;
    }
    
    /**
     * ex:~ - ex:unassigned - ex:assigned
     * and(regex(), not(valueIn()))
     * ex:~ - <http://aux.example/terms#med_>~ 
     * and(regex(), not((regex()), not(regex())))
     */
    ShexShacl process(StemRangeConstraint cst) {
        ShexShacl stem = new ShexShacl();
        if (cst instanceof LiteralStemRangeConstraint) {
            stem.pattern(getStem(cst.getStem())).nl(); 
        }
        else {
            //trace(cst);
            stem.language(getStem(cst.getStem())).nl();
        }
        ShexShacl constraint = processBasic(cst.getExclusions());
        ShexShacl exclude = new ShexShacl().insert(SH_NOT, constraint);                        
        ShexShacl res;
        if (cst.getStem() instanceof WildcardConstraint) {
            res = exclude;
        }
        else {
            res = new ShexShacl().insert(SH_AND, Arrays.asList(stem, exclude));
        }        
        return res;
    }
       
    String getStem(Constraint cst) {
        if (cst instanceof IRIStemConstraint) {
            return ((IRIStemConstraint) cst).getIriStem();
        }
        else if (cst instanceof LiteralStemConstraint) {
            return ((LiteralStemConstraint) cst).getLitStem();
        }
        else if (cst instanceof LanguageStemConstraint) {
            return (((LanguageStemConstraint) cst).getLangStem());
        }
        return cst.toString();
    }
    
    
    ShexShacl process(LiteralStemConstraint cst) {
        ShexShacl shacl = new ShexShacl();
        shacl.pattern(cst.getLitStem()).nl();
        return shacl;
    }
    
    ShexShacl processLanguage(List<LanguageConstraint> list) {
        return new ShexShacl().language(list);
    }
   
    ShexShacl processLanguage(LanguageConstraint... cst) {
        return processLanguage(Arrays.asList(cst));
    }

    ShexShacl process(LanguageStemConstraint cst) {
        ShexShacl shacl = new ShexShacl();
        shacl.openList(SH_LANGUAGE_IN);
        shacl.append(cst.getLangStem());
        shacl.closeList();
        return shacl;
    }
    
    void process(FacetNumericConstraint cst) {
        if (cst.getMaxincl() != null) {
            define(SH_MAX_INCLUSIVE, cst.getMaxincl());
        }
        if (cst.getMaxexcl() != null) {
            define(SH_MAX_EXCLUSIVE, cst.getMaxexcl());
        }
        if (cst.getMinincl() != null) {
            define(SH_MIN_INCLUSIVE, cst.getMinincl());
        }
        if (cst.getMinexcl() != null) {
            define(SH_MIN_EXCLUSIVE, cst.getMinexcl());
        }
    }

    void process(FacetStringConstraint cst) {
        cst.getFlags();

        if (cst.getPatternString() != null) {
            getShacl().pattern(cst.getPatternString()).nl();
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
