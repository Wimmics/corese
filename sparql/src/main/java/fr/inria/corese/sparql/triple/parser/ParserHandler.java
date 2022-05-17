package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.Creator;
import fr.inria.corese.sparql.triple.javacc1.ParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import fr.inria.corese.sparql.triple.javacc1.Token;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
    boolean turtleLoader = false;

    // Broker to target Graph in Load context
    // Turtle Loader create Edge(g s p o) directly in the graph
    private Creator create;
    SparqlCorese parser;
    private Metadata metadata;
    private ArrayList<EngineException> errorList;
    
    ParserHandler() {
        errorList = new ArrayList<>();
    }
         
    public void setParser(SparqlCorese parser) {
        this.parser = parser;
    }
    
    // create rdf graph Edge directly for rdf loader
    public void setCreator(Creator c) {
        setCreate(c);
        if (c != null) {
            setTurtleLoader(true);
        }
    }
      

    public Exp createTriples(ASTQuery ast, Exp stack, Expression subject, Atom predicate, ExpressionList objectList, int n)
            throws ParseException {
        for (Expression object : objectList) {
            createTripleWithAnnotation(ast, stack, subject.getAtom(), predicate, object.getAtom());
        }
        return stack;
    }
    
    /**
     * s p o1, o2, oi
     * we may have s p o whith o.reference = t and o.annotation = (t q v)
     * create triple s p o t and add its annotation t q v after triple in stack
     */        
    Exp createTripleWithAnnotation(ASTQuery ast, Exp stack, Atom subject, Atom predicate, Atom object) 
            throws ParseException {
        
        boolean turtleLoaderStatus = isTurtleLoader();
        
        if (stack.isStack() && isTurtleLoader()) {
            // create annotation triple t q v
            // switch to sparql parser mode and create/record annotation Triple instead of Edge
            // annotation Edge will be created from triple later by processTurtleAnnotation() 
            // when subject Edge s p o t will be created 
            // in order to get subject ref ID Node of subject Edge
            setTurtleLoader(false);
        }
        
        Triple triple = genericCreateTriple(ast, subject, predicate, object);

        if (isTurtleLoader()) {
            // edge created in graph
            processTurtleAnnotation(ast, object);
        } else {
            // triple created in stack
            stack.add(triple); // stack.add(n++, triple);
            processSparqlAnnotation(ast, stack, triple, object);            
        }
        
        setTurtleLoader(turtleLoaderStatus);
        
        return stack;
    }
    
    /**
     * Turtle loader create triple = s p o t where object = o
     * o.annotation = (t q v)
     * create Edge t q v now from stacked triple t q v 
     * in order to get rdf ID graph node for t
     */
    void processTurtleAnnotation(ASTQuery ast, Atom object) throws ParseException {
        if (object.getAnnotation() != null) {
            for (Exp ee : object.getAnnotation()) {
                // create edge for annotation triple t that have been stacked
                Triple t = ee.getTriple();
                genericCreateTriple(ast, t.getSubject(), t.getPredicate(), t.getObject());
            }
        }
    }
    
    /**
     * Sparql parser create triple = s p o t where object = o
     * o.annotation = (t q v) 
     */
    void processSparqlAnnotation(ASTQuery ast, Exp stack, Triple triple, Atom object) {
        if (object.getAnnotation() != null) {
            // triple is annotated by a list of triple
            // add annotation of triple in stack after t 
            // ref ID t will be created in target graph before use of t
            // in annotation
            for (Exp ee : object.getAnnotation()) {
                // add annotation of triple after triple in stack
                stack.add(ee.getTriple());
                // record annotation of triple
                triple.getCreateTripleList().add(ee.getTriple());
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

    Triple createTriple(ASTQuery ast, Atom s, Atom p, Atom o) throws ParseException {
        if (isTurtleLoader()) {
            // load turtle
            if (!getCreate().accept(s, p, o)) {
                throw parser.generateParseException();
            }
            if (getCreate().raiseLimit()) {
                logger.info("Parser stop after raising limit");
                throw parser.createStopException();
            }
            getCreate().triple(s, p, o);
            return null;
        } else {
            // sparql parser
            return ast.createTriple(s, p, o);
        }
    }
    
    public void createNquad(Atom subject, Atom predicate, Atom object, Atom graph) {
        if (isTurtleLoader()) {
            getCreate().triple(graph, subject, predicate, object);
        }
    }
    
    public Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity) {
        return createTriple(ast, p, list, matchArity, false);
    }

    Triple createTriple(ASTQuery ast, Atom p, List<Atom> list, boolean matchArity, boolean nested) {
        if (isTurtleLoader()) {
            // load turtle
            getCreate().triple(p, list, nested);
            return null;
        } else {
            // sparql query
            Triple t = ast.createTriple(p, list, nested);
            t.setMatchArity(matchArity);
            return t;
        }
    }


    public void graphPattern(Atom g) {
        if (isTurtleLoader()) {
            getCreate().graph(g.getConstant());
        }
    }

    public void endGraphPattern(Atom g) {
        if (isTurtleLoader()) {
            getCreate().endGraph(g.getConstant());
        }
    }

    public Atom list(ASTQuery ast, Exp stack, List<Atom> l, int arobase) {
        RDFList rlist = ast.createRDFList(l, arobase);

        if (isTurtleLoader()) {
            getCreate().list(rlist);
        } else {
            stack.addList(rlist);
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
        if (isTurtleLoader() || isInsideValues()) {
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
    
    public void enterService(ASTQuery ast, Atom at) {
        if (at.isConstant()) {
            ast.enterService(at);
        }
    }
    
    public void leaveService(ASTQuery ast) {
        ast.leaveService();
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
    
    boolean isTurtleLoader() {
        return turtleLoader;
    }
    
    void setTurtleLoader(boolean b) {
        turtleLoader = b;
    }
    
    boolean isSparqlParser() {
        return ! isTurtleLoader();
    }

    public Creator getCreate() {
        return create;
    }

    public void setCreate(Creator create) {
        this.create = create;
    }
    
}
