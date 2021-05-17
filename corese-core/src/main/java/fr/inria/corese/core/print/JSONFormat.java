package fr.inria.corese.core.print;

import java.util.ArrayList;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import java.util.List;

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
    private static final String OPEN_VAR ="\"vars\": [";
    private static final String CLOSE_VAR ="]";   
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
        return JSONFormat.create(q, (ASTQuery) q.getAST(), lm);
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
        // print variable or functions selected in the header
        println(OPEN_VAR);
        printVar(getSelect());
        println(CLOSE_VAR);
        printLnk(lMap.getLinkList());
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
    
    void printLnk(List<String> list) {
        for (String name : list) {
            print(", \n\"link\": [\"" + name + "\"]");
        }
    }
    
    

    @Override
    public void printAsk() {
        String res = "true";
        if (lMap == null || lMap.size() == 0) {
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
        int i = 0, n = 1;
        if (getnBind() > 0) {
            print(",\n");
        }
        incrnBind();
        String name = getName(var);

        String open = "";
        if (i == 0) {
            open = "\"" + name + "\": ";
            if (n > 1) {
                open += "[";
            }
        }

        open += "{ \"type\": ";
        print(open);
        String str = dt.getLabel();

        if (dt.isLiteral()) {
            if (dt.hasLang()) {
                print("\"literal\", \"xml:lang\": \"" + dt.getLang() + "\"");
            } else if (dt.getCode() == IDatatype.LITERAL) {
                print("\"literal\"");
            } else {
                if (DatatypeMap.isDouble(dt)) {
                    str = String.format("%1.5g",dt.doubleValue());
                }
                print("\"typed-literal\", \"datatype\": \"" + dt.getDatatype().getLabel() + "\"");
            }
        } else if (dt.isBlank()) {
            print("\"bnode\"");
            if (str.startsWith(BLANK)) {
                str = str.substring(BLANK.length());
            }
        } else {
            print("\"uri\"");
        }

        print(", \"value\": \"" + JSONFormat.addJSONEscapes(str) + "\"}");

        if (n > 1 && i == n - 1) {
            print("]");
        }
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
     * source: javacc replace special char by escape char for pprint
     * This function is needed because JSON format does not accept escaped single quotes which are possibly returned by Constant.addEscape().
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
}
