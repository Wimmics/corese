package fr.inria.corese.shex;

import fr.inria.lille.shexjava.schema.concrsynt.LanguageConstraint;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class ShexShacl {
    static final String SPACE = " ";
    static final String DOT = ".";
    static final String NL = System.getProperty("line.separator");
    static final String title = "# Shacl generated from Shex  ";
    static final String sign  = "# Olivier Corby - Inria I3S - 2020";
    
    private StringBuilder sb;
    
    ShexShacl() {
        sb = new StringBuilder();
    }
    
    void init() {
        append(title).append(new Date()).nl();
        append(sign).nl();
        nl();
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
    
    ShexShacl end() {
        getStringBuilder().append(DOT);
        nl().nl();
        return this;
    }
    
    ShexShacl pattern(String value) {
        define(ShexConstraint.SH_PATTERN, 
                (value.startsWith("^")) ? 
                        String.format("\"%s\"", clean(value)) :
                        String.format("\"^%s\"", clean(value)));
        return this;
    }
    
    ShexShacl language(List<LanguageConstraint> list) {
        openList(ShexConstraint.SH_LANGUAGE_IN);
        for (LanguageConstraint cst : list) {
            append(String.format("\"%s\"", cst.getLangTag())).space();
        }
        closeList();
        return this;
    }
    
     ShexShacl language(String lang) {
        openList(ShexConstraint.SH_LANGUAGE_IN);
        append(String.format("\"%s\"",lang));
        closeList();
        return this;
    }
    
    String clean(String value) {
        return value.replace("\\.", "\\\\.");
    } 

    ShexShacl define(String name, String value) {
        getStringBuilder().append(name).append(SPACE).append(value).append(";");
        return this;
    }

    ShexShacl define(String name, List<String> list) {
        getStringBuilder().append(name).append(" (");
        for (String val : list) {
            getStringBuilder().append(val).append(SPACE);
        }
        getStringBuilder().append(");");
        return this;
    }

    ShexShacl define(String name, BigDecimal value) {
        getStringBuilder().append(name).append(SPACE).append(value).append(";");
        return this;
    }
    
     ShexShacl define(String name, int value) {
        getStringBuilder().append(name).append(SPACE).append(value).append(";");
        return this;
    }
    
    ShexShacl append(String str) {
        getStringBuilder().append(str);
        return this;
    }
    
    ShexShacl append(Object str) {
        getStringBuilder().append(str);
        return this;
    }

    ShexShacl nl() {
        getStringBuilder().append(NL);
        return this;
    }
    
    ShexShacl space() {
        getStringBuilder().append(SPACE);
        return this;
    }
    
    ShexShacl openBracket(String name) {
       append(name).space().append("[").nl();
       return this;
    }
    
    ShexShacl openBracket() {
       append("[");
       return this;
    }
    
    ShexShacl closeBracket() {
        append("];");
        return this;
    }

    ShexShacl openList(String name) {
       append(name).space().append("(").nl();
       return this;
    }
    
    ShexShacl closeList() {
        append(");").nl();
        return this;
    }
    
    ShexShacl inBracket(ShexShacl sh) {
        append("[").append(sh.toString()).append("]");
        return this;
    }
    
    ShexShacl insert(String name, List<ShexShacl> shaclList) {
        append(name).append(" (");
        for (ShexShacl sh : shaclList) {
            inBracket(sh).nl();
        }
        append(")");
        return this;
    }
    
    ShexShacl insert(String name, ShexShacl shacl) {
        return append(name).append(SPACE).inBracket(shacl);
    }

    
    ShexShacl append(ShexShacl sh) {
        append(sh.toString());
        return this;
    }

    
    public StringBuilder getStringBuilder() {
        return sb;
    }

    
    public void setStringBuilder(StringBuilder sb) {
        this.sb = sb;
    }
    
}
