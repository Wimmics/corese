package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Atom extends Expression implements ElementClause{
	
	String datatype = null;
	String lang = null;
	boolean isone = false;
	boolean isall = false;
	boolean isdirect = false;
	boolean isset = false;
	int star;
	
	public Atom() {
	}

    
    public Atom(String name) {
		super(name);
	}

	public Atom(String name, String dt, String lg) {
		super(name);
		datatype = dt;
		lang = lg;
	}
	
	public boolean equals(Object at){
		return  false;
	}

	public Variable getVariable() {
		return null;
	}
	
	public Expression getExpression() {
        return null;
    }

	boolean isAtom() {
		return true;
	}

	public boolean isLiteral() {
		return false;
	}
	
	public boolean isBlank() {
		return false;
	}
	
	public boolean isResource() {
		return false;
	}

	public String getDatatype() {
		return datatype;
	}

	boolean hasRealDatatype() {
		if (datatype == null) return false;
		for (String str : RDFS.FAKEDT){
			if (datatype.equals(str)){
				return false;
			}
		}
		return true;				
	}

	// only xsd/rdf datatype (no rdfs:Literal no rdfs:Resource)
	public String getRealDatatype() {
		if (! hasRealDatatype())
			return null;
		else
			return datatype;
	}

	public String getLang() {
		return lang;
	}

	public boolean hasLang() {
		return lang != null && lang != "";
	}


	void setLang(String str) {
		lang = str;
	}

	void setDatatype(String str) {
		datatype = str;
	}

	public boolean isIsall() {
		return isall;
	}

	public void setIsall(boolean isall) {
		this.isall = isall;
	}

	public boolean isIsdirect() {
		return isdirect;
	}

	public void setIsdirect(boolean isdirect) {
		this.isdirect = isdirect;
	}

	public boolean isIsone() {
		return isone;
	}

	public void setIsone(boolean isone) {
		this.isone = isone;
	}

	public int getStar() {
		return star;
	}

	public void setPath(int star) {
		this.star = star;
	}

	public boolean isIsset() {
		return isset;
	}

	public void setIsset(boolean isset) {
		this.isset = isset;
	}

	public Atom getAtom() {
		return this;
	}

	public Constant getConstant() {
		return null;
	}

	public IDatatype getDatatypeValue() {
		return null;
	}

	public Atom getElement() {
		return this;
	}
	
	boolean validateData(ASTQuery ast){
		if (isBlankNode() || isBlank()){
			ast.record(this);
		}
		return true;
	}
	
}