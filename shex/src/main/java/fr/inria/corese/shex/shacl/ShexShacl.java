package fr.inria.corese.shex.shacl;

import static fr.inria.corese.shex.shacl.Constant.SH_INVERSEPATH;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.lille.shexjava.schema.concrsynt.LanguageConstraint;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.*;

/**
 * Contains the text of the Shex to Shacl translation
 * 
 * @author Olivier Corby - Inria I3S - 2020
 */
public class ShexShacl {
    static NSManager nsm;
    static final String SPACE = " ";
    static final String DOT = ".";
    static final String PW = ";";
    static final String OB = "[";
    static final String CB = "]";
    static final String OL = "(";
    static final String CL = ")"; 
    static final String PATH = "[%s %s]";
    static final String STRING = NSManager.XSD+"string";
    static final String NL = System.getProperty("line.separator");
    static final String title = "# Shex Shacl Translation";
    static final String sign  = "# Olivier Corby - Inria I3S - 2020";
    static final String SHEX  = "shex";
    
    private StringBuilder sb;
    
    static {
        nsm = NSManager.create();
    }
    
    
    ShexShacl() {
        sb = new StringBuilder();
    }
    
    NSManager nsm() {
        return nsm;
    }
    
    void init() {
        append(title).space().append(new Date()).nl();
        append(sign).nl();
        nl();
        defprefix("sh", NSManager.SHACL);
        defprefix(SHEX, Shex.SHEX_SHACL);
        nl();
        append("[] shex:shacl true ; rdfs:label 'Shex Shacl Translation'; ")
                .append(String.format("shex:date '%s' .", new Date()));
        nl().nl();        
    }
    
    void defprefix(String p, String ns) {
        append(String.format("prefix %s: <%s>", p, ns)).nl();
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
    
    
    String getValue(Value value) {
        if (value instanceof SimpleIRI) {
            SimpleIRI uri = (SimpleIRI) value;
            return getPrefixURI(uri.stringValue());
        }
        else if (value instanceof SimpleLiteral) {
            SimpleLiteral lit = (SimpleLiteral) value;
            return getValue(lit);
        }
        else {
            return value.toString();
        }
    }
     
    String getValue(SimpleLiteral val) {
       String type = val.getDatatype().toString();
       if (nsm().isNumber(type)) {
            return val.stringValue();
       }
       if (type.equals(STRING)) {
           String res = val.toString();
           return res.substring(0, res.indexOf("^^"));
       }
       return val.toString();
    }
    
    boolean isNumber(String type) {
        return false;
    }
     
    String getPrefixURI(String name) {
        String prop = nsm().toPrefix(name, true);
        if (name == prop) {
            return uri(name);
        }
        return prop;
    }
     
    String getValue(String value) {
        if (value.startsWith("http://")) {
            return uri(value);
        }
        return value;
    }
    
    String uri(String value) {
        return String.format("<%s>", value);
    }

    
    ShexShacl end() {
        append(DOT).nl().nl();
        return this;
    }
    
    String string(String str) {
        return String.format("\"%s\"", str);
    }
    
    ShexShacl pattern(String value) {
        define(ShexConstraint.SH_PATTERN, 
                (value.startsWith("^")) ? 
                        string(clean(value)) :
                        string(clean(value)));
        return this;
    }
    
    ShexShacl language(List<LanguageConstraint> list) {
        openList(ShexConstraint.SH_LANGUAGE_IN);
        for (LanguageConstraint cst : list) {
            append(string(cst.getLangTag())).space();
        }
        closeList();
        return this;
    }
    
     ShexShacl language(String lang) {
        openList(ShexConstraint.SH_LANGUAGE_IN);
        append(string(lang));
        closeList();
        return this;
    }
    
    String clean(String value) {
        return value.replace("\\.", "\\\\.");
    } 

    ShexShacl define(String name, String value) {
        append(name).space().append(value).pw();
        return this;
    }
    
    ShexShacl defineInverse(String name, String value) {
        append(name).space().inverse(value).pw();
        return this;
    }
    
    ShexShacl inverse(String value) {
        append(String.format(PATH, SH_INVERSEPATH, value));
        return this;
    }
    
    ShexShacl openParen() {
        append(OL);
        return this;
    }
    
    ShexShacl closeParen() {
        append(CL);
        return this;
    }

    ShexShacl define(String name, List<String> list) {
        append(name).space().openParen();
        for (String val : list) {
            append(val).space();
        }
        closeParen().pw();
        return this;
    }

    ShexShacl define(String name, BigDecimal value) {
        append(name).space().append(value).pw();
        return this;
    }
    
     ShexShacl define(String name, int value) {
        append(name).space().append(value).pw();
        return this;
    }
     
     
    ShexShacl append(Value val) {
        getStringBuilder().append(getValue(val));
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
    
    ShexShacl pw() {
        getStringBuilder().append(PW);
        return this;
    }
    
    ShexShacl space() {
        getStringBuilder().append(SPACE);
        return this;
    }
    
    ShexShacl openBracket(String name) {
       append(name).space().openBracket().nl();
       return this;
    }
    
    ShexShacl openBracket() {
       append(OB);
       return this;
    }
    
    ShexShacl closeBracket() {
        append(CB);
        return this;
    }
    
    ShexShacl closeBracketPw() {
        closeBracket().pw();
        return this;
    }

    ShexShacl openList(String name) {
       append(name).space().openParen().nl();
       return this;
    }
    
    ShexShacl closeList() {
        closeParen().pw().nl();
        return this;
    }
    
    ShexShacl inBracket(ShexShacl sh) {
        openBracket().append(sh.toString()).closeBracket();
        return this;
    }
    
    ShexShacl insert(String name, List<ShexShacl> shaclList) {
        append(name).space().openParen();
        for (ShexShacl sh : shaclList) {
            inBracket(sh).nl();
        }
        closeParen();
        return this;
    }
    
    ShexShacl insert(String name, ShexShacl shacl) {
        return append(name).space().inBracket(shacl);
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
