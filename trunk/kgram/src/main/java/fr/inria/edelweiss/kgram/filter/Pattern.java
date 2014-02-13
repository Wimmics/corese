package fr.inria.edelweiss.kgram.filter;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.core.Exp;

/**
 * Filter Exp Pattern Matcher
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */

public class Pattern implements ExprType, Expr {
	int type, oper;
	String label;
	// recursive pattern, ako *
	boolean rec = false, 
		matchConstant = true;;
	List<Expr> args;
	Expr exp;

	Pattern(){
		type = ANY;
		oper = ANY;
	}

	Pattern(int t){
		type = t;
		oper = ANY;
		args = new ArrayList<Expr>();
		}
	
	Pattern(int t, int o){
		this(t);
		oper = o;
	}
	
	Pattern(int t, int o, Expr e1){
		this(t, o);
		add(e1);
	}
	
	Pattern(int t, int o, Expr e1, Expr e2){
		this(t, o, e1);
		add(e2);
	}
	
	Pattern(int t, int o, int e1){
		this(t, o);
		add(new Pattern(e1));
	}
	
	Pattern(int t, int o, int e1, int e2){
		this(t, o);
		add(new Pattern(e1));
		add(new Pattern(e2));
	}
	
	static Pattern variable(String label){
		Pattern p = new Pattern(VARIABLE);
		p.setLabel(label);
		return p;
	}
	
	static Pattern constant(){
		Pattern p = new Pattern(CONSTANT);
		return p;
	}
	
	
	Pattern pat(int type){
		return new Pattern(type);
	}
	
	Pattern pat(int type, int ope, Pattern e1){
		return new Pattern(type, ope, e1);
	}
	
	Pattern pat(int type, int ope, Pattern e1, Pattern e2){
		return new Pattern(type, ope, e1, e2);
	}
	
	Pattern not(Pattern e){
		return pat(BOOLEAN, NOT, e);
	}
	
	Pattern and(Pattern e1, Pattern e2){
		return pat(BOOLEAN, AND, e1, e2);
	}
	
	Pattern or(Pattern e1, Pattern e2){
		return pat(BOOLEAN, OR, e1, e2);
	}
	
	Pattern term(int ope, Pattern e1, Pattern e2){
		return pat(TERM, ope, e1, e2);
	}
	
	Pattern fun(int ope, Pattern e1, Pattern e2){
		return pat(FUNCTION, ope, e1, e2);
	}
	
	
	
	void add(Expr exp){
		args.add(exp);
	}
	
	public int arity() {
		return args.size();
	}
	
	void setRec(boolean b){
		rec = true;
	}

	boolean isRec(){
		return rec;
	}
	
	void setMatchConstant(boolean b){
		matchConstant = b;
	}

	boolean isMatchConstant(){
		return matchConstant;
	}
	
	public Expr getExp(int i) {
		return args.get(i);
	}

	
	public Expr getExp() {
		return exp;
	}

	
	public List<Expr> getExpList() {
		return args;
	}

	
	public Filter getFilter() {
		return null;
	}

	
	public int getIndex() {
		return 0;
	}

	
	public String getLabel() {
		return label;
	}
	
	void setLabel(String l){
		label = l;
	}

	
	public Object getValue() {
		return null;
	}

	
	public boolean isAggregate() {
		return false;
	}

	
	public boolean isBound() {
		return false;
	}

	
	public int oper() {
		return oper;
	}

	
	public void setExp(Expr e) {
		exp = e;
	}

	
	public void setIndex(int index) {
	}

	
	public int type() {
		return type;
	}
	
	
	public String toString(){
		String str = "pat(";
		str += type + ", " + oper;
		str +=")";
		return str;
	}

	@Override
	public Exp getPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDistinct() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModality() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isVariable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Expr getArg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArg(Expr exp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRecAggregate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExist() {
		// TODO Auto-generated method stub
		return false;
	}
	
        @Override
	public boolean isRecExist() {
		// TODO Auto-generated method stub
		return false;
	}
}
