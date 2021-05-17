package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ASTPrinter implements KeywordPP {

    ASTQuery ast;
    private ASTBuffer sb;
    private boolean prefix = true;
    private boolean lambda = false;
    
    public ASTPrinter(ASTQuery a){
        ast = a;
        setBuffer(new ASTBuffer());
    }
    
    public ASTPrinter(ASTQuery a, ASTBuffer sb){
        ast = a;
        setBuffer(sb);
    }
    
    public void setCompiler(JavaCompiler jc) {
        getBuffer().setCompiler(jc);
    }
    
    @Override
    public String toString() {
        process();
        return getBuffer().toString();
    }
    
    public void process() {
        if (ast.isUpdate()) {
            ast.getUpdate().toString(sb);
        } else {
            if (isPrefix()){
                sb.append(ast.getNSM().toString(null, false, true));
            }
            
            if (ast.isTemplate()) {
                template();
            }
            
            getSparqlHeader();
                       
            if (!ast.isData() && (!ast.isDescribe() || ast.getBody() != null)) {
                if (ast.getBody() != null) {
                    sb.append(WHERE).append(" ");
                    ast.getBody().pretty(sb);
                }
            }

            if (!ast.isAsk()) {
                sb.nl();
                getSparqlSolutionModifier();
            }
        }

        getFinal();
    }
    
    void template() {
        sb.append("# template").append(" ");
        if (ast.getName() != null) {
            sb.append(ast.getName()).append(" ");
        }
        if (ast.getArgList() != null) {
            sb.append(ast.getArgList());
        }
        sb.nl();
    }

    public ASTBuffer getSparqlPrefix(Exp exp, ASTBuffer sb) {
        for (Exp e : exp.getBody()) {
            Triple t = e.getTriple();
            String r = t.getSubject().getName();
            String p = t.getPredicate().getName();
            String v = t.getObject().getName();

            // if v starts with "<function://", we have add a ".", so we have to remove it now
            if (v.startsWith(CORESE_PREFIX)
                    && v.endsWith(".")) {
                v = v.substring(0, v.length() - 1);
            }

            if (r.equalsIgnoreCase(PREFIX)) {
                sb.append(PREFIX + SPACE).append(p)
                        .append(": ").append(OPEN).append(v).append(CLOSE).nl();
            } else if (r.equalsIgnoreCase(BASE)) {
                sb.append(BASE + SPACE + OPEN)
                        .append(v).append(CLOSE).nl();
            }
        }
        return sb;
    }

    /**
     * Return the header part of the SPARQL-like Query (2nd parser)
     *
     * @return
     */
    ASTBuffer getSparqlHeader() {
        List<Constant> from = ast.getFrom();
        List<Constant> named = ast.getNamed();
        List<Variable> select = ast.getSelectVar();

        // Select
        if (ast.isSelect()) {
            sb.kw(SELECT);

            if (ast.isDebug()) {
                sb.kw(DEBUG);
            }

            if (ast.isMore()) {
                sb.kw(MORE);
            }

            if (ast.isDistinct()) {
                sb.kw(DISTINCT);
            }

            if (ast.isSelectAll()) {
                sb.kw(STAR);
            }

            if (select != null && select.size() > 0) {
                for (Variable s : ast.getSelectVar()) {

                    if (ast.getExpression(s) != null) {
                        if (ast.getGroupByMap().containsKey(s.getLabel())) {
                            sb.append(s);
                        }
                        else {
                            expr(ast.getExpression(s), s, sb);
                        }
                    } else {
                        sb.append(s);
                    }
                    sb.append(SPACE);
                }
            }

        } else if (ast.isAsk()) {
            sb.kw(ASK);
        } else if (ast.isDelete()) {
            sb.kw(DELETE);
            if (ast.isDeleteData()) {
                sb.kw(DATA);
            }
            ast.getDelete().toString(sb);

            if (ast.isInsert()) {
                sb.nl();
                sb.kw(INSERT);
                ast.getInsert().toString(sb);
            }

        } else if (ast.isConstruct()) {
            if (ast.isInsert()) {
                sb.kw(INSERT);
                if (ast.isInsertData()) {
                    sb.kw(DATA);
                }
                ast.getInsert().toString(sb);
            } else if (ast.getConstruct() != null) {
                sb.kw(CONSTRUCT);
                ast.getConstruct().toString(sb);
            } else if (ast.getInsert() != null) {
                sb.kw(INSERT);
                ast.getInsert().toString(sb);
            } else if (ast.getDelete() != null) {
                sb.kw(DELETE);
                if (ast.isDeleteData()) {
                    sb.kw(DATA);
                }
                ast.getDelete().toString(sb);
            }
        } else if (ast.isDescribe()) {
            sb.kw(DESCRIBE);

            if (ast.isDescribeAll()) {
                sb.kw(STAR);
            } else if (ast.adescribe != null && ast.adescribe.size() > 0) {

                for (Atom at : ast.adescribe) {
                    at.toString(sb);
                    sb.append(SPACE);
                }
            }
        }

        // DataSet
        //if (! isConstruct())    // because it's already done in the construct case
        sb.nl();

        // From
        for (Atom name : from) {
            sb.append(FROM, SPACE);
            name.toString(sb);
            sb.nl();
        }

        // From Named
        for (Atom name : named) {
            sb.append(FROM, SPACE, NAMED, SPACE);
            name.toString(sb);
            sb.nl();
        }
      
        return sb;
    }

    void expr(Expression exp, Variable var, ASTBuffer sb) {
        sb.append("(");
        exp.toString(sb);
        sb.append(" as ");

        if (var.getVariableList() != null) {
            sb.append("(");
            int i = 0;
            for (Variable v : var.getVariableList()) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(v);
            }
            sb.append(")");
        } else {
            sb.append(var);
        }
        sb.append(")");
    }
    
     /**
     * return the solution modifiers : order by, limit, offset
     *
     * @param parser
     * @return
     */
    ASTBuffer getSparqlSolutionModifier() {
        List<Expression> sort = ast.getSort();
        List<Boolean> reverse = ast.getReverse();

        if (ast.getGroupBy().size() > 0) {
            sb.kw(GROUPBY);
            for (Expression exp : ast.getGroupBy()) {
                if (exp.isVariable() && ast.getGroupByMap().containsKey(exp.getVariable().getLabel())) {
                    Variable var = exp.getVariable();
                    Expression body = ast.getGroupByMap().get(var.getLabel());
                    sb.append(String.format("(%s as %s)", body, var));
                }
                else {
                    sb.append(exp).append(SPACE);
                }
            }
            sb.nl();
        }

        if (sort.size() > 0) {
            int i = 0;
            sb.append(ORDERBY).append(SPACE);

            for (Expression exp : ast.getOrderBy()) {

                boolean breverse = reverse.get(i++);
                if (breverse) {
                    sb.append(DESC, "(");
                }
                sb.append(exp.toString());
                if (breverse) {
                    sb.append(")");
                }
                sb.append(SPACE);
            }
            sb.nl();
        }

        if (ast.getOffset() > 0) {
            sb.kw(OFFSET).append(ast.getOffset()).append(SPACE);
        }

        if (ast.getMaxResult() != ast.getDefaultMaxResult()) {
            sb.kw(LIMIT).append(ast.getMaxResult()).append(SPACE);
        }

        if (ast.getHaving() != null) {
            sb.append(HAVING);
            sb.append(OPEN_PAREN);
            ast.getHaving().toString(sb);
            sb.append(CLOSE_PAREN);
        }

        if (sb.length() > 0) {
            //sb.append(NL);
        }

        return sb;
    }

    void getFinal() {
        String SPACE = " ";

        if (ast.getValues() != null) {
            ast.getValues().toString(sb);
        }

        if (ast.getPragma() != null) {
            sb.kw(PRAGMA);
            ast.getPragma().toString(sb);
        }

        if (ast.getGlobalASTBasic()!= null) {
            // ast is a subquery, do not print functions
        }
        else {
            function(sb);
        }
        
    }
    
    void function(ASTBuffer sb) {
        function(sb, ast.getDefine());
        if (isLambda()) {
            function(sb, ast.getDefineLambda());
        }
    }
    
    void function(ASTBuffer sb, ASTExtension ext) {
        for (Expression fun : ext.getFunList()) {
            sb.nl();
            fun.toString(sb);
            sb.nl();
        }
    }
    
    /**
     * @return the prefix
     */
    public boolean isPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }
    
    /**
     * @return the lambda
     */
    public boolean isLambda() {
        return lambda;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(boolean lambda) {
        this.lambda = lambda;
    }

    /**
     * @return the sb
     */
    public ASTBuffer getBuffer() {
        return sb;
    }

    /**
     * @param sb the sb to set
     */
    public void setBuffer(ASTBuffer sb) {
        this.sb = sb;
    }
    

}
