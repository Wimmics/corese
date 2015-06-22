package fr.inria.edelweiss.kgram.api.core;

import java.util.List;


/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */

public interface Expr {
	
	Filter getFilter();
	
	// Exp as Object for modularity
	Object getPattern();

	String getLabel();
	
	String getModality();

	List<Expr> getExpList();

	Expr getExp(int i);
	void setExp(int i, Expr e);
	void addExp(int i, Expr e);
        
	Expr getArg();
	void setArg(Expr exp);

	Object getValue();
	
	int type();

	int oper();
        
        void setOper(int n);

	boolean isAggregate();
	
	boolean isRecAggregate();
	
	boolean isExist();

	boolean isRecExist();

        boolean isVariable();
	
	boolean isBound();
	
	boolean isDistinct();

	int arity();

	int getIndex();

	void setIndex(int index);
        
        void local(Expr var);
        
        Expr getDefine();
        
        void setDefine(Expr exp);
	
}
