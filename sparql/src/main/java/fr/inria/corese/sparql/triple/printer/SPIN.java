package fr.inria.corese.sparql.triple.printer;

import fr.inria.corese.sparql.triple.api.ASTVisitor;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.And;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Binding;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exist;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Minus;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Optional;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Composite;
import fr.inria.corese.sparql.triple.update.Update;
import java.util.HashMap;
import java.util.List;

/**
 * Pretty Printer: SPARQL AST to SPIN Turtle 
 * @author Wimmics Inria I3S, 2013
 *
 */
public class SPIN implements ASTVisitor {

    private final static String SP = "sp:";
    private final static String SPIN = NSManager.SPIN;
    private final static String XSD = "xsd:";
    private final static String RDF = KeywordPP.DRDF + ":";
    private final static String DECL = "[ a ";
    private final static String ATAB = "a ";
    private final static String PT_COMMA = " ;\n";
    private final static String ELEM = "elements";
    private static final String TRUE = "\"true\"^^xsd:boolean";
    private static final String SPGROUP_BY = "sp:groupBy";
    private static final String SP_ASK = "sp:Ask";
    private static final String SPORDER_BY = "sp:orderBy";
    private static final String SP_DESC = "sp:Desc";
    private static final String SPELEMENTS = "sp:elements";
    private static final String SPLIMIT = "sp:limit";
    private static final String SPOFFSET = "sp:offset";
    private static final String SPHAVING = "sp:having";
    private static final String SP_CONSTRUCT = "sp:Construct";
    private static final String SPTEMPLATES = "sp:templates";
    private static final String SP_DESCRIBE = "sp:Describe";
    private static final String SPRESULT_NODES = "sp:resultNodes";
    private static final String SP_SELECT = "sp:Select";
    private static final String SPDISTINCT = "sp:distinct";
    private static final String SPREDUCED = "sp:reduced";
    private static final String SPSTAR = "sp:star";
    private static final String SPFROM = "sp:from";
    private static final String SPEXPRESSION = "sp:expression";
    private static final String SPVARNAME = "sp:varName";
    private static final String SPSUBJECT = "sp:subject";
    private static final String SPPREDICATE = "sp:predicate";
    private static final String SPOBJECT = "sp:object";
    private static final String SP_TRIPLE_PATH = "sp:TriplePath";
    private static final String SPPATH = "sp:path";
    private static final String NL = System.getProperty("line.separator");
    private static final String SPRESULT_VARIABLES = "sp:resultVariables";
    private static final String SPFROM_NAMED = "sp:fromNamed";
    private static final String OSBRACKET = KeywordPP.OPEN_SQUARE_BRACKET;
    private static final String CSBRACKET = KeywordPP.CLOSE_SQUARE_BRACKET;
    private static final String OPAREN   = KeywordPP.OPEN_PAREN;
    private static final String CPAREN   = KeywordPP.CLOSE_PAREN;
    private static final String SPACE    = KeywordPP.SPACE;
    
    private final static String BN = "_:sb";
    
    ASTBuffer sb;
    private int counter = 0;
    int vcount = 0;

    private Boolean subQuery = false; 
    
    // var -> blank node
    HashMap <String, String> tvar, tbnode;

    
    SPIN() {
        setBuffer(new ASTBuffer());
        tvar   = new HashMap <> ();
        tbnode = new HashMap <> ();        
    }
    
    public void init(){
        tvar.clear();
        tbnode.clear();
        subQuery = false;
        counter = 0;
    }
    
    public void nl(){
        sb.append(NL);
    }

    public static SPIN create() {
        return new SPIN();
    }

    public void setBuffer(ASTBuffer s) {
        sb = s;
    }

    public ASTBuffer getBuffer() {
        return sb;
    }
    
    @Override
    public String toString(){
        return sb.toString();
    }

    /**
     * Root method visit
     */
    @Override
    public void visit(ASTQuery ast) {
        visit(ast, null);
    }
    
    public void visit(ASTQuery ast, String src) {
        visitProlog(ast); 
        if (src != null){
            sb.append("graph ");
            sb.append(src);
            sb.append(" {");
            sb.append(NL);
        }
        process(ast);
        displayVar();        
        if (src != null){
           sb.append("}");
           sb.append(NL);
        }
    }
    
