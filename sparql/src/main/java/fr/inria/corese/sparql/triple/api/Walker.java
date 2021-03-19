package fr.inria.corese.sparql.triple.api;

import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Composite;

/**
 * ASTQuery process(walker) walk through every Exp, Expression and Function
 * and call enter() leave() on each of them
 */
public interface Walker {
    
    default void start(ASTQuery ast) {
    }
    
    default void finish(ASTQuery ast) {
    }
    
    default void enter(ASTQuery ast) {
    }
    
    default void leave(ASTQuery ast) {
    }
    
    default void enter(ASTUpdate ast) {
    }
    
    default void leave(ASTUpdate ast) {
    }
    
    default void enter(Composite ast) {
    }
    
    default void leave(Composite ast) {
    }
    
    default void enter(Function fun) {
    }
    
    default void leave(Function fun) {
    }
    
    default void enterSolutionModifier(ASTQuery ast) {
    }
    
    default void leaveSolutionModifier(ASTQuery ast) {
    }
    
    default void enter(Exp exp) {
    }
    
    default void leave(Exp exp) {
    }
    
    default void enter(Expression exp) {
    }

    default void leave(Expression exp) {
    }
    
}
