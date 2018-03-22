package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Stack implements Iterable<Variable> {

    ArrayList<Variable> stack;

    Stack() {
        stack = new ArrayList<Variable>();
    }
    
    Variable get(){
        if (stack.isEmpty()){
            return null;
        }
        return stack.get(stack.size() -1);
    }
    
    void push(Variable var){
        stack.add(var);
    }
    
    void pop(){
        stack.remove(stack.size() -1);
    }
    
    boolean isEmpty(){
        return stack.isEmpty();
    }

    @Override
    public Iterator<Variable> iterator() {
        ArrayList<Variable> tmp = (ArrayList<Variable>) stack.clone();
        Collections.reverse(tmp);
        return tmp.iterator();
    }
}