    void process(ASTQuery ast){

        if (ast.isAsk()) {
            ttype(SP_ASK);
            counter++;
            where(ast);
            sb.append(PT_COMMA);
            counter--;
        } 
        else if (ast.isSelect()) {
            visitSelect(ast);
        } 
        else if (ast.isDescribe()) {
            visitDescribe(ast);
        } 
        else if (ast.isConstruct()) {
            visitConstruct(ast);            
        } 
        else if (ast.isUpdate()) {
            visitUpdate(ast);           
        } 
        else {
            Exp exp = ast.getBody();
            for (Exp ee : exp.getBody()) {
                visit(ee);
            }
        }

        counter++;
        
        if (! ast.isAsk()) {
            visitModifier(ast);
        }     
       
        counter--;
        
        
        if (! ast.isUpdate()) {
            sb.append(tab() + CSBRACKET + NL);
        }
        
 
    }
    
    
    void visitModifier(ASTQuery ast){
         //GROUP BY
        if (ast.getGroupBy().size() > 0) {

            sb.append(tab() + SPGROUP_BY + SPACE + OPAREN);
            counter++;
            sb.append(SPACE);
            for (int i = 0; i < ast.getGroupBy().size(); i++) {
                visit(ast.getGroupBy().get(i));
                sb.append(SPACE);
            }
            counter--;
            sb.append(tab() + CPAREN + PT_COMMA);

        }



        //ORDER BY
        if (ast.getSort().size() > 0) {
            sb.append(tab() + SPORDER_BY + SPACE + OPAREN);
            counter++;

            sb.append(SPACE);
            
            for (int i = 0; i < ast.getOrderBy().size(); i++) {
                if (ast.getReverse().get(i) == true) {
                    type(SP_DESC);
                    sb.append(tab() + SPEXPRESSION + SPACE + OPAREN);
                    visit(ast.getOrderBy().get(i));
                    sb.append(CPAREN);
                    sb.append(CSBRACKET);
                } else {
                    visit(ast.getOrderBy().get(i));                    
                }
                sb.append(SPACE);
            }

            counter--;
            sb.append(SPACE + CPAREN + PT_COMMA);

        }

        //LIMIT
        if (ast.getMaxResult() != Integer.MAX_VALUE) {
            sb.append(tab() + SPLIMIT + SPACE + ast.getMaxResult() + PT_COMMA);
        }

        //OFFSET
        if (ast.getOffset() > 0) {
            sb.append(tab() + SPOFFSET + SPACE + ast.getOffset() + PT_COMMA);
        }

        //HAVING
        if (ast.getHaving() != null) {
            sb.append(tab() + SPHAVING + SPACE + OPAREN + NL);
            visit(ast.getHaving());
            sb.append(tab() + CPAREN + PT_COMMA);
        }
        
        if (ast.getValues() != null){
            sb.append(tab() + "sp:values" + SPACE  + NL);
            visit(ast.getValues());
        }

    }
    
    

    void visitProlog(ASTQuery ast) {
        sb.append("@prefix sp: <" + SPIN +"> .\n");
        subQuery = true;
        for (int i = 0; i < ast.getPrefixExp().size(); i++) {
            Triple t = ast.getPrefixExp().get(i).getTriple();
            if (! t.getObject().getName().equals(SPIN) || 
                  t.getSubject().getLabel().equals(KeywordPP.BASE)){
                if (t.getSubject().getLabel().equals(KeywordPP.BASE)){
                    sb.append("@base ");
                }
                else {
                    sb.append("@prefix " + t.getPredicate().getName());
                    sb.append(": ");
                }
                sb.append(KeywordPP.OPEN);
                sb.append(t.getObject().getName());
                sb.append(KeywordPP.CLOSE);
                sb.append(" ." + KeywordPP.SPACE_LN);
            }
        }
    }
    
    void visitConstruct(ASTQuery ast){
         sb.append(tab() + OSBRACKET + SPACE + ATAB);
            counter++;
            sb.append(SP_CONSTRUCT + PT_COMMA);
            dataset(ast);
            sb.append(tab() + SPTEMPLATES );

            counter++;
            visit(ast.getConstruct());           
            counter--;
            sb.append(PT_COMMA);

            //WHERE
            where(ast);
            sb.append(PT_COMMA);
            counter--;
}
    
    // TODO
    void visitDescribe(ASTQuery ast) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append(SP_DESCRIBE + PT_COMMA);
        sb.append(tab() + SPRESULT_NODES + SPACE + OPAREN);
        counter++;
        
        for (Atom at : ast.getDescribe()) {
            visit(at);
            sb.append(SPACE);
        }
        
        counter--;
        sb.append(CPAREN + PT_COMMA);


