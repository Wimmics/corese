/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.api;

/**
 *
 * @author corby
 */
public interface ASTVisitable {
    
    	void accept(ASTVisitor visitor);

    
}
