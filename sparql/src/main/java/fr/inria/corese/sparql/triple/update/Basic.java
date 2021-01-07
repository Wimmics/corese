package fr.inria.corese.sparql.triple.update;

import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import fr.inria.corese.sparql.triple.parser.ASTPrinter;
import fr.inria.corese.sparql.triple.parser.And;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Triple;

/**
 * load clear drop create add move copy
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Basic extends Update {

    static final String SILENT = "silent";
    static final String DEFAUT = "default";
    static final String ALL = "all";
    static final String NAMED = "named";
    static final String INTO = "into";
    static final String TO = "to";
    static final String GRAPH = "graph";

    boolean defaut = false,
            silent = false,
            named = false,
            all = false;

    String uri, graph, target;
    Constant auri, agraph, atarget;
    // for additional prefix namespace, only for pretty print
    Exp prolog = new And();

    Basic(int t) {
        type = t;
    }

    @Override
    public boolean isBasic() {
        return true;
    }

    @Override
    public Basic getBasic() {
        return this;
    }

    @Override
    public boolean isInsert() {
        return type() == INSERT;
    }

    @Override
    public boolean isDelete() {
        return type() == DELETE;
    }
    
    @Override
    public boolean isLoad() {
        return type() == LOAD;
    }

    public static Basic create(int type) {
        Basic b = new Basic(type);
        return b;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {

        switch (type()) {

            case PROLOG:
                prolog(sb);
                return sb;
        }

        sb.append(title());

        if (silent) {
            sb.append(" " + SILENT);
        }

        switch (type()) {

            case LOAD:
                if (auri != null) {
                    sb.append(" " + auri);
                }
                if (atarget != null) {
                    sb.append(" " + INTO + " " + GRAPH + " " + atarget);
                }
                break;

            case ADD:
            case MOVE:
            case COPY:
                if (agraph != null) {
                    sb.append(" " + GRAPH + " " + agraph);
                } else {
                    sb.append(" " + DEFAUT);
                }

                sb.append(" " + TO + " ");

                if (atarget != null) {
                    sb.append(atarget);
                } else {
                    sb.append(DEFAUT);
                }
                break;

            case CLEAR:
            case DROP:
            case CREATE:
                if (agraph != null) {
                    sb.append(" " + GRAPH + " " + agraph);
                }
                if (named) {
                    sb.append(" " + NAMED);
                }
                if (all) {
                    sb.append(" " + ALL);
                }
                if (defaut) {
                    sb.append(" " + DEFAUT);
                }
        }

        return sb;
    }

    public boolean hasContent() {
        return prolog.size() > 0;
    }

    void prolog(ASTBuffer sb) {
        ASTPrinter pr = new ASTPrinter(getASTUpdate().getASTQuery());
        pr.getSparqlPrefix(prolog, sb);
    }

    public void defNamespace(String prefix, String ns) {
        if (prefix.endsWith(":")) {
            prefix = prefix.substring(0, prefix.length() - 1); // remove :
        }
        Triple triple = Triple.createNS(
                Constant.create(KeywordPP.PREFIX), Constant.create(prefix),
                Constant.create(ns));
        prolog.add(triple);
    }

    public void defBase(String ns) {
        Triple triple = Triple.createNS(
                Constant.create(KeywordPP.BASE), Constant.create(""),
                Constant.create(ns));
        prolog.add(triple);
    }

    public void setSilent(boolean b) {
        silent = b;
    }

    public void setDefault(boolean b) {
        defaut = b;
    }

    public void setNamed(boolean b) {
        named = b;
    }

    public void setAll(boolean b) {
        all = b;
    }

    public boolean isDefault() {
        return defaut;
    }

    public boolean isNamed() {
        return named;
    }

    public boolean isAll() {
        return all;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setGraph(String g) {
        graph = g;
    }

    public void setGraph(Constant g) {
        agraph = g;
    }

    public void setTarget(String t) {
        target = t;
    }

    public void setTarget(Constant t) {
        atarget = t;
    }

    public Constant getCTarget() {
        return atarget;
    }

    public void setURI(String t) {
        uri = t;
    }

    public void setURI(Constant t) {
        auri = t;
    }

    public Constant getCURI() {
        return auri;
    }

    @Override
    public Constant getGraphName() {
        if (getCTarget() != null) {
            // load into graph
            return getCTarget();
        }
        return agraph;
    }

    public String getGraph() {
        if (agraph == null) {
            return null;
        }
        return agraph.getLongName();
    }

    public String getTarget() {
        if (atarget == null) {
            return null;
        }
        return atarget.getLongName();
    }

    public String getURI() {
        if (auri == null) {
            return null;
        }
        return auri.getLongName();
    }

}
