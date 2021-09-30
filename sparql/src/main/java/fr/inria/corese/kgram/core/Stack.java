package fr.inria.corese.kgram.core;

import java.util.ArrayList;

import fr.inria.corese.kgram.api.core.ExpType;

/**
 * 
 *  KGRAM stack of expressions
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */
public class Stack extends ArrayList<Exp> implements ExpType {
	
	int level = 0;
	
	public static Stack create(Exp e){
		Stack st = new Stack();
		st.add(e);
		return st;
	}
	
//	public Exp get(int n){
//		Exp e = super.get(n);
//		return e;
//	}
	
	public int getLevel(){
		return level;
	}
	
	Stack copy(){
		Stack st = new Stack();
		st.addAll(this);
		return st;
	}
	
	/**
	 * use case:
	 * A union B
	 * exp is A or is B
	 * Copy the stack for exp
	 * set exp at level n of new stack
	 * In addition we store this new stack into exp.
	 * Next time the data structure will be reused (after clean)
	 */
	Stack copy(Exp exp, int n){
		Stack st = exp.getStack();
		if (st == null){
			st = new Stack();
			exp.setStack(st);
		}
		else {
			st.clear();
		}
		st.addAll(this);
		st.set(n, exp);
		return st;
	}
	
	Stack copy2(Exp exp, int n){
		Stack st = new Stack();
		st.addAll(this);
		st.set(n, exp);
		return st;
	}
        
        boolean isCompleted() {
            return size() == 1;
        }
	
	/**
	 * Push all elements of AND in the stack
	 */
	Stack and(Exp exp, int n){
		remove(n);
		int i = 0;
		for (Exp e : exp){
                    if (e.getBind() != null){
                       add(n + i++, e.getBind()); 
                    }
                    add(n + i++, e);
		}
		return this;
	}
                
        Stack addCopy(int n, Exp exp) {
            Stack copy = copy();
            copy.add(n, exp);
            return copy;
        }
        
	void reset(int n, Exp exp){
		while (size()-1 > n){
			remove(size()-1);
		}
                if (size()-1 == n){
                    set(n, exp);
                }
                else {
                    add(exp);
                }
	}
	
	/**
	 * compile OPTIONAL{EXP} and NOT{EXP}
	 * as WATCH EXP NEXT
	 * begin: WATCH
	 * next:  CONTINUE/BACKJUMP
	 */
	Stack watch(Exp exp, int start, int next, boolean skip, int n){
		Exp end   =  Exp.create(next);
		Exp begin =  Exp.create(start, end);
		end.add(begin);
		end.skip(skip);
		
		set(n, begin);
		add(n+1, exp);
		add(n+2, end);
		return this;
	}
	
        @Override
	public String toString(){
		String str = ""; //"[" + level +"] ";
		int i = 0;
		for (Exp e : this){
			str += i++ + " " + e + ", ";
		}
		return str;
	}
	
}
