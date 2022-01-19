package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.Creator;
import fr.inria.corese.sparql.triple.javacc1.ParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import fr.inria.corese.sparql.triple.javacc1.Token;
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
    
    boolean insideDelete = false;
    boolean insideDeleteData = false;
    int countWhere = 0;
    private boolean function = false;

    Creator create;
    SparqlCorese parser;
    private Metadata metadata;
         
    public void setParser(SparqlCorese parser) {
        this.parser = parser;
    }
    
    public void setCreator(Creator c) {
        create = c;
    }
      
    // s p o1, .. on
    public Exp createTriples(ASTQuery ast, Exp stack, Expression e1, Atom p, ExpressionList list, int n)
            throws ParseException {
        for (Expression e2 : list) {
            Exp e = genericCreateTriple(ast, e1.getAtom(), p, e2.getAtom()); //createTriple(ast, e1, p, e2);
            if (e != null) {
                stack.add(n++, e);
            }
        }
        return stack;
    }
    
    Triple genericCreateTriple(ASTQuery ast, Atom s, Atom p, Atom o) throws ParseException {
        if (o.getAtom().getTripleReference()!=null) {
            return createTripleStar(ast, s, p, o, o.getAtom().getTripleReference());
        }
        else {
            return createTriple(ast, s, p, o);
        }
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
        return createTriple(ast, p, list, matchArity, false);
    }

    Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity, boolean nested) {
        if (create != null) {
            create.triple(p, list, nested);
            return null;
        } else {
            Triple t = ast.createTriple(p, list, nested);
            t.setMatchArity(matchArity);
            return t;
        }
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
    

    // <<s p o>>
    public Atom createNestedTripleStar(ASTQuery ast, Exp stack, Atom s, Atom p, Atom o, Atom v) {
        Atom ref = createTripleReference(ast, v);
        return createTripleStar(ast, stack, s, p, o, ref, true);
    }

   
    
    Atom createTripleStar(ASTQuery ast, Exp stack, Atom s, Atom p, Atom o, Atom ref, boolean nested) {
        Triple t = createTripleStar(ast, s, p, o, ref, nested);
        if (t != null) {
            // sparql query (not load turtle)
            stack.add(t);
        }
        return ref;
    }
    
    Triple createTripleStar(ASTQuery ast, Atom s, Atom p, Atom o, Atom ref) {
        return createTripleStar(ast, s, p, o, ref, false);
    }
      
    Triple createTripleStar(ASTQuery ast, Atom s, Atom p, Atom o, Atom ref, boolean nested) {
        ArrayList<Atom> list = new ArrayList<>();
        list.add(s);
        list.add(o);
        list.add(ref);
        return createTriple(ast, p, list, true, nested);
    }
    
    /**
     * Generate ref st:
     * <<s p o>> q v
     * triple(s p o ref) . ref q v
     */   
    public Atom createTripleReference(ASTQuery ast) {
        return createTripleReference(ast, null);
    }
    
    public Atom createTripleReference(ASTQuery ast, Atom var) {
        if (isLoad()) {
            return ast.tripleReferenceDefinition();
        }
        if (var != null) {
            return var;
        }
        if (isInsideWhere()) { 
            if (ast.isUpdate()) {
                // delete (insert) works with a variable, not with a bnode
                // use case: delete where {} and delete is empty 
                return ast.tripleReferenceVariable();
            }
            else {
                // variable isBlankNode() == true
                // not returned by select *
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

    /**
     * @return the function
     */
    public boolean isFunction() {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(boolean function) {
        this.function = function;
    }
    
    /**
     * pragma: name is variable without ? and $
     * are we in LDScript or in SPARQL ?
     * 
     */
    public void checkVariable(Token name)  {
        if (! isFunction()) {
            System.out.println("Incorrect Variable: " + name + " Line: " + name.beginLine);
            throw new Error("Incorrect Variable: " + name + " Line: " + name.beginLine);
        }
    }
    
        public void enterWhere() {
        countWhere++;
    }
    
    public void leaveWhere() {
        countWhere--;
    }
    
    
    public boolean isInsideWhere() {
        return countWhere>0;
    }
    
    public void enterDelete() {
        insideDelete =  true;
    }
    
    public void leaveDelete() {
        insideDelete = false;
    }
    
    
    public boolean isInsideDelete() {
        return insideDelete;
    }
    
    public void enterDeleteData() {
        insideDeleteData =  true;
    }
    
    public void leaveDeleteData() {
        insideDeleteData = false;
    }
    
    
    public boolean isInsideDeleteData() {
        return insideDeleteData;
    }
    
    boolean isLoad() {
        return create != null;
    }
    

    
}
