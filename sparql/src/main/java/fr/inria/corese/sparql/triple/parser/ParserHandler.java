package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.Creator;
import fr.inria.corese.sparql.triple.javacc1.ParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import fr.inria.corese.sparql.triple.javacc1.Token;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class ParserHandler {
    public static Logger logger = LoggerFactory.getLogger(ParserHandler.class);
   
    static final String SQ3  = "\"\"\"";
    static final String SSQ3 = "'''";
    public static boolean rdf_star_validation = false;
    
    boolean insideDelete = false;
    boolean insideDeleteData = false;
    private boolean insideValues = false;
    int countWhere = 0;
    private boolean function = false;

    // Broker to target Graph in Load context
    // Turtle Loader create Edge(g s p o) directly in the graph
    Creator create;
    SparqlCorese parser;
    private Metadata metadata;
    private ArrayList<EngineException> errorList;
    
    ParserHandler() {
        errorList = new ArrayList<>();
    }
         
    public void setParser(SparqlCorese parser) {
        this.parser = parser;
    }
    
    public void setCreator(Creator c) {
        create = c;
    }
      
    // s p o1, .. on
    public Exp createTriples(ASTQuery ast, Exp stack, Expression subject, Atom p, ExpressionList objectList, int n)
            throws ParseException {
        ArrayList<Triple> tripleList = new ArrayList<>();
        
        for (Expression object : objectList) {
            Triple t = genericCreateTriple(ast, subject.getAtom(), p, object.getAtom()); 
            if (t != null) {
                stack.add(n++, t);
                if (object.getAtom().getTripleReference()!=null) {
                    tripleList.add(t);
                }
            }
        }
        
        annotate(tripleList, stack);
        return stack;
    }
    
    void annotate(List<Triple> tripleList, Exp stack) {
        for (Triple t : tripleList) {
            annotate(t, stack);
        }
    }
    
    /**
     * 
     * @param triple: s p o t st exists t q v in stack
     * @param stack 
     */
    void annotate(Triple triple, Exp stack) {
        for (Exp exp : stack) {
            if (exp.isTriple()) {
                Triple tr = exp.getTriple();
                if (triple.getObject().getTripleReference() == tr.getSubject()) {
                    triple.getCreateTripleList().add(tr);
                }
            }
        }
    }
    
    Triple genericCreateTriple(ASTQuery ast, Atom s, Atom p, Atom o) throws ParseException {
        if (o.getTripleReference()==null) {
            return createTriple(ast, s, p, o);
        }
        else {
            // s p o {| q v |} ->
            // triple(s p o t) . t q v
            // t = o.getAtom().getTripleReference()
            // create: s p o t  
            return createTripleStar(ast, s, p, o, o.getTripleReference());
        }
    }

    public Triple createTriple(ASTQuery ast, Expression s, Atom p, Expression o) throws ParseException {
        if (create != null) {
            // load turtle
            if (!create.accept(s.getAtom(), p, o.getAtom())) {
                throw parser.generateParseException();
            }
            create.triple(s.getAtom(), p, o.getAtom());
            return null;
        } else {
            // sparql parser
            return ast.createTriple(s, p, o);
        }
    }
    
    public Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity) {
        return createTriple(ast, p, list, matchArity, false);
    }

    Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity, boolean nested) {
        if (create == null) {
            // sparql query
            Triple t = ast.createTriple(p, list, nested);
            t.setMatchArity(matchArity);
            return t;
        } else {
            // load turtle
            create.triple(p, list, nested);
            return null;
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
    

    /**
     * <<s p o>> 
     * return Constant cst(dt) with: 
     * dt=bnode triple reference isTriple() == true 
     * when sparql: cst.triple = triple(s p o) 
     * when load:   cst.triple = null, edge created in graph directly
     *
     */
    public Atom createNestedTripleStar(ASTQuery ast, Exp stack, Atom s, Atom p, Atom o, Atom v) {
        if (s.isLiteral() && rdf_star_validation) {
            logger.error("RDF star illegal subject: " + s);
            getErrorList().add(new EngineException("RDF star illegal subject: " + s));
        }
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
        if (p.getExpression() != null) {
            // predicate has ppath regex: error
            errorList.add(new EngineException(
                    String.format("Illegal regex in RDF star triple: %s %s %s", s, p.getExpression(), o)));
            logger.error(String.format("Illegal regex in RDF star triple: %s %s %s", s, p.getExpression(), o));
        }
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
        Atom ref;
        if (isLoad() || isInsideValues()) {
            // Constant with Datatype Blank Node with isTriple() == true
            // Once in the graph, Datatype will contain Edge(s p o t)
            ref = ast.tripleReferenceDefinition();
        }
        else if (var != null) {
            ref = var;
        }
        else if (isInsideWhere()) { 
            if (ast.isUpdate()) {
                // delete works with a variable, not with a bnode
                // use case: delete where {} and delete is empty 
                // Variable with isTriple() == true
                ref = ast.tripleReferenceVariable();
            }
            else {
                // Variable with isBlankNode() == true and isTriple() == true
                // not returned by select *
                ref = ast.tripleReferenceQuery();                
            }
        }
        else {
        // insert delete data
        // insert delete -- where
        // construct     -- where
            ref = ast.tripleReferenceDefinition();   
        }
        
        return ref;
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

    public boolean isInsideValues() {
        return insideValues;
    }

    public void setInsideValues(boolean insideValues) {
        this.insideValues = insideValues;
    }

    public ArrayList<EngineException> getErrorList() {
        return errorList;
    }

    public void setErrorList(ArrayList<EngineException> errorList) {
        this.errorList = errorList;
    }
    

    
}