        //WHERE
        where(ast);
        counter--;
        sb.append(NL);
    }

    void visitSelect(ASTQuery ast) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append(SP_SELECT + PT_COMMA);

        if (ast.isDistinct()) {
            sb.append(tab() + SPDISTINCT + SPACE + TRUE + PT_COMMA);
        }


        if (ast.isReduced()) {
            sb.append(tab() + SPREDUCED + SPACE + TRUE + PT_COMMA);
        }


        //SELECT *
        if (ast.isSelectAll()) {
            sb.append(tab() + SPSTAR + SPACE + TRUE + PT_COMMA);
        }

       visitSelectVar(ast);
       
       dataset(ast);

        where(ast);
        sb.append(PT_COMMA);
        counter--;

    } 
    
    void dataset(ASTQuery ast){
         if (ast.getFrom().size() != 0) {

            sb.append(tab() + SPFROM + SPACE + OPAREN + SPACE);

            for (Constant g : ast.getFrom()) {
                sb.append(g + SPACE);
            }

            sb.append(CPAREN + PT_COMMA);
        }

        if (ast.getNamed().size() != 0) {
            sb.append(tab() + SPFROM_NAMED + SPACE + OPAREN + SPACE);

            for (Constant g : ast.getNamed()) {
                sb.append(g + SPACE);
            }

            sb.append(CPAREN + PT_COMMA);
        }
    }
    
    
    void visitSelectVar(ASTQuery ast){
        if (!ast.getSelectVar().isEmpty()) {
            sb.append(tab() + SPRESULT_VARIABLES + SPACE + OPAREN + NL);
            counter++;
            
            for (Variable var : ast.getSelectVar()) {
                
                Expression e = ast.getSelectFunctions().get(var.getName());

                if (e == null) {
                    tab(sb);
                    visit(var);
                    sb.append(NL);
                }
                else {
                    sb.append(tab() + OSBRACKET + SPACE + SPEXPRESSION + SPACE);
                    counter++;
                    visit(e);
                    counter--;
                    sb.append(PT_COMMA);
                    tab(sb);
                    
                    if (var.getVariableList() != null){
                        sb.append("sp:varList" + SPACE + OPAREN);
                        int i = 0;
                        for (Variable v : var.getVariableList()){
                            if (i++ > 0){
                                sb.append(" ");
                            }
                            visit(v);
                        }
                        sb.append(CPAREN);                      
                    }
                    else {
                        sb.append(SPVARNAME + SPACE);
                        sb.append(toTirer(var.getName()));
                    }
                    sb.append(CSBRACKET + NL);
                }              
            }
            counter--;
            sb.append(tab() + CPAREN + PT_COMMA);
        }
    }

    /**
     * **********************************************************
     *
     * Methods visit for the Exp part
     *
     ***********************************************************
     */
    @Override
    public void visit(Exp exp) {
        if (exp.isBind()){
            visit(exp.getBind());
        }
        else if (exp.isAnd()) {
            visit((And) exp);
        } 
        else if (exp.isFilter()) {
            visitFilter(exp.getFilter());
        }
        else if (exp.isTriple()) {
            visit(exp.getTriple());
        } 
        else if (exp.isQuery()) {
            ASTQuery ast = exp.getAST();
            if (ast.isBind()){
                visitBind((Query) exp);
            }
            else {
                visit((Query) exp);
            }
        }
        else if (exp.isOption()) {
            visit(exp.getOptional());
        } 
        else if (exp.isOptional()) {
            visitOptional(exp.getOptional());
        } 
        else if (exp.isValues()) {
            visit((Values) exp);
        } 
        else if (exp.isUnion()) {
            visit(exp.getUnion());
        } 
        else if (exp.isMinus()) {
            visit(exp.getMinus());
        } 
        else {
            for (Exp ee : exp.getBody()) {
                visit(ee);
            }
        }


    }
    
    void visitFilter(Expression exp) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Filter" + PT_COMMA);
        sb.append(tab() + "sp:expression" + SPACE + NL);
        visit(exp);
        counter--;
        sb.append(tab() + CSBRACKET);
    }

    @Override
    public void visit(Triple triple) {

        if (triple.isFilter()) {
            visitFilter(triple.getFilter());
        } else if (triple.isPath()) {
            path(triple);
        } else {

            sb.append(tab() + OSBRACKET + SPACE + NL);
            counter++;


            sb.append(tab() + SPSUBJECT + SPACE);
            visit(triple.getSubject());
            sb.append(PT_COMMA);

            //Predicate
            sb.append(tab() + SPPREDICATE + SPACE);
            visit(triple.getPredicate());
            sb.append(PT_COMMA);

            //Object
            sb.append(tab() + SPOBJECT + SPACE);
            visit(triple.getObject());

            counter--;
            sb.append(NL + tab() + CSBRACKET + NL);
        }
    }

    void path(Triple triple) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;

        sb.append(SP_TRIPLE_PATH + PT_COMMA);
        sb.append(tab() + SPSUBJECT + SPACE);
        visit(triple.getSubject());
        sb.append(PT_COMMA);
        
        if (triple.getVariable() != null && ! triple.getVariable().isBlankNode()){
            sb.append(tab() + "sp:pathVariable" + SPACE);
            visit(triple.getVariable());
            sb.append(PT_COMMA);
        }

        sb.append(tab() + SPPATH + SPACE);
        path(triple.getRegex());
        sb.append(PT_COMMA);

        //Object
        sb.append(tab() + SPOBJECT + SPACE);
        visit(triple.getObject());

        counter--;
        sb.append(NL + tab() + CSBRACKET);
    }

    // [ a sp:Statement ;
    void type(String name) {
        sb.append(DECL);
        sb.append(name);
        sb.append(PT_COMMA);
    }
    
    void ttype(String name) {
        tab(sb);
        sb.append(DECL);
        sb.append(name);
        sb.append(PT_COMMA);
    }

    void path(Expression exp) {
        if (exp.isConstant()) {
            visit(exp);
        } else if (exp.isSeq() || exp.isAlt()) {
            sb.append(SPACE);
            type(oper(exp));
            counter++;
            sb.append(tab() + "sp:path1" + SPACE);
            path(exp.getArg(0));
            sb.append(PT_COMMA);

            sb.append(tab() + "sp:path2" + SPACE);
            path(exp.getArg(1));
            counter--;
            sb.append(CSBRACKET);
        } else if (exp.getArg(0) != null){
            sb.append(SPACE);
            type(oper(exp));
            counter++;
            sb.append(tab() + "sp:subPath" + SPACE);
            path(exp.getArg(0));
            sb.append(PT_COMMA);
            modpath(exp);
            counter--;
            sb.append(tab() + CSBRACKET);
        }
    }

    void modpath(Expression exp) {
        if (exp.isPlus()) {
            sb.append(tab() + "sp:modMin" + SPACE + 1);
        } else if (exp.isOpt()) {
            sb.append(tab() + "sp:modMax" + SPACE + -1);
        }
    }

    String oper(Expression exp) {
        if (exp.isSeq()) {
            return "sp:SeqPath";
        } else if (exp.isAlt()) {
            return "sp:AltPath";
        } else if (exp.isNot()) {
            return "sp:NegPath";
        } else if (exp.isReverse()) {
            return "sp:ReversePath";
        } else if (exp.isStar() || exp.isPlus() || exp.isOpt()) {
            return "sp:ModPath";
        } else {
            return "sp:Unknown";
        }
    }

    @Override
    // TODO
    public void visit(Values values) {

        List<Variable> varList = values.getVarList();
        List<List<Constant>> constList = values.getValues();

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        sb.append("sp:Values" + PT_COMMA);
        counter++;

        //variables
        sb.append(tab() + "sp:variables" + SPACE + OPAREN);
        for (Variable var : values.getVarList()){
            visit(var);
            sb.append(SPACE);
        }
        sb.append(CPAREN + PT_COMMA);
       
        //values
        sb.append(tab() + "sp:values" + SPACE + OPAREN + NL);
        counter++;

        for (List<Constant> list : values.getValues()) {
            sb.append(tab() + OPAREN);
            for (Constant cst : list){
                if (cst != null){
                    visit(cst);
                }
                else {
                    type("sp:Undef");
                    sb.append(CSBRACKET);
                }
                sb.append(SPACE);
            }
            sb.append(CPAREN + NL);
        }

        counter--;
        sb.append(tab() + CPAREN + PT_COMMA);


        counter--;
        sb.append(tab() + CSBRACKET + NL);


    }

    @Override
    public void visit(Query query) {

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:SubQuery" + PT_COMMA);
        sb.append(tab() + "sp:query" + NL);

        process(query.getAST());

        counter--;
        sb.append(NL + tab() + CSBRACKET + NL);
    }
    
    
    /**
     * 
     * subquery is a bind(exp as var)
     */
    public void visitBind(Query query) {
        ASTQuery ast = query.getAST();
        Variable var   = ast.getSelectVar().get(0);
        Expression exp = ast.getExpression(var);
        bind(exp, var);
    }
    
     public void visit(Binding exp) {
        bind(exp.getFilter(), exp.getVariable());
    }
        
        
     void bind(Expression exp, Variable var) {
        ttype("sp:Bind");
        counter++;

        sb.append(tab() + "sp:expression" + SPACE);
        visit(exp);
        sb.append(PT_COMMA);
        sb.append(tab() + "sp:variable" + SPACE);
        visit(var);
        sb.append(PT_COMMA);

        counter--;
        sb.append(tab() + CSBRACKET + NL);
    }

    @Override
    public void visit(Union or) {

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Union" + PT_COMMA);
        sb.append(tab() + SPELEMENTS + SPACE + OPAREN + NL);
        counter++;

        for (int i = 0; i < or.size(); i++) {
           Exp e = or.eget(i);
           if (! e.isBGP()){
               sb.append(OPAREN);
           }
           visit(e);
           if (! e.isBGP()){
               sb.append(CPAREN);
           }
        }

        counter--;
        sb.append(tab() + CPAREN + NL);
        counter--;
        sb.append(tab() + CSBRACKET + NL);


    }

    @Override
    public void visit(And and) {
        if (and.isService()) {
            visit((Service) and);
        }  
//       else if (and.isMinus()) {
//            visit((Minus) and);
//        } 
        else if (and.isBGP()) {
            visit((BasicGraphPattern) and);
        } else if (and.isGraph()) {
            visit((Source) and);
        } else {
            for (Exp ee : and.getBody()) {
                visit(ee);
            }
        }


    }

    @Override
    public void visit(Optional option) {

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Optional" + PT_COMMA);
        sb.append(tab() + SPELEMENTS + SPACE + OPAREN + NL);
        counter++;

        for (int i = 0; i < option.size(); i++) {
            visit(option.eget(i));
        }

        counter--;
        sb.append(NL + tab() + CPAREN + NL);
        counter--;
        sb.append(tab() + CSBRACKET + NL);
    }
    
     void visitOptional(Optional option) {

        visit(option.eget(0));
        
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        sb.append("sp:Optional" + PT_COMMA);
        sb.append(tab() + SPELEMENTS + SPACE );
        counter++;

        visit(option.eget(1));

        counter--;
        sb.append(tab() + CSBRACKET + NL);
    }


    @Override
    public void visit(Minus minus) {

        visit(minus.get(0));
        ttype("sp:Minus");
        sb.append(tab() + SPELEMENTS + SPACE );
        visit(minus.eget(1));
        sb.append(tab() + CSBRACKET + NL);


    }

    @Override
    public void visit(Exist exist) {

        sb.append(NL);
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Exists" + PT_COMMA);
        sb.append(tab() + SPELEMENTS + SPACE +  NL);
        counter++;

        for (int i = 0; i < exist.size(); i++) {
            visit(exist.eget(i));
        }

        counter--;
        sb.append(tab() + NL);
        counter--;
        sb.append(tab() + CSBRACKET + NL);


    }

    @Override
    public void visit(BasicGraphPattern bgp) {

        if (bgp.isExist()) {
            visit((Exist) bgp);
        } else {
            sb.append(tab() +  OPAREN + NL);
            for (Exp ee : bgp.getBody()) {
                visit(ee);
            }
            sb.append(tab() +  CPAREN );
        }
    }

    @Override
    public void visit(Service service) {

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Service" + PT_COMMA);
        if (service.isSilent()){
            sb.append(tab() + "sp:silent" + SPACE + TRUE + PT_COMMA);
        }
        sb.append(tab() + "sp:serviceURI" + SPACE);
        visit(service.getServiceName());
        sb.append(PT_COMMA);
        sb.append(tab() + SPELEMENTS + SPACE +  NL);
        counter++;

        for (Exp ee : service.getBody()) {
            visit(ee);
        }

        counter--;
        sb.append(tab() + NL);
        counter--;
        sb.append(tab() + CSBRACKET + NL);


    }

    @Override
    public void visit(Source source) {

        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        sb.append(SP + "NamedGraph" + PT_COMMA);
        sb.append(tab() + SP + "graphNameNode" + SPACE);
        //sb.append("<" + source.getSource().getName().replace("?", "") + ">" + PT_COMMA);

        visit(source.getSource());
        sb.append(PT_COMMA);

        sb.append(tab() + SPELEMENTS + SPACE);
        counter++;

        visit(source.get(0));

        counter--;
        sb.append(tab() + CSBRACKET + NL);


    }

    /**
     * **********************************************************
     *
     * Methods visit for the Expression part
     *
     ***********************************************************
     */
    @Override
    public void visit(Expression expression) {
        if (expression.isTerm()) {
            visit(expression.getTerm());
        } else {
            visit(expression.getAtom());
        }
    }

    @Override
    public void visit(Term term) {
        if (term.isExist()) {
            visit((Exist) term.getExist());
        } else {
            boolean isIn = term.getName().equalsIgnoreCase(Processor.IN);
            
            sb.append(tab() + OSBRACKET + SPACE + ATAB + opeName(term) + SPACE + PT_COMMA);
            
            counter++;
            
            if (term.isDistinct()) {
                sb.append(tab() + SPDISTINCT + SPACE + TRUE + PT_COMMA);
            }
            
            int i = 1;
            List<Expression> args = term.getArgs();
            
            if (isIn){
                //  ?x in (aa, bb) 
                // ?x is sp:arg1
                // list elements starts at sp:arg2
                sb.append(tab() + "sp:arg" + i++ + SPACE);
                visit(term.getArg(0));
                sb.append(PT_COMMA);
                args = term.getArg(1).getArgs();
           }
            
            for (Expression exp : args) {
                sb.append(tab() + "sp:arg" + i++ + SPACE);
                visit(exp);
                sb.append(PT_COMMA);
            }
            
            if (term.getModality() != null && term.isAggregate()){
                // group concat separator
                sb.append(tab() + "sp:separator" + SPACE);
                Constant.toString(term.getModality(), sb);
                sb.append(PT_COMMA);
            }
            
            counter--;
            sb.append(tab() + CSBRACKET);
        }

    }

    String opeName(Term term) {
        if (term.isFunction()) {
            if (term.getCName() != null){
                return term.getCName().toString();
            }
            return funName(term);
        } else {
            return symToName(term.getName());
        }
    }

    String funName(Term fun) {
        if (fun.getLabel().startsWith("http://")) {
            return fun.getName();
        } else {
            return SP + fun.getName();
        }
    }

    // TODO: factorize variable as BN ?
    @Override
    public void visit(Atom atom) {

        if (atom.isConstant()) {
            visit((Constant) atom);
        } else {
            visit((Variable) atom);
        }
    }

    @Override
    public void visit(Variable var) {
        if (var.isBlankNode()){
            // Plays the role of beeing a BN
            // pprint it as a BN
            //var.toString(sb);
            sb.append(getName(var, tbnode));
        }
        else {
           sb.append(getName(var, tvar));
           //process(var); 
        }
    }
    
    String getName(Variable var, HashMap tvar) {
        String bn = (String) tvar.get(var.getLabel());
        if (bn == null) {
            bn = blank(var);
            tvar.put(var.getLabel(), bn);
        }
        return bn;
    }
      
    void process(Variable var) {                    
        String name = var.getLabel();
        String bn = tvar.get(name);
        if (bn == null){
            bn = blank(var);
            tvar.put(name, bn);
       }
        
       sb.append(bn);
    }
    
    void displayVar(){
        for (String var : tvar.keySet()){
            displayVar(var);
        }
    }
    
     void displayVar(String name){
        String bn = tvar.get(name);       
        
        sb.append(bn + SPACE);
        sb.append("sp:varName" + SPACE + "\"");
        sb.append(name.substring(1) + "\" ." + NL);
    }
    
    
    String blank(Variable var){
        return BN + vcount++;
    }


    @Override
    public void visit(Constant cst) {
        cst.toString(sb);

    }

    /**
     * @param sb
     * @param ast
     */
    private void where(ASTQuery ast) {
        sb.append(tab() + SP + KeywordPP.WHERE + SPACE );
        counter++;
        visit(ast.getBody());
        counter--;
        sb.append(NL);

    }

    /**
     * **********************************************************
     *
     * Methods visit for the Update part
     *
     ***********************************************************
     */
    
    void visitUpdate(ASTQuery ast) {
        boolean list = ast.getUpdate().getUpdates().size() > 1;
        if (list){
           type("sp:SPARQLUpdate");
           sb.append("sp:updates" + SPACE + OPAREN + NL);
        }
        
        for (Update u : ast.getUpdate().getUpdates()) {           
            visit(u);   
            sb.append(NL);
        }
        
        if (list){
            sb.append(CPAREN);
            sb.append(CSBRACKET);
        }
    }
    
    @Override
    public void visit(Update update) {
        if (update.isComposite()) {
            visit(update.getComposite());
        } else {
            visit(update.getBasic());
        }

    }

    @Override
    public void visit(Composite composite) {

        if (composite.getData() != null) {

            visitData(composite);

        } //Modify or Delete_Where
        else {
            //DELETE_WHERE
            boolean dw = false;
            for (Composite cc : composite.getUpdates()) {

                if (cc.getPattern() == composite.getBody()) {
                    sb.append(tab() + OSBRACKET + SPACE + ATAB);
                    counter++;
                    sb.append("sp:DeleteWhere" + PT_COMMA);

                    //Where
                    sb.append(tab() + "sp:where" );
                    counter++;
                    visit(composite.getBody());
                    counter--;

                    sb.append(NL);
                    counter--;
                    dw = true;
                    sb.append(CSBRACKET);
                }
            }

            //MODIFY
            if (!dw) {
                visitModify(composite);
            }

        }

    }

    void visitModify(Composite composite) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;
        sb.append("sp:Modify" + PT_COMMA);

        //GraphIRI
        if (composite.getWith() != null) {
            sb.append(tab() + "sp:graphIRI" + SPACE);
            visit(composite.getWith());
            sb.append(PT_COMMA);
            counter--;
        }

        //DELETE_INSERT
        for (Composite c : composite.getUpdates()) {
            counter++;

            //Delete
            if (c.type() == Update.DELETE) {
                sb.append(tab() + "sp:deletePattern");
            } //Insert
            else {
                sb.append(tab() + "sp:insertPattern");
            }

            counter++;
            visit(c.getPattern());
            counter--;

            sb.append(PT_COMMA);
            counter--;
        }

        //Using
        if (composite.getUsing().size() > 0) {
            counter++;
            sb.append(tab() + "sp:using" + SPACE + OPAREN + NL);
            counter++;
            for (Constant c : composite.getUsing()) {
                visit(c);
                sb.append(SPACE);
            }
            counter--;
            sb.append(tab() + CPAREN + PT_COMMA);
            counter--;
        }

        //Using named
        if (composite.getNamed().size() > 0) {
            counter++;
            sb.append(tab() + "sp:usingNamed" + SPACE + OPAREN + NL);
            counter++;
            for (Constant c : composite.getNamed()) {
                visit(c);
                 sb.append(SPACE);
           }
            counter--;
            sb.append(tab() + CPAREN + PT_COMMA);
            counter--;
        }

        //Where
        counter++;
        sb.append(tab() + "sp:where" );
        counter++;
        visit(composite.getBody());
        counter--;

        sb.append(NL);
        counter--;

        sb.append(CSBRACKET);

    }

    void visitData(Composite composite) {
        sb.append(tab() + OSBRACKET + SPACE + ATAB);
        counter++;

        if (composite.type() == Update.DELETE) {
            sb.append("sp:DeleteData" + PT_COMMA);
        }

        //INSERT query
        if (composite.type() == Update.INSERT) {
            sb.append("sp:InsertData" + PT_COMMA);
        }

        //DATA
        sb.append(tab() + "sp:data" );
        counter++;
        visit(composite.getData());
        counter--;
        sb.append( NL);
        counter--;
        sb.append(CSBRACKET);
    }

    @Override
    public void visit(Basic basic) {


        switch (basic.type()) {
            
            case Basic.LOAD:
                sb.append(tab() + OSBRACKET + SPACE + ATAB);

                counter++;
                sb.append("sp:Load" + PT_COMMA);
                
                 if (basic.isSilent()) {
                    sb.append(tab() + "sp:silent" + SPACE + TRUE + PT_COMMA);
                }

                if (basic.getCURI() != null) {
                    sb.append(tab() + "sp:document" + SPACE);
                    visit(basic.getCURI());
                    sb.append(PT_COMMA);
                }

                if (basic.getCTarget() != null) {
                    sb.append(tab() + "sp:into" + SPACE);
                    visit(basic.getCTarget());
                    sb.append(PT_COMMA);
                }

                counter--;
                sb.append(tab() + CSBRACKET);

                break;

           

            case Basic.CREATE:
                sb.append(tab() + OSBRACKET + SPACE + ATAB);

                counter++;
                sb.append("sp:Create" + PT_COMMA);
                if (basic.isSilent()) {
                    sb.append(tab() + "sp:silent" + SPACE + TRUE + PT_COMMA);
                }

                if (basic.getGraphName() != null) {
                    sb.append(tab() + "sp:graphIRI" + SPACE);
                    visit(basic.getGraphName());
                    sb.append(NL);
                }
                
                else if (basic.isDefault()) {
                    sb.append(tab() + "sp:default" + SPACE + TRUE + NL);
                } 
                
                counter--;
                sb.append(tab() + CSBRACKET);

                break;

                
            case Basic.CLEAR:               
            case Basic.DROP:
                              
                ttype(type(basic));

                counter++;

                if (basic.isSilent()) {
                    sb.append(tab() + "sp:silent" + SPACE + TRUE + PT_COMMA);
                }

                if (basic.getGraphName() != null) {
                    sb.append(tab() + "sp:graphIRI" + SPACE);
                    visit(basic.getGraphName());
                    sb.append(NL);
                } 
                else if (basic.isAll()) {
                    sb.append(tab() + "sp:all" + SPACE + TRUE + NL);
                } 
                else if (basic.isDefault()) {
                    sb.append(tab() + "sp:default" + SPACE + TRUE + NL);
                } 
                else if (basic.isNamed()) {
                    sb.append(tab() + "sp:named" + SPACE + TRUE + NL);
                }

                counter--;
                sb.append(tab() + CSBRACKET);

                break;
                
                
            case Basic.COPY:
            case Basic.MOVE:
            case Basic.ADD:
                
                ttype(type(basic));
                
                 if (basic.isSilent()) {
                    sb.append(tab() + "sp:silent" + SPACE + TRUE + PT_COMMA);
                }
                
                if (basic.getGraphName() != null){
                       sb.append(tab() + "sp:from" + SPACE + basic.getGraphName() + PT_COMMA);
                }
                else {
                       sb.append(tab() + "sp:from" + SPACE + "sp:default" + PT_COMMA);
                }
                
                if (basic.getCTarget() != null){
                        sb.append(tab() + "sp:to" + SPACE + basic.getCTarget() + PT_COMMA);                   
                }
                else {
                        sb.append(tab() + "sp:to" + SPACE + "sp:default" + PT_COMMA);                                     
                }
                
                sb.append(tab() + CSBRACKET);
                
                break;

            
        }

    }
    
    String type(Basic basic){
        switch(basic.type()){
            case Basic.CLEAR: return "sp:Clear";
            case Basic.DROP:  return "sp:Drop";
            case Basic.COPY:  return "sp:Copy";
            case Basic.MOVE:  return "sp:Move";
            case Basic.ADD:   return "sp:Add";
        }
        return "sp:Unknown";
    }


    /**
     * **********************************************************
     *
     * Auxiliary methods
     *
     ***********************************************************
     */
    private String toTirer(String s) {
        if (s.startsWith("?")) {
            String r = s.replaceFirst("\\?", "\"");
            r += "\"";
            return r;
        } else {
            return s;
        }
    }

    /**
     * Set the right number of \t in the visits methods
     *
     * @return a string with the right number of \t
     */
    private String tab() {
        String t = "";
        for (int j = 0; j < counter; j++) {
            t += " ";
        }

        return t;
    }
    
     private void tab(ASTBuffer sb) {
        for (int j = 0; j < counter; j++) {
            sb.append(SPACE);
        }
    }

    /**
     *
     * @param s
     * @return
     */
    private String symToName(String s) {
        if (s.equals("||")) {
            return "sp:or";
        } else if (s.equals("=")) {
            return "sp:eq";
        } else if (s.equals("!")) {
            return "sp:not";
        } else if (s.equals("!=")) {
            return "sp:ne";
        } else if (s.equals("<")) {
            return "sp:lt";
        } else if (s.equals(">")) {
            return "sp:gt";
        } else if (s.equals("&&")) {
            return "sp:and";
        } else if (s.equals("-")) {
            return "sp:sub";
        } else if (s.equals("+")) {
            return "sp:add";
        } else if (s.equals(">=")) {
            return "sp:ge";
        } else if (s.equals("<=")) {
            return "sp:le";
        } else if (s.equals("*")) {
            return "sp:mul";
        } else if (s.equals("/")) {
            return "sp:divide";
        } 
        else if (s.equals("~")) {
            return "kg:tilda";
        } 
         else if (s.equals("^")) {
            return "sp:strstarts";
        } 
        else {
            return SP + s;
        }
    }
}