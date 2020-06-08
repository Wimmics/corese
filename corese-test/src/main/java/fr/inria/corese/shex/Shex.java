package fr.inria.corese.shex;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.*;
import fr.inria.lille.shexjava.schema.concrsynt.*;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.Value;

/**
 * Shex to Shacl translator
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public class Shex {

    private static final String RDF_TYPE = "a";
    private static final String SH_SHAPE = "sh:NodeShape";
    private static final String SH_PATH = "sh:path";
    private static final String SH_NODE = "sh:node";
    private static final String SH_PROPERTY = "sh:property";
    private static final String SH_MINCOUNT = "sh:minCount";
    private static final String SH_MAXCOUNT = "sh:maxCount";
    private static final String SH_INVERSEPATH = "sh:inversePath";
    private static final String SH_OR = "sh:or";
    private static final String SH_QUALIFIED_VALUE_SHAPE = "sh:qualifiedValueShape";
    private static final String SH_QUALIFIED_MIN_COUNT = "sh:qualifiedMinCount";
    private static final String SH_QUALIFIED_MAX_COUNT = "sh:qualifiedMaxCount";
    private static final String SH_CLOSED = "sh:closed";
    private static final String SH_TARGET_CLASS = "sh:targetClass";
    private static final String SH = NSManager.SHACL;

    private StringBuilder sb;
    NSManager nsm = NSManager.create();
    private Label label, currentLabel;
    ShexConstraint shexConst;
    private ShexShacl shacl;
    private boolean extendShacl = false;

    public Shex() {
        sb = new StringBuilder();
        shacl = new ShexShacl();
        shexConst = new ShexConstraint(this);
        
    }

    public StringBuilder parse(String path) throws Exception {
        Path schemaFile = Paths.get(path);
        List<Path> importDirectories = Collections.emptyList();

        // load and create the shex schema
        trace("Reading schema");
        ShexSchema schema = GenParser.parseSchema(schemaFile, importDirectories);
        process(schema);
        return getShacl().getStringBuilder();
    }

    public StringBuilder parse(String path, String name) throws Exception {
        StringBuilder sb = parse(path);
        FileWriter myWriter = new FileWriter(name);
        myWriter.write(sb.toString());
        myWriter.close();
        return sb;
    }

    /**
     * Translate shex to shacl
     */
    public void process(ShexSchema schema) {
        getShacl().init();
        for (Label label : schema.getRules().keySet()) {
            ShapeExpr exp = schema.getRules().get(label);
            trace("");
            trace("main: " + exp.getClass().getName());
            trace("");
            setLabel(label);
            setCurrentLabel(label);
            main(exp);
        }
    }

    void main(ShapeExpr exp) {
        if (exp instanceof NodeConstraint) {
            // global NodeConstraint to be inserted in a shape
            process(null, (NodeConstraint) exp);
        } else {
            process(exp);
        }
        getShacl().end();
    }

    void process(ShapeExpr exp) {
        if (exp instanceof Shape) {
            process((Shape) exp);
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

    void process(TripleExpr exp) {
        process(exp, null);
    }

    void process(TripleExpr exp, Context ct) {
        //trace(exp);
        //trace(exp.getClass().getName());
        if (exp instanceof TripleExprRef) {
            process((TripleExprRef) exp);
        } else if (exp instanceof TripleConstraint) {
            process((TripleConstraint) exp, ct);
        } else if (exp instanceof EachOf) {
            process((EachOf) exp);
        } else if (exp instanceof OneOf) {
            process((OneOf) exp);
        } else if (exp instanceof RepeatedTripleExpression) {
            process((RepeatedTripleExpression) exp);
        } else {
            trace("**** undef: " + exp.getClass().getName());
        }
    }

    void trace(Object obj) {
        System.out.println(obj);
    }

    void other(ShapeExpr exp) {
        trace("**** undef: " + exp.getClass().getName());
    }

    void process(Shape sh) {
        process(sh, new ArrayList<>());
    }

    void process(Shape sh, NodeConstraint cst) {
        ArrayList<NodeConstraint> list = new ArrayList<>();
        list.add(cst);
        process(sh, list);
    }

    void process(Shape sh, ArrayList<NodeConstraint> list) {
        if (getCurrentLabel() != null) {
            getShacl().append(getPrefixURI(getCurrentLabel().stringValue())).space();
            define(RDF_TYPE, SH_SHAPE);
            setCurrentLabel(null);
            if (sh != null) {
                define(SH_CLOSED, "true");
                process(sh.getExtraProperties());
            }
        }

        //sh.getExtraProperties();
        for (NodeConstraint cst : list) {
            process(cst);
        }
        if (sh != null) {
            process(sh.getTripleExpression());
        }
    }
    
    
    void process(Set<TCProperty> extra) {
        if (extra.isEmpty()) {
        }
        else {
        
        }
    }

    void traceClass(Object obj) {
        System.out.println(obj.getClass().getName());
    }

    void process(TripleExprRef exp) {
        trace("Ref: " + exp);
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
            // process AND list 
            for (ShapeExpr ee : exp.getSubExpressions()) {
                process(ee);
            }
        }
    }

    void process(ShapeOr exp) {
        getShacl().openList(SH_OR);
        for (ShapeExpr ee : exp.getSubExpressions()) {
            getShacl().openBracket();
            process(ee);
            getShacl().closeBracket().nl();
        }
        getShacl().closeList();
    }

    void process(ShapeNot exp) {
        process(exp.getSubExpression());
    }

    void process(RepeatedTripleExpression exp) {
        process(exp.getSubExpression(), new Context(exp));
    }

    void process(EachOf exp) {
        Qualified map        = new Qualified(this).create(exp, true);
        Qualified mapInverse = new Qualified(this).create(exp, false);
        List<TripleExpr> doneShacl;
        if (isExtendShacl()) {
            // sh:targetClass considered as shacl statement
            doneShacl = map.process(true);
        }
        else {
            doneShacl = new ArrayList<>();
        }
        List<TripleExpr> done = map.process();
        List<TripleExpr> doneInverse = mapInverse.process();

        for (TripleExpr ee : exp.getSubExpressions()) {
            if (!doneShacl.contains(ee) && !done.contains(ee) && !doneInverse.contains(ee)) {
                process(ee);
            }
        }
    }   

    void process(OneOf exp) {
        getShacl().openList(SH_OR);
        for (TripleExpr ee : exp.getSubExpressions()) {
            getShacl().append("[");
            process(ee);
            getShacl().append("]");
        }
        getShacl().closeList();
    }

    void process(ShapeExprRef exp) {
        define(SH_NODE, getPrefixURI(exp.getLabel().stringValue()));
    }

    void process(TripleConstraint tc, Context ctx) {
        if (isExtendShacl() && isShacl(tc.getProperty())) {
            processShacl(tc);
        } else {
            processBasic(tc, ctx);
        }
    }

    /**
     * sh:targetClass [ ex:Person ] considered as shacl specification
     */
    void processShacl(TripleConstraint tc) {
        getShacl().append(getPrefixURI(tc.getProperty())).space();
        NodeConstraint node = (NodeConstraint) tc.getShapeExpr();
        for (Constraint cst : node.getConstraints()) {
            if (cst instanceof ValueSetValueConstraint) {
                ValueSetValueConstraint list = (ValueSetValueConstraint) cst;
                int i = 0;
                for (Value val : list.getExplicitValues()) {
                    if (i > 0) {
                        getShacl().append(", ");
                    }
                    i++;
                    getShacl().append(getValue(val.stringValue()));
                }
            }
        }
        getShacl().append(";").nl();
    }

    boolean isShacl(TCProperty property) {
        return property.getIri().getNamespace().equals(SH);
    }

    void processBasic(TripleConstraint tc, Context ctx) {
        ShapeExpr exp = tc.getShapeExpr();
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        tripleConstraint(exp);
        if (ctx == null) {
            cardinality(1, 1);
        } else {
            process(ctx);
        }
        getShacl().closeBracket().nl();
    }
    
    void qualify(TripleConstraint tc) {
        qualify(tc, null);
    }

    void qualify(TripleConstraint tc, Context ctx) {
        ShapeExpr exp = tc.getShapeExpr();
        getShacl().openBracket(SH_PROPERTY);
        process(tc.getProperty());
        getShacl().openBracket(SH_QUALIFIED_VALUE_SHAPE);
        tripleConstraint(exp);
        getShacl().closeBracket().nl();
        int min = (ctx == null) ? 1 : ctx.getRepeatedTripleExp().getCardinality().min;
        int max = (ctx == null) ? 1 : ctx.getRepeatedTripleExp().getCardinality().max;
        define(SH_QUALIFIED_MIN_COUNT, min);
        define(SH_QUALIFIED_MAX_COUNT, max);
        getShacl().closeBracket().nl();
    }

    String getPrefixURI(TCProperty p) {
        return getPrefixURI(p.getIri().toString());
    }

    void process(TCProperty p) {
        if (p.isForward()) {
            define(SH_PATH, getPrefixURI(p));
        } else {
            define(SH_PATH, String.format("[%s %s]", SH_INVERSEPATH, getPrefixURI(p)));
        }
    }

    void process(Context ct) {
        if (ct.getRepeatedTripleExp() != null) {
            insert(ct.getRepeatedTripleExp());
        }
    }

    void insert(RepeatedTripleExpression exp) {
        cardinality(exp.getCardinality().min, exp.getCardinality().max);
    }

    void cardinality(int min, int max) {
        define(SH_MINCOUNT, min);
        define(SH_MAXCOUNT, max);
    }

    void tripleConstraint(ShapeExpr exp) {
        if (exp instanceof NodeConstraint) {
            processBasic((NodeConstraint) exp);
        } else if (exp instanceof Shape) {
            processNested((Shape) exp);
        } else {
            process(exp);
        }
    }

    void processNested(Shape sh) {
        getShacl().openBracket(SH_NODE).nl();
        process(sh.getTripleExpression());
        getShacl().closeBracket().nl();
    }

    void processBasic(NodeConstraint node) {
        for (Constraint cst : node.getConstraints()) {
            shexConst.process(cst);
        }
    }

    void process(NodeConstraint node) {
        getShacl().openBracket(SH_NODE);
        processBasic(node);
        getShacl().closeBracket().nl();
    }

    String getPrefixURI(String name) {
        String prop = nsm.toPrefix(name, true);
        if (name == prop) {
            return "<" + name + ">";
        }
        return prop;
    }

    /**
     * **************************************************************************
     *
     */
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

    String getValue(String value) {
        if (value.startsWith("http://")) {
            return "<" + value + ">";
        }
        return value;
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

}
