package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.script.Let;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ASTFactory {
    
    ASTQuery ast;

    public ASTFactory(ASTQuery ast) {
        this.ast = ast;
    }
    
    Let let(List<Expression> el, Expression body, boolean dynamic) {
        expandLet(el);
        Let let = defineLet2(el, body, 0, dynamic);
        return let;
    }
    
    List<Expression> defineExpand(ExpressionList expList, Expression exp) {
         Term matchTerm = defLet(expList, exp); 
         return expandMatch(matchTerm);
    }
    
    List<Expression> defLetList(Variable var, Constant type, Expression exp) {
        ArrayList<Expression> list = new ArrayList<>();
        list.add(defLet(var, type, exp));
        return list;
    }
    
    Term defLet(Variable var, Constant type, Expression exp) {
        return Term.create("=", var, exp);
    }
    
    Term defLet(Variable var, Expression exp) {
        return Term.create("=", var, exp);
    }
    
    
    /**
     * let (var = exp, body)
     */    
    Let let(Expression exp, Expression body) {
        List<Expression> list = new ArrayList<>();
        list.add(exp);
        return let(list, body, false);
    }
    
    Let let(List<Expression> el, Expression body) {
        return let(el, body, false);
    }
    
    // new version where let has several declarations
    Let defineLet2(List<Expression> el, Expression body, int n, boolean dynamic) {
        return new Let(el, body, dynamic);
    }

    // old version where let has one declaration
    Let defineLet(List<Expression> el, Expression body, int n, boolean dynamic) {
        if (n == el.size() - 1) {
            return createLet(el.get(n), body, dynamic);
        }
        return createLet(el.get(n), defineLet(el, body, n + 1, dynamic), dynamic);
    }
     
    // expand ((x, y)) = exp
    void expandLet(List<Expression> expList) {
        for (int i = 0; i < expList.size();) {
            Expression exp = expList.get(i);
            if (exp.getArg(0).isTerm()) {
                // match() = exp
                Term match = exp.getArg(0).getTerm();
                List<Expression> list;
                
                if (match.isNested()) {
                    list = letList(match.getNestedList(), exp.getArg(1));

                } else {
                    list = expandMatch(exp);
                }

                int j = 0;
                for (Expression ee : list) {
                    if (j++ == 0) {
                        expList.set(i++, ee);
                    } else {
                        expList.add(i++, ee);
                    }
                }
                
                if (list.isEmpty()) {
                    i++;
                }
            } else {
                i++;
            }
        }
    }
   
    Let createLet(Expression exp, Expression body, boolean dynamic) {
        return new Let(exp, body, dynamic);
    }
          

    
    /**
     * let ((x, y) = exp)
     * (x, y) was compiled as nested match((x, y)) 
     * we have now let(match(varList) = exp)
     * compile as 
     * let (var = xt:get(exp, 0), match(varList) = var){ body }
     * use case: let (((?x, ?y)) = select where)
     * get first Mapping, match it
     * use case: let (((?var, ?val)) = ?m)
     * get first Binding, match it
     */
    ArrayList<Expression> letList(ExpressionList expList, Expression exp) { 
         if (! exp.isTerm() || expList.getList().size() == 1){
            return letList(expList, exp, 0);
         }
         // let (var = exp)
         Variable var = newLetVar();
         ArrayList<Expression> letList = letList(expList, var, 0);
         letList.add(0, defLet(var, exp));
         return letList;
     }
    
    // recurse on  expList 
    ArrayList<Expression> letList(ExpressionList expList, Expression exp, int n) { 
        Variable var = newLetVar();
        ExpressionList list = expList.getList().get(n) ;
        Term fst = defGet(var, exp, n);
        List<Expression> alist = defineExpand(list, var);
        ArrayList<Expression> letList;
        if (n+1 < expList.getList().size()){
            letList = letList(expList, exp, n+1);
        }
        else {
            letList = new ArrayList<>();           
        }
        for (int i = 0; i<alist.size(); i++) {
            letList.add(i, alist.get(i));
        }
        letList.add(0, fst);
        return letList;
    }
    

    
    /**
     * place holder to manage lvar = (x | y) ;
     * return (match(lvar) = exp)
     * match() is expanded in let by expandMatch or letList
     */
    public Term defLet(ExpressionList lvar, Expression exp) {
        complete(lvar, exp, true);
        Term t = ast.createFunction(Processor.MATCH, lvar);
        t.setNestedList(lvar);
        t.setNested(lvar.isNested());
        return Term.create("=", t, exp);
    }
    
     /**
     * exp = exists { select where }
     * use case: let (select where)
     */
    void complete(ExpressionList lvar, Expression exp, boolean nest) {
        if (lvar.isEmpty() && ! lvar.isNested() && exp.isTerm()) {
            Exp query = exp.getTerm().getExistContent();
            if (query != null){
                ASTQuery ast = query.getAST();
                ast.validate();
                ExpressionList el = new ExpressionList();
                for (Variable var : ast.getSelect()) { 
                    el.add(var);
                }
                lvar.add(el);
            }
        }
    }
    
   
    /**
     * let (match(?x, ?p, ?y) = ?l) {} ::= 
     * let (?x = xt:get(?l, 0), ?p =
     * xt:get(?l, 1), ?y = xt:get(?l, 2)) {}
     *
     */
    @Deprecated
    void processMatch(Let let) {
        Expression match = let.getVariableDefinition().getArg(0);
        if (match.isFunction() && match.getLabel().equals(Processor.MATCH)) {
            List<Expression> expList = expandMatch(let.getVariableDefinition());
            Term tmpLet = let(expList, let.getBody(), let.isDynamic());
            let.setArgs(tmpLet.getArgs());
        }
    }
    
    /**
     * term : match(var) = list
     */
    List<Expression> expandMatch(Expression term) {
        Expression match = term.getArg(0);
        Expression list  = term.getArg(1);
        ArrayList<Expression> expList = new ArrayList<>();

        Variable var;
        if (list.isVariable()) {
            var = list.getVariable();
        } else {
            // eval list exp once, store it in variable
            var = newLetVar();
            expList.add(defLet(var, list));
        }

        ExpressionList nestedList = match.getTerm().getNestedList();
        boolean isRest = nestedList != null && nestedList.isRest();
        int subList = nestedList.getSubListIndex();
        int lastElement = nestedList.getLastElementIndex();
        int nbLast = 0;
        if (subList >= 0 && lastElement >= 0) {
            nbLast = nestedList.size() - lastElement;
        }
        int last = nestedList.size() - 1;
        /**
         * (?a ?b | ?l . ?c ?d) subList = 2 -- index of ?l subList variable
         * lastElem = 3 -- index of last elements variable ?c /** (?a ?b | ?l .
         * ?c ?d) subList = 2 -- index of ?l subList variable lastElem = 3 --
         * index of last elements variable ?c
         */

        for (int i = 0; i < match.getArgs().size(); i++) {
            Expression arg = match.getArg(i);

            if (i == subList) {
                expList.add(defRest(arg.getVariable(), var, i, nbLast));
            } else if (lastElement >= 0 && i >= lastElement) {
                expList.add(defGenericGetLast(arg.getVariable(), var, last - i));
            } else {
                expList.add(defGenericGet(arg.getVariable(), var, i));
            }
        }
        
        if (expList.isEmpty()) {
            Term t = defLet(Variable.create("?_tmp_"), Constant.create(true));
            expList.add(t);
        }
        
        return expList;
    }
    
    Constant createQName(String qname) {
        return ast.createQName(qname);
    }
    
    Term createFunction(Constant name, Expression... exp) {
        return ast.createFunction(name, exp);
    }
    
    Variable newLetVar() {
        return ast.newLetVar();
    }

    
    Term defGenericGet(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GGET), exp);
        fun.add(Constant.createString(var.getLabel()));
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    /**
     * ith element of the target list in reverse order
     * i = 0 => last element
     * 
     */
    Term defGenericGetLast(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_LAST), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    Term defRest(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GREST), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    /**
     * Generate a subList starting at ith element of target list
     * nbLast is the number of last elements of target list not to include in the subList
     */
    Term defRest(Variable var, Expression exp, int i, int nbLast) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GREST), exp);
        fun.add(Constant.create(i));
        fun.add(Constant.create(nbLast));
        return defLet(var, fun);
    }
    
    Term defGet(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GET), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }    
    
    
}
