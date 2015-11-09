package fr.inria.edelweiss.kgraph.approximate.result;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Expr;

/**
 * Key.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 nov. 2015
 */
public class Key {

    private final Expr var;
    private IDatatype uri = null;

    public Key(Expr var, IDatatype uri) {
        this(var);
        this.uri = uri;
    }

    public Key(Expr var) {
        this.var = var;
    }

    public static Key create(String var) {
        return new Key(Variable.create(var));
    }

    public Expr getVar() {
        return var;
    }

    public IDatatype getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Key other = (Key) obj;
        if (this.var != other.var && (this.var == null || !this.var.equals(other.var))) {
            return false;
        }
        //only that var is equal is ok
//        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
//            return false;
//        }
        return true;
    }

    @Override
    public String toString() {
        return "Key{" + "var=" + var + ", uri=" + uri + '}';
    }
}
