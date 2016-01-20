package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.cst.KeywordPP;
import static fr.inria.acacia.corese.triple.parser.ASTQuery.NL;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ASTPrinter {
    
    ASTQuery ast;
    
    public ASTPrinter(ASTQuery a){
        ast = a;
    }
    
    
     public String toString() {
        StringBuffer sb = new StringBuffer();
        toString(sb);
        return sb.toString();
    }

    public StringBuffer toString(StringBuffer sb) {
        if (ast.isUpdate()) {
            ast.getUpdate().toString(sb);
        } else {
            getSparqlPrefix(sb);
            getSparqlHeader(sb);
            if (!ast.isData() && (!ast.isDescribe() || ast.getBody() != null)) {
                ast.getBody().toString(sb);
            }

            if (!ast.isAsk()) {
                sb.append(NL);
                getSparqlSolutionModifier(sb);
            }
        }

        getFinal(sb);

        return sb;
    }

    StringBuffer getSparqlPrefix(StringBuffer sb) {
        return getSparqlPrefix(ast.getPrefixExp(), sb);
    }

    public StringBuffer getSparqlPrefix(Exp exp, StringBuffer sb) {

        for (Exp e : exp.getBody()) {
            Triple t = e.getTriple();
            String r = t.getSubject().getName();
            String p = t.getPredicate().getName();
            String v = t.getObject().getName();

            // if v starts with "<function://", we have add a ".", so we have to remove it now
            if (v.startsWith(KeywordPP.CORESE_PREFIX)
                    && v.endsWith(".")) {
                v = v.substring(0, v.length() - 1);
            }

            if (r.equalsIgnoreCase(KeywordPP.PREFIX)) {
                sb.append(KeywordPP.PREFIX + KeywordPP.SPACE + p + ": "
                        + KeywordPP.OPEN + v + KeywordPP.CLOSE + NL);
            } else if (r.equalsIgnoreCase(KeywordPP.BASE)) {
                sb.append(KeywordPP.BASE + KeywordPP.SPACE
                        + KeywordPP.OPEN + v + KeywordPP.CLOSE + NL);
            }
        }
        return sb;
    }

    /**
     * Return the header part of the SPARQL-like Query (2nd parser)
     *
     * @return
     */
    StringBuffer getSparqlHeader(StringBuffer sb) {
        String SPACE = KeywordPP.SPACE;
        List<Constant> from = ast.getFrom();
        List<Constant> named = ast.getNamed();
        List<Variable> select = ast.getSelectVar();

        // Select
        if (ast.isSelect()) {
            sb.append(KeywordPP.SELECT + SPACE);

            if (ast.isDebug()) {
                sb.append(KeywordPP.DEBUG + SPACE);
            }

            if (ast.isMore()) {
                sb.append(KeywordPP.MORE + SPACE);
            }

            if (ast.isDistinct()) {
                sb.append(KeywordPP.DISTINCT + SPACE);
            }

            if (ast.isSelectAll()) {
                sb.append(KeywordPP.STAR + SPACE);
            }

            if (select != null && select.size() > 0) {
                for (Variable s : ast.getSelectVar()) {

                    if (ast.getExpression(s) != null) {
                        expr(ast.getExpression(s), s, sb);
                    } else {
                        sb.append(s);
                    }
                    sb.append(SPACE);
                }
            }

        } else if (ast.isAsk()) {
            sb.append(KeywordPP.ASK + SPACE);
        } else if (ast.isDelete()) {
            sb.append(KeywordPP.DELETE + SPACE);
            if (ast.isDeleteData()) {
                sb.append(KeywordPP.DATA + SPACE);
            }
            ast.getDelete().toString(sb);

            if (ast.isInsert()) {
                sb.append(KeywordPP.INSERT + SPACE);
                ast.getInsert().toString(sb);
            }

        } else if (ast.isConstruct()) {
            if (ast.isInsert()) {
                sb.append(KeywordPP.INSERT + SPACE);
                if (ast.isInsertData()) {
                    sb.append(KeywordPP.DATA + SPACE);
                }
                ast.getInsert().toString(sb);
            } else if (ast.getConstruct() != null) {
                sb.append(KeywordPP.CONSTRUCT + SPACE);
                ast.getConstruct().toString(sb);
            } else if (ast.getInsert() != null) {
                sb.append(KeywordPP.INSERT + SPACE);
                ast.getInsert().toString(sb);
            } else if (ast.getDelete() != null) {
                sb.append(KeywordPP.DELETE + SPACE);
                if (ast.isDeleteData()) {
                    sb.append(KeywordPP.DATA + SPACE);
                }
                ast.getDelete().toString(sb);
            }
        } else if (ast.isDescribe()) {
            sb.append(KeywordPP.DESCRIBE + SPACE);

            if (ast.isDescribeAll()) {
                sb.append(KeywordPP.STAR + SPACE);
            } else if (ast.adescribe != null && ast.adescribe.size() > 0) {

                for (Atom at : ast.adescribe) {
                    at.toString(sb);
                    sb.append(SPACE);
                }
            }
        }

        // DataSet
        //if (! isConstruct())    // because it's already done in the construct case
        sb.append(NL);

        // From
        for (Atom name : from) {
            sb.append(KeywordPP.FROM + SPACE);
            name.toString(sb);
            sb.append(NL);
        }

        // From Named
        for (Atom name : named) {
            sb.append(KeywordPP.FROM + SPACE + KeywordPP.NAMED + SPACE);
            name.toString(sb);
            sb.append(NL);
        }

        // Where
        if ((!(ast.isDescribe() && !ast.isWhere())) && !ast.isData()) {
            sb.append(KeywordPP.WHERE + NL);
        }

        return sb;
    }

    void expr(Expression exp, Variable var, StringBuffer sb) {
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
    public StringBuffer getSparqlSolutionModifier(StringBuffer sb) {
        String SPACE = KeywordPP.SPACE;
        List<Expression> sort = ast.getSort();
        List<Boolean> reverse = ast.getReverse();

        if (ast.getGroupBy().size() > 0) {
            sb.append(KeywordPP.GROUPBY + SPACE);
            for (Expression exp : ast.getGroupBy()) {
                sb.append(exp.toString() + SPACE);
            }
            sb.append(NL);
        }

        if (sort.size() > 0) {
            int i = 0;
            sb.append(KeywordPP.ORDERBY + SPACE);

            for (Expression exp : ast.getOrderBy()) {

                boolean breverse = reverse.get(i++);
                if (breverse) {
                    sb.append(KeywordPP.DESC + "(");
                }
                sb.append(exp.toString());
                if (breverse) {
                    sb.append(")");
                }
                sb.append(SPACE);
            }
            sb.append(NL);
        }

        if (ast.getOffset() > 0) {
            sb.append(KeywordPP.OFFSET + SPACE + ast.getOffset() + SPACE);
        }

        if (ast.getMaxResult() != ast.getDefaultMaxResult()) {
            sb.append(KeywordPP.LIMIT + SPACE + ast.getMaxResult() + KeywordPP.SPACE);
        }

        if (ast.getHaving() != null) {
            sb.append(KeywordPP.HAVING);
            sb.append(KeywordPP.OPEN_PAREN);
            ast.getHaving().toString(sb);
            sb.append(KeywordPP.CLOSE_PAREN);
        }

        if (sb.length() > 0) {
            sb.append(NL);
        }

        return sb;
    }

    void getFinal(StringBuffer sb) {
        String SPACE = " ";

        if (ast.getValues() != null) {
            ast.getValues().toString(sb);
        }

        if (ast.getPragma() != null) {
            sb.append(KeywordPP.PRAGMA);
            sb.append(SPACE);
            ast.getPragma().toString(sb);
        }

        for (Expression fun : ast.getDefine().getFunList()) {
            fun.toString(sb);
            sb.append(NL);
        }
    }
    
    

}
