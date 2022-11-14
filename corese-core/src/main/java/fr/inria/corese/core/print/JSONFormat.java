package fr.inria.corese.core.print;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 * SPARQL JSON Result Format for KGRAM Mappings
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class JSONFormat extends XMLFormat {

    private static final String OHEADER = "{";
    private static final String CHEADER = "}";
    private static final String OHEAD = "\"head\": { \n";
    private static final String CHEAD = "},";
    private static final String OPEN_VAR = "\"vars\": [";
    private static final String CLOSE_VAR = "]";
    private static final String OHEADASK = "\"head\": { } ,";
    private static final String CHEADASK = "";
    private static final String OVAR = "";
    private static final String CVAR = ",";
    private static final String ORESULTS = "\"results\": { \"bindings\": [";
    private static final String CRESULTS = "] }";
    private static final String ORESULT = "{";
    private static final String CRESULT = "\n}";
    private static final String BOOLEAN = "\"boolean\" : ";
    private static final String BLANK = "_:";

    int nBind = 0, nResult = 0;

    JSONFormat(Mappings lm) {
        super(lm);
    }

    JSONFormat() {
    }

    public static JSONFormat create(Mappings lm) {
        Query q = lm.getQuery();
        return JSONFormat.create(q, q.getAST(), lm);
    }

    public static JSONFormat create(Query q, ASTQuery ast, Mappings lm) {
        JSONFormat res;
        res = new JSONFormat(lm);
        res.setQuery(q, lm);
        res.setAST(ast);
        return res;
    }

    @Override
    public String getTitle(Title t) {
        switch (t) {
            case OHEADER:
                return OHEADER;
            case CHEADER:
                return CHEADER;
            case OHEAD:
                if (ast.isAsk()) {
                    return OHEADASK;
                }
                return OHEAD;
            case CHEAD:
                if (ast.isAsk()) {
                    return CHEADASK;
                }
                return CHEAD;
            case OVAR:
                return OVAR;
            case CVAR:
                return CVAR;
            case ORESULTS:
                return ORESULTS;
            case CRESULTS:
                return CRESULTS;
            case ORESULT:
                return ORESULT;
            case CRESULT:
                return CRESULT;
            default:
                return "";
        }
    }

    @Override
    public void printHead() {
        println(getTitle(Title.OHEAD));
        if (!ast.isAsk()) {
            // print variable or functions selected in the header
            println(OPEN_VAR);
            printVar(getSelect());
            println(CLOSE_VAR);
        }
        printLnk(getMappings().getLinkList());
        println(getTitle(Title.CHEAD));
    }

    void printVar(ArrayList<String> select) {
        int n = 1;
        for (String var : select) {
            print("\"" + getName(var) + "\"");
            if (n++ < select.size()) {
                print(", ");
            }
        }
    }

    void printLnk2(List<String> list) {
        for (String name : list) {
            print(", \n\"link\": [\"" + name + "\"]");
        }
    }

    void printLnk(List<String> list) {
        if (list.isEmpty()) {
            return;
        }
        print(", \n\"link\": [");
        int n = 0;
        for (String name : list) {
            if (n++ > 0) {
                print(", ");
            }
            print("\"" + name + "\"");
        }
        print("]");
    }

    @Override
    public void printAsk() {
        String res = "true";
        if (getMappings() == null || getMappings().size() == 0) {
            res = "false";
        }
        print(BOOLEAN);
        println(res);
    }

    @Override
    void display(String var, IDatatype dt) {
        if (dt == null) {
            // do nothing
            return;
        }
        if (getnBind() > 0) {
            print(",\n");
        }
        incrnBind();
        print(String.format("\"%s\": ", getName(var)));
        display(dt);
    }

    @Override
    void display(IDatatype dt) {
        if (dt.isTriple() && dt.getEdge() != null) {
            triple(dt);
        } else if (dt.isList()) {
            list(dt);
        } else {
            term(dt);
        }
    }

    void triple(IDatatype dt) {
        Edge e = dt.getEdge();
        println("{ \"type\": \"triple\",");
        println("\"value\": {");

        print("\"subject\": ");
        display(e.getSubjectValue());
        println(",");

        print("\"predicate\": ");
        display(e.getPredicateValue());
        println(",");

        print("\"object\": ");
        display(e.getObjectValue());
        println();
        print("}}");
    }

    void list(IDatatype list) {
        println("{ \"type\": \"list\",");
        println("\"value\": [");
        int i = 0;
        for (IDatatype dt : list) {
            if (i++ > 0) {
                print(", ");
            }
            display(dt);
        }
        print("]}");
    }

    void term(IDatatype dt) {
        print("{ \"type\": ");
        String str = dt.getLabel();

        if (dt.isLiteral()) {
            if (dt.hasLang()) {
                printf("\"literal\", \"xml:lang\": \"%s\"", dt.getLang());
            } else if (dt.getCode() == IDatatype.LITERAL) {
                print("\"literal\"");
            } else {
                if (DatatypeMap.isDouble(dt)) {
                    str = String.format("%1.5g", dt.doubleValue());
                } else if (dt.isExtension()) {
                    str = dt.getContent();
                }
                printf("\"typed-literal\", \"datatype\": \"%s\"",
                        dt.getDatatype().getLabel());
            }
        } else if (dt.isBlank()) {
            print("\"bnode\"");
            if (str.startsWith(BLANK)) {
                str = str.substring(BLANK.length());
            }
        } else {
            print("\"uri\"");
        }

        printf(", \"value\": \"%s\"", JSONFormat.addJSONEscapes(str));
        print("}");
    }

    @Override
    void newResult() {
        nBind = 0;
        if (nResult++ > 0) {
            print(",\n");
        }
    }

    void incrnBind() {
        nBind++;
    }

    int getnBind() {
        return nBind;
    }

    /**
     * source: javacc replace special char by escape char for pprint This
     * function is needed because JSON format does not accept escaped single
     * quotes which are possibly returned by Constant.addEscape().
     *
     * @param str The string to be escaped
     * @return the escaped string
     */
    public static String addJSONEscapes(String str) {
        StringBuilder retval = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
                    continue;
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    retval.append(str.charAt(i));
                    continue;
            }
        }
        return retval.toString();
    }

    @Override
    void error() {
        boolean b1 = ast != null && ast.getErrors() != null;
        boolean b2 = query != null && query.getErrors() != null;
        boolean b3 = query != null && query.getInfo() != null;

        String errorString = "";

        if (b1 || b2 || b3) {

            if (ast.getText() != null) {
                errorString += protect(ast.getText());
            }

            if (b1) {
                for (String mes : ast.getErrors()) {
                    errorString += protect(mes);
                }
            }
            if (b2) {
                for (String mes : query.getErrors()) {
                    errorString += protect(mes);
                }
            }
            if (b3) {
                for (String mes : query.getInfo()) {
                    errorString += protect(mes);
                }
            }

            println("\"error\" : \"" + escape(errorString) + "\",");
        }
    }

    private String escape(String raw) {
        String escaped = raw;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\b", "\\b");
        escaped = escaped.replace("\f", "\\f");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        // TODO: escape other non-printing characters using uXXXX notation
        return escaped;
    }
}
