package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ASTVisitable;
import fr.inria.corese.sparql.triple.api.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Root of SPARQL Expression (filter) and Exp (triple, option, ...)
 *
 * @author corby
 *
 */
public class TopExp implements ASTVisitable {
   
    public boolean isGenerated() {
        return generated;
    }

    /**
     * @param generated the generated to set
     */
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }
    private boolean generated = false;

    @Override
    public String toString() {
        ASTBuffer sb = new ASTBuffer();
        toString(sb);
        return sb.toString();
    }

    public StringBuffer toString(StringBuffer sb) {
        return sb;
    }

    public ASTBuffer toString(ASTBuffer sb) {
        return sb;
    }

    public String toSparql() {
        return toString();
    }

    public StringBuffer toJava(StringBuffer sb) {
        return toString(sb);
    }

    @Override
    public void accept(ASTVisitor visitor) {

    }

    public List<Variable> getSubscopeVariables() {
        return getVariables(VariableScope.subscope);
    }

    public List<Variable> getInscopeVariables() {
        return getVariables(VariableScope.inscope);
    }

    public List<Variable> getAllVariables() {
        return getVariables(VariableScope.allscope);
    }
    
    // if statement: consider filter and statement inscope 
    // if filter:    consider filter
    // use case: when called on object of type Exp that contains a filter (Triple as Filter)
    public List<Variable> getFilterVariables() {
        return getVariables(VariableScope.filterscope);
    }
    
    /**
     * List of variables of statement Exp (bgp, optional, union, etc.) and/or filter Expression
     * inscope:  std SPARQL inscope variables
     * subscope: subset of inscope variables which are surely bound by statement
     * eg not optional variables, surely bound by union, etc.
     * There are 2 use cases:
     * 1. entering a statement (eg bgp) by default do not consider filters because we want *inscope* variables
     * 1.1 getVariables(VariableScope.inscope().setFilter(true)) consider also filter variables and filter exists variables
     * getVariables(VariableScope.inscope().setFilter(true).setExist(false)) consider also filter but not filter exist variables
     * 2. entering a filter Expression: consider filter variables and filter exists variables
     * 2.1 getVariables(VariableScope.inscope().setExist(false)) consider filter variables but not filter exists variables
     * Hence there is a subtle difference when entering a filter as statement Exp or as filter Expression
     */
    public List<Variable> getVariables(VariableScope scope) {
        List<Variable> list = new ArrayList<>();
        getVariables(scope, list);
        return list;
    }

    void getVariables(VariableScope scope, List<Variable> list) {
    }

}
