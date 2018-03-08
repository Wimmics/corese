package fr.inria.corese.sparql.triple.parser;

import static fr.inria.corese.sparql.triple.parser.ASTQuery.KGRAMVAR;
import static fr.inria.corese.sparql.triple.parser.ASTQuery.OUT;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ASTTemplate {
        
    private static final String GROUPCONCAT = Processor.GROUPCONCAT;
    private static final String CONCAT = Processor.CONCAT;
    private static final String COALESCE = Processor.COALESCE;
    private static final String IF = Processor.IF;
    private static final String FORMAT = Processor.STL_FORMAT;
    private static String FUN_TEMPLATE_AGG = Processor.FUN_AGGREGATE; //Processor.FUN_GROUPCONCAT ;
    private static String FUN_TEMPLATE_CONCAT = Processor.FUN_CONCAT;
    private static String FUN_TURTLE = Processor.FUN_TURTLE;
    private static String FUN_FORMAT = Processor.FUN_FORMAT;
    private static final String FUN_PROCESS = Processor.FUN_PROCESS;
    private static final String FUN_PROCESS_URI = Processor.FUN_PROCESS_URI;
    private static final String FUN_NL = Processor.FUN_NL;
    private static final String FUN_INDENT = Processor.FUN_INDENT;
    private static final String IBOX = "ibox";
    private static final String SBOX = "sbox";
    private static final String BOX = "box";
    // functions whose variable are compiled as (coalesce(st:process(?x), "")
    private static String[] PPRINT_META = {GROUPCONCAT, CONCAT, FUN_TEMPLATE_CONCAT, COALESCE, IF};
    
    private String groupSeparator = " ";
    private String templateSeparator = System.getProperty("line.separator");
    private Term templateGroup;
    ArrayList<Expression> template;

    private Expression templateExpSeparator;
    
    ASTQuery ast;
    private Constant empty;
    int countVar = 0;

    
    ASTTemplate(ASTQuery a){
        ast = a;
        template = new ArrayList<Expression>();
    }
    
    public Term createGroup(ExpressionList el) {
        Term term = Term.function(CONCAT);
        for (Expression exp : el) {
            term.add(exp);
        }
        term = Term.function(GROUPCONCAT, term);
        if (el.getSeparator() == null && el.getExpSeparator() == null) {
            el.setSeparator(groupSeparator);
        }
        term.setModality(el);
        return term;
    }

     public Term createFormat(ExpressionList el) {
        Term t = ast.createFunction(FUN_FORMAT, el);
        return t;
    }
     
     
     /**
     * box: nl(+1) body nl(-1) sbox: nl(+1) body indent(-1) ibox: indent(+1)
     * body indent(-1)
     */
    public Term createBox(ExpressionList el, String type) {
        String open = FUN_NL;
        String close = FUN_NL;

        if (!type.equals(BOX)) {
            close = FUN_INDENT;
        }
        if (type.equals(IBOX)) {
            open = FUN_INDENT;
        }

        Constant fopen = ast.createQName(open);
        Constant fclose = ast.createQName(close);


        Term t1 = ast.createFunction(fopen, Constant.create(1));
        Term t2 = ast.createFunction(fclose, Constant.create(-1));
        el.add(0, t1);
        el.add(t2);
        return ast.createFunction(ast.createQName(FUN_TEMPLATE_CONCAT), el);
    }

    public Term createXML(Constant cst, ArrayList<ExpressionList> lattr, ExpressionList el) {
        Term nl = ast.createFunction(ast.createQName(FUN_NL));
        ExpressionList arg = new ExpressionList();
        if (lattr == null) {
            arg.add(Constant.create("<" + cst.getName() + ">"));
        } else {
            arg.add(0, Constant.create("<" + cst.getName() + " "));
            Constant eq = Constant.create("=");
            Constant quote = Constant.create("'");
            for (ExpressionList att : lattr) {
                arg.add(att.get(0));
                arg.add(eq);
                arg.add(quote);
                arg.add(att.get(1));
                arg.add(quote);
            }
            arg.add(Constant.create(">"));
        }
        arg.add(nl);
        for (Expression ee : el) {
            arg.add(ee);
        }
        arg.add(nl);
        arg.add(Constant.create("</" + cst.getName() + ">"));
        return ast.createFunction(ast.createQName(FUN_TEMPLATE_CONCAT), arg);
    }

    /**
     * vbox() if (type.equals(VBOX) && el.size() > 1){ // add NL between
     * elements Term t = createFunction(nl); for (int i=1; i<el.size(); ){
     * el.add(i, t); i += 2; } } @param s
     */
    public void setGroupSeparator(String s) {
        groupSeparator = s;
    }

    public void addTemplate(Expression at) {       
        template.add(at);
    }

    /**
     * template { "construct {" ?x "} where {" ?y "}" } -> select
     * (st:process(?x) as ?px) (st:process(?y) as ?py) (concat(.. ?px .. ?py ..)
     * as ?out)
     */
    void compileTemplate() {
        Expression t = compileTemplateFun();
        Variable out = Variable.create(OUT);
        ast.defSelect(out, t);
        setTemplateGroup(createTemplateGroup());
    }

    /**
     * Compile the template as a concat() where variables are compiled as
     * st:process()
     */
    Expression compileTemplateFun() {
        Term t = ast.createFunction(ast.createQName(FUN_TEMPLATE_CONCAT));

        if (template.size() == 1 && isSingleton(template.get(0))) {
            return compileTemplate(template.get(0), false, false);
        } else {
            for (Expression exp : template) {
                exp = compileTemplate(exp, false, false);
                t.add(exp);
            }
        }

        return t;
    }

    /**
     * return false when exp must be put in st:concat use case: process
     * st:number() as a Future using st:concat
     */
    boolean isSingleton(Expression exp) {
        return !exp.getLabel().equals(FORMAT);
    }

    /**
     * if exp is a variable: (st:process(?x) as ?px) if exp is meta, e.g.
     * group_concat(?exp): group_concat(st:process(?exp)) if exp is a simple
     * function: xsd:string(?x) (no st:process)
     */
    Expression compileTemplate(Expression exp, boolean coalesce, boolean group) {
        if (exp.isVariable()) {
            exp = compile(exp.getVariable(), coalesce);
        } else if (isMeta(exp)) {
            // variables of meta functions are compiled as st:process()
            // variable of xsd:string() is not
            exp = compileTemplateMeta((Term) exp, coalesce, group);
        }
        return exp;
    }

    /**
     * Some function play a special role in template: concat, group_concat,
     * coalesce, if Their variable argument are compiled as st:process(var)
     */
    boolean isMeta(Expression exp) {
        if (!exp.isFunction()) {
            return false;
        }
        for (String name : PPRINT_META) {
            if (exp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    boolean isIF(Expression exp) {
        return exp.getName().equals(IF);
    }

    boolean isCoalesce(Expression exp) {
        return exp.getName().equals(COALESCE);
    }

    boolean isGroup(Expression exp) {
        return exp.getName().equals(GROUPCONCAT);
    }

    // st:concat()
    boolean isSTCONCAT(Expression exp) {
        return exp.getName().equals(FUN_TEMPLATE_CONCAT);
    }

    /**
     * concat() st:concat() group_concat() if() coalesce() copy the function and
     * compile its variable as (coalesce(st:process(?var), "")
     */
    Term compileTemplateMeta(Term exp, boolean coalesce, boolean group) {
        Term t = copy(exp, group);
        boolean isIF = isIF(exp);
        boolean isCoalesce = isCoalesce(exp);

        int count = 0;

        for (Expression ee : exp.getArgs()) {
            if (count == 0 && isIF) {
                // not compile the test of if(test, then, else)
            } else {
                ee = compileTemplate(ee, coalesce || isCoalesce, group || isGroup(exp));
            }
            count++;
            t.add(ee);
        }

        t.setArg(exp.getArg());
        t.setModality(exp.getModality());
        t.setDistinct(exp.isDistinct());
        return t;

    }

    Term copy(Term exp, boolean group) {
        Term t;
        if (exp.isFunction()) {
            if (group && isSTCONCAT(exp)) {
                // group {  box {} } := group_concat(concat( .. st:concat()))
                // rewrite box st:concat() as concat() in case box{ st:number() }
                // otherwise st:number() would act as a Future in st:concat()
                t = Term.function(CONCAT);
            } else if (exp.getCName() != null) {
                t = ast.createFunction(exp.getCName());
            } else {
                t = Term.function(exp.getName());
            }
        } else {
            t = Term.create(exp.getName());
        }
        return t;
    }
    
    

    /**
     * In template { } a variable ?x is compiled as: coalesce(st:process(?x),
     * "") if ?x is unbound, empty string "" is returned if we are already
     * inside a coalesce, return st:process(?x)
     */
    Term compile(Variable var, boolean coalesce) {
        Term t = ast.createFunction(ast.createQName(FUN_PROCESS), var);
        if (!coalesce) {
            t = Term.function(COALESCE, t, getEmpty());
        }
        return t;
    }

    Term compile(Constant cst) {
        Term t = ast.createFunction(ast.createQName(FUN_PROCESS_URI), cst);
        return t;
    }

    /**
     *
     * additional aggregate(?out) default is st:group_concat it may be redefined
     * in template st:profile using function st:aggregate(?x) { st:agg_and(?x) }
     */
    public Term createTemplateGroup() {
        Variable var = Variable.create(OUT);
        Term t = ast.createFunction(ast.createQName(FUN_TEMPLATE_AGG));
        t.add(var);
        t.setModality(getSeparator());
        t.setArg(getExpSeparator());
        return t;
    }

    /**
     * Aggregate that build the result of a template when there are several
     * results default is group_concat draft: agg_and
     */
    static void setTemplateAggregate(String s) {
        FUN_TEMPLATE_AGG = s;
    }

    static void setTemplateConcat(String s) {
        FUN_TEMPLATE_CONCAT = s;
    }

    Constant getEmpty() {
        if (empty == null) {
            empty = Constant.create("", null, null);
        }
        return empty;
    }

    Variable templateVariable(Variable var) {
        return Variable.create(KGRAMVAR + countVar++);
    } 

     String getSeparator() {
        return templateSeparator;
    }

    public void setSeparator(String sep) {
        this.templateSeparator = sep; //clean(sep);
    }

    public void setSeparator(Expression exp) {
        if (exp.isConstant()) {
            setSeparator(exp.getLabel());
        }
        templateExpSeparator = exp;
    }

    Expression getExpSeparator() {
        return templateExpSeparator;
    }    

    Term getTemplateGroup() {
        return templateGroup;
    }

    void setTemplateGroup(Term templateGroup) {
        this.templateGroup = templateGroup;
    }  
      
    
}
