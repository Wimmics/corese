package fr.inria.corese.sparql.cg.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.storage.api.IStorage;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 
 * @author Olivier Savoie
 */

public interface ICoresePolymorphDatatype {
	
	int getCode();
	
	boolean semiEquals(IDatatype iod); // get rid of @ lang

	/**
	 * @return the string in lower case depending on the datatype
	 * <br>representing the value of this
	 */
	String getLowerCaseLabel();
	
	boolean hasLang();
	
	boolean isTrue() throws CoreseDatatypeException;
	
 	boolean isTrueAble();
 	
	void setBlank(boolean b);
	
	void setDatatype(String uri);
	
	void setValue(String str);
        
        void setValue(String str, int id, IStorage pmgr);
	
	void setValue(IDatatype dt);

	void setLang(String str);

	long getlValue();
	
}


