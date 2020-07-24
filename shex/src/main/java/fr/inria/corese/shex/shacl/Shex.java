package fr.inria.corese.shex.shacl;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.*;
import fr.inria.lille.shexjava.schema.concrsynt.*;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shex to Shacl translator
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Shex implements Constant {
    private static Logger logger = LoggerFactory.getLogger(Shex.class);

     static final String SH_PREFIX = NSManager.SHACL_PREFIX+":";
     static final String SH = NSManager.SHACL;
    // namespace for shex:targetClass shacl extension
     static final String SHEX_SHACL = NSManager.SHEX_SHACL;

    NSManager nsm = NSManager.create();
    private Label label, currentLabel;
    ShexConstraint shexConst;
    // StringBuilder that contains shacl translation of shex schema
    private ShexShacl shacl;
    // consider shex:targetClass [h:Person] as a shacl statement 
    private boolean extendShacl = false;
    // shex cardinality 
    private boolean cardinality = true;
    // (A|B) * -> A? B?
    private boolean expCardinality = true;
    private boolean optional = false;
    private boolean closed = true;

    public Shex() {
        shacl = new ShexShacl();
        shexConst = new ShexConstraint(this);
        
    }

    public StringBuilder parse(String path) throws Exception {
        Path schemaFile = Paths.get(path);
        return parse(schemaFile);
    }
    
    public StringBuilder parseString(String str) throws IOException, Exception {        
        File f = File.createTempFile("tmp", ".shex");
        f.setWritable(true);
        FileWriter w = new FileWriter(f);
        w.write(str);
        w.flush();
        w.close();
        StringBuilder sb = parse(f.toPath());
        return sb;
    }

    public StringBuilder parse(Path schemaFile) throws Exception {
        List<Path> importDirectories = Collections.emptyList();
        ShexSchema schema = GenParser.parseSchema(schemaFile, importDirectories);
        process(schema);
        return getStringBuilder();
    }

    public StringBuilder parse(String path, String name) throws Exception   {
        parse(path);
        write(name);
        return getStringBuilder();
    }
    
    void write(String name) throws IOException {
        FileWriter myWriter = new FileWriter(name);
        myWriter.write(getStringBuilder().toString());
        myWriter.close();
    }

    /**
     * Translate shex to shacl
     */
    public void process(ShexSchema schema) {
        getShacl().init();
        for (Label label : schema.getRules().keySet()) {
            ShapeExpr exp = schema.getRules().get(label);
            //trace(label);
            //trace("main: " + exp.getClass().getName());
            setLabel(label);
            setCurrentLabel(label);
            main(exp);
        }
    }

    void main(ShapeExpr exp) {
        if (exp instanceof NodeConstraint) {
            // global NodeConstraint: translate as a shape
            process(null, (NodeConstraint) exp);
        } 
        else if (exp instanceof ShapeNot) {
            // ex:test NOT exp
            // NOT is top level ...
            ShapeNot not = (ShapeNot) exp;
            process(not.getSubExpression(), new Context().setNotExpr(not));
        }
        else {
            process(exp);
        }
        getShacl().end();
    }
    
    
    void process(ShapeExpr exp) {
        process(exp, new Context());
    }

    /**
     * 
     * @param ct : context for cardinality, qualifiedValueShape, constraint list
     */
    void process(ShapeExpr exp, Context ct) {
        //trace(exp);
        if (exp instanceof Shape) {
            process((Shape) exp, ct);
        } else if (exp instanceof ShapeExprRef) {
            process((ShapeExprRef) exp);
        } else if (exp instanceof ShapeAnd) {
            process((ShapeAnd) exp, ct);
        } else if (exp instanceof ShapeOr) {
            process((ShapeOr) exp, ct);
        } else if (exp instanceof ShapeNot) {
            process((ShapeNot) exp, ct);
        } else if (exp instanceof NodeConstraint) {
            process((NodeConstraint) exp);
        } else {
            other(exp);
        }
    }


    void process(Shape sh, NodeConstraint... cst) {
        process(sh, Arrays.asList(cst));
    }

    void process(Shape sh, List<NodeConstraint> list) {
        process(sh, new Context(list));
    }
    
    
    void process(Shape sh, Context ct) {
        if (getCurrentLabel() != null) {
            declareShape();
            if (sh != null) {
                processClosed(sh);
                processExtra(sh);
            }
        }

        if (ct != null && ct.getNodeConstraintList() != null) {
            for (NodeConstraint cst : ct.getNodeConstraintList()) {
                process(cst);
            }
        }
        
        if (sh != null) {
            //ct = context(ct);
            ct.setShape(sh);
            boolean not = ct.getNotExpr() != null;
            if (not) {
                getShacl().openBracket(SH_NOT);
            }
            
            process(sh.getTripleExpression(), ct);
            
            if (not) {
               getShacl().closeBracketPw();
            }
        }
    }
    
    void processExtra(Shape sh) {
        for (TCProperty tc : sh.getExtraProperties()) {
           define(SHEX_EXTRA, getPrefixURI(tc));
        }
    }
    
    void declareShape() {
        getShacl().append(getPrefixURI(getCurrentLabel().stringValue())).space();
        define(RDF_TYPE, SH_SHAPE);
        setCurrentLabel(null);
    }
    
    
    void processClosed(Shape sh) {
        if (sh.isClosed()) {
            define(SH_CLOSED, "true");
        }
    }
    
   

    /**
     * Use case: mix of NodeConstraint ans Shape Insert node constraint inside
     * the SHACL shape my:EmployeeShape IRI /^http:\/\/hr\.example\/id#[0-9]+/ {
     * foaf:name LITERAL; }
     */
    void process(ShapeAnd exp, Context ct) {
        ArrayList<NodeConstraint> cstList = new ArrayList<>();
        Shape shape = null;

        for (ShapeExpr ee : exp.getSubExpressions()) {
            if (ee instanceof NodeConstraint) {
                cstList.add((NodeConstraint) ee);
            } else if (ee instanceof Shape) {
                shape = (Shape) ee;
            }
        }

        if (shape != null && !cstList.isEmpty()) {
            // insert node constraint list inside shape 
            process(shape, cstList);
        } else {
            //ct = context(ct);
            new Qualified().create(exp, ct);
            
            if (getCurrentLabel() != null) {
                declareShape();
            }
            if (shape != null) {
                processExtra(shape);
                processClosed(shape);
            }
            // process AND list 
            for (ShapeExpr ee : exp.getSubExpressions()) {
                process(ee, ct);
            }
        }
    }

    void process(ShapeOr exp, Context ct) {
        if (getCurrentLabel() != null) {
            declareShape();
        }
        //ct = context(ct);
//        new Qualified().create(exp, ct);
        getShacl().openList(SH_OR);
        
        for (ShapeExpr ee : exp.getSubExpressions()) {
            //traceClass(ee);trace(ee);
            getShacl().openBracket();
            // context use case: OR inside qualifiedValueShape
            process(ee, ct);
            getShacl().closeBracket().nl();
        }
        getShacl().closeList();
    }

    void process(ShapeNot exp, Context ct) {
        getShacl().openBracket(SH_NOT);
        // context use case: NOT inside qualifiedValueShape
        process(exp.getSubExpression(), ct);
        getShacl().closeBracketPw();
    }


    void process(ShapeExprRef exp) {
        define(SH_NODE, getPrefixURI(exp.getLabel().stringValue()));
    }
    
    
    
    /************************************************************************
     * 
     * Triple Expr
     * 
     ***********************************************************************/
    
//    void process(TripleExpr exp) {
//        process(exp, null);
//    }

    void process(TripleExpr exp, Context ct) {
        //traceClass(exp);
        if (exp instanceof TripleExprRef) {
            process((TripleExprRef) exp, ct);
        } else if (exp instanceof TripleConstraint) {
            process((TripleConstraint) exp, ct);
        } else if (exp instanceof AbstractNaryTripleExpr) {
            // EachOf OneOf
            process((AbstractNaryTripleExpr) exp, ct);
        }  else if (exp instanceof RepeatedTripleExpression) {
            process((RepeatedTripleExpression) exp, ct);
        } 
        else if (exp instanceof EmptyTripleExpression) {
            process((EmptyTripleExpression) exp);
        } 
        else {
            trace("**** undef: " + exp.getClass().getName());
        }
    }
    
    void process(EmptyTripleExpression exp) {
        
    }
    
    void process(TripleExprRef exp, Context ct) {
        process(exp.getTripleExp(), ct);
    }
    
    void process(RepeatedTripleExpression exp, Context ct) {
//        if (exp.getSubExpression() instanceof AbstractNaryTripleExpr) {
//            logger.info("Cardinality on: " + 
//                    exp.getSubExpression().getClass().getName());
//            logger.info(exp.toString());
//        }
        //ct = context(ct);
        RepeatedTripleExpression save = ct.getRepeatedExpr();
        process(exp.getSubExpression(), ct.setRepeatedExpr(exp));
        ct.setRepeatedExpr(save);
    }
    
    Context context(Context ct) {
        return (ct == null) ? new Context() : ct;
    }
    
    
    // EachOf OneOf
    void process(AbstractNaryTripleExpr exp, Context ct) {
        //ct = context(ct);
        if (isExpCardinality()) {
            // (A|B)* -> A? B?
            processWithCardinality(exp, ct);
        } else {
            // (A|B)* -> (A|B) -- which is not correct ...
            process(exp, ct, false);
        }
    }
    
    /**
     * (A|B)*   -> A? B?
     * 
     */
    void processWithCardinality(AbstractNaryTripleExpr exp, Context ct) {
        boolean processCardinality = ct.hasCardinality() && 
                (exp instanceof OneOf || ct.isOneOf());
        process(exp, ct, processCardinality);
    }      
    
    /**
     * 
     * processCardinality = true: handle (A|B)* ; otherwise skip *
     */
    void process(AbstractNaryTripleExpr exp, Context ct, boolean processCardinality) {
        RepeatedTripleExpression rt = null;
        boolean inOneOfLoop = ct.isInOneOfLoop();
        
        if (processCardinality) { 
            ct.setInOneOfLoop(true); 
        }
        else {
            // skip cardinality 
            rt = ct.getRepeatedExpr();
            ct.setRepeatedExpr(null);
        }

        processBasic(exp, ct);

        if (processCardinality) { 
            ct.setInOneOfLoop(inOneOfLoop);
        } 
        else  {
            ct.setRepeatedExpr(rt);
        }
    }
    
    void processBasic(AbstractNaryTripleExpr exp, Context ct) {
        if (exp instanceof EachOf) {
            process((EachOf) exp, ct);
        } else if (exp instanceof OneOf) {
            process((OneOf) exp, ct);
        }
    }
    
    /**
     * several occurrences of same (inverse) property processed as qualifiedValueShape
     */
    void process(EachOf exp, Context ct) {
        //ct = context(ct);
        // check qualifiedValueShape
        new Qualified().create(exp, ct);
        
        List<TripleExpr> doneShacl = processShacl(exp);

        for (TripleExpr ee : exp.getSubExpressions()) {
            if (!doneShacl.contains(ee)) { 
                process(ee, ct);
            }
        }
    } 
    
    void process(OneOf exp, Context ct) {  
        if (ct.hasCardinality()) { 
            // (A|B)* -> A? B? with shex:optional true
            processWithCardinality(exp, ct);
            if (ct.isPositive()) {
                // additional minCount 1 on properties
                exist(exp, ct);
            }
        }
        else {
            processBasic(exp, ct);
        }
    }
    
 
    // (A|B)* -> A? B?
    void processWithCardinality(OneOf exp, Context ct) {
        // check qualifiedValueShape
        new Qualified().create(exp, ct);
        // process as EachOf with cardinality ?
        for (TripleExpr ee : exp.getSubExpressions()) {
            process(ee, ct);
        }
    }
    
    void processBasic(OneOf exp, Context ct) {
        new Qualified().create(exp, ct);
        getShacl().openList(SH_ONE);
        for (TripleExpr ee : exp.getSubExpressions()) {
            getShacl().openBracket();
            process(ee, ct);
            getShacl().closeBracket();
        }
        getShacl().closeList();
    }
    
    /**
     * (A|B){a,b}
     * ->
     * shex:count [ shex:constraint (A B) ; shex:minCount a; shex:maxCount b ]
     * @deprecated
     */
    void processCount(OneOf exp, Context ct) {
        new Qualified().create(exp, ct);
        
        getShacl().openBracket(SHEX_COUNT);        
        getShacl().openList(SHEX_CONSTRAINT);
        boolean disjoint = ct.isDisjoint();
        ct.setDisjoint(true);
        
        for (TripleExpr ee : exp.getSubExpressions()) {
            getShacl().openBracket();
            process(ee, ct);
            getShacl().closeBracket();
        }
        
        ct.setDisjoint(disjoint);
        getShacl().closeList();        
        getShacl().define(SHEX_MINCOUNT, ct.getMin()).nl();
        getShacl().define(SHEX_MAXCOUNT, ct.getMax()).nl();
        getShacl().closeBracket().pw().nl();
    }
    
    
    
    /**
     * (A|B) +
     * check that one of them has property value (for the +)
     */
    void exist(OneOf exp, Context ct) {
        getShacl().openBracket(SH_PROPERTY);
        getShacl().openBracket(SH_PATH);
        getShacl().openList(SH_ALTERNATIVEPATH);
        
        ct.setBackward(new HashMap<>());
        ct.setForward(new HashMap<>());
        
        for (TripleExpr ee : exp.getSubExpressions()) {
            exist(ee, ct);
        }
        
        for (String key : ct.getForward().keySet()) {
            getShacl().append(key).space();
        }
        
        for (String key : ct.getBackward().keySet()) {
            getShacl().inverse(key).space();
        }
        
        getShacl().closeList();
        getShacl().closeBracket().pw().nl();
        
        getShacl().define(SH_MINCOUNT, 1).nl();
        
        getShacl().closeBracket();
    }
    
    void exist(TripleExpr exp, Context ct) {
        if (exp instanceof TripleConstraint) {
            exist((TripleConstraint) exp, ct);
        }
        else if (exp instanceof RepeatedTripleExpression) {
            exist((RepeatedTripleExpression) exp, ct);
        }
    }
    
    void exist(RepeatedTripleExpression exp, Context ct) {
        exist(exp.getSubExpression(), ct);
    }

    void exist(TripleConstraint exp, Context ct) {
        exist(exp.getProperty(), ct);
    }
    
    void exist(TCProperty exp, Context ct) {
        String p = getPrefixURI(exp);
        if (exp.isForward()) {
            ct.getForward().put(p, p);
        }
        else {
            ct.getBackward().put(p, p);
        }
    }
    
    
   
    
    
        // special case: sh:targetClass considered as shacl statement    
    List<TripleExpr> processShacl(EachOf exp) {
        ArrayList<TripleExpr> done = new ArrayList<>();
        if (isExtendShacl()) {
            for (TripleExpr te : exp.getSubExpressions()) {
                if (te instanceof TripleConstraint) {
                    TripleConstraint tc = (TripleConstraint) te;
                    // special case: sh:targetClass considered as shacl statement
                    if (isShacl(tc.getProperty())) {
                        processShacl(tc);
                        done.add(te);
                    }
                }
            }
        }
        return done;
    }



    void process(TripleConstraint tc, Context ct) {
        //ct = context(ct);
        if (isExtendShacl() && isShacl(tc.getProperty())) {
            processShacl(tc);
        } 
        else if (isExtra(tc.getProperty(), ct) || ct.isQualified(tc)) {
            // EXTRA ex:p -> sh:qualifiedValueShape to authorize extra occurrences of ex:p
            processQualify(tc, ct);
        }
        else {
            processBasic(tc, ct);
        }
    }
   
    void processBasic(TripleConstraint tc, Context ct) {
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        
        tripleConstraint(tc.getShapeExpr());
        cardinality (ct);
        
        getShacl().closeBracketPw().nl();
    }
    
    /**
     * The property is translated as sh:qualifiedValueShape [ cst ]
     */
    void processQualify(TripleConstraint tc, Context ct) {
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        
        getShacl().openBracket(SH_QUALIFIED_VALUE_SHAPE);
        ShapeExpr exp = tc.getShapeExpr();
        tripleConstraint(exp, new Context().setQualifiedExpr(exp));
        getShacl().closeBracketPw().nl();
        cardinalityQualified(ct);
        getShacl().closeBracketPw().nl();
    }
    
    void disjoint(Context ct) {
        if (ct.isDisjoint()) {
            getShacl().define(SH_QUALIFIED_DISJOINT, "true");
        }
    }
    
    void tripleConstraint(ShapeExpr exp) {
        tripleConstraint(exp, null);
    }

    void tripleConstraint(ShapeExpr exp, Context ct) {
        if (exp instanceof NodeConstraint) {
            processBasic((NodeConstraint) exp);
        } else if (exp instanceof Shape) {
            processNested((Shape) exp, context(ct));
        } else {
            process(exp, context(ct));
        }
    }

    void processNested(Shape sh, Context ct) {
        getShacl().openBracket(SH_NODE);
        process(sh.getTripleExpression(), ct);
        getShacl().closeBracketPw().nl();
    }

    void processBasic(NodeConstraint node) {
        for (Constraint cst : node.getConstraints()) {
            shexConst.process(cst);
        }
    }

    void process(NodeConstraint node) {
        getShacl().openBracket(SH_NODE);
        processBasic(node);
        getShacl().closeBracketPw().nl();
    }
    
    
    
    
    
     /**
     * shex:targetClass [ ex:Person ] considered as shacl sh:targetClass
     * shex: = NSManager.SHEX_SHACL
     */
    void processShacl(TripleConstraint tc) {
        if (tc.getShapeExpr() instanceof NodeConstraint) {
            NodeConstraint node = (NodeConstraint) tc.getShapeExpr();
            getShacl().append(shaclName(tc.getProperty())).space();

            for (Constraint cst : node.getConstraints()) {
                if (cst instanceof ValueSetValueConstraint) {
                    ValueSetValueConstraint list = (ValueSetValueConstraint) cst;
                    int i = 0;
                    for (Value val : list.getExplicitValues()) {
                        if (i++ > 0) {
                            getShacl().append(", ");
                        }
                        getShacl().append(val);
                    }
                }
            }
            getShacl().pw().nl();
        }
    }
    
    
    String shaclName(TCProperty property) {
        return SH_PREFIX + property.getIri().getLocalName();
    }

    boolean isShacl(TCProperty property) {
        return property.getIri().getNamespace().equals(SHEX_SHACL);
    }

    boolean isExtra(TCProperty p, Context ct) {
        if (ct == null) {
            return false;
        }
        return isExtra(p.getIri().stringValue(), ct.getShape());
    }
    
    boolean isExtra(String uri, Shape sh) {
        if (sh == null) {
            return false;
        }
        Set<TCProperty> extra = sh.getExtraProperties();
        for (TCProperty p : extra) {
            if (p.toString().equals(uri)) {
                return true;
            }
        }
        return false;
    }

    String getPrefixURI(TCProperty p) {
        return getPrefixURI(p.getIri().toString());
    }
    
    String getPrefixURI(String uri) {
        return getShacl().getPrefixURI(uri);
    }

    void process(TCProperty p) {
        if (p.isForward()) {
            define(SH_PATH, getPrefixURI(p));
        } else {
            getShacl().defineInverse(SH_PATH, getPrefixURI(p)).nl();
        }
    }
    
    void cardinality(Context ct) {
        if (ct == null) {
            cardinality(1, 1);
        } 
        else if (ct.hasCardinality()) {
            contextCardinality(ct);
        } else {
            cardinality(1, 1);
        }
    }

    void contextCardinality(Context ct) {
        ct.setExpCardinality(isExpCardinality());
        cardinalityBasic(ct.getMin(), ct.getMax());
        optional(ct);
    }
    
    void optional(Context ct) {
        if (isExpCardinality() && ct.isInOneOfLoop() && ct.getMin() != 0) {
            // (A|B)* the constraints are optional because of *
            // shex:optional true : constraint does not fail when there is no property at all
            define(SHEX_OPTIONAL, "true");
        }
    }
    
    void cardinality(int min, int max) {
        if (isCardinality()) {
            cardinalityBasic(min, max);
        }
    }

    void cardinalityBasic(int min, int max) {
        define(SH_MINCOUNT, min);
        if (max < Integer.MAX_VALUE) {
            define(SH_MAXCOUNT, max);
        }
        
    }
    
    void cardinalityQualified(Context ct) {
        if (ct == null) {
            cardinalityQualified(1, 1);
        }
        else {
            ct.setExpCardinality(isExpCardinality());
            cardinalityQualified(ct.getMin(), ct.getMax());
            optional(ct);
        }
    }
    
    void cardinalityQualified(int min, int max) {
        define(SH_QUALIFIED_MIN_COUNT, min);
        if (max < Integer.MAX_VALUE) {
            define(SH_QUALIFIED_MAX_COUNT, max);
        }
    }
    
    /**
     * **************************************************************************
     *
     */
    
        

    void trace(Object obj) {
        System.out.println(obj);
    }

    void other(ShapeExpr exp) {
        trace("**** undef: " + exp.getClass().getName());
    }


    void traceClass(Object obj) {
        trace(obj.getClass().getName());
    }
    
    void define(String name, String value) {
        getShacl().define(name, value).nl();
    }

    void define(String name, List<String> list) {
        getShacl().define(name, list).nl();
    }

    void define(String name, BigDecimal value) {
        getShacl().define(name, value).nl();
    }

    void define(String name, int value) {
        getShacl().define(name, value).nl();
    }

   
    /**
     * @return the label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * @return the shacl
     */
    public ShexShacl getShacl() {
        return shacl;
    }

    /**
     * @param shacl the shacl to set
     */
    public void setShacl(ShexShacl shacl) {
        this.shacl = shacl;
    }

    /**
     * @return the currentLabel
     */
    public Label getCurrentLabel() {
        return currentLabel;
    }

    /**
     * @param currentLabel the currentLabel to set
     */
    public void setCurrentLabel(Label currentLabel) {
        this.currentLabel = currentLabel;
    }

    /**
     * @return the extendShacl
     */
    public boolean isExtendShacl() {
        return extendShacl;
    }

    /**
     * @param extendShacl the extendShacl to set
     */
    public Shex setExtendShacl(boolean extendShacl) {
        this.extendShacl = extendShacl;
        return this;
    }
    
    public StringBuilder getStringBuilder() {
        return getShacl().getStringBuilder();
    }

    /**
     * @return the cardinality
     */
    public boolean isCardinality() {
        return cardinality;
    }

    /**
     * @param cardinality the cardinality to set
     */
    public Shex setCardinality(boolean cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public Shex setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    /**
     * @return the expCardinality
     */
    public boolean isExpCardinality() {
        return expCardinality;
    }

    /**
     * @param expCardinality the expCardinality to set
     */
    public Shex setExpCardinality(boolean expCardinality) {
        this.expCardinality = expCardinality;
        return this;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return isExpCardinality();
    }

    

}
