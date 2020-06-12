package fr.inria.corese.shex.shacl;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.*;
import fr.inria.lille.shexjava.schema.concrsynt.*;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.Value;

/**
 * Shex to Shacl translator
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Shex implements Constant {

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
    private boolean closed = true;

    public Shex() {
        shacl = new ShexShacl();
        shexConst = new ShexConstraint(this);
        
    }

    public StringBuilder parse(String path) throws Exception {
        Path schemaFile = Paths.get(path);
        List<Path> importDirectories = Collections.emptyList();

        // load and create the shex schema
        //trace("Reading schema");
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
        } else {
            process(exp);
        }
        getShacl().end();
    }
    
    
    void process(ShapeExpr exp) {
        process(exp, null);
    }

    /**
     * 
     * @param ct : context for cardinality, qualifiedValueShape, constraint list
     */
    void process(ShapeExpr exp, Context ct) {
        if (exp instanceof Shape) {
            process((Shape) exp, ct);
        } else if (exp instanceof ShapeExprRef) {
            process((ShapeExprRef) exp);
        } else if (exp instanceof ShapeAnd) {
            process((ShapeAnd) exp);
        } else if (exp instanceof ShapeOr) {
            process((ShapeOr) exp);
        } else if (exp instanceof ShapeNot) {
            process((ShapeNot) exp);
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
    
    
    void process(Shape sh, Context ctx) {
        if (getCurrentLabel() != null) {
            declareShape();
            if (sh != null) {
                processClosed(sh);
            }
        }

        if (ctx != null && ctx.getNodeConstraintList() != null) {
            for (NodeConstraint cst : ctx.getNodeConstraintList()) {
                process(cst);
            }
        }
        
        if (sh != null) {
            if (ctx == null) {
                ctx = new Context();
            }
            ctx.setShape(sh);
            process(sh.getTripleExpression(), ctx);
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
    void process(ShapeAnd exp) {
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
            if (getCurrentLabel() != null) {
                declareShape();
            }
            // process AND list 
            for (ShapeExpr ee : exp.getSubExpressions()) {
                process(ee);
            }
        }
    }

    void process(ShapeOr exp) {
        if (getCurrentLabel() != null) {
            declareShape();
        }
        
        getShacl().openList(SH_OR);
        
        for (ShapeExpr ee : exp.getSubExpressions()) {
            traceClass(ee);trace(ee);
            getShacl().openBracket();
            process(ee);
            getShacl().closeBracket().nl();
        }
        getShacl().closeList();
    }

    void process(ShapeNot exp) {
        getShacl().openBracket(SH_NOT);
        process(exp.getSubExpression());
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
    
    void process(TripleExpr exp) {
        process(exp, null);
    }

    void process(TripleExpr exp, Context ct) {
        //traceClass(exp);
        if (exp instanceof TripleExprRef) {
            process((TripleExprRef) exp);
        } else if (exp instanceof TripleConstraint) {
            process((TripleConstraint) exp, ct);
        } else if (exp instanceof EachOf) {
            process((EachOf) exp, ct);
        } else if (exp instanceof OneOf) {
            process((OneOf) exp, ct);
        } else if (exp instanceof RepeatedTripleExpression) {
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
    
    void process(TripleExprRef exp) {
        process(exp.getTripleExp());
    }
    
    void process(RepeatedTripleExpression exp, Context ct) {
        if (ct == null) {
            ct = new Context();
        }
        process(exp.getSubExpression(), ct.setRepeatedExpr(exp));
    }

    /**
     * several occurrences of same (inverse) property processed as qualifiedValueShape
     */
    void process(EachOf exp, Context ct) {
        Qualified map        = new Qualified(this).create(exp, true);
        Qualified mapInverse = new Qualified(this).create(exp, false);
        
        List<TripleExpr> doneShacl = processShacl(exp);
        List<TripleExpr> done = map.process(ct.getShape());
        List<TripleExpr> doneInverse = mapInverse.process(ct.getShape());

        for (TripleExpr ee : exp.getSubExpressions()) {
            if (!doneShacl.contains(ee) && !done.contains(ee) && !doneInverse.contains(ee)) {
                process(ee, ct);
            }
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

    void process(OneOf exp, Context ct) {
        getShacl().openList(SH_ONE);
        for (TripleExpr ee : exp.getSubExpressions()) {
            getShacl().openBracket();
            process(ee);
            getShacl().closeBracket();
        }
        getShacl().closeList();
    }


    void process(TripleConstraint tc, Context ctx) {
        if (isExtendShacl() && isShacl(tc.getProperty())) {
            processShacl(tc);
        } 
        else if (isExtra(tc.getProperty(), ctx)) {
            // EXTRA ex:p -> sh:qualifiedValueShape to authorize extra occurrences of ex:p
            processQualify(tc, ctx);
        }
        else {
            processBasic(tc, ctx);
        }
    }

   
    void processBasic(TripleConstraint tc, Context ctx) {
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        
        tripleConstraint(tc.getShapeExpr());
        cardinality (ctx);
        
        getShacl().closeBracketPw().nl();
    }
    
    /**
     * The constraint of the property is sh:qualifiedValueShape [ cst ]
     */
    void processQualify(TripleConstraint tc, Context ctx) {
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        
        getShacl().openBracket(SH_QUALIFIED_VALUE_SHAPE);
        ShapeExpr exp = tc.getShapeExpr();
        tripleConstraint(exp, new Context().setQualifiedExpr(exp));
        getShacl().closeBracketPw().nl();
        
        define(SH_QUALIFIED_MIN_COUNT, (ctx == null) ? 1 : ctx.getMin());
        define(SH_QUALIFIED_MAX_COUNT, (ctx == null) ? 1 : ctx.getMax());
        
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

    void processQualify(TripleConstraint tc) {
        processQualify(tc, null);
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
        } else if (ct.isQualified()) {
            // no cardinality here
        } else if (ct.getRepeatedExpr() == null) {
            cardinality(1, 1);
        } else {
            insert(ct.getRepeatedExpr());
        }
    }

    void insert(RepeatedTripleExpression exp) {
        cardinalityBasic(exp.getCardinality().min, exp.getCardinality().max);
    }
    
    void cardinality(int min, int max) {
        if (isCardinality()) {
            cardinalityBasic(min, max);
        }
    }

    void cardinalityBasic(int min, int max) {
        define(SH_MINCOUNT, min);
        define(SH_MAXCOUNT, max);
    }
    
    

    void tripleConstraint(ShapeExpr exp) {
        tripleConstraint(exp, null);
    }

    void tripleConstraint(ShapeExpr exp, Context ct) {
        if (exp instanceof NodeConstraint) {
            processBasic((NodeConstraint) exp);
        } else if (exp instanceof Shape) {
            processNested((Shape) exp, ct);
        } else {
            process(exp, ct);
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

}
