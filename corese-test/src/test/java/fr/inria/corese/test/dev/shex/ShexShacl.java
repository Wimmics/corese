package fr.inria.corese.test.dev.shex;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author corby
 */
public class ShexShacl {
    
    private StringBuilder sb;
    
    ShexShacl() {
        sb = new StringBuilder();
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
    
    ShexShacl define(String name, String value) {
        getStringBuilder().append(name).append(" ").append(value).append(";");
        return this;
    }

    ShexShacl define(String name, List<String> list) {
        getStringBuilder().append(name).append(" (");
        for (String val : list) {
            getStringBuilder().append(val).append(" ");
        }
        getStringBuilder().append(");");
        return this;
    }

    ShexShacl define(String name, BigDecimal value) {
        getStringBuilder().append(name).append(" ").append(value).append(";");
        return this;
    }
    
     ShexShacl define(String name, int value) {
        getStringBuilder().append(name).append(" ").append(value).append(";");
        return this;
    }
    
    ShexShacl append(String str) {
        getStringBuilder().append(str);
        return this;
    }

    ShexShacl nl() {
        getStringBuilder().append("\n");
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
        return append(name).append(" ").inBracket(shacl);
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
