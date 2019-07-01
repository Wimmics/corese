package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.Creator;
import fr.inria.corese.sparql.triple.javacc1.ParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class ParserHandler {
    
    static final String SQ3  = "\"\"\"";
    static final String SSQ3 = "'''";
    
    boolean insideWhere = false;

    Creator create;
    SparqlCorese parser;
    private Metadata metadata;
         
    public void setParser(SparqlCorese parser) {
        this.parser = parser;
    }
    
    public void setCreator(Creator c) {
        create = c;
    }
       
    public Exp createTriples(ASTQuery ast, Exp stack, Expression e1, Atom p, ExpressionList list, int n)
            throws ParseException {
        for (Expression e2 : list) {
            Exp e = createTriple(ast, e1, p, e2);
            if (e != null) {
                stack.add(n++, e);
            }
        }
        return stack;
    }

    public Triple createTriple(ASTQuery ast, Expression s, Atom p, Expression o) throws ParseException {
        if (create != null) {
            if (!create.accept(s.getAtom(), p, o.getAtom())) {
                throw parser.generateParseException();
            }
            create.triple(s.getAtom(), p, o.getAtom());
            return null;
        } else {
            return ast.createTriple(s, p, o);
        }
    }

    public Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity) {
        if (create != null) {
            create.triple(p, list);
            return null;
        } else {
            Triple t = ast.createTriple(p, list);
            t.setMatchArity(matchArity);
            return t;
        }
    }

    public Triple createTriple(ASTQuery ast, Atom p, Atom s, Atom o, Atom v) {
        ArrayList<Atom> list = new ArrayList<Atom>();
        list.add(s);
        list.add(o);
        list.add(v);
        return createTriple(ast, p, list, true);
    }

    public void graphPattern(Atom g) {
        if (create != null) {
            create.graph(g.getConstant());
        }
    }

    public void endGraphPattern(Atom g) {
        if (create != null) {
            create.endGraph(g.getConstant());
        }
    }

    public Atom list(ASTQuery ast, Exp stack, List<Atom> l, int arobase) {
        RDFList rlist = ast.createRDFList(l, arobase);

        if (create != null) {
            create.list(rlist);
        } else {
            stack.add(rlist);
        }

        return rlist.head();
    }

    /**
     * Replace escape char by target char
     *
     */
    public String remEscapes(String str) {
        StringBuilder retval = new StringBuilder();

        // remove leading/trailing " or '
        int start = 1, end = str.length() - 1;

        if ((str.startsWith(SQ3) && str.endsWith(SQ3))
         || (str.startsWith(SSQ3) && str.endsWith(SSQ3))) {
            // remove leading/trailing """ or '''
            start = 3;
            end = str.length() - 3;
        }

        for (int i = start; i < end; i++) {

            if (str.charAt(i) == '\\' && i + 1 < str.length()) {
                i += 1;
                switch (str.charAt(i)) {

                    case 'b':
                        retval.append('\b');
                        continue;
                    case 't':
                        retval.append('\t');
                        continue;
                    case 'n':
                        retval.append('\n');
                        continue;
                    case 'f':
                        retval.append('\f');
                        continue;
                    case 'r':
                        retval.append('\r');
                        continue;
                    case '"':
                        retval.append('\"');
                        continue;
                    case '\'':
                        retval.append('\'');
                        continue;
                    case '\\':
                        retval.append('\\');
                        continue;
                }

            } else {
                retval.append(str.charAt(i));
            }
        }

        return retval.toString();
    }
    
    public void enterWhere() {
        insideWhere = true;
    }
    
    public void leaveWhere() {
        insideWhere = false;
    }
    
    public boolean isInsideWhere() {
        return insideWhere;
    }
    
    boolean isLoad() {
        return create != null;
    }
    
    /**
     * Generate ref st:
     * <<s p o>> q v
     * triple(s p o ref) . ref q v
     */    
    public Atom createTripleReference(ASTQuery ast, Atom var) {
        if (isLoad()) {
            return ast.tripleReferenceDefinition();
        }
        if (var != null) {
            return var;
        }
        if (isInsideWhere()) { 
            if (ast.isUpdate()) {
                // use case: delete where { <<s p o>> q v }
                // delete works with a variable, not with a bnode
                return ast.tripleReferenceVariable();
            }
            else {
                return ast.tripleReferenceQuery();                
            }
        }
        // insert delete data
        // insert delete -- where
        // construct     -- where
        return ast.tripleReferenceDefinition();       
    }

    /**
     * @return the metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    public void cleanMetadata() {
        setMetadata(null);
    }

    
}
